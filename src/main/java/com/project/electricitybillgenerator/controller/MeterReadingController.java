package com.project.electricitybillgenerator.controller;

import com.project.electricitybillgenerator.dto.request.MeterReadingRequest;
import com.project.electricitybillgenerator.entity.Customer;
import com.project.electricitybillgenerator.entity.Meter;
import com.project.electricitybillgenerator.entity.MeterReading;
import com.project.electricitybillgenerator.repository.MeterReadingRepository;
import com.project.electricitybillgenerator.repository.MeterRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/meter-readings")
@CrossOrigin(origins = "*")
public class MeterReadingController {

    @Autowired
    private MeterRepository meterRepository;

    @Autowired
    private MeterReadingRepository meterReadingRepository;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> submitMeterReading(@Valid @RequestBody UserMeterReadingRequest request, 
                                                    Authentication authentication) {
        Customer customer = (Customer) authentication.getPrincipal();
        
        // Find customer's meter
        Meter meter = meterRepository.findByCustomerId(customer.getId()).stream()
            .filter(m -> m.getStatus() == Meter.MeterStatus.ACTIVE)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No active meter found for customer"));

        // Check if reading already exists for this date
        if (meterReadingRepository.existsByMeterIdAndReadingDate(meter.getId(), request.getReadingDate())) {
            throw new RuntimeException("Reading already submitted for this date");
        }

        // Get previous reading
        MeterReading previousReading = meterReadingRepository.findLatestByMeterId(meter.getId())
                .orElse(null);

        // Create new meter reading
        MeterReading meterReading = new MeterReading();
        meterReading.setMeter(meter);
        meterReading.setCurrentReading(request.getCurrentReading());
        meterReading.setReadingDate(request.getReadingDate());

        if (previousReading != null) {
            meterReading.setPreviousReading(previousReading.getCurrentReading());
        } else {
            meterReading.setPreviousReading(BigDecimal.ZERO);
        }

        // Validate current reading is not less than previous
        if (meterReading.getCurrentReading().compareTo(meterReading.getPreviousReading()) < 0) {
            throw new RuntimeException("Current reading cannot be less than previous reading");
        }

        meterReadingRepository.save(meterReading);
        return ResponseEntity.ok("Meter reading submitted successfully. Admin will generate your bill.");
    }

    @GetMapping("/my-readings")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<MeterReadingResponse>> getMyReadings(Authentication authentication) {
        Customer customer = (Customer) authentication.getPrincipal();
        
        List<Meter> meters = meterRepository.findByCustomerId(customer.getId());
        List<MeterReading> readings = meters.stream()
            .flatMap(meter -> meterReadingRepository.findByMeterIdOrderByReadingDateDesc(meter.getId()).stream())
            .collect(Collectors.toList());
            
        List<MeterReadingResponse> response = readings.stream()
            .map(MeterReadingResponse::new)
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending-bills")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MeterReadingResponse>> getPendingBillReadings() {
        // This would need a more complex query to find readings without bills
        // For now, return all readings from last 30 days
        List<MeterReading> allReadings = meterReadingRepository.findAll();
        List<MeterReadingResponse> response = allReadings.stream()
            .map(MeterReadingResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // DTOs
    public static class UserMeterReadingRequest {
        private BigDecimal currentReading;
        private java.time.LocalDate readingDate;

        public BigDecimal getCurrentReading() { return currentReading; }
        public void setCurrentReading(BigDecimal currentReading) { this.currentReading = currentReading; }

        public java.time.LocalDate getReadingDate() { return readingDate; }
        public void setReadingDate(java.time.LocalDate readingDate) { this.readingDate = readingDate; }
    }

    public static class MeterReadingResponse {
        private Long id;
        private String meterNumber;
        private BigDecimal currentReading;
        private BigDecimal previousReading;
        private BigDecimal unitsConsumed;
        private java.time.LocalDate readingDate;
        private java.time.LocalDateTime createdAt;

        public MeterReadingResponse(MeterReading reading) {
            this.id = reading.getId();
            this.meterNumber = reading.getMeter().getMeterNumber();
            this.currentReading = reading.getCurrentReading();
            this.previousReading = reading.getPreviousReading();
            this.unitsConsumed = reading.getUnitsConsumed();
            this.readingDate = reading.getReadingDate();
            this.createdAt = reading.getCreatedAt();
        }

        // Getters
        public Long getId() { return id; }
        public String getMeterNumber() { return meterNumber; }
        public BigDecimal getCurrentReading() { return currentReading; }
        public BigDecimal getPreviousReading() { return previousReading; }
        public BigDecimal getUnitsConsumed() { return unitsConsumed; }
        public java.time.LocalDate getReadingDate() { return readingDate; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    }
}
