package com.project.electricitybillgenerator.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MeterReadingRequest {
    
    @NotNull(message = "Meter ID is required")
    private Long meterId;
    
    @NotNull(message = "Current reading is required")
    @DecimalMin(value = "0.0", message = "Reading cannot be negative")
    private BigDecimal currentReading;
    
    @NotNull(message = "Reading date is required")
    private LocalDate readingDate;
    
    // Constructors
    public MeterReadingRequest() {}
    
    public MeterReadingRequest(Long meterId, BigDecimal currentReading, LocalDate readingDate) {
        this.meterId = meterId;
        this.currentReading = currentReading;
        this.readingDate = readingDate;
    }
    
    // Getters and Setters
    public Long getMeterId() { return meterId; }
    public void setMeterId(Long meterId) { this.meterId = meterId; }
    
    public BigDecimal getCurrentReading() { return currentReading; }
    public void setCurrentReading(BigDecimal currentReading) { this.currentReading = currentReading; }
    
    public LocalDate getReadingDate() { return readingDate; }
    public void setReadingDate(LocalDate readingDate) { this.readingDate = readingDate; }
}
