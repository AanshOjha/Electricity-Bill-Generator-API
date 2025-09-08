package com.project.electricitybillgenerator.service;

import com.project.electricitybillgenerator.config.BillConfiguration;
import com.project.electricitybillgenerator.model.BillReading;
import com.project.electricitybillgenerator.model.BillUser;
import com.project.electricitybillgenerator.repository.ReadingRepository;
import com.project.electricitybillgenerator.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing user operations in the electricity billing system.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final ReadingRepository readingRepository;
    private final BillConfiguration billConfiguration;
    private final SecureRandom secureRandom;

    /**
     * Constructor for UserService.
     * 
     * @param userRepository the repository for user operations
     * @param readingRepository the repository for reading operations
     * @param billConfiguration the configuration for billing constants
     */
    public UserService(UserRepository userRepository, 
                      ReadingRepository readingRepository,
                      BillConfiguration billConfiguration) {
        this.userRepository = userRepository;
        this.readingRepository = readingRepository;
        this.billConfiguration = billConfiguration;
        this.secureRandom = new SecureRandom();
    }

    /**
     * Saves a new user with a randomly generated meter ID.
     * 
     * @param user the user to save
     * @return the saved user with assigned meter ID
     * @throws IllegalArgumentException if user data is invalid
     * @throws RuntimeException if email already exists
     */
    public BillUser saveUser(BillUser user) {
        validateUser(user);
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("User with email " + user.getEmail() + " already exists");
        }
        
        int meterId = generateUniqueMeterId();
        user.setMeterId(meterId);
        
        BillUser savedUser = userRepository.save(user);
        logger.info("Successfully saved user with meter ID: {}", meterId);
        
        return savedUser;
    }

    /**
     * Retrieves all users in the system.
     * 
     * @return list of all users
     */
    @Transactional(readOnly = true)
    public List<BillUser> getAllUsers() {
        logger.debug("Retrieving all users");
        return userRepository.findAll();
    }

    /**
     * Deletes a user by meter ID.
     * 
     * @param meterId the meter ID of the user to delete
     * @throws RuntimeException if user not found
     */
    public void deleteUser(int meterId) {
        if (!userRepository.existsById(meterId)) {
            throw new RuntimeException("User with meter ID " + meterId + " not found");
        }
        
        userRepository.deleteById(meterId);
        logger.info("Successfully deleted user with meter ID: {}", meterId);
    }

    /**
     * Deletes all users from the system.
     */
    public void deleteAllUsers() {
        long userCount = userRepository.count();
        userRepository.deleteAll();
        logger.info("Successfully deleted {} users", userCount);
    }

    /**
     * Finds a user by email address.
     * 
     * @param email the email address to search for
     * @return optional containing the user if found
     */
    @Transactional(readOnly = true)
    public Optional<BillUser> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Finds a user by meter ID.
     * 
     * @param meterId the meter ID to search for
     * @return optional containing the user if found
     */
    @Transactional(readOnly = true)
    public Optional<BillUser> findUserByMeterId(Integer meterId) {
        return userRepository.findByMeterId(meterId);
    }

    /**
     * Inserts a new bill reading.
     * 
     * @param billReading the reading to save
     * @return the saved reading
     * @throws IllegalArgumentException if reading data is invalid
     */
    public BillReading insertReading(BillReading billReading) {
        validateReading(billReading);
        
        // Ensure the user exists
        if (!userRepository.existsById(billReading.getMeterId())) {
            throw new RuntimeException("User with meter ID " + billReading.getMeterId() + " not found");
        }
        
        // Set current date if not provided
        if (billReading.getDate() == null) {
            billReading.setDate(LocalDate.now());
        }
        
        BillReading savedReading = readingRepository.save(billReading);
        logger.info("Successfully saved reading for meter ID: {}", billReading.getMeterId());
        
        return savedReading;
    }

    /**
     * Generates a unique meter ID that doesn't already exist.
     * 
     * @return a unique meter ID
     */
    private int generateUniqueMeterId() {
        int meterId;
        int attempts = 0;
        final int maxAttempts = 100;
        
        do {
            meterId = secureRandom.nextInt(
                billConfiguration.getMaxMeterId() - billConfiguration.getMinMeterId() + 1
            ) + billConfiguration.getMinMeterId();
            attempts++;
            
            if (attempts >= maxAttempts) {
                throw new RuntimeException("Unable to generate unique meter ID after " + maxAttempts + " attempts");
            }
        } while (userRepository.existsById(meterId));
        
        return meterId;
    }

    /**
     * Validates user data.
     * 
     * @param user the user to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateUser(BillUser user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("User email cannot be empty");
        }
        if (user.getAddress() == null || user.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("User address cannot be empty");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("User password cannot be empty");
        }
        
        // Basic email validation
        if (!user.getEmail().contains("@") || !user.getEmail().contains(".")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    /**
     * Validates bill reading data.
     * 
     * @param reading the reading to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateReading(BillReading reading) {
        if (reading == null) {
            throw new IllegalArgumentException("Reading cannot be null");
        }
        if (reading.getMeterId() == null || reading.getMeterId() <= 0) {
            throw new IllegalArgumentException("Invalid meter ID");
        }
        if (reading.getCurrentMonthReading() == null || reading.getCurrentMonthReading() < 0) {
            throw new IllegalArgumentException("Current month reading must be non-negative");
        }
        if (reading.getCurrentMonthReading() > billConfiguration.getMaxReadingValue()) {
            throw new IllegalArgumentException("Reading value exceeds maximum allowed");
        }
    }
}