package com.project.electricitybillgenerator.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.project.electricitybillgenerator.repository.UserRepository;
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
    private UserRepository userRepository;

    // User registration and operations
    @PostMapping("/register")
    public BillUser register(@RequestBody BillUser user) {
        return userService.saveUser(user);
    }

    @GetMapping("/getallusers")
    public List<BillUser> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/deleteuser")
    public void deleteUser(@RequestParam int meter_id) {
        userService.deleteUser(meter_id);
    }

    @DeleteMapping("/deleteall")
    public ResponseEntity<String> deleteAllUsers() {
        userService.deleteAllUsers();
        return ResponseEntity.ok("Deleted all users!");
    }

    @PostMapping("/insertreading")
    public BillReading insertReading(@RequestBody BillReading reading) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // Get meter_id & currentMonthReading from user
        reading.setPreviousMonthReading(readingService.previousMonthReading(reading.getMeter_id(), date));
        reading.setDate(sdf.format(date).toString());

        // Calculate units consumed and bill
        reading.setUnitConsumed(reading.getCurrentMonthReading() - reading.getPreviousMonthReading());
        reading.setBillAmount(reading.getUnitConsumed()*7.5);
        return userService.insertReading(reading);
    }
}