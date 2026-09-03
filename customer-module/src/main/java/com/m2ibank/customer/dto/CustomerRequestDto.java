package com.m2ibank.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body used to register a DigiBank customer.
 *
 * <p>The record contains names, email, and identity number. These are the only customer fields accepted
 * from the client; status and creation time are assigned by the service/entity layer.</p>
 *
 * <p>Bean Validation keeps names and identity numbers present, limits field lengths to match the
 * database schema, and checks email format before the service performs normalization and duplicate
 * checks.</p>
 */
public record CustomerRequestDto(
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        String email,

        @NotBlank(message = "Identity number is required")
        @Size(max = 100, message = "Identity number must not exceed 100 characters")
        String identityNumber) {
}
