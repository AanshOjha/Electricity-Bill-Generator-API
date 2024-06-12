package com.project.electricitybillgenerator.controller;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.project.electricitybillgenerator.repository.ReadingRepository;
import com.project.electricitybillgenerator.repository.UserRepository;
import com.project.electricitybillgenerator.service.BillPDFGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.electricitybillgenerator.model.BillReading;
import com.project.electricitybillgenerator.model.BillUser;
import com.project.electricitybillgenerator.service.ReadingService;
import com.project.electricitybillgenerator.service.UserService;

@RestController
@RequestMapping("/bill")
public class BillController {
    @Autowired
    UserService userService;
    @Autowired
    ReadingService readingService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReadingRepository readingRepository;

    // User registration and operations
    @PostMapping("/register")
    public BillUser register(@RequestBody BillUser user) {
        return userService.saveUser(user);
    }

    @GetMapping("/getallusers")
    public List<BillUser> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/deleteuser")
    public ResponseEntity<String> deleteUser(@RequestParam int meterId) {
        userService.deleteUserAndReadings(meterId);
        return ResponseEntity.ok("Successfully deleted user with ID: " + meterId);
    }

    @DeleteMapping("/deleteall")
    public ResponseEntity<String> deleteAllUsers() {
        userService.deleteAllUserAndReadings();
        return ResponseEntity.ok("Deleted all users!");
    }

    @PostMapping("/insertreading")
    public BillReading insertReading(@RequestBody BillReading reading) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // Get meterId & currentMonthReading from user
        reading.setPreviousMonthReading(readingService.previousMonthReading(reading.getMeterId(), date));
        reading.setDate(sdf.format(date));

        // Calculate units consumed and bill
        var currentReading = reading.getCurrentMonthReading();
        var previousReading = reading.getPreviousMonthReading();
        reading.setUnitConsumed(currentReading - previousReading);
        reading.setBillAmount(readingService.calculateBill(currentReading, previousReading));
        return userService.insertReading(reading);
    }

    @GetMapping("/pdf")
    public ResponseEntity<String> generatePDF(@RequestBody BillReading reading) throws FileNotFoundException {
        BillPDFGenerator pdf = new BillPDFGenerator(userRepository, readingRepository);
        pdf.statement(reading);
        return ResponseEntity.ok(pdf.statement(reading));
    }
}