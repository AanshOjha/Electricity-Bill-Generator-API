package com.project.electricitybillgenerator.dto.response;

import com.project.electricitybillgenerator.entity.Customer;

import java.time.LocalDateTime;

public class CustomerResponse {
    
    private Long id;
    private String name;
    private String email;
    private String address;
    private String phone;
    private Customer.CustomerType customerType;
    private Customer.Role role;
    private Boolean active;
    private LocalDateTime createdAt;
    
    // Constructors
    public CustomerResponse() {}
    
    public CustomerResponse(Customer customer) {
        this.id = customer.getId();
        this.name = customer.getName();
        this.email = customer.getEmail();
        this.address = customer.getAddress();
        this.phone = customer.getPhone();
        this.customerType = customer.getCustomerType();
        this.role = customer.getRole();
        this.active = customer.getActive();
        this.createdAt = customer.getCreatedAt();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public Customer.CustomerType getCustomerType() { return customerType; }
    public void setCustomerType(Customer.CustomerType customerType) { this.customerType = customerType; }
    
    public Customer.Role getRole() { return role; }
    public void setRole(Customer.Role role) { this.role = role; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
