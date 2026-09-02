package com.m2ibank.common.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testSuccessResponse() {
        ApiResponse<String> response = ApiResponse.success("Test Payload", "Success message");

        assertTrue(response.isSuccess());
        assertEquals("Test Payload", response.getData());
        assertEquals("Success message", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error("Error occurred");

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("Error occurred", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testBuilderDefaultTimestamp() {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Builder default test")
                .build();

        assertTrue(response.isSuccess());
        assertNotNull(response.getTimestamp());
    }
}
