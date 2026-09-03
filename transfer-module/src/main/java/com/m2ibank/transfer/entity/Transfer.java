package com.m2ibank.transfer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA entity that records a completed transfer.
 *
 * <p>Transfer rows are audit records. After creation, the reference, accounts, amount, status, execution
 * date, and description are marked as non-updatable so saved history cannot be edited through normal JPA
 * updates.</p>
 *
 * <p>Indexes on source and target account numbers support transaction-history lookups. The factory
 * method currently creates successful transfers because failed transfer attempts are rejected before an
 * audit row is saved.</p>
 */
@Entity
@Table(
        name = "transfers",
        indexes = {
            @Index(name = "idx_transfers_source_account", columnList = "source_account_number"),
            @Index(name = "idx_transfers_target_account", columnList = "target_account_number")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_reference", nullable = false, unique = true, updatable = false, length = 40)
    private String transferReference;

    @Column(name = "source_account_number", nullable = false, updatable = false, length = 34)
    private String sourceAccountNumber;

    @Column(name = "target_account_number", nullable = false, updatable = false, length = 34)
    private String targetAccountNumber;

    @Column(nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private TransferStatus status;

    @Column(name = "execution_date", nullable = false, updatable = false)
    private Instant executionDate;

    @Column(updatable = false, length = 255)
    private String description;

    public static Transfer success(
            String transferReference,
            String sourceAccountNumber,
            String targetAccountNumber,
            BigDecimal amount,
            Instant executionDate,
            String description) {
        return new Transfer(
                null,
                transferReference,
                sourceAccountNumber,
                targetAccountNumber,
                amount,
                TransferStatus.SUCCESS,
                executionDate,
                description);
    }
}
