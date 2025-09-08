package com.project.electricitybillgenerator.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity representing a bill reading for electricity consumption.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@Entity
@Table(name = "billreading", indexes = {
    @Index(name = "idx_meter_id", columnList = "meterId"),
    @Index(name = "idx_date", columnList = "date")
})
public class BillReading {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "meter_id", nullable = false)
    private Integer meterId;
    
    @Column(name = "current_month_reading", nullable = false)
    private Double currentMonthReading;
    
    @Column(name = "previous_month_reading", nullable = false)
    private Double previousMonthReading;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "unit_consumed", nullable = false)
    private Double unitConsumed;
    
    @Column(name = "bill_amount", nullable = false)
    private Double billAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_id", referencedColumnName = "meter_id", insertable = false, updatable = false)
    private BillUser billUser;

    // Default constructor
    public BillReading() {
        this.date = LocalDate.now();
        this.previousMonthReading = 0.0;
        this.unitConsumed = 0.0;
        this.billAmount = 0.0;
    }

    // Constructor with essential fields
    public BillReading(Integer meterId, Double currentMonthReading, LocalDate date) {
        this();
        this.meterId = meterId;
        this.currentMonthReading = currentMonthReading;
        this.date = date;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Double getPreviousMonthReading() {
        return previousMonthReading;
    }

    public void setPreviousMonthReading(Double previousMonthReading) {
        this.previousMonthReading = previousMonthReading;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getUnitConsumed() {
        return unitConsumed;
    }

    public void setUnitConsumed(Double unitConsumed) {
        this.unitConsumed = unitConsumed;
    }

    public Double getBillAmount() {
        return billAmount;
    }

    public void setBillAmount(Double billAmount) {
        this.billAmount = billAmount;
    }

    public BillUser getBillUser() {
        return billUser;
    }

    public void setBillUser(BillUser billUser) {
        this.billUser = billUser;
    }

    /**
     * Calculates the unit consumed based on current and previous readings.
     */
    public void calculateUnitConsumed() {
        if (currentMonthReading != null && previousMonthReading != null) {
            this.unitConsumed = Math.max(0, currentMonthReading - previousMonthReading);
        }
    }

    /**
     * Calculates the bill amount based on unit consumed and rate per unit.
     * 
     * @param ratePerUnit the rate per unit of electricity
     */
    public void calculateBillAmount(double ratePerUnit) {
        if (unitConsumed != null) {
            this.billAmount = unitConsumed * ratePerUnit;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillReading that = (BillReading) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(meterId, that.meterId) &&
               Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, meterId, date);
    }

    @Override
    public String toString() {
        return "BillReading{" +
                "id=" + id +
                ", meterId=" + meterId +
                ", currentMonthReading=" + currentMonthReading +
                ", previousMonthReading=" + previousMonthReading +
                ", date=" + date +
                ", unitConsumed=" + unitConsumed +
                ", billAmount=" + billAmount +
                '}';
    }
}