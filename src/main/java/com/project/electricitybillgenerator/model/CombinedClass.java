package com.project.electricitybillgenerator.model;

// For entering both BillUser and BillReading variables in one json object
public class CombinedClass {
    private int meterId;
    private double currentMonthReading;
    private String password;
    private String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getMeterId() {
        return meterId;
    }

    public void setMeterId(int meterId) {
        this.meterId = meterId;
    }

    public double getCurrentMonthReading() {
        return currentMonthReading;
    }

    public void setCurrentMonthReading(double currentMonthReading) {
        this.currentMonthReading = currentMonthReading;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
