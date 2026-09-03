package com.m2ibank.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot entry point for the DigiBank application.
 *
 * <p>This module assembles the common, customer, account, transfer, and web modules into one runnable
 * service. The explicit scan settings tell Spring to discover components, JPA entities, and repositories
 * across the shared {@code com.m2ibank} package tree.</p>
 *
 * <p>The class intentionally contains no business logic. Startup belongs here; customer, account, and
 * transfer rules stay inside their own modules so the application remains easier to test and review.</p>
 */
@SpringBootApplication(scanBasePackages = "com.m2ibank")
@EntityScan(basePackages = "com.m2ibank")
@EnableJpaRepositories(basePackages = "com.m2ibank")
public class DigiBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigiBankApplication.class, args);
    }
}
