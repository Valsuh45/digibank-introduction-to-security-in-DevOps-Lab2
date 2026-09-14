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
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
 * <p>Security matters here: the client-facing message never echoes the internal exception message.
 * Known business and not-found errors return a concise generic message (so callers cannot infer the
 * existence of resources, enumerate identifiers, or learn business state), while the real reason is
 * logged server-side. Unexpected failures are also logged and returned as a generic message. That
 * prevents stack traces, database errors, resource identifiers, and implementation details from
 * leaking to callers.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String NOT_FOUND_MESSAGE = "Resource not found";
    private static final String BUSINESS_FAILURE_MESSAGE = "Request could not be processed";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException exception) {
        LOGGER.warn("Resource not found: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(NOT_FOUND_MESSAGE));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException exception) {
        LOGGER.warn("No resource found for path: {}", exception.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(NOT_FOUND_MESSAGE));
    }

    @ExceptionHandler({
            BusinessException.class,
            InsufficientBalanceException.class,
            InvalidOperationException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBusinessFailure(DigiBankException exception) {
        LOGGER.warn("Business failure: {}", exception.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error(BUSINESS_FAILURE_MESSAGE));
    }

    @ExceptionHandler(DigiBankException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainFailure(DigiBankException exception) {
        LOGGER.warn("Domain failure: {}", exception.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error(BUSINESS_FAILURE_MESSAGE));
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

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(TypeMismatchException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Request parameter is invalid"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedFailure(Exception exception) {
        // Spring MVC exceptions carry protocol status and headers (for example Allow for 405).
        // Keep those semantics without returning exception details or rejected user input.
        if (exception instanceof ErrorResponse error && error.getStatusCode().is4xxClientError()) {
            HttpStatus status = HttpStatus.resolve(error.getStatusCode().value());
            String message = status == null ? "Request is invalid" : status.getReasonPhrase();
            return ResponseEntity.status(error.getStatusCode()).headers(error.getHeaders())
                    .body(ApiResponse.error(message));
        }
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
