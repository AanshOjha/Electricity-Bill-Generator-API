package com.project.electricitybillgenerator.service;

import com.project.electricitybillgenerator.model.BillUser;
import com.project.electricitybillgenerator.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service for handling authentication-related operations.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@Service
public class AuthenticationService {
    
    private final UserRepository userRepository;
    
    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Gets the currently authenticated user.
     * 
     * @return the current BillUser
     * @throws UsernameNotFoundException if user is not found
     */
    public BillUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("No authenticated user found");
        }
        
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
    
    /**
     * Checks if the current user is an admin.
     * 
     * @return true if current user is admin, false otherwise
     */
    public boolean isCurrentUserAdmin() {
        try {
            BillUser currentUser = getCurrentUser();
            return currentUser.getRole().name().equals("ROLE_ADMIN");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Checks if the current user can access the specified meter ID.
     * Admins can access all meter IDs, users can only access their own.
     * 
     * @param meterId the meter ID to check access for
     * @return true if access is allowed, false otherwise
     */
    public boolean canAccessMeter(Integer meterId) {
        try {
            BillUser currentUser = getCurrentUser();
            
            // Admin can access all meters
            if (currentUser.getRole().name().equals("ROLE_ADMIN")) {
                return true;
            }
            
            // User can only access their own meter
            return currentUser.getMeterId().equals(meterId);
        } catch (Exception e) {
            return false;
        }
    }
}
