package com.m2ibank.customer.entity;

/**
 * Lifecycle states for a customer record.
 *
 * <p>Active customers can use the normal DigiBank flows. Inactive and suspended states are available for
 * future rules, audits, or access restrictions without changing the database column shape.</p>
 */
public enum CustomerStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED
}
