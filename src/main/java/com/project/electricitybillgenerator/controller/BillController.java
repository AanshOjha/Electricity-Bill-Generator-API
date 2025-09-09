package com.project.electricitybillgenerator.controller;

import com.project.electricitybillgenerator.model.Bill;
import com.project.electricitybillgenerator.model.BillReading;
import com.project.electricitybillgenerator.model.BillStatus;
import com.project.electricitybillgenerator.model.BillUser;
import com.project.electricitybillgenerator.model.UserRole;
import com.project.electricitybillgenerator.dto.BillGenerationRequest;
import com.project.electricitybillgenerator.dto.BillResponse;
import com.project.electricitybillgenerator.dto.UserCreateRequest;
import com.project.electricitybillgenerator.dto.UserResponse;
import com.project.electricitybillgenerator.dto.ErrorResponse;
import com.project.electricitybillgenerator.service.AuthenticationService;
import com.project.electricitybillgenerator.service.BillCalculationService;
import com.project.electricitybillgenerator.service.BillService;
import com.project.electricitybillgenerator.service.UserService;
import com.project.electricitybillgenerator.service.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final BillService billService;
    private final AuthenticationService authenticationService;
    private final UserMapper userMapper;

    /**
     * Constructor for BillController.
     * 
     * @param userService the service for user operations
     * @param billCalculationService the service for bill calculations
     * @param billService the service for bill generation operations
     * @param authenticationService the service for authentication operations
     * @param userMapper the mapper for user entity/DTO conversions
     */
    public BillController(UserService userService, BillCalculationService billCalculationService, 
                         BillService billService, AuthenticationService authenticationService,
                         UserMapper userMapper) {
        this.userService = userService;
        this.billCalculationService = billCalculationService;
        this.billService = billService;
        this.authenticationService = authenticationService;
        this.userMapper = userMapper;
    }

    /**
     * Registers a new user in the system.
     * 
     * @param request the user registration request
     * @return ResponseEntity with the registered user or error message
     */
    @PostMapping("/users/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserCreateRequest request) {
        try {
            logger.info("Attempting to register user with email: {}", request.getEmail());
            
            // Convert DTO to Entity
            BillUser user = userMapper.toEntity(request, UserRole.ROLE_USER);
            BillUser savedUser = userService.saveUser(user);
            
            // Convert Entity to Response DTO
            UserResponse response = userMapper.toResponse(savedUser);
            
            logger.info("Successfully registered user with meter ID: {}", savedUser.getMeterId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
     * Creates an admin user in the system.
     * Only existing admins can create new admin users.
     * 
     * @param request the admin user creation request
     * @return ResponseEntity with the created admin user or error message
     */
    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAdminUser(@Valid @RequestBody UserCreateRequest request) {
        try {
            logger.info("Attempting to create admin user with email: {}", request.getEmail());
            
            // Convert DTO to Entity with ADMIN role
            BillUser user = userMapper.toEntity(request, UserRole.ROLE_ADMIN);
            BillUser savedUser = userService.createAdminUser(user);
            
            // Convert Entity to Response DTO
            UserResponse response = userMapper.toResponse(savedUser);
            
            logger.info("Successfully created admin user with meter ID: {}", savedUser.getMeterId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            logger.error("Error creating admin user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Admin creation failed: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during admin user creation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error occurred"));
        }
    }

    /**
     * Retrieves all users in the system.
     * Only admins can access this endpoint.
     * 
     * @return ResponseEntity with list of all users
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        try {
            logger.debug("Retrieving all users");
            List<BillUser> users = userService.getAllUsers();
            List<UserResponse> userResponses = userMapper.toResponseList(users);
            return ResponseEntity.ok(userResponses);
        } catch (Exception e) {
            logger.error("Error retrieving users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a user by meter ID.
     * Admins can access any user, regular users can only access their own data.
     * 
     * @param meterId the meter ID to search for
     * @return ResponseEntity with the user or not found status
     */
    @GetMapping("/users/{meterId}")
    @PreAuthorize("hasRole('ADMIN') or @authenticationService.canAccessMeter(#meterId)")
    public ResponseEntity<?> getUserByMeterId(@PathVariable Integer meterId) {
        try {
            logger.debug("Retrieving user with meter ID: {}", meterId);
            Optional<BillUser> user = userService.findUserByMeterId(meterId);
            
            if (user.isPresent()) {
                UserResponse response = userMapper.toResponse(user.get());
                return ResponseEntity.ok(response);
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
     * Only admins can delete users.
     * 
     * @param meterId the meter ID of the user to delete
     * @return ResponseEntity with success or error message
     */
    @DeleteMapping("/users/{meterId}")
    @PreAuthorize("hasRole('ADMIN')")
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
     * Only admins can delete all users.
     * 
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
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
     * Users can only insert readings for their own meter, admins can insert for any meter.
     * 
     * @param reading the reading to insert
     * @return ResponseEntity with the processed reading or error message
     */
    @PostMapping("/readings")
    @PreAuthorize("hasRole('ADMIN') or @authenticationService.canAccessMeter(#reading.meterId)")
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

    // ===== NEW BILL GENERATION AND RETRIEVAL ENDPOINTS =====

    /**
     * Generate a new bill for a user for the current month.
     * This endpoint automatically finds the latest reading and generates a bill.
     * Only admins can generate bills.
     * 
     * @param meterId the meter ID for which to generate the bill
     * @return ResponseEntity with the generated bill or error message
     */
    @PostMapping("/api/v1/bill/generate/{meterId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateBillForCurrentMonth(@PathVariable Integer meterId) {
        try {
            logger.info("Generating current month bill for meter ID: {}", meterId);
            
            Bill bill = billService.generateCurrentMonthBill(meterId);
            BillResponse response = convertToResponse(bill);
            
            logger.info("Successfully generated bill with ID: {} for meter: {}", bill.getBillId(), meterId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            logger.error("Error generating bill for meter {}: {}", meterId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Bill generation failed: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during bill generation for meter: {}", meterId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error occurred"));
        }
    }

    /**
     * Get all bills for a specific user.
     * Users can only view their own bills, admins can view any user's bills.
     * 
     * @param meterId the meter ID of the user
     * @return ResponseEntity with list of bills for the user
     */
    @GetMapping("/api/v1/bill/users/{meterId}/bills")
    @PreAuthorize("hasRole('ADMIN') or @authenticationService.canAccessMeter(#meterId)")
    public ResponseEntity<?> getAllBillsForUser(@PathVariable Integer meterId) {
        try {
            logger.debug("Retrieving all bills for user with meter ID: {}", meterId);
            
            // Verify user exists
            Optional<BillUser> userOpt = userService.findUserByMeterId(meterId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("User not found with meter ID: " + meterId));
            }
            
            List<Bill> bills = billService.getBillsByMeterId(meterId);
            List<BillResponse> responses = bills.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            logger.debug("Found {} bills for meter ID: {}", bills.size(), meterId);
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            logger.error("Error retrieving bills for meter {}: {}", meterId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error retrieving bills for meter: {}", meterId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error occurred"));
        }
    }

    /**
     * Get details of a single bill.
     * Users can only view their own bills, admins can view any bill.
     * 
     * @param billId the bill ID
     * @return ResponseEntity with the bill details or not found status
     */
    @GetMapping("/api/v1/bill/bills/{billId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getSingleBillDetails(@PathVariable Long billId) {
        try {
            logger.debug("Retrieving bill details for bill ID: {}", billId);
            
            Bill bill = billService.getBillById(billId);
            
            // Check if user can access this bill
            if (!authenticationService.isCurrentUserAdmin()) {
                BillUser currentUser = authenticationService.getCurrentUser();
                if (!bill.getUser().getMeterId().equals(currentUser.getMeterId())) {
                    logger.warn("User {} attempted to access bill {} which belongs to different user", 
                               currentUser.getEmail(), billId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new ErrorResponse("Access denied: You can only view your own bills"));
                }
            }
            
            BillResponse response = convertToResponse(bill);
            
            logger.debug("Successfully retrieved bill details for bill ID: {}", billId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error retrieving bill {}: {}", billId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error retrieving bill: {}", billId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error occurred"));
        }
    }

    // ===== EXISTING BILL GENERATION AND MANAGEMENT ENDPOINTS =====

    /**
     * Generate a bill for a specific meter and billing period.
     * Only admins can generate bills.
     * 
     * @param request the bill generation request
     * @return ResponseEntity with the generated bill or error message
     */
    @PostMapping("/bills/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateBill(@RequestBody BillGenerationRequest request) {
        try {
            logger.info("Generating bill for meter ID: {} for period {} to {}", 
                       request.getMeterId(), request.getBillingPeriodStart(), request.getBillingPeriodEnd());
            
            Bill bill = billService.generateBill(
                request.getMeterId(),
                request.getBillingPeriodStart(),
                request.getBillingPeriodEnd(),
                request.getCurrentReading()
            );
            
            BillResponse response = convertToResponse(bill);
            logger.info("Successfully generated bill with ID: {}", bill.getBillId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            logger.error("Error generating bill: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Bill generation failed: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during bill generation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error occurred"));
        }
    }

    /**
     * Generate a monthly bill for current period.
     * Only admins can generate bills.
     * 
     * @param meterId the meter ID
     * @param currentReading the current reading
     * @return ResponseEntity with the generated bill or error message
     */
    @PostMapping("/bills/generate/monthly/{meterId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateMonthlyBill(@PathVariable Integer meterId, 
                                                @RequestParam String currentReading) {
        try {
            logger.info("Generating monthly bill for meter ID: {}", meterId);
            
            Bill bill = billService.generateMonthlyBill(meterId, new java.math.BigDecimal(currentReading));
            BillResponse response = convertToResponse(bill);
            
            logger.info("Successfully generated monthly bill with ID: {}", bill.getBillId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            logger.error("Error generating monthly bill: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Monthly bill generation failed: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during monthly bill generation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error occurred"));
        }
    }

    /**
     * Get all bills for a specific meter.
     * 
     * @param meterId the meter ID
     * @return ResponseEntity with list of bills
     */
    @GetMapping("/bills/meter/{meterId}")
    public ResponseEntity<?> getBillsByMeterId(@PathVariable Integer meterId) {
        try {
            logger.debug("Retrieving bills for meter ID: {}", meterId);
            List<Bill> bills = billService.getBillsByMeterId(meterId);
            List<BillResponse> responses = bills.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("Error retrieving bills for meter ID: {}", meterId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error occurred"));
        }
    }

    /**
     * Get a specific bill by ID.
     * 
     * @param billId the bill ID
     * @return ResponseEntity with the bill or not found status
     */
    @GetMapping("/bills/{billId}")
    public ResponseEntity<?> getBillById(@PathVariable Long billId) {
        try {
            logger.debug("Retrieving bill with ID: {}", billId);
            Bill bill = billService.getBillById(billId);
            BillResponse response = convertToResponse(bill);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error retrieving bill: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error retrieving bill", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error occurred"));
        }
    }

    /**
     * Mark a bill as paid.
     * 
     * @param billId the bill ID to mark as paid
     * @return ResponseEntity with success message or error
     */
    @PutMapping("/bills/{billId}/pay")
    public ResponseEntity<?> markBillAsPaid(@PathVariable Long billId) {
        try {
            logger.info("Marking bill {} as paid", billId);
            Bill bill = billService.markBillAsPaid(billId);
            BillResponse response = convertToResponse(bill);
            
            logger.info("Successfully marked bill {} as paid", billId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error marking bill as paid: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error marking bill as paid", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error occurred"));
        }
    }

    /**
     * Get bills by status.
     * 
     * @param status the bill status
     * @return ResponseEntity with list of bills
     */
    @GetMapping("/bills/status/{status}")
    public ResponseEntity<?> getBillsByStatus(@PathVariable String status) {
        try {
            logger.debug("Retrieving bills with status: {}", status);
            BillStatus billStatus = BillStatus.valueOf(status.toUpperCase());
            List<Bill> bills = billService.getBillsByStatus(billStatus);
            List<BillResponse> responses = bills.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid status: " + status));
        } catch (Exception e) {
            logger.error("Error retrieving bills by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error occurred"));
        }
    }

    /**
     * Get total unpaid amount for a meter.
     * 
     * @param meterId the meter ID
     * @return ResponseEntity with total unpaid amount
     */
    @GetMapping("/bills/meter/{meterId}/unpaid-total")
    public ResponseEntity<?> getTotalUnpaidAmount(@PathVariable Integer meterId) {
        try {
            logger.debug("Getting total unpaid amount for meter ID: {}", meterId);
            java.math.BigDecimal totalAmount = billService.getTotalUnpaidAmount(meterId);
            
            return ResponseEntity.ok(new UnpaidAmountResponse(totalAmount, 
                    billService.getUnpaidBillCount(meterId)));
        } catch (Exception e) {
            logger.error("Error getting unpaid amount for meter ID: {}", meterId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error occurred"));
        }
    }

    /**
     * Process overdue bills and apply late fees.
     * Only admins can process overdue bills.
     * 
     * @return ResponseEntity with processed bills count
     */
    @PostMapping("/bills/process-overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> processOverdueBills() {
        try {
            logger.info("Processing overdue bills");
            List<Bill> processedBills = billService.processOverdueBills();
            
            logger.info("Processed {} overdue bills", processedBills.size());
            return ResponseEntity.ok(new SuccessResponse(
                    "Processed " + processedBills.size() + " overdue bills"));
        } catch (Exception e) {
            logger.error("Error processing overdue bills", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error occurred"));
        }
    }

    // ===== HELPER METHODS =====

    /**
     * Convert Bill entity to BillResponse DTO.
     */
    private BillResponse convertToResponse(Bill bill) {
        BillResponse response = new BillResponse();
        response.setBillId(bill.getBillId());
        response.setMeterId(bill.getMeterId());
        response.setBillingPeriodStart(bill.getBillingPeriodStart());
        response.setBillingPeriodEnd(bill.getBillingPeriodEnd());
        response.setPreviousReading(bill.getPreviousReading());
        response.setCurrentReading(bill.getCurrentReading());
        response.setUnitsConsumed(bill.getUnitsConsumed());
        response.setAmount(bill.getAmount());
        response.setServiceCharge(bill.getServiceCharge());
        response.setTaxAmount(bill.getTaxAmount());
        response.setLateFee(bill.getLateFee());
        response.setTotalAmount(bill.getTotalAmount());
        response.setDueDate(bill.getDueDate());
        response.setStatus(bill.getStatus());
        response.setCreatedAt(bill.getCreatedAt());
        response.setPaidAt(bill.getPaidAt());
        response.setRatePerUnit(bill.getRatePerUnit());
        
        // Set user information if available
        if (bill.getUser() != null) {
            response.setUserName(bill.getUser().getName());
            response.setUserAddress(bill.getUser().getAddress());
        }
        
        return response;
    }

    /**
     * Response class for unpaid amount information.
     */
    public static class UnpaidAmountResponse {
        private final java.math.BigDecimal totalAmount;
        private final Long billCount;
        private final String timestamp;

        public UnpaidAmountResponse(java.math.BigDecimal totalAmount, Long billCount) {
            this.totalAmount = totalAmount;
            this.billCount = billCount;
            this.timestamp = LocalDate.now().toString();
        }

        public java.math.BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public Long billCount() {
            return billCount;
        }

        public String getTimestamp() {
            return timestamp;
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