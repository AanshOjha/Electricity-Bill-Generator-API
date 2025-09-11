package com.project.electricitybillgenerator.controller;

import com.project.electricitybillgenerator.service.ScheduledBillingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Admin controller for billing operations
 */
@RestController
@RequestMapping("/api/admin/billing")
@PreAuthorize("hasRole('ADMIN')")
public class BillingAdminController {

    private static final Logger logger = LoggerFactory.getLogger(BillingAdminController.class);

    @Autowired
    private ScheduledBillingService billingService;

    /**
     * Manually trigger bill generation for the previous month
     * This endpoint allows admins to test the billing process or run it on-demand
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> triggerManualBillGeneration() {
        logger.info("Manual bill generation triggered by admin");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            billingService.generateBillsForPreviousMonth();
            
            response.put("success", true);
            response.put("message", "Bill generation completed successfully for the previous month.");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            logger.info("Manual bill generation completed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error during manual bill generation: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "An error occurred during bill generation: " + e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get billing statistics for a specific month
     * Format: YYYY-MM (e.g., 2024-09)
     */
    @GetMapping("/stats/{month}")
    public ResponseEntity<Map<String, Object>> getBillingStats(@PathVariable String month) {
        logger.info("Billing stats requested for month: {}", month);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            YearMonth yearMonth = YearMonth.parse(month);
            ScheduledBillingService.BillingStats stats = billingService.getBillingStats(yearMonth);
            
            response.put("success", true);
            response.put("month", month);
            response.put("totalBills", stats.getTotalBills());
            response.put("totalAmount", stats.getTotalAmount());
            response.put("pendingBills", stats.getPendingBills());
            response.put("paidBills", stats.getPaidBills());
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (DateTimeParseException e) {
            logger.error("Invalid month format provided: {}", month);
            
            response.put("success", false);
            response.put("message", "Invalid month format. Use YYYY-MM format (e.g., 2024-09)");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving billing stats for month {}: {}", month, e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "An error occurred while retrieving billing statistics: " + e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get current billing status and system health
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getBillingStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get current month stats
            YearMonth currentMonth = YearMonth.now();
            YearMonth previousMonth = currentMonth.minusMonths(1);
            
            ScheduledBillingService.BillingStats currentStats = billingService.getBillingStats(currentMonth);
            ScheduledBillingService.BillingStats previousStats = billingService.getBillingStats(previousMonth);
            
            response.put("success", true);
            response.put("currentMonth", Map.of(
                "month", currentMonth.toString(),
                "totalBills", currentStats.getTotalBills(),
                "totalAmount", currentStats.getTotalAmount(),
                "pendingBills", currentStats.getPendingBills(),
                "paidBills", currentStats.getPaidBills()
            ));
            response.put("previousMonth", Map.of(
                "month", previousMonth.toString(),
                "totalBills", previousStats.getTotalBills(),
                "totalAmount", previousStats.getTotalAmount(),
                "pendingBills", previousStats.getPendingBills(),
                "paidBills", previousStats.getPaidBills()
            ));
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving billing status: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "An error occurred while retrieving billing status: " + e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
