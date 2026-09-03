package com.m2ibank.transfer.dto;

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
public record TransferRequestDto(
        @NotBlank(message = "Source account number is required")
        String sourceAccountNumber,

        @NotBlank(message = "Target account number is required")
        String targetAccountNumber,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description) {
}
