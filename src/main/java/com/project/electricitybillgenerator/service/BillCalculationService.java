package com.project.electricitybillgenerator.service;

import com.project.electricitybillgenerator.config.BillConfiguration;
import com.project.electricitybillgenerator.model.BillReading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
     * Creates a new BillReading object with calculated values instead of modifying the input.
     * 
     * @param inputReading the reading to process
     * @param currentDate the current date for reference
     * @return a new processed reading with calculated values
     * @throws IllegalArgumentException if reading or date is invalid
     */
    public BillReading processBillReading(BillReading inputReading, LocalDate currentDate) {
        if (inputReading == null) {
            throw new IllegalArgumentException("Reading cannot be null");
        }
        if (currentDate == null) {
            throw new IllegalArgumentException("Current date cannot be null");
        }
        if (inputReading.getCurrentMonthReading() == null) {
            throw new IllegalArgumentException("Current month reading cannot be null");
        }
        if (inputReading.getMeterId() == null) {
            throw new IllegalArgumentException("Meter ID cannot be null");
        }
        
        logger.info("Processing bill reading for meter ID: {}", inputReading.getMeterId());
        
        // Get previous month reading
        double previousReading = readingService.getPreviousMonthReading(inputReading.getMeterId(), currentDate);
        
        // Use the date from input or current date as fallback
        LocalDate readingDate = inputReading.getDate() != null ? inputReading.getDate() : currentDate;
        
        // Calculate units consumed using service method
        double unitsConsumed = calculateUnitsConsumed(inputReading.getCurrentMonthReading(), previousReading);
        
        // Calculate bill amount using service method
        double billAmount = calculateBillAmount(unitsConsumed, billConfiguration.getRatePerUnit());
        
        // Create a new BillReading object with all calculated values
        BillReading processedReading = new BillReading(
            inputReading.getMeterId(),
            inputReading.getCurrentMonthReading(),
            previousReading,
            readingDate,
            unitsConsumed,
            billAmount
        );
        
        logger.info("Processed reading - Units consumed: {}, Bill amount: {}", 
                   unitsConsumed, billAmount);
        
        return processedReading;
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
