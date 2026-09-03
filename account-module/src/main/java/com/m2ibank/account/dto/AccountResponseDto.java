package com.m2ibank.account.dto;

import com.m2ibank.account.entity.AccountStatus;
import com.m2ibank.account.entity.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response body returned to clients for bank account data.
 *
 * <p>This record exposes the public account view after the service has created or loaded a
 * {@code BankAccount}. It includes identifiers, balance, currency, account type, status, creation time,
 * and the owning customer id.</p>
 *
 * <p>The response does not expose persistence internals or any credentials. It is safe for the REST API
 * to serialize directly.</p>
 */
@Schema(description = "Public bank account details returned by account endpoints.")
public record AccountResponseDto(
        @Schema(description = "Internal account identifier.", example = "1")
        Long id,

        @Schema(description = "Generated 12-digit account number.", example = "100000000001")
        String accountNumber,

        @Schema(description = "Current available account balance.", example = "50000.00")
        BigDecimal balance,

        @Schema(description = "Account currency code.", example = "XAF")
        String currency,

        @Schema(description = "Type of account product.", example = "SAVINGS")
        AccountType accountType,

        @Schema(description = "Current account lifecycle status.", example = "ACTIVE")
        AccountStatus status,

        @Schema(description = "Time when the account was created.", example = "2026-09-03T11:46:47Z")
        Instant createdAt,

        @Schema(description = "Identifier of the customer who owns the account.", example = "1")
        Long customerId
) {
}
