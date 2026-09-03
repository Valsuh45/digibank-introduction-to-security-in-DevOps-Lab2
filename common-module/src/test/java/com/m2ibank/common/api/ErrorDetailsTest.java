package com.m2ibank.common.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for detailed API error payloads.
 *
 * <p>The tests cover default timestamps, builder behavior, null validation maps, and defensive copying.
 * Defensive copying is important because validation errors should not change after an error response has
 * been created.</p>
 */
class ErrorDetailsTest {

    @Test
    void builderDefaultsValidationErrorsToEmptyMap() {
        ErrorDetails errorDetails = ErrorDetails.builder()
                .status(400)
                .error("Bad Request")
                .message("Request validation failed")
                .path("/api/transfers")
                .build();

        assertNotNull(errorDetails.getValidationErrors());
        assertTrue(errorDetails.getValidationErrors().isEmpty());
    }

    @Test
    void validationErrorsRemainFieldSpecific() {
        ErrorDetails errorDetails = ErrorDetails.builder()
                .status(400)
                .error("Bad Request")
                .message("Request validation failed")
                .path("/api/transfers")
                .validationErrors(Map.of("amount", "must be positive"))
                .build();

        assertEquals(Map.of("amount", "must be positive"), errorDetails.getValidationErrors());
    }

    @Test
    void validationErrorsAreDefensivelyCopied() {
        Map<String, String> validationErrors = new HashMap<>();
        validationErrors.put("amount", "must be positive");

        ErrorDetails errorDetails = ErrorDetails.builder()
                .status(400)
                .error("Bad Request")
                .message("Request validation failed")
                .path("/api/transfers")
                .validationErrors(validationErrors)
                .build();

        validationErrors.put("accountNumber", "must be valid");

        assertEquals(Map.of("amount", "must be positive"), errorDetails.getValidationErrors());
    }

    @Test
    void serializationExposesOnlySafePublicFields() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        ErrorDetails errorDetails = ErrorDetails.builder()
                .status(403)
                .error("Forbidden")
                .message("Access denied")
                .path("/api/accounts")
                .build();

        JsonNode json = objectMapper.valueToTree(errorDetails);

        Set<String> fieldNames = new HashSet<>();
        json.fieldNames().forEachRemaining(fieldNames::add);
        assertEquals(
                Set.of("timestamp", "status", "error", "message", "path", "validationErrors"),
                fieldNames
        );
    }
}
