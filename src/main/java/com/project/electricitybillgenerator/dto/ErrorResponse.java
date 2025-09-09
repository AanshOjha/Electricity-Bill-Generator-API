package com.project.electricitybillgenerator.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Error Response.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
public class ErrorResponse {
    
    private final String error;
    private final String timestamp;

    public ErrorResponse(String error) {
        this.error = error;
        this.timestamp = LocalDateTime.now().toString();
    }

    public String getError() {
        return error;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "error='" + error + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
