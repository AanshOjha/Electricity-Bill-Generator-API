package com.project.electricitybillgenerator.controller;

import com.project.electricitybillgenerator.model.BillReading;
import com.project.electricitybillgenerator.model.BillUser;
import com.project.electricitybillgenerator.service.BillCalculationService;
import com.project.electricitybillgenerator.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for handling electricity bill operations.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/bill")
@CrossOrigin(origins = "*")
public class BillController {
    
    private static final Logger logger = LoggerFactory.getLogger(BillController.class);
    
    private final UserService userService;
    private final BillCalculationService billCalculationService;

    /**
     * Constructor for BillController.
     * 
     * @param userService the service for user operations
     * @param billCalculationService the service for bill calculations
     */
    public BillController(UserService userService, BillCalculationService billCalculationService) {
        this.userService = userService;
        this.billCalculationService = billCalculationService;
    }

    /**
     * Registers a new user in the system.
     * 
     * @param user the user to register
     * @return ResponseEntity with the registered user or error message
     */
    @PostMapping("/users/register")
    public ResponseEntity<?> registerUser(@RequestBody BillUser user) {
        try {
            logger.info("Attempting to register user with email: {}", user.getEmail());
            BillUser savedUser = userService.saveUser(user);
            logger.info("Successfully registered user with meter ID: {}", savedUser.getMeterId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (RuntimeException e) {
            logger.error("Error registering user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Registration failed: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during user registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error occurred"));
        }
    }

    /**
     * Retrieves all users in the system.
     * 
     * @return ResponseEntity with list of all users
     */
    @GetMapping("/users")
    public ResponseEntity<List<BillUser>> getAllUsers() {
        try {
            logger.debug("Retrieving all users");
            List<BillUser> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error retrieving users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a user by meter ID.
     * 
     * @param meterId the meter ID to search for
     * @return ResponseEntity with the user or not found status
     */
    @GetMapping("/users/{meterId}")
    public ResponseEntity<?> getUserByMeterId(@PathVariable Integer meterId) {
        try {
            logger.debug("Retrieving user with meter ID: {}", meterId);
            Optional<BillUser> user = userService.findUserByMeterId(meterId);
            
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("User with meter ID " + meterId + " not found"));
            }
        } catch (Exception e) {
            logger.error("Error retrieving user with meter ID: {}", meterId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error occurred"));
        }
    }

    /**
     * Deletes a user by meter ID.
     * 
     * @param meterId the meter ID of the user to delete
     * @return ResponseEntity with success or error message
     */
    @DeleteMapping("/users/{meterId}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer meterId) {
        try {
            logger.info("Attempting to delete user with meter ID: {}", meterId);
            userService.deleteUser(meterId);
            logger.info("Successfully deleted user with meter ID: {}", meterId);
            return ResponseEntity.ok(new SuccessResponse("User deleted successfully"));
        } catch (RuntimeException e) {
            logger.error("Error deleting user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during user deletion", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error occurred"));
        }
    }

    /**
     * Deletes all users from the system.
     * 
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/users")
    public ResponseEntity<SuccessResponse> deleteAllUsers() {
        try {
            logger.info("Attempting to delete all users");
            userService.deleteAllUsers();
            logger.info("Successfully deleted all users");
            return ResponseEntity.ok(new SuccessResponse("All users deleted successfully"));
        } catch (Exception e) {
            logger.error("Error deleting all users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Inserts a new bill reading with automatic calculations.
     * 
     * @param reading the reading to insert
     * @return ResponseEntity with the processed reading or error message
     */
    @PostMapping("/readings")
    public ResponseEntity<?> insertReading(@RequestBody BillReading reading) {
        try {
            logger.info("Attempting to insert reading for meter ID: {}", reading.getMeterId());
            
            // Process the reading with calculations
            LocalDate currentDate = LocalDate.now();
            BillReading processedReading = billCalculationService.processBillReading(reading, currentDate);
            
            // Save the processed reading
            BillReading savedReading = userService.insertReading(processedReading);
            
            logger.info("Successfully inserted reading for meter ID: {}", reading.getMeterId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedReading);
        } catch (RuntimeException e) {
            logger.error("Error inserting reading: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Reading insertion failed: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during reading insertion", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error occurred"));
        }
    }

    /**
     * Response class for error messages.
     */
    public static class ErrorResponse {
        private final String error;
        private final String timestamp;

        public ErrorResponse(String error) {
            this.error = error;
            this.timestamp = LocalDate.now().toString();
        }

        public String getError() {
            return error;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Response class for success messages.
     */
    public static class SuccessResponse {
        private final String message;
        private final String timestamp;

        public SuccessResponse(String message) {
            this.message = message;
            this.timestamp = LocalDate.now().toString();
        }

        public String getMessage() {
            return message;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }
}