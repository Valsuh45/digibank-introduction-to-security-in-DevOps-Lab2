package com.m2ibank.common.exception;

/**
 * Raised when a requested customer, account, transfer, or other record cannot be found.
 *
 * <p>Services throw this exception after repository lookups fail. The web layer then turns it into a
 * 404-style response, giving API clients a clear result without exposing query or database internals.</p>
 */
public class ResourceNotFoundException extends DigiBankException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
