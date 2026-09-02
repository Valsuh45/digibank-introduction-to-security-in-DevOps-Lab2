package com.m2ibank.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void publishesTheWorkshopApiIdentity() {
        OpenAPI openAPI = new OpenApiConfig().digiBankOpenApi();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("DigiBank API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0");
        assertThat(openAPI.getInfo().getDescription()).doesNotContain("TODO");
    }
}
