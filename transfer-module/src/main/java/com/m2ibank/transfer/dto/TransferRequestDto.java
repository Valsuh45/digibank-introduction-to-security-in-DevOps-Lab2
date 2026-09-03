package com.m2ibank.transfer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request body used to execute a transfer between two accounts.
 *
 * <p>The client supplies source account number, target account number, amount, and an optional
 * description. The server creates the transfer reference, status, and execution time so trusted audit
 * fields cannot be forged by a caller.</p>
 *
 * <p>Bean Validation checks the simple shape of the request. The service performs the deeper business
 * checks, such as preventing transfers to the same account and rejecting insufficient balances.</p>
 */
@Schema(description = "Request body used to execute a money transfer between two DigiBank accounts.")
public record TransferRequestDto(
        @NotBlank(message = "Source account number is required")
        @Schema(description = "12-digit account number that money will be debited from.",
                example = "100000000001")
        String sourceAccountNumber,

        @NotBlank(message = "Target account number is required")
        @Schema(description = "12-digit account number that money will be credited to.",
                example = "100000000002")
        String targetAccountNumber,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        @Schema(description = "Required positive transfer amount.", example = "100.00")
        BigDecimal amount,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        @Schema(description = "Optional short note stored with the transfer audit record.",
                example = "Monthly savings transfer", maxLength = 255)
        String description) {
}
