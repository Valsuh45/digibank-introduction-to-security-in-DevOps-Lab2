package com.m2ibank.transfer.dto;

import com.m2ibank.transfer.entity.TransferStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponseDto(
        Long id,
        String transferReference,
        String sourceAccountNumber,
        String targetAccountNumber,
        BigDecimal amount,
        TransferStatus status,
        Instant executionDate,
        String description) {
}
