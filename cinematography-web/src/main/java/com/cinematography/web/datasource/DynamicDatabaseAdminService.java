package com.cinematography.web.datasource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicDatabaseAdminService {

    private static final String CI_CONN_MANAGER = "CI_ConnManager";

    private final DynamicDataSource routingDataSource;
    private final DataSourceProperties dataSourceProperties;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * ✅ Force switch DB using explicit DB name
     */
    public synchronized String switchDatabase(String dbName) {
        if (!StringUtils.hasText(dbName)) {
            throw new IllegalArgumentException("Database name must not be empty");
        }

        String currentDb = DynamicDatabaseContext.getCurrentDatabaseName();

        if (currentDb != null && dbName.equalsIgnoreCase(currentDb)) {
            log.info("Database '{}' already active. Skipping switch.", dbName);
            return currentDb;
        }

        log.warn("Switching database. Old='{}', New='{}'", currentDb, dbName);

        performSwitch(dbName);
        return dbName;
    }

    /**
     * ✅ Refresh DB without providing dbName
     * Resolves DB internally and switches if required
     */
    public synchronized String refreshDatabase() {
        String resolvedDb = resolveDatabaseName();

        if (!StringUtils.hasText(resolvedDb)) {
            throw new IllegalStateException("Unable to resolve database name");
        }

        log.warn("Manual database refresh triggered via API. Resolved DB='{}'", resolvedDb);
        return switchDatabase(resolvedDb);
    }

    /**
     * ✅ Get current active DB
     */
    public String currentDatabase() {
        return DynamicDatabaseContext.getCurrentDatabaseName();
    }

    /**
     * 🔒 Core switching logic (single source of truth)
     */
    private void performSwitch(String dbName) {
        DynamicDatabaseContext.setCurrentDatabaseName(dbName);

        DataSource newDataSource = buildDataSource(dbName);

        Map<Object, Object> targets = new HashMap<>();
        targets.put(dbName, newDataSource);

        routingDataSource.setTargetDataSources(targets);
        routingDataSource.setDefaultTargetDataSource(newDataSource);
        routingDataSource.afterPropertiesSet();

        eventPublisher.publishEvent(new DatabaseSwitchedEvent(dbName));

        log.info("✅ Database switched successfully to '{}'", dbName);
    }

    /**
     * Resolve DB name from Connections table
     */
    private String resolveDatabaseName() {
        DriverManagerDataSource bootstrapDs = new DriverManagerDataSource();
        bootstrapDs.setDriverClassName(Objects.requireNonNull(dataSourceProperties.getDriverClassName(),
                "spring.datasource.driver-class-name must be set"));
        bootstrapDs.setUrl(requiredUrl());
        bootstrapDs.setUsername(dataSourceProperties.getUsername());
        bootstrapDs.setPassword(dataSourceProperties.getPassword());

        JdbcTemplate jdbcTemplate = new JdbcTemplate(bootstrapDs);

        return jdbcTemplate.queryForObject(
            "SELECT conn_database FROM Connections WHERE conn_key = 'GlobalHTS'",
            String.class
        );
    }

    /**
     * Build pooled datasource for resolved DB
     */
    private DataSource buildDataSource(String dbName) {
        String baseUrl = requiredUrl();
        String finalUrl = baseUrl.contains(CI_CONN_MANAGER)
                ? baseUrl.replace(CI_CONN_MANAGER, dbName)
                : baseUrl;

        return dataSourceProperties
                .initializeDataSourceBuilder()
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .url(finalUrl)
                .build();
    }

    /** spring.datasource.url is mandatory: fail with a clear message instead of a NullPointerException. */
    private String requiredUrl() {
        return Objects.requireNonNull(dataSourceProperties.getUrl(), "spring.datasource.url must be set");
    }
}