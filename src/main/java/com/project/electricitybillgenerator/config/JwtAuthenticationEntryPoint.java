package com.project.electricitybillgenerator.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;

/**
 * JWT Authentication Entry Point to handle unauthorized access attempts.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {

        logger.warn("Unauthorized access attempt to: {} - {}", request.getRequestURI(), authException.getMessage());
        
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(
            "{ \"error\": \"Unauthorized access. Please provide a valid JWT token.\", " +
            "\"timestamp\": \"" + java.time.LocalDateTime.now() + "\" }"
        );
    }
}
