package com.m2ibank.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for the generated OpenAPI document.
 *
 * <p>The test protects the public Swagger contract that reviewers see in the browser. It checks that
 * endpoints have meaningful summaries, descriptions, tags, response explanations, and request schema
 * descriptions instead of relying only on names inferred from Java methods.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openApiJsonDescribesEndpointsAndRequestSchemas() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags[?(@.name == 'Customers')].description").isNotEmpty())
                .andExpect(jsonPath("$.tags[?(@.name == 'Accounts')].description").isNotEmpty())
                .andExpect(jsonPath("$.tags[?(@.name == 'Transfers')].description").isNotEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/customers'].post.summary")
                        .value("Register a customer"))
                .andExpect(jsonPath("$.paths['/api/v1/customers'].post.description",
                        containsString("Creates a customer profile")))
                .andExpect(jsonPath("$.paths['/api/v1/accounts'].post.summary")
                        .value("Open a bank account"))
                .andExpect(jsonPath("$.paths['/api/v1/transfers'].post.summary")
                        .value("Execute a transfer"))
                .andExpect(jsonPath("$.paths['/api/v1/transfers'].post.responses['201'].description",
                        containsString("Transfer accepted")))
                .andExpect(jsonPath("$.components.schemas.CustomerRequestDto.description",
                        containsString("Request body used to register")))
                .andExpect(jsonPath("$.components.schemas.CustomerRequestDto.properties.email.description",
                        containsString("Unique email address")))
                .andExpect(jsonPath("$.components.schemas.TransferRequestDto.properties.amount.description",
                        containsString("positive transfer amount")))
                .andExpect(jsonPath("$.paths['/api/v1/customers'].post.summary",
                        not(containsString("createCustomer"))));
    }
}
