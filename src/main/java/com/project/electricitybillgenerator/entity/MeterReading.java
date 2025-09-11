package com.project.electricitybillgenerator.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "meter_readings")
public class MeterReading {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;
    
    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "Reading cannot be negative")
    private BigDecimal currentReading;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal previousReading = BigDecimal.ZERO;
    
    @Column(nullable = false)
    private LocalDate readingDate;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal unitsConsumed;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReadingType readingType = ReadingType.MANUAL;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Constructors
    public MeterReading() {}
    
    public MeterReading(Meter meter, BigDecimal currentReading, LocalDate readingDate) {
        this.meter = meter;
        this.currentReading = currentReading;
        this.readingDate = readingDate;
        this.createdAt = LocalDateTime.now();
    }
    
    @PrePersist
    @PreUpdate
    private void calculateUnitsConsumed() {
        if (currentReading != null && previousReading != null) {
            this.unitsConsumed = currentReading.subtract(previousReading);
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Meter getMeter() { return meter; }
    public void setMeter(Meter meter) { this.meter = meter; }
    
    public BigDecimal getCurrentReading() { return currentReading; }
    public void setCurrentReading(BigDecimal currentReading) { this.currentReading = currentReading; }
    
    public BigDecimal getPreviousReading() { return previousReading; }
    public void setPreviousReading(BigDecimal previousReading) { this.previousReading = previousReading; }
    
    public LocalDate getReadingDate() { return readingDate; }
    public void setReadingDate(LocalDate readingDate) { this.readingDate = readingDate; }
    
    public BigDecimal getUnitsConsumed() { return unitsConsumed; }
    public void setUnitsConsumed(BigDecimal unitsConsumed) { this.unitsConsumed = unitsConsumed; }
    
    public ReadingType getReadingType() { return readingType; }
    public void setReadingType(ReadingType readingType) { this.readingType = readingType; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public enum ReadingType {
        MANUAL, AUTOMATIC, ESTIMATED
    }
}
