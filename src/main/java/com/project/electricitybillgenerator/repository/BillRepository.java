package com.project.electricitybillgenerator.repository;

import com.project.electricitybillgenerator.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    
    List<Bill> findByMeterCustomerIdOrderByBillDateDesc(Long customerId);
    
    List<Bill> findByMeterIdOrderByBillDateDesc(Long meterId);
    
    Optional<Bill> findByBillNumber(String billNumber);
    
    List<Bill> findByStatusAndDueDateBefore(Bill.BillStatus status, LocalDate date);
    
    List<Bill> findByMeterCustomerIdAndBillDateBetween(Long customerId, LocalDate startDate, LocalDate endDate);
    
    List<Bill> findByBillDateBetween(LocalDate startDate, LocalDate endDate);
    
    Optional<Bill> findByMeterIdAndBillDateBetween(Long meterId, LocalDate startDate, LocalDate endDate);
}
