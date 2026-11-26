package com.cinematography.translationapi;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAspectJAutoProxy
@SpringBootApplication(scanBasePackages = {
		"com.cinematography.translationapi",
		"com.cinematography.cust"
})
@EntityScan(basePackages = { "com.cinematography.translationapi", "com.cinematography.cust" })
@EnableJpaRepositories(basePackages = { "com.cinematography.translationapi",  "com.cinematography.cust" })
@EnableDiscoveryClient
@EnableScheduling
public class TranslationApiApplication {
	public static void main(String[] args) {
        new SpringApplicationBuilder(TranslationApiApplication.class)
            .properties("spring.config.name=translation-api")  // 👈 this loads translation-api.yml
            .build()
            .run(args);
	}
}
