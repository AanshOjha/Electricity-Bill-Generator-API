package com.project.electricitybillgenerator.dto.response;

public class AuthResponse {
    
    private String token;
    private String type = "Bearer";
    private CustomerResponse customer;
    
    // Constructors
    public AuthResponse() {}
    
    public AuthResponse(String token, CustomerResponse customer) {
        this.token = token;
        this.customer = customer;
    }
    
    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public CustomerResponse getCustomer() { return customer; }
    public void setCustomer(CustomerResponse customer) { this.customer = customer; }
}
