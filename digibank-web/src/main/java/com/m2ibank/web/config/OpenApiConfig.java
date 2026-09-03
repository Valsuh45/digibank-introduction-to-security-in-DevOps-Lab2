package com.m2ibank.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata configuration for the DigiBank REST API.
 *
 * <p>The bean describes the API title, version, summary, and contact shown in the generated Swagger UI
 * and {@code /v3/api-docs}. Keeping this metadata in code makes the documentation travel with the
 * application and lets CI smoke-test the OpenAPI endpoint.</p>
 *
 * <p>This file does not define security rules. It only documents the API surface; actual validation and
 * safe error handling live in controllers, services, DTOs, and the global exception handler.</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI digiBankOpenApi() {
        return new OpenAPI().info(new Info()
                .title("DigiBank API")
                .version("1.0")
                .description("Secure modular banking API for the UCC152-2 DevSecOps workshop.")
                .contact(new Contact().name("DigiBank Workshop Team")));
    }
}
