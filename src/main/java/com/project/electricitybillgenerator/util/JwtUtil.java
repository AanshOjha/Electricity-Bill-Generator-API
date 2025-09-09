package com.project.electricitybillgenerator.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Utility class for token generation, validation, and extraction of claims.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@Component
public class JwtUtil {

    private static final String SECRET_KEY = "mySecretKey123456789012345678901234567890123456789012345678901234567890";
    private static final int JWT_TOKEN_VALIDITY = 5 * 60 * 60; // 5 hours

    private final SecretKey key;

    public JwtUtil() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * Retrieve username from JWT token
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Retrieve expiration date from JWT token
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Retrieve any claim from token
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Retrieve all claims from token
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    /**
     * Check if the token has expired
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Generate token for user
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        
        // Add role information to claims
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        claims.put("role", role);
        
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Generate token for user with custom claims
     */
    public String generateToken(String username, String role, Integer meterId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("meterId", meterId);
        return createToken(claims, username);
    }

    /**
     * Create JWT token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Validate token
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Validate token without UserDetails
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract role from token
     */
    public String getRoleFromToken(String token) {
        return getClaimFromToken(token, claims -> (String) claims.get("role"));
    }

    /**
     * Extract meter ID from token
     */
    public Integer getMeterIdFromToken(String token) {
        return getClaimFromToken(token, claims -> (Integer) claims.get("meterId"));
    }
}
