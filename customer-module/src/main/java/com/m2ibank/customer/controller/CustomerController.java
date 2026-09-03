package com.m2ibank.customer.controller;

import com.m2ibank.common.api.ApiResponse;
import com.m2ibank.customer.dto.CustomerRequestDto;
import com.m2ibank.customer.dto.CustomerResponseDto;
import com.m2ibank.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Customers", description = "Register customers and retrieve customer profiles.")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(
            summary = "Register a customer",
            description = "Creates a customer profile after validating the request body and checking that the email and identity number are unique."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Customer created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed or the customer already exists")
    })
    public ResponseEntity<ApiResponse<CustomerResponseDto>> createCustomer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Customer registration details.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CustomerRequestDto.class)))
            @Valid @RequestBody CustomerRequestDto request) {
        CustomerResponseDto customer = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(customer, "Customer created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get customer by id",
            description = "Returns one customer profile by its internal identifier."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customer found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer id was not found")
    })
    public ApiResponse<CustomerResponseDto> getCustomerById(
            @Parameter(description = "Positive internal customer id.", example = "1")
            @PathVariable @Positive(message = "Customer id must be positive") Long id) {
        return ApiResponse.success(customerService.getCustomerById(id));
    }

    @GetMapping("/email/{email}")
    @Operation(
            summary = "Get customer by email",
            description = "Returns one customer profile using the customer's normalized email address."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customer found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer email was not found")
    })
    public ApiResponse<CustomerResponseDto> getCustomerByEmail(
            @Parameter(description = "Valid customer email address.", example = "amina.ndi@example.com")
            @PathVariable
            @Email(message = "Email must be valid")
            @Size(max = 254, message = "Email must not exceed 254 characters") String email) {
        return ApiResponse.success(customerService.getCustomerByEmail(email));
    }

    @GetMapping
    @Operation(
            summary = "List customers",
            description = "Returns all registered customer profiles in the database."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Customer list returned",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    public ApiResponse<List<CustomerResponseDto>> getAllCustomers() {
        return ApiResponse.success(customerService.getAllCustomers());
    }
}
