package com.project.electricitybillgenerator.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for User Response.
 * Used to return user information without exposing sensitive data.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
public class UserResponse {
    
    private Integer meterId;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    private String address;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    private String role;

    // Default constructor
    public UserResponse() {
    }

    // Constructor with parameters
    public UserResponse(Integer meterId, String name, String address, String email, String role) {
        this.meterId = meterId;
        this.name = name;
        this.address = address;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public Integer getMeterId() {
        return meterId;
    }

    public void setMeterId(Integer meterId) {
        this.meterId = meterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "meterId=" + meterId +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
