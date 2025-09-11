package com.project.electricitybillgenerator.dto.request;

import com.project.electricitybillgenerator.entity.Customer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CustomerRegistrationRequest {
    
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    @NotBlank(message = "Phone is required")
    @Size(max = 15, message = "Phone cannot exceed 15 characters")
    private String phone;
    
    private Customer.CustomerType customerType = Customer.CustomerType.RESIDENTIAL;
    
    private Customer.Role role = Customer.Role.ROLE_USER;
    
    // Constructors
    public CustomerRegistrationRequest() {}
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public Customer.CustomerType getCustomerType() { return customerType; }
    public void setCustomerType(Customer.CustomerType customerType) { this.customerType = customerType; }
    
    public Customer.Role getRole() { return role; }
    public void setRole(Customer.Role role) { this.role = role; }
}
