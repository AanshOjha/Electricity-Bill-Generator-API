package com.project.electricitybillgenerator.controller;

import com.project.electricitybillgenerator.dto.request.CustomerRegistrationRequest;
import com.project.electricitybillgenerator.dto.request.LoginRequest;
import com.project.electricitybillgenerator.dto.response.AuthResponse;
import com.project.electricitybillgenerator.dto.response.CustomerResponse;
import com.project.electricitybillgenerator.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody CustomerRegistrationRequest request) {
        CustomerResponse response = customerService.registerCustomer(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = customerService.authenticateCustomer(request);
        return ResponseEntity.ok(response);
    }
}
