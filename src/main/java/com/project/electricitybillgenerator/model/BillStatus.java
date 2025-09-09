package com.project.electricitybillgenerator.model;

/**
 * Enumeration representing the status of an electricity bill.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
public enum BillStatus {
    /**
     * Bill has been generated and is due for payment
     */
    DUE("Due"),
    
    /**
     * Bill has been paid in full
     */
    PAID("Paid"),
    
    /**
     * Bill is past the due date and remains unpaid
     */
    OVERDUE("Overdue"),
    
    /**
     * Bill has been cancelled or voided
     */
    CANCELLED("Cancelled"),
    
    /**
     * Bill is in dispute
     */
    DISPUTED("Disputed");
    
    private final String displayName;
    
    BillStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    /**
     * Check if the status represents a paid state
     */
    public boolean isPaid() {
        return this == PAID;
    }
    
    /**
     * Check if the status represents an unpaid state
     */
    public boolean isUnpaid() {
        return this == DUE || this == OVERDUE;
    }
    
    /**
     * Check if the status represents an overdue state
     */
    public boolean isOverdue() {
        return this == OVERDUE;
    }
}
