package com.project.electricitybillgenerator.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.electricitybillgenerator.dto.UserRegistrationRequest;
import com.project.electricitybillgenerator.dto.LoginRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple H2 Integration Test - Working Docker-free alternative
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class H2IntegrationTestSimple {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_NAME = "Test User";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_ADDRESS = "123 Test Street, Test City";
    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Should register a new user successfully")
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
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() throws Exception {
        // First register a user
        registerTestUser();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    @Order(3)
    @DisplayName("Should reject duplicate registration")
    void shouldRejectDuplicateRegistration() throws Exception {
        // First register a user
        registerTestUser();

        // Try to register again with same email
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName("Another Name");
        request.setEmail(TEST_EMAIL); // Same email
        request.setAddress("Different Address");
        request.setPassword("differentpassword");

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @Order(4)
    @DisplayName("Should reject unauthorized requests")
    void shouldRejectUnauthorizedRequests() throws Exception {
        mockMvc.perform(get("/api/v1/bills")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(5) 
    @DisplayName("Should validate request data")
    @WithMockUser(roles = "USER")
    void shouldValidateRequestData() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName(""); // Invalid empty name
        request.setEmail("invalid-email"); // Invalid email format
        request.setAddress(""); // Invalid empty address
        request.setPassword("123"); // Too short password

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    // Helper method
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
}
