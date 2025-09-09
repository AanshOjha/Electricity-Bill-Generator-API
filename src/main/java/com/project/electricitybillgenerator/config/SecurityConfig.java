package com.project.electricitybillgenerator.config;

import com.project.electricitybillgenerator.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the Electricity Bill Generator API.
 * Implements role-based access control with ROLE_USER and ROLE_ADMIN.
 * Supports both HTTP Basic Authentication and JWT Authentication.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    public SecurityConfig(CustomUserDetailsService userDetailsService,
                         JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                         JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    
    /**
     * Password encoder bean for securing user passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Authentication manager bean.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * Security filter chain configuration.
     * Configures HTTP security, authentication, and authorization rules.
     * Supports both HTTP Basic Authentication and JWT Authentication.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for API
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - no authentication required
                .requestMatchers("/api/v1/bill/users/register").permitAll()
                .requestMatchers("/api/v1/bill/login").permitAll()
                .requestMatchers("/api/v1/auth/register").permitAll()  // JWT registration
                .requestMatchers("/api/v1/auth/login").permitAll()     // JWT login
                .requestMatchers("/api/v1/auth/validate").permitAll()  // JWT validation
                .requestMatchers("/h2-console/**").permitAll() // For testing with H2
                
                // Admin-only endpoints
                .requestMatchers("/api/v1/bill/users").hasRole("ADMIN")
                .requestMatchers("/api/v1/bill/users/{meterId}").hasRole("ADMIN")
                .requestMatchers("/api/v1/bill/generate/{meterId}").hasRole("ADMIN")
                .requestMatchers("/api/v1/bill/bills/generate").hasRole("ADMIN")
                .requestMatchers("/api/v1/bill/bills/monthly").hasRole("ADMIN")
                .requestMatchers("/api/v1/bill/bills/process-overdue").hasRole("ADMIN")
                
                // User and Admin can access these (will be further restricted by @PreAuthorize)
                .requestMatchers("/api/v1/bill/users/{meterId}/bills").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/v1/bill/bills/{billId}").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/v1/bill/readings/insert").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/v1/bill/bills/**").hasAnyRole("USER", "ADMIN")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .httpBasic(httpBasic -> httpBasic.realmName("Electricity Bill Generator")) // Enable HTTP Basic authentication for backward compatibility
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())); // For H2 console
            
        // Add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}
