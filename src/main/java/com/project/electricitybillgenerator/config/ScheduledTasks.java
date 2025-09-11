package com.project.electricitybillgenerator.config;

import com.project.electricitybillgenerator.service.ScheduledBillingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks for automated billing operations
 */
@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private ScheduledBillingService billingService;

    /**
     * Runs monthly billing on the 1st day of every month at 1:00 AM
     * Cron expression: "0 0 1 1 * ?" means:
     * - 0 seconds
     * - 0 minutes  
     * - 1 hour (1:00 AM)
     * - 1st day of month
     * - every month
     * - any day of week
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    public void runMonthlyBilling() {
        logger.info("Starting scheduled monthly billing process...");
        try {
            billingService.generateBillsForPreviousMonth();
            logger.info("Scheduled monthly billing completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled monthly billing: {}", e.getMessage(), e);
        }
    }

    /**
     * Optional: Run a daily check for overdue bills at 2:00 AM
     * This can be used for notifications or status updates
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkOverdueBills() {
        logger.info("Running daily overdue bills check...");
        try {
            // This is a placeholder for future overdue bill processing
            // You can implement notifications, status updates, etc.
            logger.info("Overdue bills check completed");
        } catch (Exception e) {
            logger.error("Error during overdue bills check: {}", e.getMessage(), e);
        }
    }
}
