package com.cinematography.web.datasource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * Replaces the service's {@code DataSource} with the switchable routing data source and exposes the
 * database admin endpoints under {@code dsg.datasource.admin-path}.
 * <p>
 * Put it on a {@code @Configuration} class of the service (used by hs-api and lc-api).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({ DynamicDataSourceConfig.class, DynamicDatabaseAdminService.class, DatabaseAdminController.class })
public @interface EnableDynamicDataSource {
}
