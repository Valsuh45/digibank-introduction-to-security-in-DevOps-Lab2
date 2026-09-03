package com.m2ibank.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Structured error response used for validation, domain, and unexpected request failures.")
public class ErrorDetails {

    @Schema(description = "Server time when the error response was created.", example = "2026-09-03T11:46:47Z")
    private Instant timestamp = Instant.now();

    @Schema(description = "HTTP status code returned to the client.", example = "400")
    private int status;

    @Schema(description = "Short HTTP error name.", example = "Bad Request")
    private String error;

    @Schema(description = "Safe client-facing explanation of what went wrong.", example = "Validation failed")
    private String message;

    @Schema(description = "Request path that produced the error.", example = "/api/v1/customers")
    private String path;

    @Schema(description = "Field-level validation errors keyed by request field name.")
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
