package com.m2ibank.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standard response envelope used by the REST controllers.
 *
 * <p>Every successful or failed API call can return the same shape: a success flag, a human-readable
 * message, optional data, and the time when the response was created. Keeping one envelope makes the
 * web API easier to test and easier for clients to parse.</p>
 *
 * <p>The static factory methods should be preferred over manually building common responses because
 * they set the success flag and timestamp consistently.</p>
 *
 * @param <T> type of the response body stored in {@link #data}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard DigiBank API response envelope returned by successful REST calls.")
public class ApiResponse<T> {

    @Schema(description = "Shows whether the request completed successfully.", example = "true")
    private boolean success;

    @Schema(description = "Human-readable result message for the client.", example = "Operation completed successfully")
    private String message;

    @Schema(description = "Endpoint-specific response body. The shape changes based on the API operation.")
    private T data;

    @Builder.Default
    @Schema(description = "Server time when the response was created.", example = "2026-09-03T11:46:47Z")
    private Instant timestamp = Instant.now();

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation completed successfully");
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .timestamp(Instant.now())
                .build();
    }
}
