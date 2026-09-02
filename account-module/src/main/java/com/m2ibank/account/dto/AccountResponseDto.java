package com.m2ibank.account.dto;

import com.m2ibank.account.entity.AccountStatus;
import com.m2ibank.account.entity.AccountType;

import java.math.BigDecimal;
import java.time.Instant;

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
