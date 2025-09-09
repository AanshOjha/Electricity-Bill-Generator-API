package com.project.electricitybillgenerator.model;

/**
 * Enumeration representing user roles in the electricity billing system.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
public enum UserRole {
    /**
     * Regular user role - can view their own bills and submit readings
     */
    ROLE_USER,
    
    /**
     * Administrative role - can perform any action including user management and bill generation
     */
    ROLE_ADMIN
}
