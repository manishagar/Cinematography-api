package com.cinematography.adminapi ;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
@OpenAPIDefinition
public class OpenAPIAdminConfig {
	@Bean
	OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info().title("ADMIN API Documentation").version("1.0.0")
				.description("ADMIN API documentation for the microservice")
				.contact(new Contact().name("ADMIN API").email("admin-email@example.com")));
	}
}