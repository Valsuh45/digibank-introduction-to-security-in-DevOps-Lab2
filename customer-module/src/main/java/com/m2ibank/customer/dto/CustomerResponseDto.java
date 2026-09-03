package com.m2ibank.customer.dto;

import com.m2ibank.customer.entity.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Response body returned when customer data is sent to API clients.
 *
 * <p>The response contains the public customer fields needed by the workshop API: id, names, normalized
 * email, status, and creation time. It intentionally does not expose the identity number because that is
 * sensitive customer data and should not be echoed unless a use case truly needs it.</p>
 */
@Schema(description = "Public customer details returned by DigiBank. Sensitive identity data is not exposed.")
public record CustomerResponseDto(
        @Schema(description = "Internal customer identifier.", example = "1")
        Long id,

        @Schema(description = "Customer's legal first name.", example = "Amina")
        String firstName,

        @Schema(description = "Customer's legal last name.", example = "Ndi")
        String lastName,

        @Schema(description = "Normalized unique customer email address.", example = "amina.ndi@example.com")
        String email,

        @Schema(description = "Current customer lifecycle status.", example = "ACTIVE")
        CustomerStatus status,

        @Schema(description = "Time when the customer record was created.", example = "2026-09-03T11:46:47Z")
        Instant createdAt) {
}
