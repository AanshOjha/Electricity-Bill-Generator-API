package com.project.electricitybillgenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Main Spring Boot application class for the Electricity Bill Generator API.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@SpringBootApplication
@EnableConfigurationProperties
public class BillApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(BillApplication.class);

    /**
     * Main method to start the Spring Boot application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            logger.info("Starting Electricity Bill Generator API...");
            ConfigurableApplicationContext context = SpringApplication.run(BillApplication.class, args);
            logger.info("Electricity Bill Generator API started successfully!");
            logger.info("Application is running on port: {}", 
                       context.getEnvironment().getProperty("server.port", "8080"));
        } catch (Exception e) {
            logger.error("Failed to start Electricity Bill Generator API", e);
            System.exit(1);
        }
    }
}
