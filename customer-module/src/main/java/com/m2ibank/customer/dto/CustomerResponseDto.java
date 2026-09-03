package com.m2ibank.customer.dto;

import com.m2ibank.customer.entity.CustomerStatus;

import java.time.Instant;

/**
 * Response body returned when customer data is sent to API clients.
 *
 * <p>The response contains the public customer fields needed by the workshop API: id, names, normalized
 * email, status, and creation time. It intentionally does not expose the identity number because that is
 * sensitive customer data and should not be echoed unless a use case truly needs it.</p>
 */
public record CustomerResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        CustomerStatus status,
        Instant createdAt) {
}
