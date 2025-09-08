package com.project.electricitybillgenerator.repository;

import com.project.electricitybillgenerator.model.BillReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BillReading entity operations.
 * Extends JpaRepository for enhanced functionality and performance.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@Repository
public interface ReadingRepository extends JpaRepository<BillReading, Integer> {
    
    /**
     * Retrieves the previous month's reading for a specific meter.
     * 
     * @param meterId the meter identifier
     * @param previousDate the date to search for previous readings
     * @return list of previous readings (should typically contain one or zero elements)
     */
    @Query(value = "SELECT current_month_reading FROM billreading " +
                   "WHERE meter_id = :meterId AND DATE(date) <= DATE(:previousDate) " +
                   "ORDER BY date DESC LIMIT 1", 
           nativeQuery = true)
    List<Double> previousReading(@Param("meterId") Integer meterId, @Param("previousDate") Date previousDate);
    
    /**
     * Finds all readings for a specific meter ordered by date descending.
     * 
     * @param meterId the meter identifier
     * @return list of bill readings for the meter
     */
    List<BillReading> findByMeterIdOrderByDateDesc(Integer meterId);
    
    /**
     * Finds the most recent reading for a specific meter.
     * 
     * @param meterId the meter identifier
     * @return optional containing the most recent reading if found
     */
    Optional<BillReading> findTopByMeterIdOrderByDateDesc(Integer meterId);
}
