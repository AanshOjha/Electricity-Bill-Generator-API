package com.project.electricitybillgenerator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for billing-related constants and properties.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "bill")
public class BillConfiguration {
    
    /**
     * Default rate per unit of electricity consumption (in currency units).
     */
    public static final double DEFAULT_RATE_PER_UNIT = 7.5;
    
    /**
     * Minimum meter ID value for random generation.
     */
    public static final int MIN_METER_ID = 1000;
    
    /**
     * Maximum meter ID value for random generation.
     */
    public static final int MAX_METER_ID = 9999;
    
    /**
     * Maximum reading value allowed.
     */
    public static final double MAX_READING_VALUE = 999999.99;
    
    private double ratePerUnit = DEFAULT_RATE_PER_UNIT;
    private int minMeterId = MIN_METER_ID;
    private int maxMeterId = MAX_METER_ID;
    private double maxReadingValue = MAX_READING_VALUE;
    
    // Getters and Setters
    public double getRatePerUnit() {
        return ratePerUnit;
    }
    
    public void setRatePerUnit(double ratePerUnit) {
        this.ratePerUnit = ratePerUnit;
    }
    
    public int getMinMeterId() {
        return minMeterId;
    }
    
    public void setMinMeterId(int minMeterId) {
        this.minMeterId = minMeterId;
    }
    
    public int getMaxMeterId() {
        return maxMeterId;
    }
    
    public void setMaxMeterId(int maxMeterId) {
        this.maxMeterId = maxMeterId;
    }
    
    public double getMaxReadingValue() {
        return maxReadingValue;
    }
    
    public void setMaxReadingValue(double maxReadingValue) {
        this.maxReadingValue = maxReadingValue;
    }
}
