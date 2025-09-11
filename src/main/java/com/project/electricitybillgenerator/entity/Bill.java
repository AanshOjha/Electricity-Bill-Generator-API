package com.project.electricitybillgenerator.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
public class Bill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 20)
    private String billNumber;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_reading_id", nullable = false)
    private MeterReading meterReading;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariff tariff;
    
    @Column(nullable = false)
    private LocalDate billDate;
    
    @Column(nullable = false)
    private LocalDate dueDate;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitsConsumed;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillStatus status = BillStatus.PENDING;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.Set<Payment> payments;
    
    // Constructors
    public Bill() {}
    
    public Bill(String billNumber, Meter meter, MeterReading meterReading, Tariff tariff, LocalDate billDate, LocalDate dueDate) {
        this.billNumber = billNumber;
        this.meter = meter;
        this.meterReading = meterReading;
        this.tariff = tariff;
        this.billDate = billDate;
        this.dueDate = dueDate;
        this.unitsConsumed = meterReading.getUnitsConsumed();
        this.createdAt = LocalDateTime.now();
        calculateBillAmount();
    }
    
    @PrePersist
    @PreUpdate
    private void calculateBillAmount() {
        if (tariff != null && unitsConsumed != null) {
            this.amount = tariff.calculateBillAmount(unitsConsumed);
            this.totalAmount = amount.add(tax != null ? tax : BigDecimal.ZERO);
        }
    }
    
    // Business methods
    public BigDecimal getRemainingAmount() {
        if (payments == null || payments.isEmpty()) {
            return totalAmount;
        }
        
        BigDecimal paidAmount = payments.stream()
            .filter(payment -> payment.getStatus() == Payment.PaymentStatus.COMPLETED)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        return totalAmount.subtract(paidAmount);
    }
    
    public boolean isFullyPaid() {
        return getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getBillNumber() { return billNumber; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }
    
    public Meter getMeter() { return meter; }
    public void setMeter(Meter meter) { this.meter = meter; }
    
    public MeterReading getMeterReading() { return meterReading; }
    public void setMeterReading(MeterReading meterReading) { this.meterReading = meterReading; }
    
    public Tariff getTariff() { return tariff; }
    public void setTariff(Tariff tariff) { this.tariff = tariff; }
    
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
    
    public BillStatus getStatus() { return status; }
    public void setStatus(BillStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public java.util.Set<Payment> getPayments() { return payments; }
    public void setPayments(java.util.Set<Payment> payments) { this.payments = payments; }
    
    public enum BillStatus {
        PENDING, PAID, PARTIALLY_PAID, OVERDUE, CANCELLED
    }
}
