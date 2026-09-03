package com.m2ibank.transfer.dto;

import com.m2ibank.transfer.entity.TransferStatus;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response body returned after a transfer is created or read.
 *
 * <p>The record contains the transfer id, public reference, source and target account numbers, amount,
 * final status, execution time, and optional description. It mirrors the immutable audit fields stored
 * by the {@code Transfer} entity.</p>
 */
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
