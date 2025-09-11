package com.project.electricitybillgenerator.controller;

import com.project.electricitybillgenerator.dto.request.MeterReadingRequest;
import com.project.electricitybillgenerator.dto.response.BillResponse;
import com.project.electricitybillgenerator.entity.Customer;
import com.project.electricitybillgenerator.service.BillService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@CrossOrigin(origins = "*")
public class BillController {

    @Autowired
    private BillService billService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillResponse> generateBill(@Valid @RequestBody MeterReadingRequest request) {
        BillResponse response = billService.createBillFromReading(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-bills")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<BillResponse>> getMyBills(Authentication authentication) {
        Customer customer = (Customer) authentication.getPrincipal();
        List<BillResponse> bills = billService.getCustomerBills(customer.getId());
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BillResponse>> getCustomerBills(@PathVariable Long customerId) {
        List<BillResponse> bills = billService.getCustomerBills(customerId);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/number/{billNumber}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<BillResponse> getBillByNumber(@PathVariable String billNumber) {
        BillResponse bill = billService.getBillByNumber(billNumber);
        return ResponseEntity.ok(bill);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BillResponse>> getOverdueBills() {
        List<BillResponse> bills = billService.getOverdueBills();
        return ResponseEntity.ok(bills);
    }
}
