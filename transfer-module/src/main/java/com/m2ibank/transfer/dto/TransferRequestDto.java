package com.m2ibank.transfer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
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
        @Pattern(regexp = "[1-9][0-9]{11}", message = "Account number must contain 12 digits and start with 1-9")
        @Schema(description = "12-digit account number that money will be debited from.",
                example = "100000000001")
        String sourceAccountNumber,

        @NotBlank(message = "Target account number is required")
        @Pattern(regexp = "[1-9][0-9]{11}", message = "Account number must contain 12 digits and start with 1-9")
        @Schema(description = "12-digit account number that money will be credited to.",
                example = "100000000002")
        String targetAccountNumber,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        @Digits(integer = 17, fraction = 2, message = "Amount must have at most 17 integer digits and 2 decimal places")
        @Schema(description = "Required positive transfer amount.", example = "100.00")
        BigDecimal amount,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        @Schema(description = "Optional short note stored with the transfer audit record.",
                example = "Monthly savings transfer", maxLength = 255)
        String description) {

    public TransferRequestDto {
        sourceAccountNumber = sourceAccountNumber == null ? null : sourceAccountNumber.trim();
        targetAccountNumber = targetAccountNumber == null ? null : targetAccountNumber.trim();
    }
}
