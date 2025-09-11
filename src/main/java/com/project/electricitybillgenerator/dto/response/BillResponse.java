package com.project.electricitybillgenerator.dto.response;

import com.project.electricitybillgenerator.entity.Bill;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BillResponse {
    
    private Long id;
    private String billNumber;
    private String meterNumber;
    private String customerName;
    private LocalDate billDate;
    private LocalDate dueDate;
    private BigDecimal unitsConsumed;
    private BigDecimal amount;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private BigDecimal remainingAmount;
    private Bill.BillStatus status;
    private LocalDateTime createdAt;
    
    // Constructors
    public BillResponse() {}
    
    public BillResponse(Bill bill) {
        this.id = bill.getId();
        this.billNumber = bill.getBillNumber();
        this.meterNumber = bill.getMeter().getMeterNumber();
        this.customerName = bill.getMeter().getCustomer().getName();
        this.billDate = bill.getBillDate();
        this.dueDate = bill.getDueDate();
        this.unitsConsumed = bill.getUnitsConsumed();
        this.amount = bill.getAmount();
        this.tax = bill.getTax();
        this.totalAmount = bill.getTotalAmount();
        this.remainingAmount = bill.getRemainingAmount();
        this.status = bill.getStatus();
        this.createdAt = bill.getCreatedAt();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getBillNumber() { return billNumber; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }
    
    public String getMeterNumber() { return meterNumber; }
    public void setMeterNumber(String meterNumber) { this.meterNumber = meterNumber; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public LocalDate getBillDate() { return billDate; }
    public void setBillDate(LocalDate billDate) { this.billDate = billDate; }
    
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    
    public BigDecimal getUnitsConsumed() { return unitsConsumed; }
    public void setUnitsConsumed(BigDecimal unitsConsumed) { this.unitsConsumed = unitsConsumed; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }
    
    public Bill.BillStatus getStatus() { return status; }
    public void setStatus(Bill.BillStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
