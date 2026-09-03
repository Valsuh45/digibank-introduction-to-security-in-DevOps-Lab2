package com.m2ibank.account.entity;

/**
 * Lifecycle states for a bank account.
 *
 * <p>The current workflow uses active accounts for normal operations and keeps closed accounts available
 * for future business rules. Storing the enum name in the database makes records readable and avoids
 * fragile numeric status codes.</p>
 */
public enum AccountStatus {
    ACTIVE,
    FROZEN,
    CLOSED
}
