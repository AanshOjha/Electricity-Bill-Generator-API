package com.project.electricitybillgenerator.dto;

import com.project.electricitybillgenerator.model.BillStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for bill responses.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
public class BillResponse {
    
    private Long billId;
    private Integer meterId;
    private String userName;
    private String userAddress;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private BigDecimal previousReading;
    private BigDecimal currentReading;
    private BigDecimal unitsConsumed;
    private BigDecimal amount;
    private BigDecimal serviceCharge;
    private BigDecimal taxAmount;
    private BigDecimal lateFee;
    private BigDecimal totalAmount;
    private LocalDate dueDate;
    private BillStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private BigDecimal ratePerUnit;
    
    // Default constructor
    public BillResponse() {
    }
    
    // Getters and Setters
    
    public Long getBillId() {
        return billId;
    }
    
    public void setBillId(Long billId) {
        this.billId = billId;
    }
    
    public Integer getMeterId() {
        return meterId;
    }
    
    public void setMeterId(Integer meterId) {
        this.meterId = meterId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserAddress() {
        return userAddress;
    }
    
    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
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
    
    public BigDecimal getPreviousReading() {
        return previousReading;
    }
    
    public void setPreviousReading(BigDecimal previousReading) {
        this.previousReading = previousReading;
    }
    
    public BigDecimal getCurrentReading() {
        return currentReading;
    }
    
    public void setCurrentReading(BigDecimal currentReading) {
        this.currentReading = currentReading;
    }
    
    public BigDecimal getUnitsConsumed() {
        return unitsConsumed;
    }
    
    public void setUnitsConsumed(BigDecimal unitsConsumed) {
        this.unitsConsumed = unitsConsumed;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public BigDecimal getServiceCharge() {
        return serviceCharge;
    }
    
    public void setServiceCharge(BigDecimal serviceCharge) {
        this.serviceCharge = serviceCharge;
    }
    
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }
    
    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }
    
    public BigDecimal getLateFee() {
        return lateFee;
    }
    
    public void setLateFee(BigDecimal lateFee) {
        this.lateFee = lateFee;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public BillStatus getStatus() {
        return status;
    }
    
    public void setStatus(BillStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getPaidAt() {
        return paidAt;
    }
    
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
    
    public BigDecimal getRatePerUnit() {
        return ratePerUnit;
    }
    
    public void setRatePerUnit(BigDecimal ratePerUnit) {
        this.ratePerUnit = ratePerUnit;
    }
    
    @Override
    public String toString() {
        return "BillResponse{" +
                "billId=" + billId +
                ", meterId=" + meterId +
                ", userName='" + userName + '\'' +
                ", billingPeriodStart=" + billingPeriodStart +
                ", billingPeriodEnd=" + billingPeriodEnd +
                ", unitsConsumed=" + unitsConsumed +
                ", totalAmount=" + totalAmount +
                ", dueDate=" + dueDate +
                ", status=" + status +
                '}';
    }
}
