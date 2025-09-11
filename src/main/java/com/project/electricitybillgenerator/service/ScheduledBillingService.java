package com.project.electricitybillgenerator.service;

import com.project.electricitybillgenerator.entity.*;
import com.project.electricitybillgenerator.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class ScheduledBillingService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledBillingService.class);
    private final Random random = new Random();

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MeterRepository meterRepository;

    @Autowired
    private MeterReadingRepository meterReadingRepository;

    @Autowired
    private TariffRepository tariffRepository;

    @Autowired
    private BillRepository billRepository;

    /**
     * Generate bills for the previous month for all active customers.
     * This method is designed to be called on the 1st of each month.
     */
    public void generateBillsForPreviousMonth() {
        logger.info("Starting automated bill generation for previous month...");
        
        LocalDate today = LocalDate.now();
        YearMonth previousMonth = YearMonth.from(today).minusMonths(1);
        LocalDate billingPeriodStart = previousMonth.atDay(1);
        LocalDate billingPeriodEnd = previousMonth.atEndOfMonth();
        
        logger.info("Generating bills for period: {} to {}", billingPeriodStart, billingPeriodEnd);

        // Get all active customers with meters
        List<Customer> activeCustomers = customerRepository.findAll().stream()
                .filter(customer -> customer.getActive() != null && customer.getActive())
                .toList();

        int billsGenerated = 0;
        int errors = 0;

        for (Customer customer : activeCustomers) {
            try {
                List<Meter> customerMeters = meterRepository.findByCustomerIdAndStatus(
                        customer.getId(), Meter.MeterStatus.ACTIVE);

                for (Meter meter : customerMeters) {
                    if (generateBillForMeter(meter, billingPeriodStart, billingPeriodEnd, today)) {
                        billsGenerated++;
                    }
                }
            } catch (Exception e) {
                logger.error("Error generating bill for customer {}: {}", customer.getId(), e.getMessage(), e);
                errors++;
            }
        }

        logger.info("Bill generation completed. Generated: {}, Errors: {}", billsGenerated, errors);
    }

    /**
     * Generate a bill for a specific meter and billing period
     */
    private boolean generateBillForMeter(Meter meter, LocalDate periodStart, LocalDate periodEnd, LocalDate billDate) {
        logger.debug("Processing meter {} for customer {}", meter.getId(), meter.getCustomer().getId());

        // Check if bill already exists for this period
        Optional<Bill> existingBill = billRepository.findByMeterIdAndBillDateBetween(
                meter.getId(), periodStart, periodEnd);
        
        if (existingBill.isPresent()) {
            logger.debug("Bill already exists for meter {} in period {} to {}", 
                    meter.getId(), periodStart, periodEnd);
            return false;
        }

        // Find current reading (latest reading in the billing period)
        Optional<MeterReading> currentReadingOpt = findLatestReadingInPeriod(meter.getId(), periodStart, periodEnd);
        
        if (currentReadingOpt.isEmpty()) {
            logger.warn("No meter reading found for meter {} in period {} to {}", 
                    meter.getId(), periodStart, periodEnd);
            return false;
        }

        MeterReading currentReading = currentReadingOpt.get();

        // Find previous reading (latest reading before the billing period)
        Optional<MeterReading> previousReadingOpt = findLatestReadingBeforeDate(meter.getId(), periodStart);
        
        BigDecimal previousReadingValue = previousReadingOpt
                .map(MeterReading::getCurrentReading)
                .orElse(BigDecimal.ZERO);

        // Calculate consumption
        BigDecimal unitsConsumed = currentReading.getCurrentReading().subtract(previousReadingValue);
        
        if (unitsConsumed.compareTo(BigDecimal.ZERO) < 0) {
            logger.warn("Negative consumption detected for meter {}. Current: {}, Previous: {}", 
                    meter.getId(), currentReading.getCurrentReading(), previousReadingValue);
            unitsConsumed = BigDecimal.ZERO; // Set to zero for safety
        }

        // Get applicable tariff
        Optional<Tariff> tariffOpt = tariffRepository.findActiveByCustomerTypeAndDate(
                meter.getCustomer().getCustomerType(), billDate);
        
        if (tariffOpt.isEmpty()) {
            logger.error("No active tariff found for customer type {} on date {}", 
                    meter.getCustomer().getCustomerType(), billDate);
            return false;
        }

        Tariff tariff = tariffOpt.get();

        // Create and save the bill
        Bill bill = createBill(meter, currentReading, tariff, unitsConsumed, billDate, periodStart, periodEnd);
        billRepository.save(bill);

        logger.info("Generated bill {} for meter {} with amount {}", 
                bill.getBillNumber(), meter.getId(), bill.getTotalAmount());
        
        return true;
    }

    /**
     * Create a new bill with calculated amounts
     */
    private Bill createBill(Meter meter, MeterReading meterReading, Tariff tariff, 
                           BigDecimal unitsConsumed, LocalDate billDate, 
                           LocalDate periodStart, LocalDate periodEnd) {
        
        Bill bill = new Bill();
        bill.setBillNumber(generateBillNumber());
        bill.setMeter(meter);
        bill.setMeterReading(meterReading);
        bill.setTariff(tariff);
        bill.setBillDate(billDate);
        bill.setDueDate(billDate.plusDays(30)); // 30 days to pay
        bill.setUnitsConsumed(unitsConsumed);
        bill.setStatus(Bill.BillStatus.PENDING);

        // Calculate bill amount using tariff
        BigDecimal amount = tariff.calculateBillAmount(unitsConsumed);
        bill.setAmount(amount);
        
        // For now, no tax calculation - can be extended later
        bill.setTax(BigDecimal.ZERO);
        bill.setTotalAmount(amount);

        return bill;
    }

    /**
     * Find the latest meter reading within a specific period
     */
    private Optional<MeterReading> findLatestReadingInPeriod(Long meterId, LocalDate start, LocalDate end) {
        List<MeterReading> readings = meterReadingRepository.findByMeterIdAndReadingDateBetween(meterId, start, end);
        return readings.stream()
                .max((r1, r2) -> r1.getReadingDate().compareTo(r2.getReadingDate()));
    }

    /**
     * Find the latest meter reading before a specific date
     */
    private Optional<MeterReading> findLatestReadingBeforeDate(Long meterId, LocalDate beforeDate) {
        List<MeterReading> readings = meterReadingRepository.findByMeterIdOrderByReadingDateDesc(meterId);
        return readings.stream()
                .filter(reading -> reading.getReadingDate().isBefore(beforeDate))
                .findFirst(); // Already ordered by date desc
    }

    /**
     * Generate a unique bill number
     */
    private String generateBillNumber() {
        return "BILL" + System.currentTimeMillis() + String.format("%04d", random.nextInt(9999));
    }

    /**
     * Get billing statistics for monitoring purposes
     */
    public BillingStats getBillingStats(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        
        List<Bill> monthlyBills = billRepository.findByBillDateBetween(start, end);
        
        long totalBills = monthlyBills.size();
        BigDecimal totalAmount = monthlyBills.stream()
                .map(Bill::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long pendingBills = monthlyBills.stream()
                .mapToLong(bill -> bill.getStatus() == Bill.BillStatus.PENDING ? 1 : 0)
                .sum();
        
        long paidBills = monthlyBills.stream()
                .mapToLong(bill -> bill.getStatus() == Bill.BillStatus.PAID ? 1 : 0)
                .sum();

        return new BillingStats(totalBills, totalAmount, pendingBills, paidBills, month);
    }

    /**
     * Statistics class for billing information
     */
    public static class BillingStats {
        private final long totalBills;
        private final BigDecimal totalAmount;
        private final long pendingBills;
        private final long paidBills;
        private final YearMonth month;

        public BillingStats(long totalBills, BigDecimal totalAmount, long pendingBills, long paidBills, YearMonth month) {
            this.totalBills = totalBills;
            this.totalAmount = totalAmount;
            this.pendingBills = pendingBills;
            this.paidBills = paidBills;
            this.month = month;
        }

        // Getters
        public long getTotalBills() { return totalBills; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public long getPendingBills() { return pendingBills; }
        public long getPaidBills() { return paidBills; }
        public YearMonth getMonth() { return month; }
    }
}
