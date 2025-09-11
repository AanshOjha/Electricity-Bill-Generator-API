package com.project.electricitybillgenerator.controller;

import com.project.electricitybillgenerator.entity.Customer;
import com.project.electricitybillgenerator.entity.Meter;
import com.project.electricitybillgenerator.entity.MeterReading;
import com.project.electricitybillgenerator.repository.CustomerRepository;
import com.project.electricitybillgenerator.repository.MeterRepository;
import com.project.electricitybillgenerator.repository.MeterReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/admin/debug")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class DebugController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MeterRepository meterRepository;

    @Autowired
    private MeterReadingRepository meterReadingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @PostMapping("/seed-data")
    public ResponseEntity<String> seedDatabase() {
        try {
            // 1. Create sample customers if they don't exist
            Customer userA = createSampleCustomer("user.a@example.com", "John Doe", "123 Main St, City", "1234567890");
            Customer userB = createSampleCustomer("user.b@example.com", "Jane Smith", "456 Oak Ave, Town", "0987654321");
            Customer userC = createSampleCustomer("user.c@example.com", "Bob Johnson", "789 Pine Rd, Village", "5555551234");

            // 2. Generate historical readings for the users
            generateReadingsForCustomer(userA);
            generateReadingsForCustomer(userB);
            generateReadingsForCustomer(userC);

            return ResponseEntity.ok("Sample customers and meter readings have been seeded successfully. " +
                    "Created customers: " + userA.getEmail() + ", " + userB.getEmail() + ", " + userC.getEmail());

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error seeding data: " + e.getMessage());
        }
    }

    @PostMapping("/seed-readings/{customerId}")
    public ResponseEntity<String> seedReadingsForCustomer(@PathVariable Long customerId) {
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

            generateReadingsForCustomer(customer);

            return ResponseEntity.ok("Historical readings generated for customer: " + customer.getName());

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error seeding readings: " + e.getMessage());
        }
    }

    @DeleteMapping("/clear-test-data")
    public ResponseEntity<String> clearTestData() {
        try {
            // Delete test customers (those with example.com emails)
            customerRepository.deleteAll(
                customerRepository.findAll().stream()
                    .filter(customer -> customer.getEmail().contains("example.com"))
                    .toList()
            );

            return ResponseEntity.ok("Test data cleared successfully");

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error clearing test data: " + e.getMessage());
        }
    }

    @GetMapping("/test-customers")
    public ResponseEntity<Object> getTestCustomers() {
        try {
            List<Customer> testCustomers = customerRepository.findAll().stream()
                    .filter(customer -> customer.getEmail().contains("example.com"))
                    .toList();

            if (testCustomers.isEmpty()) {
                return ResponseEntity.ok("No test customers found. Use /seed-data to create some.");
            }

            // Create simplified response
            List<Map<String, Object>> customerInfo = testCustomers.stream()
                    .map(customer -> {
                        List<Meter> meters = meterRepository.findByCustomerId(customer.getId());
                        return Map.of(
                                "id", customer.getId(),
                                "name", customer.getName(),
                                "email", customer.getEmail(),
                                "meterNumbers", meters.stream().map(Meter::getMeterNumber).toList(),
                                "totalReadings", meters.stream()
                                        .mapToLong(meter -> meterReadingRepository.findByMeterIdOrderByReadingDateDesc(meter.getId()).size())
                                        .sum()
                        );
                    })
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "message", "Found " + testCustomers.size() + " test customer(s)",
                    "customers", customerInfo
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error retrieving test customers: " + e.getMessage());
        }
    }

    private Customer createSampleCustomer(String email, String name, String address, String phone) {
        // Check if customer already exists
        if (customerRepository.existsByEmail(email)) {
            return customerRepository.findByEmailAndActive(email, true)
                    .orElseThrow(() -> new RuntimeException("Customer exists but is not active"));
        }

        // Create new customer
        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setPassword(passwordEncoder.encode("password123")); // Default password for test users
        customer.setAddress(address);
        customer.setPhone(phone);
        customer.setCustomerType(Customer.CustomerType.RESIDENTIAL);
        customer.setRole(Customer.Role.ROLE_USER);
        customer.setActive(true);

        Customer savedCustomer = customerRepository.save(customer);

        // Create meter for the customer
        createMeterForCustomer(savedCustomer);

        return savedCustomer;
    }

    private void createMeterForCustomer(Customer customer) {
        // Check if customer already has a meter
        List<Meter> existingMeters = meterRepository.findByCustomerId(customer.getId());
        if (!existingMeters.isEmpty()) {
            return; // Customer already has a meter
        }

        String meterNumber = generateMeterNumber();
        while (meterRepository.existsByMeterNumber(meterNumber)) {
            meterNumber = generateMeterNumber();
        }

        Meter meter = new Meter();
        meter.setMeterNumber(meterNumber);
        meter.setCustomer(customer);
        meter.setMeterType(Meter.MeterType.DIGITAL);
        meter.setStatus(Meter.MeterStatus.ACTIVE);

        meterRepository.save(meter);
    }

    private void generateReadingsForCustomer(Customer customer) {
        // Get the customer's meter
        List<Meter> meters = meterRepository.findByCustomerId(customer.getId());
        if (meters.isEmpty()) {
            throw new RuntimeException("No meter found for customer: " + customer.getName());
        }
        Meter meter = meters.get(0); // Use the first meter

        double currentReading = 10000 + (random.nextDouble() * 5000); // Starting reading between 10000-15000
        LocalDate date = LocalDate.now().minusMonths(8); // Start 8 months ago

        // Generate readings for the last 8 months
        for (int i = 0; i < 8; i++) {
            // Check if reading already exists for this date
            if (!meterReadingRepository.existsByMeterIdAndReadingDate(meter.getId(), date)) {
                // Add consumption between 150 and 500 units per month
                double consumption = 150 + (random.nextDouble() * 350);
                currentReading += consumption;

                MeterReading reading = new MeterReading();
                reading.setMeter(meter);
                reading.setCurrentReading(BigDecimal.valueOf(currentReading));
                reading.setReadingDate(date);
                reading.setReadingType(MeterReading.ReadingType.MANUAL);

                // Set previous reading if this is not the first reading
                if (i > 0) {
                    reading.setPreviousReading(BigDecimal.valueOf(currentReading - consumption));
                    reading.setUnitsConsumed(BigDecimal.valueOf(consumption));
                } else {
                    reading.setPreviousReading(BigDecimal.valueOf(currentReading - consumption));
                    reading.setUnitsConsumed(BigDecimal.valueOf(consumption));
                }

                meterReadingRepository.save(reading);
            }

            // Move to the next month
            date = date.plusMonths(1);
        }
    }

    private String generateMeterNumber() {
        return "TEST" + String.format("%06d", random.nextInt(999999));
    }
}
