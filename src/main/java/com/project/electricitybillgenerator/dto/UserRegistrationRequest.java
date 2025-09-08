package com.project.electricitybillgenerator.dto;

/**
 * Data Transfer Object for User Registration requests.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
public class UserRegistrationRequest {
    
    private String name;
    private String address;
    private String email;
    private String password;

    // Default constructor
    public UserRegistrationRequest() {
    }

    // Constructor with parameters
    public UserRegistrationRequest(String name, String address, String email, String password) {
        this.name = name;
        this.address = address;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserRegistrationRequest{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
