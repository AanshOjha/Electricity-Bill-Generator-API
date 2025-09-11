package com.project.electricitybillgenerator.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "meters")
public class Meter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 20)
    private String meterNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeterType meterType = MeterType.DIGITAL;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeterStatus status = MeterStatus.ACTIVE;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime installationDate = LocalDateTime.now();
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @OneToMany(mappedBy = "meter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<MeterReading> meterReadings;
    
    @OneToMany(mappedBy = "meter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Bill> bills;
    
    // Constructors
    public Meter() {}
    
    public Meter(String meterNumber, Customer customer) {
        this.meterNumber = meterNumber;
        this.customer = customer;
        this.installationDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getMeterNumber() { return meterNumber; }
    public void setMeterNumber(String meterNumber) { this.meterNumber = meterNumber; }
    
    public MeterType getMeterType() { return meterType; }
    public void setMeterType(MeterType meterType) { this.meterType = meterType; }
    
    public MeterStatus getStatus() { return status; }
    public void setStatus(MeterStatus status) { this.status = status; }
    
    public LocalDateTime getInstallationDate() { return installationDate; }
    public void setInstallationDate(LocalDateTime installationDate) { this.installationDate = installationDate; }
    
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    
    public Set<MeterReading> getMeterReadings() { return meterReadings; }
    public void setMeterReadings(Set<MeterReading> meterReadings) { this.meterReadings = meterReadings; }
    
    public Set<Bill> getBills() { return bills; }
    public void setBills(Set<Bill> bills) { this.bills = bills; }
    
    public enum MeterType {
        ANALOG, DIGITAL, SMART
    }
    
    public enum MeterStatus {
        ACTIVE, INACTIVE, MAINTENANCE, DISCONNECTED
    }
}
