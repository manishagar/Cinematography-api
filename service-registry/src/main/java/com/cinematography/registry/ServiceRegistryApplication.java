package com.cinematography.registry;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer 
public class ServiceRegistryApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ServiceRegistryApplication.class)
        .properties("spring.config.name=service-registry")
        .build().run(args);
    }
}
