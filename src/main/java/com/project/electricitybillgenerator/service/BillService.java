package com.project.electricitybillgenerator.service;

import com.project.electricitybillgenerator.dto.request.MeterReadingRequest;
import com.project.electricitybillgenerator.dto.response.BillResponse;
import com.project.electricitybillgenerator.entity.*;
import com.project.electricitybillgenerator.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional
public class BillService {

    @Autowired
    private MeterRepository meterRepository;

    @Autowired
    private MeterReadingRepository meterReadingRepository;

    @Autowired
    private TariffRepository tariffRepository;

    @Autowired
    private BillRepository billRepository;

    private final Random random = new Random();

    public BillResponse createBillFromReading(MeterReadingRequest request) {
        // Validate meter exists
        Meter meter = meterRepository.findById(request.getMeterId())
                .orElseThrow(() -> new RuntimeException("Meter not found"));

        // Check if reading already exists for this date
        if (meterReadingRepository.existsByMeterIdAndReadingDate(request.getMeterId(), request.getReadingDate())) {
            throw new RuntimeException("Reading already exists for this date");
        }

        // Get previous reading
        MeterReading previousReading = meterReadingRepository.findLatestByMeterId(request.getMeterId())
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

        MeterReading savedReading = meterReadingRepository.save(meterReading);

        // Get applicable tariff
        Tariff tariff = tariffRepository.findActiveByCustomerTypeAndDate(
                meter.getCustomer().getCustomerType(), request.getReadingDate())
                .orElseThrow(() -> new RuntimeException("No active tariff found for customer type"));

        // Create bill
        Bill bill = new Bill();
        bill.setBillNumber(generateBillNumber());
        bill.setMeter(meter);
        bill.setMeterReading(savedReading);
        bill.setTariff(tariff);
        bill.setBillDate(request.getReadingDate());
        bill.setDueDate(request.getReadingDate().plusDays(30)); // 30 days to pay
        bill.setUnitsConsumed(savedReading.getUnitsConsumed());

        Bill savedBill = billRepository.save(bill);
        return new BillResponse(savedBill);
    }

    public List<BillResponse> getCustomerBills(Long customerId) {
        return billRepository.findByMeterCustomerIdOrderByBillDateDesc(customerId)
                .stream()
                .map(BillResponse::new)
                .collect(Collectors.toList());
    }

    public BillResponse getBillByNumber(String billNumber) {
        Bill bill = billRepository.findByBillNumber(billNumber)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        return new BillResponse(bill);
    }

    public List<BillResponse> getOverdueBills() {
        return billRepository.findByStatusAndDueDateBefore(Bill.BillStatus.PENDING, LocalDate.now())
                .stream()
                .map(BillResponse::new)
                .collect(Collectors.toList());
    }

    public List<BillResponse> getAllBills() {
        return billRepository.findAll()
                .stream()
                .map(BillResponse::new)
                .collect(Collectors.toList());
    }

    public List<BillResponse> getBillsByStatus(Bill.BillStatus status) {
        return billRepository.findAll()
                .stream()
                .filter(bill -> bill.getStatus() == status)
                .map(BillResponse::new)
                .collect(Collectors.toList());
    }

    private String generateBillNumber() {
        return "BILL" + System.currentTimeMillis() + String.format("%04d", random.nextInt(9999));
    }
}
