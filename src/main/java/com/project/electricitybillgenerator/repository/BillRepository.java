package com.project.electricitybillgenerator.repository;

import com.project.electricitybillgenerator.model.Bill;
import com.project.electricitybillgenerator.model.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Bill entity operations.
 * Provides data access methods for electricity bills.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    
    /**
     * Find all bills for a specific meter ID
     */
    List<Bill> findByMeterIdOrderByBillingPeriodEndDesc(Integer meterId);
    
    /**
     * Find bills by status
     */
    List<Bill> findByStatusOrderByDueDateAsc(BillStatus status);
    
    /**
     * Find bills that are overdue (due date passed and status is not PAID)
     */
    @Query("SELECT b FROM Bill b WHERE b.dueDate < :currentDate AND b.status != 'PAID'")
    List<Bill> findOverdueBills(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Find bill for a specific meter and billing period
     */
    Optional<Bill> findByMeterIdAndBillingPeriodStartAndBillingPeriodEnd(
        Integer meterId, LocalDate billingPeriodStart, LocalDate billingPeriodEnd);
    
    /**
     * Find the latest bill for a specific meter
     */
    Optional<Bill> findTopByMeterIdOrderByBillingPeriodEndDesc(Integer meterId);
    
    /**
     * Find bills within a date range
     */
    @Query("SELECT b FROM Bill b WHERE b.billingPeriodStart >= :startDate AND b.billingPeriodEnd <= :endDate")
    List<Bill> findBillsByDateRange(@Param("startDate") LocalDate startDate, 
                                   @Param("endDate") LocalDate endDate);
    
    /**
     * Find bills for a specific meter within a date range
     */
    @Query("SELECT b FROM Bill b WHERE b.meterId = :meterId AND b.billingPeriodStart >= :startDate AND b.billingPeriodEnd <= :endDate ORDER BY b.billingPeriodEnd DESC")
    List<Bill> findBillsByMeterIdAndDateRange(@Param("meterId") Integer meterId,
                                             @Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);
    
    /**
     * Check if a bill exists for a specific meter and billing period
     */
    boolean existsByMeterIdAndBillingPeriodStartAndBillingPeriodEnd(
        Integer meterId, LocalDate billingPeriodStart, LocalDate billingPeriodEnd);
    
    /**
     * Get total unpaid amount for a specific meter
     */
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.meterId = :meterId AND b.status IN ('DUE', 'OVERDUE')")
    Double getTotalUnpaidAmountByMeterId(@Param("meterId") Integer meterId);
    
    /**
     * Get count of unpaid bills for a specific meter
     */
    @Query("SELECT COUNT(b) FROM Bill b WHERE b.meterId = :meterId AND b.status IN ('DUE', 'OVERDUE')")
    Long getUnpaidBillCountByMeterId(@Param("meterId") Integer meterId);
    
    /**
     * Find bills by multiple statuses
     */
    List<Bill> findByStatusInOrderByDueDateAsc(List<BillStatus> statuses);
    
    /**
     * Find bills due within a specific number of days
     */
    @Query("SELECT b FROM Bill b WHERE b.dueDate BETWEEN :startDate AND :endDate AND b.status IN ('DUE', 'OVERDUE')")
    List<Bill> findBillsDueWithinDays(@Param("startDate") LocalDate startDate, 
                                     @Param("endDate") LocalDate endDate);
}
