package com.m2ibank.common.exception;

/**
 * Raised when a request asks DigiBank to perform an operation that is not allowed.
 *
 * <p>Examples include invalid account relationships, blocked actions, or other domain rules that are
 * separate from simple input-format validation. Keeping this as a domain exception lets the API return
 * a controlled message instead of leaking internal implementation details.</p>
 */
public class InvalidOperationException extends DigiBankException {

    public InvalidOperationException(String message) {
        super(message);
    }
}
