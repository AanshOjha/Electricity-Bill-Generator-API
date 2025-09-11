package com.project.electricitybillgenerator.controller;

import com.project.electricitybillgenerator.entity.Customer;
import com.project.electricitybillgenerator.entity.Tariff;
import com.project.electricitybillgenerator.repository.CustomerRepository;
import com.project.electricitybillgenerator.repository.TariffRepository;
import com.project.electricitybillgenerator.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private TariffRepository tariffRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private CustomerService customerService;

    @PostMapping("/tariffs")
    public ResponseEntity<Tariff> createTariff(@Valid @RequestBody TariffRequest request) {
        Tariff tariff = new Tariff();
        tariff.setName(request.getName());
        tariff.setCustomerType(request.getCustomerType());
        tariff.setRatePerUnit(request.getRatePerUnit());
        tariff.setFixedCharge(request.getFixedCharge());
        tariff.setMinimumCharge(request.getMinimumCharge());
        tariff.setEffectiveDate(request.getEffectiveDate());
        
        Tariff savedTariff = tariffRepository.save(tariff);
        return ResponseEntity.ok(savedTariff);
    }

    @GetMapping("/tariffs")
    public ResponseEntity<List<Tariff>> getAllTariffs() {
        List<Tariff> tariffs = tariffRepository.findAll();
        return ResponseEntity.ok(tariffs);
    }
    
    // User Management Endpoints
    @PostMapping("/users/{userId}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable Long userId) {
        Customer customer = customerRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setActive(false);
        customerRepository.save(customer);
        return ResponseEntity.ok("User deactivated successfully");
    }
    
    @PostMapping("/users/{userId}/activate")
    public ResponseEntity<String> activateUser(@PathVariable Long userId) {
        Customer customer = customerRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setActive(true);
        customerRepository.save(customer);
        return ResponseEntity.ok("User activated successfully");
    }
    
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        customerService.deleteCustomer(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    // Inner class for request
    public static class TariffRequest {
        private String name;
        private Customer.CustomerType customerType;
        private BigDecimal ratePerUnit;
        private BigDecimal fixedCharge = BigDecimal.ZERO;
        private BigDecimal minimumCharge = BigDecimal.ZERO;
        private LocalDate effectiveDate;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Customer.CustomerType getCustomerType() { return customerType; }
        public void setCustomerType(Customer.CustomerType customerType) { this.customerType = customerType; }
        
        public BigDecimal getRatePerUnit() { return ratePerUnit; }
        public void setRatePerUnit(BigDecimal ratePerUnit) { this.ratePerUnit = ratePerUnit; }
        
        public BigDecimal getFixedCharge() { return fixedCharge; }
        public void setFixedCharge(BigDecimal fixedCharge) { this.fixedCharge = fixedCharge; }
        
        public BigDecimal getMinimumCharge() { return minimumCharge; }
        public void setMinimumCharge(BigDecimal minimumCharge) { this.minimumCharge = minimumCharge; }
        
        public LocalDate getEffectiveDate() { return effectiveDate; }
        public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    }
}
