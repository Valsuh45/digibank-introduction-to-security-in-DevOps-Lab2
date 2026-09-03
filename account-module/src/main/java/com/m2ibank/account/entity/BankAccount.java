package com.m2ibank.account.entity;

import com.m2ibank.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * JPA entity for a DigiBank account.
 *
 * <p>The entity stores the account number, balance, currency, account type, status, creation time, and
 * owning customer id. It is mapped to the {@code bank_accounts} table and mirrors the constraints used
 * by the Flyway schema.</p>
 *
 * <p>Account numbers, currency, type, creation time, and customer ownership are immutable after
 * creation. Balance is the only mutable business value here, and {@link #setBalance(BigDecimal)}
 * rejects negative balances so invalid money state cannot be saved accidentally.</p>
 */
@Entity
@Table(name = "bank_accounts")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12, updatable = false)
    private String accountNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false, length = 3, updatable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false, updatable = false)
    private Long customerId;

    protected BankAccount() {
        // Required by JPA.
    }

    private BankAccount(
            String accountNumber,
            BigDecimal balance,
            String currency,
            AccountType accountType,
            AccountStatus status,
            Instant createdAt,
            Long customerId
    ) {
        this.accountNumber = Objects.requireNonNull(accountNumber, "accountNumber");
        this.currency = Objects.requireNonNull(currency, "currency");
        this.accountType = Objects.requireNonNull(accountType, "accountType");
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.customerId = Objects.requireNonNull(customerId, "customerId");
        setBalance(balance);
    }

    public static BankAccount open(
            String accountNumber,
            BigDecimal initialBalance,
            String currency,
            AccountType accountType,
            Long customerId
    ) {
        return new BankAccount(
                accountNumber,
                initialBalance,
                currency,
                accountType,
                AccountStatus.ACTIVE,
                Instant.now(),
                customerId
        );
    }

    @PrePersist
    void initializeCreatedAt() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void setBalance(BigDecimal balance) {
        if (balance == null || balance.signum() < 0) {
            throw new BusinessException("Account balance must not be negative");
        }
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getCustomerId() {
        return customerId;
    }
}
