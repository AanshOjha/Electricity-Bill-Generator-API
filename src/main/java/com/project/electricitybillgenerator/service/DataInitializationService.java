package com.project.electricitybillgenerator.service;

import com.project.electricitybillgenerator.entity.Customer;
import com.project.electricitybillgenerator.entity.Meter;
import com.project.electricitybillgenerator.entity.Tariff;
import com.project.electricitybillgenerator.repository.CustomerRepository;
import com.project.electricitybillgenerator.repository.MeterRepository;
import com.project.electricitybillgenerator.repository.TariffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

@Component
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private TariffRepository tariffRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private MeterRepository meterRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultAdmin();
        initializeTariffs();
    }

    private void initializeDefaultAdmin() {
        String adminEmail = "admin@electricitybill.com";
        
        if (!customerRepository.findByEmail(adminEmail).isPresent()) {
            // Create default admin customer
            Customer admin = new Customer();
            admin.setName("System Administrator");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setAddress("System Default Address");
            admin.setPhone("0000000000");
            admin.setCustomerType(Customer.CustomerType.RESIDENTIAL);
            admin.setRole(Customer.Role.ROLE_ADMIN);
            admin.setActive(true);
            admin.setCreatedAt(LocalDateTime.now());
            
            Customer savedAdmin = customerRepository.save(admin);
            
            // Create a meter for the admin (optional, but follows the pattern)
            Meter adminMeter = new Meter();
            adminMeter.setMeterNumber("ADMIN-" + (1000 + random.nextInt(9000)));
            adminMeter.setCustomer(savedAdmin);
            adminMeter.setMeterType(Meter.MeterType.DIGITAL);
            adminMeter.setStatus(Meter.MeterStatus.ACTIVE);
            adminMeter.setInstallationDate(LocalDateTime.now());
            
            meterRepository.save(adminMeter);
            
            System.out.println("✅ Default admin account created successfully!");
            System.out.println("   📧 Email: " + adminEmail);
            System.out.println("   🔑 Password: admin123");
            System.out.println("   👑 Role: ADMIN");
        } else {
            System.out.println("ℹ️ Default admin account already exists.");
        }
    }

    private void initializeTariffs() {
        if (tariffRepository.count() == 0) {
            // Residential Tariff
            Tariff residentialTariff = new Tariff();
            residentialTariff.setName("Residential Standard");
            residentialTariff.setCustomerType(Customer.CustomerType.RESIDENTIAL);
            residentialTariff.setRatePerUnit(new BigDecimal("7.50"));
            residentialTariff.setFixedCharge(new BigDecimal("50.00"));
            residentialTariff.setMinimumCharge(new BigDecimal("100.00"));
            residentialTariff.setEffectiveDate(LocalDate.now().minusYears(1));
            tariffRepository.save(residentialTariff);

            // Commercial Tariff
            Tariff commercialTariff = new Tariff();
            commercialTariff.setName("Commercial Standard");
            commercialTariff.setCustomerType(Customer.CustomerType.COMMERCIAL);
            commercialTariff.setRatePerUnit(new BigDecimal("9.00"));
            commercialTariff.setFixedCharge(new BigDecimal("100.00"));
            commercialTariff.setMinimumCharge(new BigDecimal("200.00"));
            commercialTariff.setEffectiveDate(LocalDate.now().minusYears(1));
            tariffRepository.save(commercialTariff);

            // Industrial Tariff
            Tariff industrialTariff = new Tariff();
            industrialTariff.setName("Industrial Standard");
            industrialTariff.setCustomerType(Customer.CustomerType.INDUSTRIAL);
            industrialTariff.setRatePerUnit(new BigDecimal("6.50"));
            industrialTariff.setFixedCharge(new BigDecimal("200.00"));
            industrialTariff.setMinimumCharge(new BigDecimal("500.00"));
            industrialTariff.setEffectiveDate(LocalDate.now().minusYears(1));
            tariffRepository.save(industrialTariff);

            System.out.println("✅ Default tariffs initialized successfully!");
        } else {
            System.out.println("ℹ️ Default tariffs already exist.");
        }
    }
}
