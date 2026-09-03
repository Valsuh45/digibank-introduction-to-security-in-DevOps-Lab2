package com.m2ibank.common.exception;

/**
 * Base runtime exception for DigiBank domain errors.
 *
 * <p>All custom business exceptions extend this type so the web layer can catch domain failures in one
 * place and convert them into clean API responses. It keeps business errors separate from unexpected
 * technical failures.</p>
 *
 * <p>The constructors keep the original message and optional cause. Preserving the cause helps debugging
 * while still allowing the public API to return a safe, simple message.</p>
 */
public class DigiBankException extends RuntimeException {

    public DigiBankException(String message) {
        super(message);
    }

    public DigiBankException(String message, Throwable cause) {
        super(message, cause);
    }
}
