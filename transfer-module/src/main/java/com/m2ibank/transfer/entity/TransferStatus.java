package com.m2ibank.transfer.entity;

/**
 * Final status stored with each transfer audit record.
 *
 * <p>The current workshop flow saves only successful transfers. The enum keeps the database design ready
 * for future failed or pending statuses without changing the response type.</p>
 */
public enum TransferStatus {
    SUCCESS
}
