package com.project.electricitybillgenerator.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for bill generation requests.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
public class BillGenerationRequest {
    
    private Integer meterId;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private BigDecimal currentReading;
    
    // Default constructor
    public BillGenerationRequest() {
    }
    
    // Constructor with all fields
    public BillGenerationRequest(Integer meterId, LocalDate billingPeriodStart, 
                               LocalDate billingPeriodEnd, BigDecimal currentReading) {
        this.meterId = meterId;
        this.billingPeriodStart = billingPeriodStart;
        this.billingPeriodEnd = billingPeriodEnd;
        this.currentReading = currentReading;
    }
    
    // Getters and Setters
    
    public Integer getMeterId() {
        return meterId;
    }
    
    public void setMeterId(Integer meterId) {
        this.meterId = meterId;
    }
    
    public LocalDate getBillingPeriodStart() {
        return billingPeriodStart;
    }
    
    public void setBillingPeriodStart(LocalDate billingPeriodStart) {
        this.billingPeriodStart = billingPeriodStart;
    }
    
    public LocalDate getBillingPeriodEnd() {
        return billingPeriodEnd;
    }
    
    public void setBillingPeriodEnd(LocalDate billingPeriodEnd) {
        this.billingPeriodEnd = billingPeriodEnd;
    }
    
    public BigDecimal getCurrentReading() {
        return currentReading;
    }
    
    public void setCurrentReading(BigDecimal currentReading) {
        this.currentReading = currentReading;
    }
    
    @Override
    public String toString() {
        return "BillGenerationRequest{" +
                "meterId=" + meterId +
                ", billingPeriodStart=" + billingPeriodStart +
                ", billingPeriodEnd=" + billingPeriodEnd +
                ", currentReading=" + currentReading +
                '}';
    }
}
