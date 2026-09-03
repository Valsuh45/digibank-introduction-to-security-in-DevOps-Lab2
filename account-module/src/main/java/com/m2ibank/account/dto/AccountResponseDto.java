package com.m2ibank.account.dto;

import com.m2ibank.account.entity.AccountStatus;
import com.m2ibank.account.entity.AccountType;

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
public record AccountResponseDto(
        Long id,
        String accountNumber,
        BigDecimal balance,
        String currency,
        AccountType accountType,
        AccountStatus status,
        Instant createdAt,
        Long customerId
) {
}
