package com.m2ibank.web.exception;

import com.m2ibank.common.api.ApiResponse;
import com.m2ibank.common.exception.BusinessException;
import com.m2ibank.common.exception.DigiBankException;
import com.m2ibank.common.exception.InsufficientBalanceException;
import com.m2ibank.common.exception.InvalidOperationException;
import com.m2ibank.common.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central REST exception handler for the DigiBank API.
 *
 * <p>This advice converts domain exceptions, validation failures, unreadable request bodies, and
 * unexpected errors into consistent {@link ApiResponse} JSON payloads. Centralizing this logic keeps
 * controllers small and makes the API behavior predictable.</p>
 *
 * <p>Security matters here: known business errors return clear client-facing messages, but unexpected
 * failures are logged server-side and returned as a generic message. That prevents stack traces,
 * database errors, and implementation details from leaking to callers.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler({
            BusinessException.class,
            InsufficientBalanceException.class,
            InvalidOperationException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBusinessFailure(DigiBankException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(DigiBankException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainFailure(DigiBankException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBodyValidation(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(validationResponse(errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintValidation(
            ConstraintViolationException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation ->
                errors.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage()));
        return ResponseEntity.badRequest().body(validationResponse(errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableRequest(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Request body is invalid"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedFailure(Exception exception) {
        LOGGER.error("Unhandled request failure", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred"));
    }

    private ApiResponse<Map<String, String>> validationResponse(Map<String, String> errors) {
        return ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Validation failed")
                .data(errors)
                .timestamp(Instant.now())
                .build();
    }
}
