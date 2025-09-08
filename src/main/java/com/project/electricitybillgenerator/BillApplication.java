package com.project.electricitybillgenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

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
    
    @Autowired
    private Environment environment;

    /**
     * Main method to start the Spring Boot application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        logger.info("Starting Electricity Bill Generator API...");
        SpringApplication.run(BillApplication.class, args);
    }
    
    /**
     * Event listener that executes after the application is ready.
     * 
     * @param event ApplicationReadyEvent
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        logger.info("Electricity Bill Generator API started successfully!");
        logger.info("Application is running on port: {}", 
                   environment.getProperty("server.port", "8080"));
    }
}
