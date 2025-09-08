package com.project.electricitybillgenerator.service;

import com.project.electricitybillgenerator.config.BillConfiguration;
import com.project.electricitybillgenerator.model.BillReading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;

/**
 * Service class for handling bill calculation operations.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@Service
public class BillCalculationService {
    
    private static final Logger logger = LoggerFactory.getLogger(BillCalculationService.class);
    
    private final ReadingService readingService;
    private final BillConfiguration billConfiguration;

    /**
     * Constructor for BillCalculationService.
     * 
     * @param readingService the service for reading operations
     * @param billConfiguration the configuration for billing constants
     */
    public BillCalculationService(ReadingService readingService, BillConfiguration billConfiguration) {
        this.readingService = readingService;
        this.billConfiguration = billConfiguration;
    }

    /**
     * Processes a bill reading by calculating previous reading, units consumed, and bill amount.
     * 
     * @param reading the reading to process
     * @param currentDate the current date for reference
     * @return the processed reading with calculated values
     * @throws IllegalArgumentException if reading or date is invalid
     */
    public BillReading processBillReading(BillReading reading, Date currentDate) {
        if (reading == null) {
            throw new IllegalArgumentException("Reading cannot be null");
        }
        if (currentDate == null) {
            throw new IllegalArgumentException("Current date cannot be null");
        }
        
        logger.info("Processing bill reading for meter ID: {}", reading.getMeterId());
        
        // Get previous month reading
        double previousReading = readingService.getPreviousMonthReading(reading.getMeterId(), currentDate);
        reading.setPreviousMonthReading(previousReading);
        
        // Set current date if not already set
        if (reading.getDate() == null) {
            reading.setDate(LocalDate.now());
        }
        
        // Calculate units consumed
        reading.calculateUnitConsumed();
        
        // Calculate bill amount
        reading.calculateBillAmount(billConfiguration.getRatePerUnit());
        
        logger.info("Processed reading - Units consumed: {}, Bill amount: {}", 
                   reading.getUnitConsumed(), reading.getBillAmount());
        
        return reading;
    }

    /**
     * Calculates the bill amount based on units consumed and rate per unit.
     * 
     * @param unitsConsumed the number of units consumed
     * @param ratePerUnit the rate per unit (optional, uses default if null)
     * @return the calculated bill amount
     */
    public double calculateBillAmount(double unitsConsumed, Double ratePerUnit) {
        if (unitsConsumed < 0) {
            throw new IllegalArgumentException("Units consumed cannot be negative");
        }
        
        double rate = (ratePerUnit != null) ? ratePerUnit : billConfiguration.getRatePerUnit();
        return unitsConsumed * rate;
    }

    /**
     * Calculates units consumed based on current and previous readings.
     * 
     * @param currentReading the current month reading
     * @param previousReading the previous month reading
     * @return the units consumed (non-negative)
     */
    public double calculateUnitsConsumed(double currentReading, double previousReading) {
        if (currentReading < 0 || previousReading < 0) {
            throw new IllegalArgumentException("Readings cannot be negative");
        }
        
        return Math.max(0, currentReading - previousReading);
    }
}
