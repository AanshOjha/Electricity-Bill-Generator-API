package com.project.electricitybillgenerator.dto;

import java.time.LocalDate;

/**
 * Data Transfer Object for Bill Reading requests.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
public class BillReadingRequest {
    
    private Integer meterId;
    private Double currentMonthReading;
    private LocalDate date;

    // Default constructor
    public BillReadingRequest() {
    }

    // Constructor with parameters
    public BillReadingRequest(Integer meterId, Double currentMonthReading, LocalDate date) {
        this.meterId = meterId;
        this.currentMonthReading = currentMonthReading;
        this.date = date;
    }

    // Getters and Setters
    public Integer getMeterId() {
        return meterId;
    }

    public void setMeterId(Integer meterId) {
        this.meterId = meterId;
    }

    public Double getCurrentMonthReading() {
        return currentMonthReading;
    }

    public void setCurrentMonthReading(Double currentMonthReading) {
        this.currentMonthReading = currentMonthReading;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "BillReadingRequest{" +
                "meterId=" + meterId +
                ", currentMonthReading=" + currentMonthReading +
                ", date=" + date +
                '}';
    }
}
