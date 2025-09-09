package com.project.electricitybillgenerator.dto;

/**
 * Data Transfer Object for JWT Authentication Response.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
public class JwtResponse {
    
    private String token;
    private String type = "Bearer";
    private String email;
    private String role;
    private Integer meterId;
    private String name;

    // Default constructor
    public JwtResponse() {
    }

    // Constructor with parameters
    public JwtResponse(String token, String email, String role, Integer meterId, String name) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.meterId = meterId;
        this.name = name;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    @Override
    public String toString() {
        return "JwtResponse{" +
                "token='[PROTECTED]'" +
                ", type='" + type + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", meterId=" + meterId +
                ", name='" + name + '\'' +
                '}';
    }
}
