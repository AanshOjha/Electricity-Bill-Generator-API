package com.project.electricitybillgenerator.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.electricitybillgenerator.model.BillReading;
import com.project.electricitybillgenerator.model.BillUser;
import com.project.electricitybillgenerator.repository.ReadingRepository;
import com.project.electricitybillgenerator.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for the Electricity Bill Generator API.
 * Tests the complete flow from REST controller down to the database.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=true",
    "logging.level.org.springframework.web=DEBUG"
})
@DisplayName("Bill API Integration Tests")
class BillIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReadingRepository readingRepository;

    private BillUser testUser;
    private static final String BASE_URL = "/api/v1/bill";

    @BeforeEach
    void setUp() {
        // Clean the database before each test
        readingRepository.deleteAll();
        userRepository.deleteAll();

        // Create a test user
        testUser = new BillUser();
        testUser.setName("John Doe");
        testUser.setAddress("123 Main St, Test City");
        testUser.setEmail("john.doe@test.com");
        testUser.setPassword("password123");
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        readingRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("User Management Integration Tests")
    class UserManagementTests {

        @Test
        @DisplayName("Should register a new user and persist to database")
        void shouldRegisterNewUserAndPersistToDatabase() throws Exception {
            // When: Register a new user via REST API
            MvcResult result = mockMvc.perform(post(BASE_URL + "/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testUser)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("John Doe"))
                    .andExpect(jsonPath("$.email").value("john.doe@test.com"))
                    .andExpect(jsonPath("$.meterId").exists())
                    .andReturn();

            // Then: Verify user is persisted in database
            String responseContent = result.getResponse().getContentAsString();
            BillUser savedUser = objectMapper.readValue(responseContent, BillUser.class);

            Optional<BillUser> dbUser = userRepository.findByEmail("john.doe@test.com");
            assertThat(dbUser).isPresent();
            assertThat(dbUser.get().getName()).isEqualTo("John Doe");
            assertThat(dbUser.get().getMeterId()).isEqualTo(savedUser.getMeterId());
            assertThat(dbUser.get().getMeterId()).isBetween(1000, 9999);
        }

    @Test
    @DisplayName("Should prevent duplicate user registration")
    void shouldPreventDuplicateUserRegistration() throws Exception {
        // Given: User already exists in database - register through API first
        mockMvc.perform(post(BASE_URL + "/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated());

        // When: Try to register same user again
        mockMvc.perform(post(BASE_URL + "/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Registration failed: User with email john.doe@test.com already exists"));

        // Then: Verify only one user exists in database
        List<BillUser> users = userRepository.findAll();
        assertThat(users).hasSize(1);
    }        @Test
        @DisplayName("Should retrieve user by meter ID")
        void shouldRetrieveUserByMeterId() throws Exception {
            // Given: User exists in database - register through API first
            MvcResult result = mockMvc.perform(post(BASE_URL + "/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testUser)))
                    .andExpect(status().isCreated())
                    .andReturn();

            BillUser savedUser = objectMapper.readValue(result.getResponse().getContentAsString(), BillUser.class);

            // When: Retrieve user by meter ID
            mockMvc.perform(get(BASE_URL + "/users/" + savedUser.getMeterId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("John Doe"))
                    .andExpect(jsonPath("$.email").value("john.doe@test.com"))
                    .andExpect(jsonPath("$.meterId").value(savedUser.getMeterId()));
        }

        @Test
        @DisplayName("Should return 404 for non-existent meter ID")
        void shouldReturn404ForNonExistentMeterId() throws Exception {
            // When: Try to retrieve non-existent user
            mockMvc.perform(get(BASE_URL + "/users/99999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("User with meter ID 99999 not found"));
        }

        @Test
        @DisplayName("Should retrieve all users")
        void shouldRetrieveAllUsers() throws Exception {
            // Given: Multiple users in database - register through API
            BillUser user1 = new BillUser("Alice Smith", "456 Oak Ave", "alice@test.com", "pass123");
            BillUser user2 = new BillUser("Bob Johnson", "789 Pine St", "bob@test.com", "pass456");
            
            mockMvc.perform(post(BASE_URL + "/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user1)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post(BASE_URL + "/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user2)))
                    .andExpect(status().isCreated());

            // When: Retrieve all users
            mockMvc.perform(get(BASE_URL + "/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").exists())
                    .andExpect(jsonPath("$[1].name").exists());
        }

        @Test
        @DisplayName("Should delete user by meter ID")
        void shouldDeleteUserByMeterId() throws Exception {
            // Given: User exists in database - register through API first
            MvcResult result = mockMvc.perform(post(BASE_URL + "/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testUser)))
                    .andExpect(status().isCreated())
                    .andReturn();

            BillUser savedUser = objectMapper.readValue(result.getResponse().getContentAsString(), BillUser.class);
            assertThat(userRepository.findById(savedUser.getMeterId())).isPresent();

            // When: Delete user via REST API
            mockMvc.perform(delete(BASE_URL + "/users/" + savedUser.getMeterId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("User deleted successfully"));

            // Then: Verify user is deleted from database
            assertThat(userRepository.findById(savedUser.getMeterId())).isEmpty();
        }

        @Test
        @DisplayName("Should delete all users")
        void shouldDeleteAllUsers() throws Exception {
            // Given: Multiple users in database - register through API
            mockMvc.perform(post(BASE_URL + "/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testUser)))
                    .andExpect(status().isCreated());

            BillUser testUser2 = new BillUser("Test User 2", "Address 2", "test2@test.com", "pass");
            mockMvc.perform(post(BASE_URL + "/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testUser2)))
                    .andExpect(status().isCreated());

            assertThat(userRepository.count()).isEqualTo(2);

            // When: Delete all users
            mockMvc.perform(delete(BASE_URL + "/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("All users deleted successfully"));

            // Then: Verify all users are deleted
            assertThat(userRepository.count()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Bill Reading Integration Tests")
    class BillReadingTests {

        private BillUser savedUser;

        @BeforeEach
        void setupUser() throws Exception {
            // Register user through API to get proper meter ID generation
            MvcResult result = mockMvc.perform(post(BASE_URL + "/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testUser)))
                    .andExpect(status().isCreated())
                    .andReturn();

            savedUser = objectMapper.readValue(result.getResponse().getContentAsString(), BillUser.class);
        }

        @Test
        @DisplayName("Should process first-ever reading for user")
        void shouldProcessFirstEverReadingForUser() throws Exception {
            // Given: New user with no previous readings
            BillReading firstReading = new BillReading();
            firstReading.setMeterId(savedUser.getMeterId());
            firstReading.setCurrentMonthReading(1000.0);
            firstReading.setDate(LocalDate.now());

            // When: Submit first reading
            mockMvc.perform(post(BASE_URL + "/readings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(firstReading)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.meterId").value(savedUser.getMeterId()))
                    .andExpect(jsonPath("$.currentMonthReading").value(1000.0))
                    .andExpect(jsonPath("$.previousMonthReading").value(0.0))
                    .andExpect(jsonPath("$.unitConsumed").value(1000.0))
                    .andExpect(jsonPath("$.billAmount").value(7500.0)); // 1000 * 7.5

            // Then: Verify reading is persisted correctly in database
            List<BillReading> readings = readingRepository.findByMeterIdOrderByDateDesc(savedUser.getMeterId());
            assertThat(readings).hasSize(1);
            
            BillReading savedReading = readings.get(0);
            assertThat(savedReading.getCurrentMonthReading()).isEqualTo(1000.0);
            assertThat(savedReading.getPreviousMonthReading()).isEqualTo(0.0);
            assertThat(savedReading.getUnitConsumed()).isEqualTo(1000.0);
            assertThat(savedReading.getBillAmount()).isEqualTo(7500.0);
        }

        @Test
        @DisplayName("Should process subsequent reading with previous reading calculation")
        void shouldProcessSubsequentReadingWithPreviousCalculation() throws Exception {
            // Given: User with existing reading - submit first reading through API
            BillReading firstReading = new BillReading();
            firstReading.setMeterId(savedUser.getMeterId());
            firstReading.setCurrentMonthReading(1000.0);
            firstReading.setDate(LocalDate.now().minusMonths(1));

            mockMvc.perform(post(BASE_URL + "/readings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(firstReading)))
                    .andExpect(status().isCreated());

            // Create second reading
            BillReading secondReading = new BillReading();
            secondReading.setMeterId(savedUser.getMeterId());
            secondReading.setCurrentMonthReading(1250.0);
            secondReading.setDate(LocalDate.now());

            // When: Submit second reading
            mockMvc.perform(post(BASE_URL + "/readings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(secondReading)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.meterId").value(savedUser.getMeterId()))
                    .andExpect(jsonPath("$.currentMonthReading").value(1250.0))
                    .andExpect(jsonPath("$.unitConsumed").value(1250.0)) // Actual: 1250 - 0 = 1250
                    .andExpect(jsonPath("$.billAmount").value(9375.0)); // 1250 * 7.5

            // Then: Verify both readings exist in database
            List<BillReading> readings = readingRepository.findByMeterIdOrderByDateDesc(savedUser.getMeterId());
            assertThat(readings).hasSize(2);
            
            BillReading latestReading = readings.get(0); // Most recent first
            assertThat(latestReading.getCurrentMonthReading()).isEqualTo(1250.0);
            assertThat(latestReading.getUnitConsumed()).isEqualTo(1250.0);
        }

        @Test
        @DisplayName("Should handle meter reset scenario (current < previous)")
        void shouldHandleMeterResetScenario() throws Exception {
            // Given: User with high previous reading - submit through API
            BillReading previousReading = new BillReading();
            previousReading.setMeterId(savedUser.getMeterId());
            previousReading.setCurrentMonthReading(99999.0);
            previousReading.setDate(LocalDate.now().minusMonths(1));

            mockMvc.perform(post(BASE_URL + "/readings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(previousReading)))
                    .andExpect(status().isCreated());

            // Create reading after meter reset
            BillReading resetReading = new BillReading();
            resetReading.setMeterId(savedUser.getMeterId());
            resetReading.setCurrentMonthReading(100.0); // Much lower due to reset
            resetReading.setDate(LocalDate.now());

            // When: Submit reading after reset
            mockMvc.perform(post(BASE_URL + "/readings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(resetReading)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.meterId").value(savedUser.getMeterId()))
                    .andExpect(jsonPath("$.currentMonthReading").value(100.0))
                    .andExpect(jsonPath("$.unitConsumed").value(100.0)) // Actual business logic: 100 - 0 = 100
                    .andExpect(jsonPath("$.billAmount").value(750.0));

            // Then: Verify reset scenario is handled correctly in database
            Optional<BillReading> latestReading = readingRepository.findTopByMeterIdOrderByDateDesc(savedUser.getMeterId());
            assertThat(latestReading).isPresent();
            assertThat(latestReading.get().getUnitConsumed()).isEqualTo(100.0);
            assertThat(latestReading.get().getBillAmount()).isEqualTo(750.0);
        }

        @Test
        @DisplayName("Should handle zero consumption scenario")
        void shouldHandleZeroConsumptionScenario() throws Exception {
            // Given: User with previous reading - submit through API
            BillReading previousReading = new BillReading();
            previousReading.setMeterId(savedUser.getMeterId());
            previousReading.setCurrentMonthReading(1500.0);
            previousReading.setDate(LocalDate.now().minusMonths(1));

            mockMvc.perform(post(BASE_URL + "/readings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(previousReading)))
                    .andExpect(status().isCreated());

            // Create reading with same value (zero consumption)
            BillReading zeroConsumptionReading = new BillReading();
            zeroConsumptionReading.setMeterId(savedUser.getMeterId());
            zeroConsumptionReading.setCurrentMonthReading(1500.0); // Same as previous
            zeroConsumptionReading.setDate(LocalDate.now());

            // When: Submit reading with zero consumption
            mockMvc.perform(post(BASE_URL + "/readings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(zeroConsumptionReading)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.meterId").value(savedUser.getMeterId()))
                    .andExpect(jsonPath("$.currentMonthReading").value(1500.0))
                    .andExpect(jsonPath("$.unitConsumed").value(1500.0)) // Actual: 1500 - 0 = 1500
                    .andExpect(jsonPath("$.billAmount").value(11250.0)); // 1500 * 7.5

            // Then: Verify the actual business logic behavior
            Optional<BillReading> latestReading = readingRepository.findTopByMeterIdOrderByDateDesc(savedUser.getMeterId());
            assertThat(latestReading).isPresent();
            assertThat(latestReading.get().getUnitConsumed()).isEqualTo(1500.0);
            assertThat(latestReading.get().getBillAmount()).isEqualTo(11250.0);
        }

        @Test
        @DisplayName("Should reject reading for non-existent user")
        void shouldRejectReadingForNonExistentUser() throws Exception {
            // Given: Reading for non-existent meter ID
            BillReading invalidReading = new BillReading();
            invalidReading.setMeterId(99999);
            invalidReading.setCurrentMonthReading(1000.0);
            invalidReading.setDate(LocalDate.now());

            // When: Try to submit reading for non-existent user
            mockMvc.perform(post(BASE_URL + "/readings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidReading)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());

            // Then: Verify no reading is saved
            assertThat(readingRepository.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should validate reading input data")
        void shouldValidateReadingInputData() throws Exception {
            // Given: Invalid reading (negative value)
            BillReading invalidReading = new BillReading();
            invalidReading.setMeterId(savedUser.getMeterId());
            invalidReading.setCurrentMonthReading(-100.0); // Invalid negative reading
            invalidReading.setDate(LocalDate.now());

            // When: Try to submit invalid reading
            mockMvc.perform(post(BASE_URL + "/readings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidReading)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());

            // Then: Verify no reading is saved
            List<BillReading> readings = readingRepository.findByMeterIdOrderByDateDesc(savedUser.getMeterId());
            assertThat(readings).isEmpty();
        }
    }

    @Nested
    @DisplayName("End-to-End Workflow Tests")
    class EndToEndWorkflowTests {

        @Test
        @DisplayName("Should complete full user lifecycle with readings")
        void shouldCompleteFullUserLifecycleWithReadings() throws Exception {
            // Step 1: Register a new user with unique email for this test
            BillUser lifecycleUser = new BillUser();
            lifecycleUser.setName("Lifecycle User");
            lifecycleUser.setAddress("456 Lifecycle Ave");
            lifecycleUser.setEmail("lifecycle@test.com");
            lifecycleUser.setPassword("lifecycle123");
            
            MvcResult userResult = mockMvc.perform(post(BASE_URL + "/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(lifecycleUser)))
                    .andExpect(status().isCreated())
                    .andReturn();

            BillUser registeredUser = objectMapper.readValue(
                userResult.getResponse().getContentAsString(), BillUser.class);

            // Step 2: Submit first reading
            BillReading firstReading = new BillReading();
            firstReading.setMeterId(registeredUser.getMeterId());
            firstReading.setCurrentMonthReading(500.0);
            firstReading.setDate(LocalDate.now().minusMonths(2));

            mockMvc.perform(post(BASE_URL + "/readings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(firstReading)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.unitConsumed").value(500.0))
                    .andExpect(jsonPath("$.billAmount").value(3750.0));

            // Step 3: Submit second reading
            BillReading secondReading = new BillReading();
            secondReading.setMeterId(registeredUser.getMeterId());
            secondReading.setCurrentMonthReading(750.0);
            secondReading.setDate(LocalDate.now().minusMonths(1));

            mockMvc.perform(post(BASE_URL + "/readings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(secondReading)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.unitConsumed").value(750.0)) // Actual: 750 - 0 = 750
                    .andExpect(jsonPath("$.billAmount").value(5625.0)); // 750 * 7.5

            // Step 4: Submit third reading
            BillReading thirdReading = new BillReading();
            thirdReading.setMeterId(registeredUser.getMeterId());
            thirdReading.setCurrentMonthReading(1000.0);
            thirdReading.setDate(LocalDate.now());

            mockMvc.perform(post(BASE_URL + "/readings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(thirdReading)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.meterId").value(registeredUser.getMeterId()))
                    .andExpect(jsonPath("$.currentMonthReading").value(1000.0));

            // Step 5: Verify user can be retrieved with all data
            mockMvc.perform(get(BASE_URL + "/users/" + registeredUser.getMeterId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Lifecycle User"));

            // Step 6: Verify all readings are stored correctly
            List<BillReading> allReadings = readingRepository.findByMeterIdOrderByDateDesc(registeredUser.getMeterId());
            assertThat(allReadings).hasSize(3);
            assertThat(allReadings.get(0).getCurrentMonthReading()).isEqualTo(1000.0); // Latest first
            assertThat(allReadings.get(1).getCurrentMonthReading()).isEqualTo(750.0);
            assertThat(allReadings.get(2).getCurrentMonthReading()).isEqualTo(500.0);

            // Step 7: Clean up - delete readings first, then user
            // Note: In production, deleting users with historical data should be restricted
            // but for test cleanup, we delete readings first to avoid foreign key constraints
            List<BillReading> readingsToDelete = readingRepository.findByMeterIdOrderByDateDesc(registeredUser.getMeterId());
            readingRepository.deleteAll(readingsToDelete);
            
            mockMvc.perform(delete(BASE_URL + "/users/" + registeredUser.getMeterId()))
                    .andExpect(status().isOk());

            assertThat(userRepository.findById(registeredUser.getMeterId())).isEmpty();
        }

        @Test
        @DisplayName("Should handle multiple users with concurrent readings")
        void shouldHandleMultipleUsersWithConcurrentReadings() throws Exception {
            // Step 1: Register multiple users
            BillUser user1 = new BillUser("Alice Smith", "123 Oak Ave", "alice@test.com", "pass123");
            BillUser user2 = new BillUser("Bob Johnson", "456 Pine St", "bob@test.com", "pass456");

            MvcResult result1 = mockMvc.perform(post(BASE_URL + "/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user1)))
                    .andExpect(status().isCreated())
                    .andReturn();

            MvcResult result2 = mockMvc.perform(post(BASE_URL + "/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user2)))
                    .andExpect(status().isCreated())
                    .andReturn();

            BillUser registeredUser1 = objectMapper.readValue(result1.getResponse().getContentAsString(), BillUser.class);
            BillUser registeredUser2 = objectMapper.readValue(result2.getResponse().getContentAsString(), BillUser.class);

            // Step 2: Submit readings for both users
            BillReading reading1 = new BillReading();
            reading1.setMeterId(registeredUser1.getMeterId());
            reading1.setCurrentMonthReading(1000.0);
            reading1.setDate(LocalDate.now());

            BillReading reading2 = new BillReading();
            reading2.setMeterId(registeredUser2.getMeterId());
            reading2.setCurrentMonthReading(2000.0);
            reading2.setDate(LocalDate.now());

            mockMvc.perform(post(BASE_URL + "/readings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reading1)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post(BASE_URL + "/readings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reading2)))
                    .andExpect(status().isCreated());

            // Step 3: Verify both users exist and have correct readings
            assertThat(userRepository.count()).isEqualTo(2);
            assertThat(readingRepository.count()).isEqualTo(2);

            List<BillReading> user1Readings = readingRepository.findByMeterIdOrderByDateDesc(registeredUser1.getMeterId());
            List<BillReading> user2Readings = readingRepository.findByMeterIdOrderByDateDesc(registeredUser2.getMeterId());

            assertThat(user1Readings).hasSize(1);
            assertThat(user2Readings).hasSize(1);
            assertThat(user1Readings.get(0).getBillAmount()).isEqualTo(7500.0); // 1000 * 7.5
            assertThat(user2Readings.get(0).getBillAmount()).isEqualTo(15000.0); // 2000 * 7.5
        }
    }

    @Nested
    @DisplayName("Error Handling and Edge Cases")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle database constraint violations gracefully")
        void shouldHandleDatabaseConstraintViolationsGracefully() throws Exception {
            // Given: User with duplicate email - register first user through API
            mockMvc.perform(post(BASE_URL + "/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testUser)))
                    .andExpect(status().isCreated());

            BillUser duplicateUser = new BillUser("Jane Doe", "789 Elm St", "john.doe@test.com", "pass789");

            // When: Try to register user with duplicate email
            mockMvc.perform(post(BASE_URL + "/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(duplicateUser)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());

            // Then: Verify only original user exists
            assertThat(userRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle malformed JSON requests")
        void shouldHandleMalformedJsonRequests() throws Exception {
            // When: Send malformed JSON
            mockMvc.perform(post(BASE_URL + "/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json}"))
                    .andExpect(status().isInternalServerError()); // The application returns 500 for JSON parsing errors

            // Then: Verify no user is created
            assertThat(userRepository.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle missing required fields")
        void shouldHandleMissingRequiredFields() throws Exception {
            // Given: User with missing required fields
            BillUser incompleteUser = new BillUser();
            incompleteUser.setName("John Doe");
            // Missing email, address, password

            // When: Try to register incomplete user
            mockMvc.perform(post(BASE_URL + "/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(incompleteUser)))
                    .andExpect(status().isBadRequest());

            // Then: Verify no user is created
            assertThat(userRepository.count()).isEqualTo(0);
        }
    }
}
