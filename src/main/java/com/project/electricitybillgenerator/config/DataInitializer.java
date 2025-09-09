package com.project.electricitybillgenerator.config;

import com.project.electricitybillgenerator.model.BillUser;
import com.project.electricitybillgenerator.model.UserRole;
import com.project.electricitybillgenerator.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data initialization component that creates default admin user if none exists.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(String... args) throws Exception {
        initializeDefaultAdminUser();
    }
    
    /**
     * Creates a default admin user if no admin users exist in the system.
     */
    private void initializeDefaultAdminUser() {
        try {
            // Check if any admin users exist
            boolean adminExists = userRepository.findAll().stream()
                    .anyMatch(user -> user.getRole() == UserRole.ROLE_ADMIN);
            
            if (!adminExists) {
                // Create default admin user
                BillUser adminUser = new BillUser(
                    "System Administrator",
                    "System Address",
                    "admin@billgenerator.com",
                    passwordEncoder.encode("admin123"), // Default password
                    UserRole.ROLE_ADMIN
                );
                
                // Set a specific meter ID for admin (outside normal range)
                adminUser.setMeterId(1000); // Starting at configured min
                
                userRepository.save(adminUser);
                
                logger.info("=== IMPORTANT SECURITY NOTICE ===");
                logger.info("Default admin user created:");
                logger.info("Email: admin@billgenerator.com");
                logger.info("Password: admin123");
                logger.info("Meter ID: 1000");
                logger.info("PLEASE CHANGE THE DEFAULT PASSWORD IMMEDIATELY!");
                logger.info("==================================");
            } else {
                logger.debug("Admin user(s) already exist. Skipping default admin creation.");
            }
        } catch (Exception e) {
            logger.error("Error during admin user initialization", e);
        }
    }
}
