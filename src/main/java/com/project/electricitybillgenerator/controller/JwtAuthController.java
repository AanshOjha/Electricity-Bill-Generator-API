package com.project.electricitybillgenerator.controller;

import com.project.electricitybillgenerator.dto.*;
import com.project.electricitybillgenerator.model.BillUser;
import com.project.electricitybillgenerator.model.UserRole;
import com.project.electricitybillgenerator.service.UserService;
import com.project.electricitybillgenerator.service.mapper.UserMapper;
import com.project.electricitybillgenerator.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST Controller for JWT Authentication operations.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class JwtAuthController {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    /**
     * User registration endpoint.
     * 
     * @param userRegistrationRequest the user registration request
     * @return ResponseEntity with JWT token or error message
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest userRegistrationRequest) {
        try {
            logger.info("Attempting to register user with email: {}", userRegistrationRequest.getEmail());
            
            // Convert DTO to entity
            BillUser user = new BillUser();
            user.setName(userRegistrationRequest.getName());
            user.setAddress(userRegistrationRequest.getAddress());
            user.setEmail(userRegistrationRequest.getEmail());
            user.setPassword(userRegistrationRequest.getPassword());
            user.setRole(UserRole.ROLE_USER); // Default role for registration
            
            // Save user
            BillUser savedUser = userService.saveUser(user);
            
            // Generate JWT token
            String token = jwtUtil.generateToken(
                savedUser.getEmail(), 
                savedUser.getRole().name(), 
                savedUser.getMeterId()
            );
            
            // Create response
            JwtResponse response = new JwtResponse(
                token,
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.getMeterId(),
                savedUser.getName()
            );
            
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
     * User login endpoint.
     * 
     * @param authenticationRequest the authentication request
     * @return ResponseEntity with JWT token or error message
     */
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody JwtRequest authenticationRequest) {
        try {
            logger.info("Attempting login for user: {}", authenticationRequest.getEmail());
            
            // Authenticate user
            authenticate(authenticationRequest.getEmail(), authenticationRequest.getPassword());
            
            // Load user details
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getEmail());
            
            // Get user information
            BillUser user = userService.findUserByEmail(authenticationRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found after authentication"));
            
            // Generate JWT token
            final String token = jwtUtil.generateToken(userDetails);
            
            // Create response
            JwtResponse response = new JwtResponse(
                token,
                user.getEmail(),
                user.getRole().name(),
                user.getMeterId(),
                user.getName()
            );
            
            logger.info("Successful login for user: {} with role: {}", user.getEmail(), user.getRole());
            return ResponseEntity.ok(response);
            
        } catch (DisabledException e) {
            logger.warn("Account disabled for user: {}", authenticationRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("User account is disabled"));
        } catch (BadCredentialsException e) {
            logger.warn("Invalid credentials for user: {}", authenticationRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid credentials"));
        } catch (Exception e) {
            logger.error("Unexpected error during login for user: {}", authenticationRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error occurred"));
        }
    }

    /**
     * Validate JWT token endpoint.
     * 
     * @param token the JWT token to validate
     * @return ResponseEntity with validation result
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            logger.debug("Validating JWT token");
            
            boolean isValid = jwtUtil.validateToken(token);
            if (isValid) {
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                Integer meterId = jwtUtil.getMeterIdFromToken(token);
                
                return ResponseEntity.ok(new JwtValidationResponse(true, username, role, meterId));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new JwtValidationResponse(false, null, null, null));
            }
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JwtValidationResponse(false, null, null, null));
        }
    }

    /**
     * Helper method to authenticate user credentials
     */
    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    /**
     * Inner class for JWT validation response
     */
    public static class JwtValidationResponse {
        private boolean valid;
        private String username;
        private String role;
        private Integer meterId;

        public JwtValidationResponse(boolean valid, String username, String role, Integer meterId) {
            this.valid = valid;
            this.username = username;
            this.role = role;
            this.meterId = meterId;
        }

        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public Integer getMeterId() { return meterId; }
        public void setMeterId(Integer meterId) { this.meterId = meterId; }
    }
}
