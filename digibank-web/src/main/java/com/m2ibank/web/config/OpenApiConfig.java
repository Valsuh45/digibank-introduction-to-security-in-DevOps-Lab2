package com.m2ibank.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
