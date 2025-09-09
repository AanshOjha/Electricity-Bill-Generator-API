package com.project.electricitybillgenerator.service;

import com.project.electricitybillgenerator.model.BillUser;
import com.project.electricitybillgenerator.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetailsService implementation for Spring Security.
 * Loads user details from the database for authentication and authorization.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Loads user details by email (username) for Spring Security authentication.
     * 
     * @param email the email address used as username
     * @return UserDetails object containing user information and authorities
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        BillUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .build();
    }
    
    /**
     * Gets the authorities (roles) for the user.
     * 
     * @param user the BillUser entity
     * @return collection of granted authorities
     */
    private Collection<? extends GrantedAuthority> getAuthorities(BillUser user) {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
    }
    
    /**
     * Finds a user by email - helper method for other services.
     * 
     * @param email the email address
     * @return BillUser if found
     * @throws UsernameNotFoundException if user is not found
     */
    public BillUser findByEmail(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
