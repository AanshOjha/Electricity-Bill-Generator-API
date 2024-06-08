package com.project.electricitybillgenerator.service;

import java.util.List;
import java.util.Random;

import com.project.electricitybillgenerator.model.BillReading;
import com.project.electricitybillgenerator.repository.ReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.electricitybillgenerator.model.BillUser;
import com.project.electricitybillgenerator.repository.UserRepository;

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

    public BillUser saveUser(BillUser model) {
        int meter_id = rand.nextInt(8999) + 1000;
        model.setMeter_id(meter_id);
        return userRepository.save(model);
    }

    public List<BillUser> getAllUsers() {
        return (List<BillUser>) userRepository.findAll();
    }

    public void deleteUser(int meter_id) {
        userRepository.deleteById(meter_id);
    }

    public void deleteAllUsers() {
        userRepository.deleteAll();
    }

    public BillReading insertReading(BillReading billReading) {
        return readingRepository.save(billReading);
    }
}