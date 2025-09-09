package com.project.electricitybillgenerator.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response format for the API.
 * This class provides a consistent structure for all error responses.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private String errorCode;
    private String message;
    private Map<String, String> fieldErrors;
    private LocalDateTime timestamp;
    private String path;
    
    /**
     * Constructor with error code and message.
     */
    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructor with error code, message, and field errors.
     */
    public ErrorResponse(String errorCode, String message, Map<String, String> fieldErrors) {
        this.errorCode = errorCode;
        this.message = message;
        this.fieldErrors = fieldErrors;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Complete constructor with all fields.
     */
    public ErrorResponse(String errorCode, String message, Map<String, String> fieldErrors, String path) {
        this.errorCode = errorCode;
        this.message = message;
        this.fieldErrors = fieldErrors;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and setters
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
    
    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
}
