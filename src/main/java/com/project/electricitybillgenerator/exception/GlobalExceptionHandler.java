package com.project.electricitybillgenerator.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the electricity bill generator application.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles IllegalArgumentException.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "RESOURCE_NOT_FOUND",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        logger.warn("IllegalArgumentException occurred: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Invalid Input",
            ex.getMessage(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles validation errors for @Valid annotations on request bodies.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        logger.warn("Validation failed for request: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    FieldError::getField,
                    fieldError -> fieldError.getDefaultMessage() != null ? 
                        fieldError.getDefaultMessage() : "Invalid value",
                    (existing, duplicate) -> existing
                ));
        
        String message = "Validation failed: " + 
            fieldErrors.entrySet().stream()
                .map(entry -> entry.getKey() + " - " + entry.getValue())
                .collect(Collectors.joining(", "));
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Validation Error",
            message,
            request.getDescription(false)
        );
        
        errorResponse.put("fieldErrors", fieldErrors);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles binding errors.
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBindException(
            BindException ex, WebRequest request) {
        
        logger.warn("Binding exception occurred: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    FieldError::getField,
                    fieldError -> fieldError.getDefaultMessage() != null ? 
                        fieldError.getDefaultMessage() : "Invalid value"
                ));
        
        String message = "Binding failed: " + 
            fieldErrors.entrySet().stream()
                .map(entry -> entry.getKey() + " - " + entry.getValue())
                .collect(Collectors.joining(", "));
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Binding Error",
            message,
            request.getDescription(false)
        );
        
        errorResponse.put("fieldErrors", fieldErrors);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles constraint violation exceptions.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        logger.warn("Constraint violation occurred: {}", ex.getMessage());
        
        Map<String, String> violations = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            violations.put(propertyPath, message);
        }
        
        String message = "Constraint violations: " + 
            violations.entrySet().stream()
                .map(entry -> entry.getKey() + " - " + entry.getValue())
                .collect(Collectors.joining(", "));
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Validation Error",
            message,
            request.getDescription(false)
        );
        
        errorResponse.put("violations", violations);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles malformed JSON requests.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {
        
        logger.warn("Malformed JSON request: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Malformed Request",
            "Invalid JSON format. Please check your request body.",
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles method argument type mismatch (e.g., string where number expected).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        logger.warn("Method argument type mismatch: {}", ex.getMessage());
        
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", 
            ex.getValue(), 
            ex.getName(), 
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Type Mismatch",
            message,
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles RuntimeException.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        logger.error("RuntimeException occurred: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Runtime Error",
            ex.getMessage(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles generic Exception.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {
        
        logger.error("Unexpected exception occurred", ex);
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "An unexpected error occurred. Please try again later.",
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Creates a standardized error response.
     */
    private Map<String, Object> createErrorResponse(HttpStatus status, String error, 
                                                   String message, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        return errorResponse;
    }
}
