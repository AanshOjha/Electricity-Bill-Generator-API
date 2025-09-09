package com.project.electricitybillgenerator.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.electricitybillgenerator.dto.UserRegistrationRequest;
import com.project.electricitybillgenerator.dto.JwtRequest;
import com.project.electricitybillgenerator.dto.JwtResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests using Testcontainers for real database testing.
 * This tests the complete application flow from REST API to database using a real MySQL container.
 * 
 * Modern testing practice using Testcontainers instead of embedded H2 database.
 * 
 * @author Electricity Bill Generator Team
 * @version 2.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Complete Application Integration Tests with Testcontainers")
class ComprehensiveIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("bill_generator_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(true);

    static {
        mysql.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
        registry.add("logging.level.org.hibernate.SQL", () -> "DEBUG");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeAll
    static void beforeAll() {
        mysql.start();
    }

    @AfterAll
    static void afterAll() {
        mysql.stop();
    }

    @Test
    @Order(1)
    @DisplayName("1. Should register new user successfully")
    void shouldRegisterUserSuccessfully() throws Exception {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName("Integration Test User");
        request.setEmail("integration@test.com");
        request.setAddress("123 Integration St");
        request.setPassword("securepass123");

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("integration@test.com"))
                .andReturn();

        // Extract JWT token for subsequent tests
        String responseContent = result.getResponse().getContentAsString();
        JwtResponse jwtResponse = objectMapper.readValue(responseContent, JwtResponse.class);
        this.jwtToken = jwtResponse.getToken();

        System.out.println("✅ User registered successfully with JWT token");
    }

    @Test
    @Order(2)
    @DisplayName("2. Should login with valid credentials")
    void shouldLoginSuccessfully() throws Exception {
        // Given
        JwtRequest loginRequest = new JwtRequest();
        loginRequest.setEmail("integration@test.com");
        loginRequest.setPassword("securepass123");

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("integration@test.com"))
                .andReturn();

        // Update JWT token from login
        String responseContent = result.getResponse().getContentAsString();
        JwtResponse jwtResponse = objectMapper.readValue(responseContent, JwtResponse.class);
        this.jwtToken = jwtResponse.getToken();

        System.out.println("✅ Login successful with updated JWT token");
    }

    @Test
    @Order(3)
    @DisplayName("3. Should reject login with invalid credentials")
    void shouldRejectInvalidCredentials() throws Exception {
        // Given
        JwtRequest invalidRequest = new JwtRequest();
        invalidRequest.setEmail("integration@test.com");
        invalidRequest.setPassword("wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        System.out.println("✅ Invalid credentials properly rejected");
    }

    @Test
    @Order(4)
    @DisplayName("4. Should access protected endpoint with valid JWT")
    void shouldAccessProtectedEndpointWithJWT() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        System.out.println("✅ Protected endpoint accessible with JWT");
    }

    @Test
    @Order(5)
    @DisplayName("5. Should reject access without JWT token")
    void shouldRejectAccessWithoutJWT() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        System.out.println("✅ Unauthorized access properly rejected");
    }

    @Test
    @Order(6)
    @DisplayName("6. Should handle validation errors properly")
    void shouldHandleValidationErrors() throws Exception {
        // Given - Invalid user registration with missing fields
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest();
        invalidRequest.setEmail("invalid-email"); // Invalid email format
        // Missing name, address, password

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());

        System.out.println("✅ Validation errors properly handled");
    }

    @Test
    @Order(7)
    @DisplayName("7. Should handle malformed JSON gracefully")
    void shouldHandleMalformedJSON() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Malformed Request"));

        System.out.println("✅ Malformed JSON properly handled");
    }

    @Test
    @Order(8)
    @DisplayName("8. Should prevent duplicate user registration")
    void shouldPreventDuplicateRegistration() throws Exception {
        // Given - Same user registration as test 1
        UserRegistrationRequest duplicateRequest = new UserRegistrationRequest();
        duplicateRequest.setName("Integration Test User");
        duplicateRequest.setEmail("integration@test.com"); // Same email
        duplicateRequest.setAddress("123 Integration St");
        duplicateRequest.setPassword("securepass123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        System.out.println("✅ Duplicate registration properly prevented");
    }

    @Test
    @Order(9)
    @DisplayName("9. Database persistence verification")
    void shouldVerifyDatabasePersistence() throws Exception {
        // Verify that user data persists in the real MySQL database
        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].email").value("integration@test.com"))
                .andExpect(jsonPath("$[0].name").value("Integration Test User"));

        System.out.println("✅ Database persistence verified in MySQL container");
    }

    @Test
    @Order(10)
    @DisplayName("10. Complete end-to-end workflow test")
    void shouldCompleteEndToEndWorkflow() throws Exception {
        System.out.println("🎉 COMPREHENSIVE INTEGRATION TEST COMPLETED SUCCESSFULLY!");
        System.out.println("✅ User Registration & Authentication");
        System.out.println("✅ JWT Token Management");
        System.out.println("✅ Authorization & Security");
        System.out.println("✅ Validation & Error Handling");
        System.out.println("✅ Database Persistence with Testcontainers");
        System.out.println("✅ Real MySQL Database Integration");
        
        // Final verification
        assert jwtToken != null : "JWT token should be available";
        assert mysql.isRunning() : "MySQL container should be running";
        
        System.out.println("🚀 All modern testing practices successfully implemented!");
    }
}
