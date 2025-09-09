package com.project.electricitybillgenerator.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.electricitybillgenerator.dto.UserRegistrationRequest;
import com.project.electricitybillgenerator.dto.LoginRequest;
import com.project.electricitybillgenerator.dto.BillReadingRequest;
import com.project.electricitybillgenerator.model.BillUser;
import com.project.electricitybillgenerator.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * H2 Integration Test - Comprehensive testing without Docker requirements
 * 
 * This test class provides complete integration testing using H2 in-memory database
 * instead of Testcontainers, making it suitable for environments without Docker.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class H2IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String jwtToken;
    private static final String TEST_EMAIL = "test@h2integration.com";
    private static final String TEST_PASSWORD = "testPassword123";
    private static final String TEST_NAME = "Test User";
    private static final String TEST_ADDRESS = "123 Test Street, Test City";

    @BeforeEach
    void setUp() {
        // Clean up before each test
        userRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("User Registration - Should successfully register a new user")
    void shouldRegisterNewUser() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName(TEST_NAME);
        request.setEmail(TEST_EMAIL);
        request.setAddress(TEST_ADDRESS);
        request.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.meterId").exists());

        // Verify user was created in database
        BillUser savedUser = userRepository.findByEmail(TEST_EMAIL).orElse(null);
        assertNotNull(savedUser);
        assertEquals(TEST_NAME, savedUser.getName());
        assertEquals(TEST_EMAIL, savedUser.getEmail());
        assertTrue(passwordEncoder.matches(TEST_PASSWORD, savedUser.getPassword()));
    }

    @Test
    @Order(2)
    @DisplayName("User Login - Should authenticate valid credentials")
    void shouldAuthenticateValidUser() throws Exception {
        // First register a user
        registerTestUser();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andReturn();

        // Extract JWT token for subsequent tests
        String response = result.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(response).get("token").asText();
        assertNotNull(jwtToken);
        assertFalse(jwtToken.isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("Bill Reading Submission - Should submit reading with valid JWT")
    void shouldSubmitBillReading() throws Exception {
        // Setup user and get JWT token
        String token = registerAndLogin();

        BillReadingRequest readingRequest = new BillReadingRequest();
        readingRequest.setCurrentMonthReading(1500.50);
        readingRequest.setDate(LocalDate.now());

        mockMvc.perform(post("/api/v1/bills/readings")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(readingRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Bill reading submitted successfully"))
                .andExpect(jsonPath("$.readingId").exists());
    }

    @Test
    @Order(4)
    @DisplayName("Bill Generation - Should generate bill for user with readings")
    void shouldGenerateBill() throws Exception {
        // Setup user, login, and submit reading
        String token = registerAndLogin();
        submitTestReading(token);

        mockMvc.perform(post("/api/v1/bills/generate")
                .header("Authorization", "Bearer " + token)
                .param("billingPeriodStart", "2024-01-01")
                .param("billingPeriodEnd", "2024-01-31"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Bill generated successfully"))
                .andExpect(jsonPath("$.billId").exists())
                .andExpect(jsonPath("$.amount").exists())
                .andExpect(jsonPath("$.dueDate").exists());
    }

    @Test
    @Order(5)
    @DisplayName("Unauthorized Access - Should reject requests without JWT")
    void shouldRejectUnauthorizedRequests() throws Exception {
        BillReadingRequest readingRequest = new BillReadingRequest();
        readingRequest.setCurrentMonthReading(1000.0);
        readingRequest.setDate(LocalDate.now());

        mockMvc.perform(post("/api/v1/bills/readings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(readingRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(6)
    @DisplayName("Validation Errors - Should return 400 for invalid data")
    void shouldValidateRequestData() throws Exception {
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest();
        invalidRequest.setName(""); // Invalid: empty name
        invalidRequest.setEmail("invalid-email"); // Invalid: bad format
        invalidRequest.setAddress(""); // Invalid: empty address
        invalidRequest.setPassword("123"); // Invalid: too short

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    @Order(7)
    @DisplayName("Duplicate Registration - Should prevent duplicate email registration")
    void shouldPreventDuplicateRegistration() throws Exception {
        // Register user first time
        registerTestUser();

        // Try to register same email again
        UserRegistrationRequest duplicateRequest = new UserRegistrationRequest();
        duplicateRequest.setName("Another User");
        duplicateRequest.setEmail(TEST_EMAIL); // Same email
        duplicateRequest.setAddress("Different Address");
        duplicateRequest.setPassword("differentPassword123");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Registration Error"))
                .andExpect(jsonPath("$.message").value("User with this email already exists"));
    }

    @Test
    @Order(8)
    @DisplayName("Invalid Login - Should reject invalid credentials")
    void shouldRejectInvalidCredentials() throws Exception {
        // Register a user first
        registerTestUser();

        LoginRequest invalidLogin = new LoginRequest();
        invalidLogin.setEmail(TEST_EMAIL);
        invalidLogin.setPassword("wrongPassword");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication Error"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    // Helper methods
    private void registerTestUser() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName(TEST_NAME);
        request.setEmail(TEST_EMAIL);
        request.setAddress(TEST_ADDRESS);
        request.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private String registerAndLogin() throws Exception {
        registerTestUser();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private void submitTestReading(String token) throws Exception {
        BillReadingRequest readingRequest = new BillReadingRequest();
        readingRequest.setCurrentMonthReading(1200.75);
        readingRequest.setDate(LocalDate.now());

        mockMvc.perform(post("/api/v1/bills/readings")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(readingRequest)))
                .andExpect(status().isCreated());
    }
}
