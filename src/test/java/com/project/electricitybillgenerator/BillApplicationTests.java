package com.project.electricitybillgenerator;

import com.project.electricitybillgenerator.config.BillConfiguration;
import com.project.electricitybillgenerator.service.BillCalculationService;
import com.project.electricitybillgenerator.service.ReadingService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the Electricity Bill Generator application.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
class BillApplicationTests {

    /**
     * Test bill calculation functionality without Spring context.
     */
    @Test
    void testBillCalculationLogic() {
        BillConfiguration config = new BillConfiguration();
        ReadingService readingService = null; // Will be mocked in real tests
        BillCalculationService billCalculationService = new BillCalculationService(readingService, config);
        
        double unitsConsumed = 100.0;
        double ratePerUnit = 7.5;
        
        double billAmount = billCalculationService.calculateBillAmount(unitsConsumed, ratePerUnit);
        
        assertThat(billAmount).isEqualTo(750.0);
    }

    /**
     * Test units consumed calculation.
     */
    @Test
    void testUnitsConsumedCalculation() {
        BillConfiguration config = new BillConfiguration();
        ReadingService readingService = null; // Will be mocked in real tests
        BillCalculationService billCalculationService = new BillCalculationService(readingService, config);
        
        double currentReading = 1500.0;
        double previousReading = 1400.0;
        
        double unitsConsumed = billCalculationService.calculateUnitsConsumed(currentReading, previousReading);
        
        assertThat(unitsConsumed).isEqualTo(100.0);
    }

    /**
     * Test that configuration defaults are properly set.
     */
    @Test
    void testBillConfigurationDefaults() {
        BillConfiguration config = new BillConfiguration();
        
        assertThat(config.getRatePerUnit()).isEqualTo(7.5);
        assertThat(config.getMinMeterId()).isEqualTo(1000);
        assertThat(config.getMaxMeterId()).isEqualTo(9999);
        assertThat(config.getMaxReadingValue()).isEqualTo(999999.99);
    }

    /**
     * Test edge case where current reading equals previous reading.
     */
    @Test
    void testZeroUnitsConsumed() {
        BillConfiguration config = new BillConfiguration();
        ReadingService readingService = null;
        BillCalculationService billCalculationService = new BillCalculationService(readingService, config);
        
        double reading = 1000.0;
        double unitsConsumed = billCalculationService.calculateUnitsConsumed(reading, reading);
        
        assertThat(unitsConsumed).isEqualTo(0.0);
        
        double billAmount = billCalculationService.calculateBillAmount(unitsConsumed, 7.5);
        assertThat(billAmount).isEqualTo(0.0);
    }
}
