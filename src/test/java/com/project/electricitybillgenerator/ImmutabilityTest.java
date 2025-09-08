package com.project.electricitybillgenerator;

import com.project.electricitybillgenerator.config.BillConfiguration;
import com.project.electricitybillgenerator.model.BillReading;
import com.project.electricitybillgenerator.service.BillCalculationService;
import com.project.electricitybillgenerator.service.ReadingService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to demonstrate that the processBillReading method doesn't have side effects
 * and properly maintains immutability of input objects.
 */
class ImmutabilityTest {

    @Test
    void testProcessBillReadingDoesNotModifyInput() {
        // Arrange
        BillConfiguration config = new BillConfiguration();
        
        // Create a mock ReadingService that returns a predictable value
        ReadingService mockReadingService = new ReadingService(null) {
            @Override
            public double getPreviousMonthReading(Integer meterId, LocalDate currentDate) {
                return 100.0; // Return a predictable previous reading
            }
        };
        
        BillCalculationService billCalculationService = new BillCalculationService(mockReadingService, config);
        
        // Create input reading with specific values
        Integer originalMeterId = 1234;
        Double originalCurrentReading = 1500.0;
        LocalDate originalDate = LocalDate.of(2025, 9, 8);
        
        BillReading inputReading = new BillReading(originalMeterId, originalCurrentReading, originalDate);
        
        // Store original values to verify they don't change
        Integer inputMeterId = inputReading.getMeterId();
        Double inputCurrentReading = inputReading.getCurrentMonthReading();
        LocalDate inputDate = inputReading.getDate();
        Double inputPreviousReading = inputReading.getPreviousMonthReading(); // Should be 0.0 from constructor
        Double inputUnitsConsumed = inputReading.getUnitConsumed(); // Should be 0.0 from constructor
        Double inputBillAmount = inputReading.getBillAmount(); // Should be 0.0 from constructor
        
        // Act
        LocalDate currentDate = LocalDate.now();
        BillReading processedReading = billCalculationService.processBillReading(inputReading, currentDate);
        
        // Assert - Input object should remain unchanged
        assertThat(inputReading.getMeterId()).isEqualTo(inputMeterId);
        assertThat(inputReading.getCurrentMonthReading()).isEqualTo(inputCurrentReading);
        assertThat(inputReading.getDate()).isEqualTo(inputDate);
        assertThat(inputReading.getPreviousMonthReading()).isEqualTo(inputPreviousReading);
        assertThat(inputReading.getUnitConsumed()).isEqualTo(inputUnitsConsumed);
        assertThat(inputReading.getBillAmount()).isEqualTo(inputBillAmount);
        
        // Assert - Processed reading should be a different object
        assertThat(processedReading).isNotSameAs(inputReading);
        
        // Assert - Processed reading should have calculated values
        assertThat(processedReading.getMeterId()).isEqualTo(originalMeterId);
        assertThat(processedReading.getCurrentMonthReading()).isEqualTo(originalCurrentReading);
        assertThat(processedReading.getDate()).isEqualTo(originalDate);
        assertThat(processedReading.getPreviousMonthReading()).isEqualTo(100.0); // From mock
        assertThat(processedReading.getUnitConsumed()).isEqualTo(1400.0); // 1500 - 100
        assertThat(processedReading.getBillAmount()).isEqualTo(10500.0); // 1400 * 7.5
    }
}
