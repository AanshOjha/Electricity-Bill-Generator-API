package com.project.electricitybillgenerator.model;

import java.util.Date;

import jakarta.persistence.*;

@Entity
@Table(name = "billreading")
public class BillReading {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int meter_id;
    private double currentMonthReading;
    private double previousMonthReading;
    private String date;
    private double unitConsumed;
    private double billAmount;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @ManyToOne
    @JoinColumn(name = "meter_id", referencedColumnName = "meter_id", insertable = false, updatable = false)
    private BillUser billUser;

    // getters and setters

    /**
     * @return double return the currentMonthReading
     */
    public double getCurrentMonthReading() {
        return currentMonthReading;
    }

    /**
     * @param currentMonthReading the currentMonthReading to set
     */
    public void setCurrentMonthReading(double currentMonthReading) {
        this.currentMonthReading = currentMonthReading;
    }

    

    /**
     * @return double return the unitConsumed
     */
    public double getUnitConsumed() {
        return unitConsumed;
    }

    /**
     * @param unitConsumed the unitConsumed to set
     */
    public void setUnitConsumed(double unitConsumed) {
        this.unitConsumed = unitConsumed;
    }

    /**
     * @return double return the billAmount
     */
    public double getBillAmount() {
        return billAmount;
    }

    /**
     * @param billAmount the billAmount to set
     */
    public void setBillAmount(double billAmount) {
        this.billAmount = billAmount;
    }

    /**
     * @return BillUser return the billUser
     */
    public BillUser getBillUser() {
        return billUser;
    }

    /**
     * @param billUser the billUser to set
     */
    public void setBillUser(BillUser billUser) {
        this.billUser = billUser;
    }


    /**
     * @return int return the meter_id
     */
    public int getMeter_id() {
        return meter_id;
    }

    /**
     * @param meter_id the meter_id to set
     */
    public void setMeter_id(int meter_id) {
        this.meter_id = meter_id;
    }

    /**
     * @return double return the previousMonthReading
     */
    public double getPreviousMonthReading() {
        return previousMonthReading;
    }

    /**
     * @param previousMonthReading the previousMonthReading to set
     */
    public void setPreviousMonthReading(double previousMonthReading) {
        this.previousMonthReading = previousMonthReading;
    }



}