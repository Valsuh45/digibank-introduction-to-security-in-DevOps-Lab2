package com.m2ibank.account.dto;

import com.m2ibank.account.entity.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request body used when a client opens a new bank account.
 *
 * <p>The record contains only the values the client is allowed to choose: the owning customer, account
 * type, and opening balance. The server generates the account number, currency, status, and creation
 * time so clients cannot forge those trusted fields.</p>
 *
 * <p>Bean Validation keeps invalid values out of the service layer. Balances must be present, must not
 * be negative, and must fit the two-decimal money format used by the database schema.</p>
 */
@Schema(description = "Request body used to open a new bank account for an existing customer.")
public record AccountRequestDto(
        @NotNull(message = "Customer ID is required")
        @Positive(message = "Customer ID must be positive")
        @Schema(description = "Identifier of the customer who will own the new account.", example = "1")
        Long customerId,

        @NotNull(message = "Account type is required")
        @Schema(description = "Type of bank account to open.", example = "SAVINGS")
        AccountType accountType,

        @NotNull(message = "Initial balance is required")
        @DecimalMin(value = "0.00", message = "Initial balance must not be negative")
        @Digits(integer = 17, fraction = 2, message = "Initial balance must have at most two decimal places")
        @Schema(description = "Opening balance for the account. It must be zero or a positive money amount.",
                example = "25000.00")
        BigDecimal initialBalance
) {
}
