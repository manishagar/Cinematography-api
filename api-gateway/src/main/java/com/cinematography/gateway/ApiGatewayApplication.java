package com.cinematography.gateway;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
	public static void main(String[] args) {
        new SpringApplicationBuilder(ApiGatewayApplication.class)
        .properties("spring.config.name=api-gateway")  // 👈 this loads api-gateway.yml
        .build()
        .run(args);
}
}
