package com.cinematography.web.autoconfigure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.cinematography.web.audit.AuditLogClient;
import com.cinematography.web.cache.CountryCodeCache;
import com.cinematography.web.client.CinematographyHttpClientAutoConfiguration;
import com.cinematography.web.exception.BaseGlobalExceptionHandler;
import com.cinematography.web.observability.ProcedureObserver;
import com.cinematography.web.security.JwtTokenValidator;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

/**
 * Shared servlet infrastructure. Every bean backs off when the service defines its own.
 */
@AutoConfiguration(after = CinematographyHttpClientAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CinematographyWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ResponseEntityExceptionHandler.class)
    BaseGlobalExceptionHandler dsgGlobalExceptionHandler() {
        return new BaseGlobalExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    JwtTokenValidator jwtTokenValidator() {
        return new JwtTokenValidator();
    }

    @Bean
    @ConditionalOnMissingBean
    ProcedureObserver procedureObserver(ObservationRegistry observationRegistry,
            @Value("${spring.application.name}") String applicationName) {
        return new ProcedureObserver(observationRegistry, applicationName);
    }

    @Bean
    @ConditionalOnMissingBean
    AuditLogClient auditLogClient(RestTemplate restTemplate, LoadBalancerClient loadBalancerClient) {
        return new AuditLogClient(restTemplate, loadBalancerClient);
    }

    /** {@code @Observed} support; opt-in with {@code cinematography.observation.observed-aspect=true}. */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "cinematography.observation", name = "observed-aspect", havingValue = "true")
    ObservedAspect observedAspect(ObservationRegistry registry) {
        return new ObservedAspect(registry);
    }

    /** OpenAPI document info from {@code cinematography.openapi.*}. */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(OpenAPI.class)
    @ConditionalOnProperty(prefix = "cinematography.openapi", name = "title")
    static class OpenApiConfiguration {

        @Bean
        @ConditionalOnMissingBean
        OpenAPI customOpenAPI(@Value("${cinematography.openapi.title}") String title,
                @Value("${cinematography.openapi.version:1.0.0}") String version,
                @Value("${cinematography.openapi.description:}") String description,
                @Value("${cinematography.openapi.contact-name:}") String contactName,
                @Value("${cinematography.openapi.contact-email:}") String contactEmail) {
            return new OpenAPI()
                    .info(new Info().title(title).version(version)
                    .description(description)
                    .contact(new Contact().name(contactName).email(contactEmail)));
        }
    }

    /** Country code cache; registered when {@code cinematography.cache.country-query} is set. */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(NamedParameterJdbcTemplate.class)
    @ConditionalOnProperty(prefix = "cinematography.cache", name = "country-query")
    static class CountryCacheConfiguration {

        @Bean
        @ConditionalOnMissingBean
        CountryCodeCache countryCodeCache(NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                @Value("${cinematography.cache.country-query}") String countryQuery) {
            return new CountryCodeCache(namedParameterJdbcTemplate, countryQuery);
        }
    }
}
