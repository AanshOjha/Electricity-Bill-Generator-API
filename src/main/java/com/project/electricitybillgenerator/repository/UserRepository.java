package com.project.electricitybillgenerator.repository;

import com.project.electricitybillgenerator.model.BillUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for BillUser entity operations.
 * Extends JpaRepository for enhanced functionality and performance.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<BillUser, Integer> {
    
    /**
     * Finds a user by email address.
     * 
     * @param email the email address to search for
     * @return optional containing the user if found
     */
    Optional<BillUser> findByEmail(String email);
    
    /**
     * Checks if a user exists with the given email.
     * 
     * @param email the email address to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Finds a user by meter ID.
     * 
     * @param meterId the meter ID to search for
     * @return optional containing the user if found
     */
    Optional<BillUser> findByMeterId(Integer meterId);
}
