package com.project.electricitybillgenerator.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for JWT Authentication Request.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
public class JwtRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;

    // Default constructor
    public JwtRequest() {
    }

    // Constructor with parameters
    public JwtRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "JwtRequest{" +
                "email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
