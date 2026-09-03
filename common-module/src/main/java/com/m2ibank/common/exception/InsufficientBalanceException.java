package com.m2ibank.common.exception;

/**
 * Raised when an account does not have enough money for a requested transfer.
 *
 * <p>This exception lets the transfer module explain the failure without exposing database details or
 * partial transaction state. The global exception handler maps it to a client error response.</p>
 */
public class InsufficientBalanceException extends DigiBankException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
