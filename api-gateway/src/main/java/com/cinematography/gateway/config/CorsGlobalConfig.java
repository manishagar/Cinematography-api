package com.cinematography.gateway.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsGlobalConfig {

    @Value("${api.gui.baseUrlUI}")
    private String allowedOriginsRaw;

    @Bean
    CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allowed Origins from property
        List<String> allowedOrigins = Arrays.stream(allowedOriginsRaw.split(","))
                                            .map(String::trim)
                                            .toList();
        config.setAllowedOrigins(allowedOrigins);

        // Allowed Headers
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // Allowed Methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        config.setAllowCredentials(true);

        // Register configuration for all routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
