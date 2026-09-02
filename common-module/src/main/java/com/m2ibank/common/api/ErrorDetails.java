package com.m2ibank.common.api;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

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
