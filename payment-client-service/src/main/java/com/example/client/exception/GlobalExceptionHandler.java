package com.example.client.exception;

import com.example.client.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "MISSING_HEADER", ex.getHeaderName() + " header is required");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage());
    }

    @ExceptionHandler(ProviderBadRequestException.class)
    public ResponseEntity<ErrorResponse> handleProviderBadRequest(ProviderBadRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "PROVIDER_BAD_REQUEST", ex.getMessage());
    }

    @ExceptionHandler(BulkheadFullException.class)
    public ResponseEntity<ErrorResponse> handleBulkheadFull(BulkheadFullException ex) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "BULKHEAD_FULL", ex.getMessage());
    }

    @ExceptionHandler(CircuitBreakerOpenException.class)
    public ResponseEntity<ErrorResponse> handleCircuitOpen(CircuitBreakerOpenException ex) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "CIRCUIT_OPEN", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOtherExceptions(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message));
    }
}
