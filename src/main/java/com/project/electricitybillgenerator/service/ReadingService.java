package com.project.electricitybillgenerator.service;

import com.project.electricitybillgenerator.repository.ReadingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service class for handling electricity meter reading operations.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@Service
public class ReadingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReadingService.class);
    private static final double DEFAULT_READING = 0.0;
    
    private final ReadingRepository readingRepository;

    /**
     * Constructor for ReadingService.
     * 
     * @param readingRepository the repository for reading data operations
     */
    public ReadingService(ReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }

    /**
     * Calculates the date one month prior to the given date.
     * 
     * @param date the reference date
     * @return the date one month prior
     * @throws IllegalArgumentException if date is null
     */
    public LocalDate getPreviousMonthDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        LocalDate previousMonth = date.minusMonths(1);
        
        logger.debug("Original date: {}, Previous month date: {}", 
                    date, previousMonth);
        
        return previousMonth;
    }

    /**
     * Retrieves the previous month's reading for a specific meter.
     * 
     * @param meterId the meter identifier
     * @param currentDate the current date for reference
     * @return the previous month's reading, or default value if not found
     * @throws IllegalArgumentException if meterId is invalid or currentDate is null
     */
    public double getPreviousMonthReading(Integer meterId, LocalDate currentDate) {
        if (meterId == null || meterId <= 0) {
            throw new IllegalArgumentException("Meter ID must be positive");
        }
        if (currentDate == null) {
            throw new IllegalArgumentException("Current date cannot be null");
        }
        
        try {
            LocalDate previousDate = getPreviousMonthDate(currentDate);
            // Convert LocalDate to Date for repository compatibility
            Date previousDateAsDate = Date.from(previousDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            List<Double> previousReadings = readingRepository.previousReading(meterId, previousDateAsDate);
            
            Optional<Double> reading = previousReadings.stream().findFirst();
            
            if (reading.isPresent()) {
                logger.info("Found previous reading {} for meter {}", reading.get(), meterId);
                return reading.get();
            } else {
                logger.warn("No previous reading found for meter {}, returning default value", meterId);
                return DEFAULT_READING;
            }
        } catch (Exception e) {
            logger.error("Error retrieving previous reading for meter {}: {}", meterId, e.getMessage());
            return DEFAULT_READING;
        }
    }
}
