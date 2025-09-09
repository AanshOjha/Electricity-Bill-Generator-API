package com.project.electricitybillgenerator.controller;

import com.project.electricitybillgenerator.dto.LoginRequest;
import com.project.electricitybillgenerator.model.BillUser;
import com.project.electricitybillgenerator.service.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication controller for login operations.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/bill")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    
    public AuthController(AuthenticationManager authenticationManager, 
                         CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }
    
    /**
     * Authenticates a user and returns basic user information.
     * 
     * @param loginRequest the login credentials
     * @return ResponseEntity with user information or error
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Login attempt for email: {}", loginRequest.getEmail());
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Get user details
            BillUser user = userDetailsService.findByEmail(loginRequest.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("user", Map.of(
                "meterId", user.getMeterId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole().name()
            ));
            
            logger.info("Successful login for user: {} with role: {}", user.getEmail(), user.getRole());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Login failed for email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
    }
}
