package com.m2ibank.common.api;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

/**
 * Detailed error payload used when the API needs more context than {@link ApiResponse}.
 *
 * <p>This object keeps the HTTP status, short error name, clear message, request path, timestamp, and
 * optional field validation errors together. It is useful for predictable error handling because clients
 * can read the same fields for validation failures and other request problems.</p>
 *
 * <p>The validation error map is defensively copied when it is set. That protects the error response
 * from accidental mutation after it has been created, which is important when several layers may hold
 * a reference to the original map.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class ErrorDetails {

    private Instant timestamp = Instant.now();
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors = Map.of();

    @Builder
    public ErrorDetails(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            Map<String, String> validationErrors
    ) {
        this.timestamp = timestamp == null ? Instant.now() : timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        setValidationErrors(validationErrors);
    }

    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors == null ? Map.of() : Map.copyOf(validationErrors);
    }
}
