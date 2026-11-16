package com.cinematography.web.datasource;

import com.cinematography.web.observability.ProcedureObserver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Routing {@link DataSource} whose target database is resolved from the {@code Connections} table at startup
 * and on {@code scheduler.dynamic-db.refresh-cron}. Enabled with {@link EnableDynamicDataSource}.
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class DynamicDataSourceConfig {

    private static final String CI_CONN_MANAGER = "CI_ConnManager";
    private static final String BOOTSTRAP_KEY = "BOOTSTRAP";

    private final DataSourceProperties dataSourceProperties;
    private final ProcedureObserver observer;
    private final ApplicationEventPublisher eventPublisher;

    private final DynamicDataSource routingDataSource = new DynamicDataSource();

    /** Cache all pooled datasources (BOOTSTRAP + dynamic DBs) */
    private final Map<String, DataSource> dataSourceCache = new HashMap<>();

    /**
     * ✅ Trigger initial resolution AFTER application is fully ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application ready. Resolving initial dynamic database.");
        refreshDatabaseName();
    }

    @Scheduled(cron = "${scheduler.dynamic-db.refresh-cron}")
    public synchronized void refreshDatabaseName() {

        String resolvedDb = resolveDatabaseName();
        resolvedDb = (resolvedDb == null) ? null : resolvedDb.strip();

        if (!StringUtils.hasText(resolvedDb)) {
            log.error("Dynamic database resolution failed");
            return;
        }

        String currentDb = DynamicDatabaseContext.getCurrentDatabaseName();
        currentDb = (currentDb == null) ? null : currentDb.strip();

        boolean changed = (currentDb == null || !resolvedDb.equalsIgnoreCase(currentDb));

        if (changed) {
            switchDatabase(resolvedDb);
        }

        log.info(
            "Database check -> currentDb='{}', resolvedDb='{}', action={}",
            currentDb,
            resolvedDb,
            changed ? "SWITCHED" : "UNCHANGED"
        );
    }

    /**
     * ✅ Safe dynamic switch with pooled cache
     */
    private synchronized void switchDatabase(String dbName) {

        DataSource targetDs = dataSourceCache.computeIfAbsent(
            dbName, this::buildPooledDataSource
        );

        DynamicDatabaseContext.setCurrentDatabaseName(dbName);

        routingDataSource.setTargetDataSources(new HashMap<>(dataSourceCache));
        routingDataSource.setDefaultTargetDataSource(targetDs);
        routingDataSource.afterPropertiesSet();

        eventPublisher.publishEvent(new DatabaseSwitchedEvent(dbName));
    }

    /**
     * ✅ Uses pooled bootstrap datasource (no DriverManagerDataSource)
     */
    private String resolveDatabaseName() {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(
            dataSourceCache.get(BOOTSTRAP_KEY)
        );

        return observer.observeProcedure(
            "hsapi.db.getDynamicDatabaseName",
            () -> jdbcTemplate.queryForObject(
                "SELECT conn_database FROM Connections WHERE conn_key = 'GlobalHTS'",
                String.class
            ),
            this
        );
    }

    /**
     * ✅ Routing datasource bean — MUST be valid at startup
     */
    @Bean
    public DataSource dataSource() {

        DataSource bootstrapDs = createBootstrapDataSource();

        dataSourceCache.put(BOOTSTRAP_KEY, bootstrapDs);

        routingDataSource.setTargetDataSources(
            Map.of(BOOTSTRAP_KEY, bootstrapDs)
        );
        routingDataSource.setDefaultTargetDataSource(bootstrapDs);
        routingDataSource.afterPropertiesSet();

        DynamicDatabaseContext.setCurrentDatabaseName(BOOTSTRAP_KEY);

        return routingDataSource;
    }

    private DataSource buildPooledDataSource(String dbName) {

        String baseUrl = requiredUrl();
        String resolvedUrl = baseUrl.contains(CI_CONN_MANAGER)
            ? baseUrl.replace(CI_CONN_MANAGER, dbName)
            : baseUrl;

        return dataSourceProperties
            .initializeDataSourceBuilder()
            .type(com.zaxxer.hikari.HikariDataSource.class)
            .url(resolvedUrl)
            .build();
    }

    private DataSource createBootstrapDataSource() {
        return dataSourceProperties
            .initializeDataSourceBuilder()
            .type(com.zaxxer.hikari.HikariDataSource.class)
            .url(requiredUrl())
            .build();
    }

    /** spring.datasource.url is mandatory: fail with a clear message instead of a NullPointerException. */
    private String requiredUrl() {
        return Objects.requireNonNull(dataSourceProperties.getUrl(), "spring.datasource.url must be set");
    }
}