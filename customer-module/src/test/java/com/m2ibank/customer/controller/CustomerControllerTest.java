package com.m2ibank.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2ibank.customer.dto.CustomerRequestDto;
import com.m2ibank.customer.dto.CustomerResponseDto;
import com.m2ibank.customer.entity.CustomerStatus;
import com.m2ibank.customer.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CustomerController(customerService)).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createsCustomerAndReturnsCreatedResponse() throws Exception {
        CustomerRequestDto request = new CustomerRequestDto("Jane", "Doe", "jane@example.com", "ID-12345");
        when(customerService.createCustomer(request)).thenReturn(response(10L, "jane@example.com"));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.identityNumber").doesNotExist());
    }

    @Test
    void rejectsInvalidCustomerRequest() throws Exception {
        CustomerRequestDto request = new CustomerRequestDto("", "Doe", "invalid", "");

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsCustomerById() throws Exception {
        when(customerService.getCustomerById(10L)).thenReturn(response(10L, "jane@example.com"));

        mockMvc.perform(get("/api/v1/customers/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10L));
    }

    @Test
    void returnsCustomerByEmail() throws Exception {
        when(customerService.getCustomerByEmail("jane@example.com"))
                .thenReturn(response(10L, "jane@example.com"));

        mockMvc.perform(get("/api/v1/customers/email/jane@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("jane@example.com"));
    }

    @Test
    void returnsAllCustomers() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of(
                response(10L, "jane@example.com"),
                response(11L, "john@example.com")));

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    private CustomerResponseDto response(Long id, String email) {
        return new CustomerResponseDto(
                id, "Jane", "Doe", email, CustomerStatus.ACTIVE, Instant.parse("2026-01-01T00:00:00Z"));
    }
}
