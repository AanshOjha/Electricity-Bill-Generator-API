package com.project.electricitybillgenerator.service;

import com.project.electricitybillgenerator.config.BillConfiguration;
import com.project.electricitybillgenerator.model.BillReading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive unit tests for BillCalculationService.
 * Tests cover normal operations, edge cases, and error scenarios.
 * Note: Using manual mocks due to Java 24 compatibility issues with Mockito's Byte Buddy.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@DisplayName("BillCalculationService Tests")
class BillCalculationServiceTest {

    private ReadingService readingService;
    private BillConfiguration billConfiguration;
    private BillCalculationService billCalculationService;

    private static final double DEFAULT_RATE_PER_UNIT = 7.5;
    private static final Integer TEST_METER_ID = 1234;
    private static final LocalDate TEST_DATE = LocalDate.of(2025, 9, 8);

    @BeforeEach
    void setUp() {
        // Create manual mocks due to Java 24 compatibility issues
        readingService = new MockReadingService();
        billConfiguration = new MockBillConfiguration();
        billCalculationService = new BillCalculationService(readingService, billConfiguration);
    }

    @Nested
    @DisplayName("processBillReading Tests")
    class ProcessBillReadingTests {

        @Test
        @DisplayName("Should process normal bill reading successfully")
        void shouldProcessNormalBillReadingSuccessfully() {
            // Arrange
            double currentReading = 1500.0;
            double previousReading = 1400.0;
            BillReading inputReading = createTestBillReading(currentReading);
            
            ((MockReadingService) readingService).setPreviousReading(previousReading);

            // Act
            BillReading result = billCalculationService.processBillReading(inputReading, TEST_DATE);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getMeterId()).isEqualTo(TEST_METER_ID);
            assertThat(result.getCurrentMonthReading()).isEqualTo(currentReading);
            assertThat(result.getPreviousMonthReading()).isEqualTo(previousReading);
            assertThat(result.getUnitConsumed()).isEqualTo(100.0); // 1500 - 1400
            assertThat(result.getBillAmount()).isEqualTo(750.0); // 100 * 7.5
            assertThat(result.getDate()).isEqualTo(TEST_DATE);
        }

        @Test
        @DisplayName("Should handle first-ever reading for user (previous reading is 0)")
        void shouldHandleFirstEverReadingForUser() {
            // Arrange
            double currentReading = 500.0;
            double previousReading = 0.0; // First-ever reading
            BillReading inputReading = createTestBillReading(currentReading);
            
            ((MockReadingService) readingService).setPreviousReading(previousReading);

            // Act
            BillReading result = billCalculationService.processBillReading(inputReading, TEST_DATE);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getPreviousMonthReading()).isEqualTo(0.0);
            assertThat(result.getUnitConsumed()).isEqualTo(500.0); // 500 - 0
            assertThat(result.getBillAmount()).isEqualTo(3750.0); // 500 * 7.5
        }

        @Test
        @DisplayName("Should handle current reading less than previous reading")
        void shouldHandleCurrentReadingLessThanPrevious() {
            // Arrange
            double currentReading = 1400.0;
            double previousReading = 1500.0; // Previous is higher than current
            BillReading inputReading = createTestBillReading(currentReading);
            
            ((MockReadingService) readingService).setPreviousReading(previousReading);

            // Act
            BillReading result = billCalculationService.processBillReading(inputReading, TEST_DATE);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getPreviousMonthReading()).isEqualTo(previousReading);
            assertThat(result.getUnitConsumed()).isEqualTo(0.0); // Math.max(0, 1400 - 1500)
            assertThat(result.getBillAmount()).isEqualTo(0.0); // 0 * 7.5
        }

        @Test
        @DisplayName("Should handle zero consumption")
        void shouldHandleZeroConsumption() {
            // Arrange
            double currentReading = 1500.0;
            double previousReading = 1500.0; // Same as current, zero consumption
            BillReading inputReading = createTestBillReading(currentReading);
            
            ((MockReadingService) readingService).setPreviousReading(previousReading);

            // Act
            BillReading result = billCalculationService.processBillReading(inputReading, TEST_DATE);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUnitConsumed()).isEqualTo(0.0); // 1500 - 1500
            assertThat(result.getBillAmount()).isEqualTo(0.0); // 0 * 7.5
        }

        @Test
        @DisplayName("Should use input reading date when provided")
        void shouldUseInputReadingDateWhenProvided() {
            // Arrange
            LocalDate customDate = LocalDate.of(2025, 8, 15);
            BillReading inputReading = new BillReading();
            inputReading.setMeterId(TEST_METER_ID);
            inputReading.setCurrentMonthReading(1500.0);
            inputReading.setDate(customDate);
            
            ((MockReadingService) readingService).setPreviousReading(1400.0);

            // Act
            BillReading result = billCalculationService.processBillReading(inputReading, TEST_DATE);

            // Assert
            assertThat(result.getDate()).isEqualTo(customDate);
        }

        @Test
        @DisplayName("Should throw exception when reading is null")
        void shouldThrowExceptionWhenReadingIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> billCalculationService.processBillReading(null, TEST_DATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reading cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when current date is null")
        void shouldThrowExceptionWhenCurrentDateIsNull() {
            // Arrange
            BillReading inputReading = createTestBillReading(1500.0);

            // Act & Assert
            assertThatThrownBy(() -> billCalculationService.processBillReading(inputReading, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current date cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when current month reading is null")
        void shouldThrowExceptionWhenCurrentMonthReadingIsNull() {
            // Arrange
            BillReading inputReading = new BillReading();
            inputReading.setMeterId(TEST_METER_ID);
            inputReading.setCurrentMonthReading(null);

            // Act & Assert
            assertThatThrownBy(() -> billCalculationService.processBillReading(inputReading, TEST_DATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current month reading cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when meter ID is null")
        void shouldThrowExceptionWhenMeterIdIsNull() {
            // Arrange
            BillReading inputReading = new BillReading();
            inputReading.setMeterId(null);
            inputReading.setCurrentMonthReading(1500.0);

            // Act & Assert
            assertThatThrownBy(() -> billCalculationService.processBillReading(inputReading, TEST_DATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Meter ID cannot be null");
        }
    }

    @Nested
    @DisplayName("calculateBillAmount Tests")
    class CalculateBillAmountTests {

        @Test
        @DisplayName("Should calculate bill amount with provided rate")
        void shouldCalculateBillAmountWithProvidedRate() {
            // Arrange
            double unitsConsumed = 100.0;
            double customRate = 8.0;

            // Act
            double result = billCalculationService.calculateBillAmount(unitsConsumed, customRate);

            // Assert
            assertThat(result).isEqualTo(800.0);
        }

        @Test
        @DisplayName("Should calculate bill amount with default rate when rate is null")
        void shouldCalculateBillAmountWithDefaultRateWhenRateIsNull() {
            // Arrange
            double unitsConsumed = 100.0;

            // Act
            double result = billCalculationService.calculateBillAmount(unitsConsumed, null);

            // Assert
            assertThat(result).isEqualTo(750.0); // 100 * 7.5
        }

        @Test
        @DisplayName("Should calculate zero bill amount for zero consumption")
        void shouldCalculateZeroBillAmountForZeroConsumption() {
            // Arrange
            double unitsConsumed = 0.0;
            double rate = 7.5;

            // Act
            double result = billCalculationService.calculateBillAmount(unitsConsumed, rate);

            // Assert
            assertThat(result).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should throw exception for negative units consumed")
        void shouldThrowExceptionForNegativeUnitsConsumed() {
            // Act & Assert
            assertThatThrownBy(() -> billCalculationService.calculateBillAmount(-10.0, 7.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Units consumed cannot be negative");
        }

        @Test
        @DisplayName("Should handle very large consumption values")
        void shouldHandleVeryLargeConsumptionValues() {
            // Arrange
            double unitsConsumed = 999999.99;
            double rate = 7.5;

            // Act
            double result = billCalculationService.calculateBillAmount(unitsConsumed, rate);

            // Assert
            assertThat(result).isEqualTo(7499999.925);
        }
    }

    @Nested
    @DisplayName("calculateUnitsConsumed Tests")
    class CalculateUnitsConsumedTests {

        @Test
        @DisplayName("Should calculate units consumed normally")
        void shouldCalculateUnitsConsumedNormally() {
            // Arrange
            double currentReading = 1500.0;
            double previousReading = 1400.0;

            // Act
            double result = billCalculationService.calculateUnitsConsumed(currentReading, previousReading);

            // Assert
            assertThat(result).isEqualTo(100.0);
        }

        @Test
        @DisplayName("Should return zero when current reading equals previous reading")
        void shouldReturnZeroWhenCurrentEqualsPrevious() {
            // Arrange
            double currentReading = 1500.0;
            double previousReading = 1500.0;

            // Act
            double result = billCalculationService.calculateUnitsConsumed(currentReading, previousReading);

            // Assert
            assertThat(result).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should return zero when current reading is less than previous reading")
        void shouldReturnZeroWhenCurrentIsLessThanPrevious() {
            // Arrange
            double currentReading = 1400.0;
            double previousReading = 1500.0;

            // Act
            double result = billCalculationService.calculateUnitsConsumed(currentReading, previousReading);

            // Assert
            assertThat(result).isEqualTo(0.0); // Math.max(0, 1400 - 1500)
        }

        @Test
        @DisplayName("Should handle first-ever reading (previous reading is 0)")
        void shouldHandleFirstEverReading() {
            // Arrange
            double currentReading = 500.0;
            double previousReading = 0.0;

            // Act
            double result = billCalculationService.calculateUnitsConsumed(currentReading, previousReading);

            // Assert
            assertThat(result).isEqualTo(500.0);
        }

        @Test
        @DisplayName("Should throw exception for negative current reading")
        void shouldThrowExceptionForNegativeCurrentReading() {
            // Act & Assert
            assertThatThrownBy(() -> billCalculationService.calculateUnitsConsumed(-100.0, 1400.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Readings cannot be negative");
        }

        @Test
        @DisplayName("Should throw exception for negative previous reading")
        void shouldThrowExceptionForNegativePreviousReading() {
            // Act & Assert
            assertThatThrownBy(() -> billCalculationService.calculateUnitsConsumed(1500.0, -100.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Readings cannot be negative");
        }

        @Test
        @DisplayName("Should handle decimal readings")
        void shouldHandleDecimalReadings() {
            // Arrange
            double currentReading = 1500.75;
            double previousReading = 1400.25;

            // Act
            double result = billCalculationService.calculateUnitsConsumed(currentReading, previousReading);

            // Assert
            assertThat(result).isEqualTo(100.5);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete bill processing workflow for new user")
        void shouldHandleCompleteBillProcessingWorkflowForNewUser() {
            // Arrange
            double currentReading = 750.0;
            BillReading inputReading = createTestBillReading(currentReading);
            
            ((MockReadingService) readingService).setPreviousReading(0.0);

            // Act
            BillReading result = billCalculationService.processBillReading(inputReading, TEST_DATE);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getMeterId()).isEqualTo(TEST_METER_ID);
            assertThat(result.getCurrentMonthReading()).isEqualTo(currentReading);
            assertThat(result.getPreviousMonthReading()).isEqualTo(0.0);
            assertThat(result.getUnitConsumed()).isEqualTo(750.0);
            assertThat(result.getBillAmount()).isEqualTo(5625.0); // 750 * 7.5
            assertThat(result.getDate()).isEqualTo(TEST_DATE);
        }

        @Test
        @DisplayName("Should handle meter reset scenario (current reading much lower than previous)")
        void shouldHandleMeterResetScenario() {
            // Arrange - Meter was reset, so current reading is much lower
            double currentReading = 100.0;
            double previousReading = 99999.0;
            BillReading inputReading = createTestBillReading(currentReading);
            
            ((MockReadingService) readingService).setPreviousReading(previousReading);

            // Act
            BillReading result = billCalculationService.processBillReading(inputReading, TEST_DATE);

            // Assert
            assertThat(result.getUnitConsumed()).isEqualTo(0.0); // Should not be negative
            assertThat(result.getBillAmount()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should handle very high consumption")
        void shouldHandleVeryHighConsumption() {
            // Arrange
            double currentReading = 999999.0;
            double previousReading = 900000.0;
            BillReading inputReading = createTestBillReading(currentReading);
            
            ((MockReadingService) readingService).setPreviousReading(previousReading);

            // Act
            BillReading result = billCalculationService.processBillReading(inputReading, TEST_DATE);

            // Assert
            assertThat(result.getUnitConsumed()).isEqualTo(99999.0);
            assertThat(result.getBillAmount()).isEqualTo(749992.5); // 99999 * 7.5
        }
    }

    /**
     * Helper method to create test BillReading objects.
     */
    private BillReading createTestBillReading(double currentReading) {
        BillReading reading = new BillReading();
        reading.setMeterId(TEST_METER_ID);
        reading.setCurrentMonthReading(currentReading);
        reading.setDate(TEST_DATE);
        return reading;
    }

    /**
     * Manual mock for ReadingService to work around Java 24 compatibility issues.
     */
    private static class MockReadingService extends ReadingService {
        private double previousReading = 0.0;

        public MockReadingService() {
            super(null); // Repository not needed for our tests
        }

        public void setPreviousReading(double reading) {
            this.previousReading = reading;
        }

        @Override
        public double getPreviousMonthReading(Integer meterId, LocalDate currentDate) {
            return previousReading;
        }
    }

    /**
     * Manual mock for BillConfiguration to work around Java 24 compatibility issues.
     */
    private static class MockBillConfiguration extends BillConfiguration {
        
        @Override
        public double getRatePerUnit() {
            return DEFAULT_RATE_PER_UNIT;
        }
    }
}
