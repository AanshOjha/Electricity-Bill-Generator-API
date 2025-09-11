package com.project.electricitybillgenerator.repository;

import com.project.electricitybillgenerator.entity.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {
    
    List<MeterReading> findByMeterIdOrderByReadingDateDesc(Long meterId);
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.meter.id = :meterId " +
           "AND mr.readingDate = (SELECT MAX(mr2.readingDate) FROM MeterReading mr2 WHERE mr2.meter.id = :meterId)")
    Optional<MeterReading> findLatestByMeterId(Long meterId);
    
    List<MeterReading> findByMeterIdAndReadingDateBetween(Long meterId, LocalDate startDate, LocalDate endDate);
    
    boolean existsByMeterIdAndReadingDate(Long meterId, LocalDate readingDate);
}
