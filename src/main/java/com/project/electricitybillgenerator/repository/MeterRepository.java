package com.project.electricitybillgenerator.repository;

import com.project.electricitybillgenerator.entity.Meter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeterRepository extends JpaRepository<Meter, Long> {
    
    List<Meter> findByCustomerId(Long customerId);
    
    Optional<Meter> findByMeterNumber(String meterNumber);
    
    boolean existsByMeterNumber(String meterNumber);
    
    List<Meter> findByCustomerIdAndStatus(Long customerId, Meter.MeterStatus status);
}
