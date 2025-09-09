package com.project.electricitybillgenerator.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entity representing an electricity bill with comprehensive billing information.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@Entity
@Table(name = "bill", indexes = {
    @Index(name = "idx_bill_user_id", columnList = "user_id"),
    @Index(name = "idx_bill_status", columnList = "status"),
    @Index(name = "idx_bill_due_date", columnList = "dueDate"),
    @Index(name = "idx_bill_period", columnList = "billingPeriodStart, billingPeriodEnd")
})
public class Bill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_id")
    private Long billId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "meter_id", nullable = false)
    private BillUser user;
    
    @Column(name = "billing_period_start", nullable = false)
    private LocalDate billingPeriodStart;
    
    @Column(name = "billing_period_end", nullable = false)
    private LocalDate billingPeriodEnd;
    
    @Column(name = "previous_reading", nullable = false, precision = 10, scale = 2)
    private BigDecimal previousReading;
    
    @Column(name = "current_reading", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentReading;
    
    @Column(name = "units_consumed", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitsConsumed;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BillStatus status;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "rate_per_unit", nullable = false, precision = 6, scale = 2)
    private BigDecimal ratePerUnit;
    
    @Column(name = "service_charge", precision = 8, scale = 2)
    private BigDecimal serviceCharge;
    
    @Column(name = "tax_amount", precision = 8, scale = 2)
    private BigDecimal taxAmount;
    
    @Column(name = "late_fee", precision = 8, scale = 2)
    private BigDecimal lateFee;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "meter_id", nullable = false)
    private Integer meterId;
    
    // Default constructor
    public Bill() {
        this.createdAt = LocalDateTime.now();
        this.status = BillStatus.DUE;
        this.serviceCharge = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.lateFee = BigDecimal.ZERO;
    }
    
    // Constructor with essential fields
    public Bill(BillUser user, LocalDate billingPeriodStart, LocalDate billingPeriodEnd,
                BigDecimal previousReading, BigDecimal currentReading, BigDecimal ratePerUnit) {
        this();
        this.user = user;
        this.meterId = user.getMeterId();
        this.billingPeriodStart = billingPeriodStart;
        this.billingPeriodEnd = billingPeriodEnd;
        this.previousReading = previousReading;
        this.currentReading = currentReading;
        this.ratePerUnit = ratePerUnit;
        
        // Calculate units consumed
        this.unitsConsumed = currentReading.subtract(previousReading);
        
        // Calculate basic amount
        this.amount = unitsConsumed.multiply(ratePerUnit);
        
        // Set due date (30 days from billing period end)
        this.dueDate = billingPeriodEnd.plusDays(30);
        
        // Calculate total amount (initially same as amount, can be updated with charges)
        this.totalAmount = this.amount;
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    
    /**
     * Calculate total amount including all charges and fees
     */
    public void calculateTotalAmount() {
        this.totalAmount = this.amount
            .add(this.serviceCharge != null ? this.serviceCharge : BigDecimal.ZERO)
            .add(this.taxAmount != null ? this.taxAmount : BigDecimal.ZERO)
            .add(this.lateFee != null ? this.lateFee : BigDecimal.ZERO);
    }
    
    /**
     * Mark bill as paid
     */
    public void markAsPaid() {
        this.status = BillStatus.PAID;
        this.paidAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark bill as overdue
     */
    public void markAsOverdue() {
        this.status = BillStatus.OVERDUE;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if bill is overdue based on current date
     */
    public boolean isOverdue() {
        return LocalDate.now().isAfter(this.dueDate) && this.status != BillStatus.PAID;
    }
    
    /**
     * Apply late fee for overdue bills
     */
    public void applyLateFee(BigDecimal lateFeeAmount) {
        if (isOverdue() && this.status != BillStatus.PAID) {
            this.lateFee = lateFeeAmount;
            calculateTotalAmount();
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    
    public Long getBillId() {
        return billId;
    }
    
    public void setBillId(Long billId) {
        this.billId = billId;
    }
    
    public BillUser getUser() {
        return user;
    }
    
    public void setUser(BillUser user) {
        this.user = user;
        if (user != null) {
            this.meterId = user.getMeterId();
        }
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
        if (this.previousReading != null) {
            this.unitsConsumed = currentReading.subtract(this.previousReading);
        }
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
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
    
    public Integer getMeterId() {
        return meterId;
    }
    
    public void setMeterId(Integer meterId) {
        this.meterId = meterId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bill bill = (Bill) o;
        return Objects.equals(billId, bill.billId) &&
               Objects.equals(meterId, bill.meterId) &&
               Objects.equals(billingPeriodStart, bill.billingPeriodStart) &&
               Objects.equals(billingPeriodEnd, bill.billingPeriodEnd);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(billId, meterId, billingPeriodStart, billingPeriodEnd);
    }
    
    @Override
    public String toString() {
        return "Bill{" +
                "billId=" + billId +
                ", meterId=" + meterId +
                ", billingPeriodStart=" + billingPeriodStart +
                ", billingPeriodEnd=" + billingPeriodEnd +
                ", unitsConsumed=" + unitsConsumed +
                ", amount=" + amount +
                ", totalAmount=" + totalAmount +
                ", dueDate=" + dueDate +
                ", status=" + status +
                '}';
    }
}
