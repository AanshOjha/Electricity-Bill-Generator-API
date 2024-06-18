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
        if (userExistsWithCreds==false) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials. Try again!");
        }

        // User exists, continue inserting readings
        // Assigning current date to date of BillReading
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        reading.setDate(sdf.format(date));

        // Get and Set previousMonthReading using meter_id and date
        reading.setPreviousMonthReading(readingService.previousMonthReading(reading.getMeterId(), date));

        // Check if this month reading exists
        var optionalReading = readingRepository.findByMeterIdAndDate(
                reading.getMeterId(), reading.getDate());

        if (optionalReading.isPresent()) {
            String dateFromDB = optionalReading.get().getDate();
            System.out.println("Dates: " + reading.getDate() + " and " + dateFromDB);
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
        billUser.setPassword(data.getPassword());
        billUser.setMeterId(data.getMeterId());
        System.out.println("Meter id obtained: "+data.getMeterId());
        System.out.println("Date obtained: "+data.getDate());
        reading.setMeterId(data.getMeterId());
        reading.setDate(data.getDate());

        var checkCreds = userService.checkCredentials(billUser.getMeterId(), billUser.getPassword());

        if (checkCreds==false) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials. Try again!");
        }
        return ResponseEntity.ok(pdf.statement(reading));
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
}