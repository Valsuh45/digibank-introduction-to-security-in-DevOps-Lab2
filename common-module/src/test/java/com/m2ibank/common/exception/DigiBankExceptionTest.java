package com.m2ibank.common.exception;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for the base DigiBank exception type.
 *
 * <p>The tests verify that messages and causes are preserved. That matters because services throw domain
 * exceptions and the web layer converts them into safe API responses while logs can still keep the cause.</p>
 */
class DigiBankExceptionTest {

    @Test
    void baseExceptionPreservesMessageAndCauseForInternalHandling() {
        IllegalStateException cause = new IllegalStateException("database diagnostic");

        DigiBankException exception = new DigiBankException("Operation failed", cause);

        assertEquals("Operation failed", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void domainExceptionsShareTheDigiBankExceptionContract() {
        List<DigiBankException> exceptions = List.of(
                new ResourceNotFoundException("Account not found"),
                new InsufficientBalanceException("Insufficient balance"),
                new BusinessException("Business rule rejected the request"),
                new InvalidOperationException("Operation is not permitted")
        );

        assertEquals(
                List.of(
                        "Account not found",
                        "Insufficient balance",
                        "Business rule rejected the request",
                        "Operation is not permitted"
                ),
                exceptions.stream().map(Throwable::getMessage).toList()
        );
        exceptions.forEach(exception -> assertInstanceOf(DigiBankException.class, exception));
    }
}
