package com.cinematography.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cinematography.gateway.filter.AuthenticationFilter;

@Configuration
public class SpringCloudConfiguration {

    @Bean
    RouteLocator gatewayRoutes(RouteLocatorBuilder builder, AuthenticationFilter authenticationFilter) {
        return builder.routes()
                .route("admin-api", route -> route.path("/admin/users/admin/**")
                        .filters(filters -> filters.filter(authenticationFilter))
                        .uri("lb://ADMIN-API"))
                .build();
    }
}
