package com.project.electricitybillgenerator.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tariffs")
public class Tariff {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Tariff name is required")
    @Column(nullable = false, length = 100)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Customer.CustomerType customerType;
    
    @Column(nullable = false, precision = 8, scale = 4)
    @DecimalMin(value = "0.0", message = "Rate cannot be negative")
    private BigDecimal ratePerUnit;
    
    @Column(precision = 8, scale = 2)
    private BigDecimal fixedCharge = BigDecimal.ZERO;
    
    @Column(precision = 8, scale = 2)
    private BigDecimal minimumCharge = BigDecimal.ZERO;
    
    @Column(nullable = false)
    private LocalDate effectiveDate;
    
    @Column
    private LocalDate expiryDate;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Constructors
    public Tariff() {}
    
    public Tariff(String name, Customer.CustomerType customerType, BigDecimal ratePerUnit, LocalDate effectiveDate) {
        this.name = name;
        this.customerType = customerType;
        this.ratePerUnit = ratePerUnit;
        this.effectiveDate = effectiveDate;
        this.createdAt = LocalDateTime.now();
    }
    
    // Business method to calculate bill amount
    public BigDecimal calculateBillAmount(BigDecimal unitsConsumed) {
        if (unitsConsumed == null || unitsConsumed.compareTo(BigDecimal.ZERO) < 0) {
            return minimumCharge != null ? minimumCharge : BigDecimal.ZERO;
        }
        
        BigDecimal variableCharge = unitsConsumed.multiply(ratePerUnit);
        BigDecimal totalCharge = variableCharge.add(fixedCharge != null ? fixedCharge : BigDecimal.ZERO);
        
        return minimumCharge != null && totalCharge.compareTo(minimumCharge) < 0 
            ? minimumCharge 
            : totalCharge;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Customer.CustomerType getCustomerType() { return customerType; }
    public void setCustomerType(Customer.CustomerType customerType) { this.customerType = customerType; }
    
    public BigDecimal getRatePerUnit() { return ratePerUnit; }
    public void setRatePerUnit(BigDecimal ratePerUnit) { this.ratePerUnit = ratePerUnit; }
    
    public BigDecimal getFixedCharge() { return fixedCharge; }
    public void setFixedCharge(BigDecimal fixedCharge) { this.fixedCharge = fixedCharge; }
    
    public BigDecimal getMinimumCharge() { return minimumCharge; }
    public void setMinimumCharge(BigDecimal minimumCharge) { this.minimumCharge = minimumCharge; }
    
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
