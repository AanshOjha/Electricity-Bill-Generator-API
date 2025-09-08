package com.project.electricitybillgenerator.model;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Entity representing a user in the electricity billing system.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@Entity
@Table(name = "bill_user", indexes = {
    @Index(name = "idx_email", columnList = "email", unique = true)
})
public class BillUser {
    
    @Id
    @Column(name = "meter_id")
    private Integer meterId;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "address", nullable = false, length = 255)
    private String address;
    
    @Column(name = "password", nullable = false, length = 100)
    private String password;
    
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    // Default constructor
    public BillUser() {
    }

    // Constructor with essential fields
    public BillUser(String name, String address, String email, String password) {
        this.name = name;
        this.address = address;
        this.email = email;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillUser billUser = (BillUser) o;
        return Objects.equals(meterId, billUser.meterId) &&
               Objects.equals(email, billUser.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meterId, email);
    }

    @Override
    public String toString() {
        return "BillUser{" +
                "meterId=" + meterId +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
