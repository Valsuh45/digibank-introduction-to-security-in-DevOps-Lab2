package com.m2ibank.account.entity;

/**
 * Supported account products in DigiBank.
 *
 * <p>This enum keeps account creation limited to known product types. Using a fixed enum is safer than
 * accepting arbitrary strings from clients because invalid products are rejected during validation.</p>
 */
public enum AccountType {
    CURRENT,
    SAVINGS
}
