package com.m2ibank.customer.controller;

import com.m2ibank.common.api.ApiResponse;
import com.m2ibank.customer.dto.CustomerRequestDto;
import com.m2ibank.customer.dto.CustomerResponseDto;
import com.m2ibank.customer.service.CustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for customer API endpoints.
 *
 * <p>This class accepts customer creation and lookup requests at {@code /api/v1/customers}. It keeps
 * HTTP-specific details in one place and delegates customer rules, duplicate checks, normalization, and
 * persistence to {@link CustomerService}.</p>
 *
 * <p>Request validation is applied at the controller boundary. That gives clients clear validation
 * errors and prevents obviously unsafe or malformed values from entering deeper application layers.</p>
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Validated
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponseDto>> createCustomer(
            @Valid @RequestBody CustomerRequestDto request) {
        CustomerResponseDto customer = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(customer, "Customer created successfully"));
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerResponseDto> getCustomerById(
            @PathVariable @Positive(message = "Customer id must be positive") Long id) {
        return ApiResponse.success(customerService.getCustomerById(id));
    }

    @GetMapping("/email/{email}")
    public ApiResponse<CustomerResponseDto> getCustomerByEmail(
            @PathVariable
            @Email(message = "Email must be valid")
            @Size(max = 254, message = "Email must not exceed 254 characters") String email) {
        return ApiResponse.success(customerService.getCustomerByEmail(email));
    }

    @GetMapping
    public ApiResponse<List<CustomerResponseDto>> getAllCustomers() {
        return ApiResponse.success(customerService.getAllCustomers());
    }
}
