package com.project.electricitybillgenerator.service;

import com.project.electricitybillgenerator.dto.request.CustomerRegistrationRequest;
import com.project.electricitybillgenerator.dto.request.CustomerUpdateRequest;
import com.project.electricitybillgenerator.dto.request.LoginRequest;
import com.project.electricitybillgenerator.dto.response.AuthResponse;
import com.project.electricitybillgenerator.dto.response.CustomerResponse;
import com.project.electricitybillgenerator.entity.Customer;
import com.project.electricitybillgenerator.entity.Meter;
import com.project.electricitybillgenerator.repository.CustomerRepository;
import com.project.electricitybillgenerator.repository.MeterRepository;
import com.project.electricitybillgenerator.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerService implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MeterRepository meterRepository;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private final Random random = new Random();

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return customerRepository.findByEmailAndActive(email, true)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found: " + email));
    }

    public CustomerResponse registerCustomer(CustomerRegistrationRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setAddress(request.getAddress());
        customer.setPhone(request.getPhone());
        customer.setCustomerType(request.getCustomerType());
        customer.setRole(request.getRole() != null ? request.getRole() : Customer.Role.ROLE_USER);

        Customer savedCustomer = customerRepository.save(customer);

        // Auto-create a meter for the customer
        createMeterForCustomer(savedCustomer);

        return new CustomerResponse(savedCustomer);
    }

    public AuthResponse authenticateCustomer(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Customer customer = customerRepository.findByEmailAndActive(request.getEmail(), true)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found"));

        String token = jwtTokenUtil.generateToken(customer);
        return new AuthResponse(token, new CustomerResponse(customer));
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(CustomerResponse::new)
                .collect(Collectors.toList());
    }

    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return new CustomerResponse(customer);
    }

    public CustomerResponse updateProfile(Long customerId, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        customer.setName(request.getName());
        customer.setAddress(request.getAddress());
        customer.setPhone(request.getPhone());
        
        Customer updatedCustomer = customerRepository.save(customer);
        return new CustomerResponse(updatedCustomer);
    }

    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Customer not found");
        }
        customerRepository.deleteById(id);
    }

    private void createMeterForCustomer(Customer customer) {
        String meterNumber = generateMeterNumber();
        while (meterRepository.existsByMeterNumber(meterNumber)) {
            meterNumber = generateMeterNumber();
        }

        Meter meter = new Meter();
        meter.setMeterNumber(meterNumber);
        meter.setCustomer(customer);
        meterRepository.save(meter);
    }

    private String generateMeterNumber() {
        return "MTR" + String.format("%08d", random.nextInt(99999999));
    }
}
