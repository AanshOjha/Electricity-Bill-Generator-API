package com.project.electricitybillgenerator.service;

import com.project.electricitybillgenerator.config.BillConfiguration;
import com.project.electricitybillgenerator.model.BillReading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Comprehensive unit tests for BillCalculationService using Mockito.
 * This version requires Java 23 or lower, or enabling -Dnet.bytebuddy.experimental=true for Java 24+.
 * For Java 24+ compatibility without flags, use BillCalculationServiceTest.java instead.
 * 
 * Tests cover normal operations, edge cases, and error scenarios.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BillCalculationService Tests (Mockito Version)")
class BillCalculationServiceMockitoTest {

    @Mock
    private ReadingService readingService;

    @Mock
    private BillConfiguration billConfiguration;

    private BillCalculationService billCalculationService;

    private static final double DEFAULT_RATE_PER_UNIT = 7.5;
    private static final Integer TEST_METER_ID = 1234;
    private static final LocalDate TEST_DATE = LocalDate.of(2025, 9, 8);

    @BeforeEach
    void setUp() {
        billCalculationService = new BillCalculationService(readingService, billConfiguration);
        
        // Default mock behavior with lenient stubbing to avoid UnnecessaryStubbing errors
        lenient().when(billConfiguration.getRatePerUnit()).thenReturn(DEFAULT_RATE_PER_UNIT);
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
            
            when(readingService.getPreviousMonthReading(TEST_METER_ID, TEST_DATE))
                .thenReturn(previousReading);

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
            
            verify(readingService).getPreviousMonthReading(TEST_METER_ID, TEST_DATE);
            verify(billConfiguration).getRatePerUnit();
        }

        @Test
        @DisplayName("Should handle first-ever reading for user (previous reading is 0)")
        void shouldHandleFirstEverReadingForUser() {
            // Arrange
            double currentReading = 500.0;
            double previousReading = 0.0; // First-ever reading
            BillReading inputReading = createTestBillReading(currentReading);
            
            when(readingService.getPreviousMonthReading(TEST_METER_ID, TEST_DATE))
                .thenReturn(previousReading);

            // Act
            BillReading result = billCalculationService.processBillReading(inputReading, TEST_DATE);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getPreviousMonthReading()).isEqualTo(0.0);
            assertThat(result.getUnitConsumed()).isEqualTo(500.0); // 500 - 0
            assertThat(result.getBillAmount()).isEqualTo(3750.0); // 500 * 7.5
            
            verify(readingService).getPreviousMonthReading(TEST_METER_ID, TEST_DATE);
        }

        @Test
        @DisplayName("Should handle current reading less than previous reading")
        void shouldHandleCurrentReadingLessThanPrevious() {
            // Arrange
            double currentReading = 1400.0;
            double previousReading = 1500.0; // Previous is higher than current
            BillReading inputReading = createTestBillReading(currentReading);
            
            when(readingService.getPreviousMonthReading(TEST_METER_ID, TEST_DATE))
                .thenReturn(previousReading);

            // Act
            BillReading result = billCalculationService.processBillReading(inputReading, TEST_DATE);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getPreviousMonthReading()).isEqualTo(previousReading);
            assertThat(result.getUnitConsumed()).isEqualTo(0.0); // Math.max(0, 1400 - 1500)
            assertThat(result.getBillAmount()).isEqualTo(0.0); // 0 * 7.5
            
            verify(readingService).getPreviousMonthReading(TEST_METER_ID, TEST_DATE);
        }

        @Test
        @DisplayName("Should handle zero consumption")
        void shouldHandleZeroConsumption() {
            // Arrange
            double currentReading = 1500.0;
            double previousReading = 1500.0; // Same as current, zero consumption
            BillReading inputReading = createTestBillReading(currentReading);
            
            when(readingService.getPreviousMonthReading(TEST_METER_ID, TEST_DATE))
                .thenReturn(previousReading);

            // Act
            BillReading result = billCalculationService.processBillReading(inputReading, TEST_DATE);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUnitConsumed()).isEqualTo(0.0); // 1500 - 1500
            assertThat(result.getBillAmount()).isEqualTo(0.0); // 0 * 7.5
            
            verify(readingService).getPreviousMonthReading(TEST_METER_ID, TEST_DATE);
        }

        @Test
        @DisplayName("Should throw exception when reading is null")
        void shouldThrowExceptionWhenReadingIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> billCalculationService.processBillReading(null, TEST_DATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reading cannot be null");
            
            verifyNoInteractions(readingService, billConfiguration);
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
            
            verifyNoInteractions(readingService, billConfiguration);
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
            
            verifyNoInteractions(readingService, billConfiguration);
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
            verifyNoInteractions(billConfiguration);
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
            verify(billConfiguration).getRatePerUnit();
        }

        @Test
        @DisplayName("Should throw exception for negative units consumed")
        void shouldThrowExceptionForNegativeUnitsConsumed() {
            // Act & Assert
            assertThatThrownBy(() -> billCalculationService.calculateBillAmount(-10.0, 7.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Units consumed cannot be negative");
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
        @DisplayName("Should throw exception for negative readings")
        void shouldThrowExceptionForNegativeReadings() {
            // Act & Assert
            assertThatThrownBy(() -> billCalculationService.calculateUnitsConsumed(-100.0, 1400.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Readings cannot be negative");

            assertThatThrownBy(() -> billCalculationService.calculateUnitsConsumed(1500.0, -100.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Readings cannot be negative");
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
}
