package com.project.electricitybillgenerator.controller;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.project.electricitybillgenerator.model.CombinedClass;
import com.project.electricitybillgenerator.repository.ReadingRepository;
import com.project.electricitybillgenerator.repository.UserRepository;
import com.project.electricitybillgenerator.service.BillPDFGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    // Insert Reading
    @PostMapping("/insertreading")
    public ResponseEntity<String> insertReading(@RequestBody CombinedClass data) {
        BillUser billUser = new BillUser();
        BillReading reading = new BillReading();

        // Assign values to billuser and reading from entered data
        billUser.setPassword(data.getPassword());
        billUser.setMeterId(data.getMeterId());
        reading.setCurrentMonthReading(data.getCurrentMonthReading());
        reading.setMeterId(data.getMeterId());

        // Check if meter Id and password are correct
        var userExistsWithCreds = userService.checkCredentials(billUser.getMeterId(), billUser.getPassword());

        // If user not exists
        if (!userExistsWithCreds) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials. Try again!");
        }

        // User exists, continue inserting readings ==================
        // Assigning current date to date of BillReading
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        reading.setDate(sdf.format(date));

        // Get and Set previousMonthReading using meter_id and date
        reading.setPreviousMonthReading(readingService.previousMonthReading(reading.getMeterId(), date));

        // Check if this month reading already exists
        var optionalReading = readingRepository.findByMeterIdAndDateTest(
                reading.getMeterId(), reading.getDate());

        if (optionalReading.isPresent()) {
            String dateFromDB = optionalReading.get().getDate();
            if (Objects.equals(reading.getDate(), dateFromDB)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("This month reading already exists. Try again!");
            }
        }

        // Calculate units consumed and bill
        var currentReading = reading.getCurrentMonthReading();
        var previousReading = reading.getPreviousMonthReading();
        reading.setUnitConsumed(currentReading - previousReading);
        reading.setBillAmount(readingService.calculateBill(currentReading, previousReading));

        // Insert in DB
        userService.insertReading(reading);
        return ResponseEntity.ok("Successfully inserted readings!");
    }

    @GetMapping("/pdf")
    public ResponseEntity<String> generatePDF(@RequestBody CombinedClass data) throws FileNotFoundException {
        BillPDFGenerator pdf = new BillPDFGenerator(userRepository, readingRepository);
        BillReading reading = new BillReading();
        BillUser billUser = new BillUser();

        // Assign values to billuser and reading from entered data
        billUser.setPassword(data.getPassword());
        billUser.setMeterId(data.getMeterId());
        reading.setMeterId(data.getMeterId());

        // Date in form of yyyy-MM
        reading.setDate(data.getDate());

        // Check if meter id and password is correct
        boolean checkCreds = userService.checkCredentials(billUser.getMeterId(), billUser.getPassword());

        if (!checkCreds) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials. Try again!");
        }
        return pdf.generatePDF(reading);
    }

    @GetMapping("/getallusers")
    public List<BillUser> getAllUsers() {
        return userService.getAllUsers();
    }

    // meterId, password and date
    @DeleteMapping("/deletereading")
    public ResponseEntity<String> deleteReading(@RequestBody CombinedClass data) {
        BillReading reading = new BillReading();
        BillUser billUser = new BillUser();

        // Assign values to billuser and reading from entered data
        billUser.setPassword(data.getPassword());
        billUser.setMeterId(data.getMeterId());
        reading.setMeterId(data.getMeterId());

        // Date in form of yyyy-MM
        reading.setDate(data.getDate());

        // Check if meter id and password is correct
        boolean checkCreds = userService.checkCredentials(billUser.getMeterId(), billUser.getPassword());

        if (!checkCreds) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials. Try again!");
        }
        return readingService.deleteReadingWithDate(reading.getMeterId(), reading.getDate());
    }

    @DeleteMapping("/deleteuser")
    public ResponseEntity<String> deleteUser(@RequestParam int meterId) {
        return userService.deleteUserAndReadings(meterId);
    }

    @DeleteMapping("/deleteall")
    public ResponseEntity<String> deleteAllUsers() {
        return userService.deleteAllUserAndReadings();
    }
}