package com.m2ibank.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request body used to register a new DigiBank customer.")
public record CustomerRequestDto(
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        @Schema(description = "Customer's legal first name.", example = "Amina", maxLength = 100)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        @Schema(description = "Customer's legal last name.", example = "Ndi", maxLength = 100)
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        @Schema(description = "Unique email address used to identify and contact the customer.",
                example = "amina.ndi@example.com", maxLength = 254)
        String email,

        @NotBlank(message = "Identity number is required")
        @Size(max = 100, message = "Identity number must not exceed 100 characters")
        @Schema(description = "Government or bank-approved identity number used for customer checks.",
                example = "CMR-1998-00001", maxLength = 100)
        String identityNumber) {
}
