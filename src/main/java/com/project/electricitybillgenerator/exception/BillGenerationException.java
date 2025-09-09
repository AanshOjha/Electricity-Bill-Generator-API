package com.project.electricitybillgenerator.exception;

/**
 * Exception thrown when bill generation fails due to business logic violations.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
public class BillGenerationException extends RuntimeException {
    
    public BillGenerationException(String message) {
        super(message);
    }
    
    public BillGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
