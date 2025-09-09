package com.project.electricitybillgenerator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.electricitybillgenerator.config.JwtUtil;
import com.project.electricitybillgenerator.dto.UserCreateRequest;
import com.project.electricitybillgenerator.dto.UserResponse;
import com.project.electricitybillgenerator.model.BillUser;
import com.project.electricitybillgenerator.model.UserRole;
import com.project.electricitybillgenerator.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for UserController.
 * Tests controller layer with mocked service dependencies.
 * Includes JWT authentication and authorization testing.
 */
@WebMvcTest(UserController.class)
@Import({JwtUtil.class})
@DisplayName("User Controller Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    private UserCreateRequest validUserRequest;
    private UserResponse userResponse;
    private BillUser testUser;

    @BeforeEach
    void setUp() {
        validUserRequest = new UserCreateRequest();
        validUserRequest.setName("John Doe");
        validUserRequest.setEmail("john.doe@example.com");
        validUserRequest.setAddress("123 Main St");
        validUserRequest.setPassword("securepass123");

        testUser = new BillUser();
        testUser.setMeterId(1001L);
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setAddress("123 Main St");
        testUser.setRole(UserRole.ROLE_USER);

        userResponse = new UserResponse();
        userResponse.setMeterId(1001L);
        userResponse.setName("John Doe");
        userResponse.setEmail("john.doe@example.com");
        userResponse.setAddress("123 Main St");
        userResponse.setRole("ROLE_USER");
    }

    @Nested
    @DisplayName("User Registration Tests")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register user successfully with valid data")
        void shouldRegisterUserSuccessfully() throws Exception {
            // Given
            when(userService.createUser(any(UserCreateRequest.class))).thenReturn(userResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUserRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.meterId").value(1001L))
                    .andExpect(jsonPath("$.name").value("John Doe"))
                    .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.address").value("123 Main St"))
                    .andExpected(jsonPath("$.role").value("ROLE_USER"));

            verify(userService).createUser(any(UserCreateRequest.class));
        }

        @Test
        @DisplayName("Should return 400 for invalid email format")
        void shouldReturn400ForInvalidEmail() throws Exception {
            // Given
            validUserRequest.setEmail("invalid-email");

            // When & Then
            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUserRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpected(jsonPath("$.fieldErrors.email").exists());

            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should return 400 for missing required fields")
        void shouldReturn400ForMissingFields() throws Exception {
            // Given
            UserCreateRequest invalidRequest = new UserCreateRequest();
            invalidRequest.setEmail("test@example.com"); // Only email provided

            // When & Then
            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.name").exists())
                    .andExpected(jsonPath("$.fieldErrors.password").exists());

            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should return 400 for malformed JSON")
        void shouldReturn400ForMalformedJson() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json"))
                    .andDo(print())
                    .andExpected(status().isBadRequest());

            verify(userService, never()).createUser(any());
        }
    }

    @Nested
    @DisplayName("User Retrieval Tests - Authenticated")
    class AuthenticatedUserRetrievalTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should get all users when authenticated as admin")
        void shouldGetAllUsersAsAdmin() throws Exception {
            // Given
            List<UserResponse> users = Arrays.asList(userResponse);
            when(userService.getAllUsers()).thenReturn(users);

            // When & Then
            mockMvc.perform(get("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpected(jsonPath("$").isArray())
                    .andExpected(jsonPath("$[0].meterId").value(1001L));

            verify(userService).getAllUsers();
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should get user by meter ID when authenticated")
        void shouldGetUserByMeterIdWhenAuthenticated() throws Exception {
            // Given
            when(userService.getUserByMeterId(1001L)).thenReturn(userResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/users/1001")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpected(jsonPath("$.meterId").value(1001L));

            verify(userService).getUserByMeterId(1001L);
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpected(status().isUnauthorized());

            verify(userService, never()).getAllUsers();
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 404 for non-existent user")
        void shouldReturn404ForNonExistentUser() throws Exception {
            // Given
            when(userService.getUserByMeterId(9999L))
                    .thenThrow(new RuntimeException("User not found"));

            // When & Then
            mockMvc.perform(get("/api/v1/users/9999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpected(status().isInternalServerError());

            verify(userService).getUserByMeterId(9999L);
        }
    }

    @Nested
    @DisplayName("JWT Token Tests")
    class JwtTokenTests {

        @Test
        @DisplayName("Should access protected endpoint with valid JWT token")
        void shouldAccessWithValidJwtToken() throws Exception {
            // Given
            String validToken = "valid.jwt.token";
            when(jwtUtil.validateToken(validToken, "john.doe@example.com")).thenReturn(true);
            when(jwtUtil.getUsernameFromToken(validToken)).thenReturn("john.doe@example.com");
            when(userService.getAllUsers()).thenReturn(Arrays.asList(userResponse));

            // When & Then
            mockMvc.perform(get("/api/v1/users")
                    .with(jwt().jwt(jwt -> jwt.claim("sub", "john.doe@example.com")))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpected(status().isOk());
        }

        @Test
        @DisplayName("Should reject request with invalid JWT token")
        void shouldRejectInvalidJwtToken() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/users")
                    .header("Authorization", "Bearer invalid.token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpected(status().isUnauthorized());

            verify(userService, never()).getAllUsers();
        }
    }

    @Nested
    @DisplayName("User Deletion Tests")
    class UserDeletionTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should delete user when authenticated as admin")
        void shouldDeleteUserAsAdmin() throws Exception {
            // Given
            doNothing().when(userService).deleteUserByMeterId(1001L);

            // When & Then
            mockMvc.perform(delete("/api/v1/users/1001")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpected(status().isNoContent());

            verify(userService).deleteUserByMeterId(1001L);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 403 when user tries to delete (insufficient privileges)")
        void shouldReturn403ForUserRole() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/v1/users/1001")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpected(status().isForbidden());

            verify(userService, never()).deleteUserByMeterId(any());
        }
    }
}
