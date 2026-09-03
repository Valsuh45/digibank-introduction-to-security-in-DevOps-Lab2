package com.m2ibank.common.exception;

/**
 * Generic business-rule exception.
 *
 * <p>Use this exception when an operation is understood by the system but cannot be accepted because it
 * breaks a business rule. More specific exceptions, such as insufficient balance or missing resources,
 * should be used when they describe the problem more clearly.</p>
 */
public class BusinessException extends DigiBankException {

    public BusinessException(String message) {
        super(message);
    }
}
