package com.cinematography.adminapi;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableAspectJAutoProxy
@SpringBootApplication(scanBasePackages = { "com.cinematography.adminapi", "com.cinematography.cust" })
@EntityScan(basePackages = { "com.cinematography.adminapi", "com.cinematography.cust" })
@EnableJpaRepositories(basePackages = { "com.cinematography.adminapi", "com.cinematography.cust" })
@EnableDiscoveryClient
public class AdminApiApplication {
	public static void main(String[] args) {
        new SpringApplicationBuilder(AdminApiApplication.class)
            .properties("spring.config.name=admin-api")  // 👈 this loads admin-api.yml
            .build()
            .run(args);
	}
}

