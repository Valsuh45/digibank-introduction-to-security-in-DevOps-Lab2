package com.m2ibank.account.dto;

import com.m2ibank.account.entity.AccountType;
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
public record AccountRequestDto(
        @NotNull(message = "Customer ID is required")
        @Positive(message = "Customer ID must be positive")
        Long customerId,

        @NotNull(message = "Account type is required")
        AccountType accountType,

        @NotNull(message = "Initial balance is required")
        @DecimalMin(value = "0.00", message = "Initial balance must not be negative")
        @Digits(integer = 17, fraction = 2, message = "Initial balance must have at most two decimal places")
        BigDecimal initialBalance
) {
}
