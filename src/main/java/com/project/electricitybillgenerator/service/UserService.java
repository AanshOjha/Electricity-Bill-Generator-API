package com.project.electricitybillgenerator.service;

import java.util.List;
import java.util.Random;

import com.project.electricitybillgenerator.model.BillReading;
import com.project.electricitybillgenerator.repository.ReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.electricitybillgenerator.model.BillUser;
import com.project.electricitybillgenerator.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService{
    @Autowired
    UserRepository userRepository;

    private final ReadingRepository readingRepository;

    @Autowired
    public UserService(ReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }

    Random rand = new Random();

    public boolean checkCredentials(int meterId, String password) {
        var user = userRepository.findByMeterIdAndPassword(meterId, password);
        return user.isPresent();
    }

    public BillUser saveUser(BillUser model) {
        int meterId = rand.nextInt(8999) + 1000;
        model.setMeterId(meterId);
        return userRepository.save(model);
    }

    public List<BillUser> getAllUsers() {
        return (List<BillUser>) userRepository.findAll();
    }

    @Transactional
    public ResponseEntity<String> deleteUserAndReadings(int meterId) {
        readingRepository.deleteByMeterId(meterId);
        userRepository.deleteByMeterId(meterId);
        return ResponseEntity.ok("Successfully deleted user with ID: " + meterId);
    }

    @Transactional
    public ResponseEntity<String> deleteAllUserAndReadings() {
        readingRepository.deleteAll();
        userRepository.deleteAll();
        return ResponseEntity.ok("Deleted all users!");
    }

    public BillReading insertReading(BillReading billReading) {
        return readingRepository.save(billReading);
    }
}