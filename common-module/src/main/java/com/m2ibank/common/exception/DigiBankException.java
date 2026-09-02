package com.m2ibank.common.exception;

public class DigiBankException extends RuntimeException {

    public DigiBankException(String message) {
        super(message);
    }

    public DigiBankException(String message, Throwable cause) {
        super(message, cause);
    }
}
