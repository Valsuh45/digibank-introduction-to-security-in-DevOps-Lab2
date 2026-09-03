package com.m2ibank.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for OpenAPI metadata.
 *
 * <p>The test protects the published API title, version, and description so generated documentation
 * remains clear and does not contain unfinished placeholder text.</p>
 */
class OpenApiConfigTest {

    @Test
    void publishesTheWorkshopApiIdentity() {
        OpenAPI openAPI = new OpenApiConfig().digiBankOpenApi();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("DigiBank API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0");
        assertThat(openAPI.getInfo().getDescription()).doesNotContain("TODO");
    }
}
