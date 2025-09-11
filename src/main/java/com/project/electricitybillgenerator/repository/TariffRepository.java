package com.project.electricitybillgenerator.repository;

import com.project.electricitybillgenerator.entity.Customer;
import com.project.electricitybillgenerator.entity.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, Long> {
    
    @Query("SELECT t FROM Tariff t WHERE t.customerType = :customerType " +
           "AND t.active = true " +
           "AND t.effectiveDate <= :date " +
           "AND (t.expiryDate IS NULL OR t.expiryDate >= :date) " +
           "ORDER BY t.effectiveDate DESC")
    Optional<Tariff> findActiveByCustomerTypeAndDate(Customer.CustomerType customerType, LocalDate date);
}
