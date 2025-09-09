package com.project.electricitybillgenerator.service;

import com.project.electricitybillgenerator.model.Bill;
import com.project.electricitybillgenerator.model.BillReading;
import com.project.electricitybillgenerator.model.BillStatus;
import com.project.electricitybillgenerator.model.BillUser;
import com.project.electricitybillgenerator.repository.BillRepository;
import com.project.electricitybillgenerator.repository.ReadingRepository;
import com.project.electricitybillgenerator.repository.UserRepository;
import com.project.electricitybillgenerator.exception.BillGenerationException;
import com.project.electricitybillgenerator.exception.ResourceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Service class for handling bill generation and management operations.
 * Provides comprehensive bill lifecycle management including generation,
 * billing calculations, and overdue handling.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@Service
@Transactional
public class BillService {
    
    private static final Logger logger = LoggerFactory.getLogger(BillService.class);
    
    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final ReadingRepository readingRepository;
    
    // Configuration constants - could be moved to configuration class
    private static final BigDecimal DEFAULT_RATE_PER_UNIT = new BigDecimal("7.50");
    private static final BigDecimal SERVICE_CHARGE = new BigDecimal("50.00");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.18"); // 18% GST
    private static final BigDecimal LATE_FEE_PERCENTAGE = new BigDecimal("0.02"); // 2% late fee
    
    @Autowired
    public BillService(BillRepository billRepository, 
                      UserRepository userRepository, 
                      ReadingRepository readingRepository) {
        this.billRepository = billRepository;
        this.userRepository = userRepository;
        this.readingRepository = readingRepository;
    }
    
    /**
     * Generate a bill for a specific meter and billing period
     */
    public Bill generateBill(Integer meterId, LocalDate billingPeriodStart, 
                           LocalDate billingPeriodEnd, BigDecimal currentReading) {
        
        logger.info("Generating bill for meter: {}, period: {} to {}", 
                   meterId, billingPeriodStart, billingPeriodEnd);
        
        // Validate input parameters
        validateBillGenerationInput(meterId, billingPeriodStart, billingPeriodEnd, currentReading);
        
        // Check if bill already exists for this period
        if (billRepository.existsByMeterIdAndBillingPeriodStartAndBillingPeriodEnd(
                meterId, billingPeriodStart, billingPeriodEnd)) {
            throw new BillGenerationException(
                "Bill already exists for meter " + meterId + " for period " + 
                billingPeriodStart + " to " + billingPeriodEnd);
        }
        
        // Get user information
        BillUser user = userRepository.findById(meterId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with meter ID: " + meterId));
        
        // Get previous reading
        BigDecimal previousReading = getPreviousReading(meterId, billingPeriodStart);
        
        // Create and configure the bill
        Bill bill = new Bill(user, billingPeriodStart, billingPeriodEnd, 
                           previousReading, currentReading, DEFAULT_RATE_PER_UNIT);
        
        // Apply additional charges
        applyServiceCharges(bill);
        applyTaxes(bill);
        
        // Calculate final total
        bill.calculateTotalAmount();
        
        // Save the bill
        Bill savedBill = billRepository.save(bill);
        
        logger.info("Bill generated successfully with ID: {} for meter: {}", 
                   savedBill.getBillId(), meterId);
        
        return savedBill;
    }
    
    /**
     * Generate monthly bill based on current date
     */
    public Bill generateMonthlyBill(Integer meterId, BigDecimal currentReading) {
        YearMonth currentMonth = YearMonth.now().minusMonths(1); // Previous month
        LocalDate billingPeriodStart = currentMonth.atDay(1);
        LocalDate billingPeriodEnd = currentMonth.atEndOfMonth();
        
        return generateBill(meterId, billingPeriodStart, billingPeriodEnd, currentReading);
    }
    
    /**
     * Generate bill for current month automatically using the latest reading from the database.
     * This method finds the latest reading for the meter and generates a bill for the current month.
     */
    public Bill generateCurrentMonthBill(Integer meterId) {
        logger.info("Generating current month bill for meter: {}", meterId);
        
        // Validate that user exists
        userRepository.findById(meterId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with meter ID: " + meterId));
        
        // Get the latest reading from BillReading table
        BigDecimal currentReading = getLatestReadingForMeter(meterId);
        
        // Set billing period for current month
        YearMonth currentMonth = YearMonth.now().minusMonths(1); // Previous month for billing
        LocalDate billingPeriodStart = currentMonth.atDay(1);
        LocalDate billingPeriodEnd = currentMonth.atEndOfMonth();
        
        // Check if bill already exists for this period
        if (billRepository.existsByMeterIdAndBillingPeriodStartAndBillingPeriodEnd(
                meterId, billingPeriodStart, billingPeriodEnd)) {
            throw new BillGenerationException(
                "Bill already exists for meter " + meterId + " for period " + 
                billingPeriodStart + " to " + billingPeriodEnd);
        }
        
        // Generate the bill
        return generateBill(meterId, billingPeriodStart, billingPeriodEnd, currentReading);
    }
    
    /**
     * Get all bills for a specific meter
     */
    @Transactional(readOnly = true)
    public List<Bill> getBillsByMeterId(Integer meterId) {
        return billRepository.findByMeterIdOrderByBillingPeriodEndDesc(meterId);
    }
    
    /**
     * Get bill by ID
     */
    @Transactional(readOnly = true)
    public Bill getBillById(Long billId) {
        return billRepository.findById(billId)
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + billId));
    }
    
    /**
     * Get latest bill for a meter
     */
    @Transactional(readOnly = true)
    public Optional<Bill> getLatestBillByMeterId(Integer meterId) {
        return billRepository.findTopByMeterIdOrderByBillingPeriodEndDesc(meterId);
    }
    
    /**
     * Mark bill as paid
     */
    public Bill markBillAsPaid(Long billId) {
        Bill bill = getBillById(billId);
        bill.markAsPaid();
        
        Bill savedBill = billRepository.save(bill);
        logger.info("Bill {} marked as paid for meter: {}", billId, bill.getMeterId());
        
        return savedBill;
    }
    
    /**
     * Process overdue bills and apply late fees
     */
    public List<Bill> processOverdueBills() {
        List<Bill> overdueBills = billRepository.findOverdueBills(LocalDate.now());
        
        for (Bill bill : overdueBills) {
            if (!bill.getStatus().isOverdue()) {
                bill.markAsOverdue();
                
                // Calculate and apply late fee
                BigDecimal lateFee = bill.getAmount()
                    .multiply(LATE_FEE_PERCENTAGE)
                    .setScale(2, RoundingMode.HALF_UP);
                
                bill.applyLateFee(lateFee);
                
                logger.info("Applied late fee of {} to bill {} for meter: {}", 
                           lateFee, bill.getBillId(), bill.getMeterId());
            }
        }
        
        return billRepository.saveAll(overdueBills);
    }
    
    /**
     * Get total unpaid amount for a meter
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalUnpaidAmount(Integer meterId) {
        Double amount = billRepository.getTotalUnpaidAmountByMeterId(meterId);
        return amount != null ? BigDecimal.valueOf(amount) : BigDecimal.ZERO;
    }
    
    /**
     * Get count of unpaid bills for a meter
     */
    @Transactional(readOnly = true)
    public Long getUnpaidBillCount(Integer meterId) {
        return billRepository.getUnpaidBillCountByMeterId(meterId);
    }
    
    /**
     * Get bills by status
     */
    @Transactional(readOnly = true)
    public List<Bill> getBillsByStatus(BillStatus status) {
        return billRepository.findByStatusOrderByDueDateAsc(status);
    }
    
    /**
     * Get bills within date range
     */
    @Transactional(readOnly = true)
    public List<Bill> getBillsByDateRange(LocalDate startDate, LocalDate endDate) {
        return billRepository.findBillsByDateRange(startDate, endDate);
    }
    
    /**
     * Get bills for a meter within date range
     */
    @Transactional(readOnly = true)
    public List<Bill> getBillsByMeterAndDateRange(Integer meterId, 
                                                 LocalDate startDate, LocalDate endDate) {
        return billRepository.findBillsByMeterIdAndDateRange(meterId, startDate, endDate);
    }
    
    // Private helper methods
    
    private void validateBillGenerationInput(Integer meterId, LocalDate billingPeriodStart, 
                                           LocalDate billingPeriodEnd, BigDecimal currentReading) {
        if (meterId == null || meterId <= 0) {
            throw new IllegalArgumentException("Invalid meter ID");
        }
        
        if (billingPeriodStart == null || billingPeriodEnd == null) {
            throw new IllegalArgumentException("Billing period dates cannot be null");
        }
        
        if (billingPeriodStart.isAfter(billingPeriodEnd)) {
            throw new IllegalArgumentException("Billing period start date must be before end date");
        }
        
        if (currentReading == null || currentReading.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Current reading must be a positive value");
        }
    }
    
    private BigDecimal getPreviousReading(Integer meterId, LocalDate billingPeriodStart) {
        // Try to get the last bill's current reading
        Optional<Bill> previousBill = billRepository.findTopByMeterIdOrderByBillingPeriodEndDesc(meterId);
        
        if (previousBill.isPresent()) {
            return previousBill.get().getCurrentReading();
        }
        
        // If no previous bill, try to get from reading repository (legacy support)
        // This maintains compatibility with existing BillReading data
        List<Double> previousReadings = readingRepository.previousReading(meterId, 
            java.sql.Date.valueOf(billingPeriodStart));
        
        if (!previousReadings.isEmpty()) {
            return BigDecimal.valueOf(previousReadings.get(0));
        }
        
        // Default to zero for new customers
        logger.warn("No previous reading found for meter: {}, defaulting to 0", meterId);
        return BigDecimal.ZERO;
    }
    
    /**
     * Get the latest reading for a meter from the BillReading table
     */
    private BigDecimal getLatestReadingForMeter(Integer meterId) {
        // First try to get from the latest bill
        Optional<Bill> latestBill = billRepository.findTopByMeterIdOrderByBillingPeriodEndDesc(meterId);
        if (latestBill.isPresent()) {
            return latestBill.get().getCurrentReading();
        }
        
        // If no bills exist, try to get from reading repository
        // Query the reading repository for the most recent reading
        // This is a simplified approach - in a real system you might have a separate method
        // to get the latest reading from the readings table ordered by date
        try {
            // This is a workaround using the existing method - in practice you'd want a proper query
            List<Double> readings = readingRepository.previousReading(meterId, java.sql.Date.valueOf(LocalDate.now()));
            if (!readings.isEmpty()) {
                return BigDecimal.valueOf(readings.get(0));
            }
        } catch (Exception e) {
            logger.warn("Error retrieving latest reading from reading repository for meter: {}", meterId, e);
        }
        
        throw new BillGenerationException(
            "No readings found for meter " + meterId + ". Please ensure readings are recorded before generating a bill.");
    }
    
    private void applyServiceCharges(Bill bill) {
        bill.setServiceCharge(SERVICE_CHARGE);
    }
    
    private void applyTaxes(Bill bill) {
        BigDecimal taxableAmount = bill.getAmount().add(bill.getServiceCharge());
        BigDecimal taxAmount = taxableAmount.multiply(TAX_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        bill.setTaxAmount(taxAmount);
    }
}
