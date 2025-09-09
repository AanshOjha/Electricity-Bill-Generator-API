package com.project.electricitybillgenerator.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Simplified exception handler test that focuses on working scenarios
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Global Exception Handler Tests")
class SimpleGlobalExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should handle validation errors with detailed field messages")
    void shouldHandleValidationErrors() throws Exception {
        // Create invalid registration request
        String invalidRequest = objectMapper.writeValueAsString(Map.of(
                "password", "123",           // Too short
                "address", "",               // Empty
                "name", "",                  // Empty
                "email", "invalid-email"     // Invalid format
        ));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("uri=/api/v1/auth/register"))
                .andExpect(jsonPath("$.fieldErrors.password").exists())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.address").exists());
    }

    @Test
    @DisplayName("Should handle malformed JSON requests")
    void shouldHandleMalformedJson() throws Exception {
        String malformedJson = """
            {"name": "test", "email": invalid}
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Malformed Request"))
                .andExpect(jsonPath("$.message").value("Invalid JSON format. Please check your request body."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("uri=/api/v1/auth/register"));
    }

    @Test
    @DisplayName("Should handle empty request body")
    void shouldHandleEmptyRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Malformed Request"))
                .andExpect(jsonPath("$.message").value("Invalid JSON format. Please check your request body."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("uri=/api/v1/auth/register"));
    }

    @Test
    @DisplayName("Should handle incomplete JSON objects")
    void shouldHandleIncompleteJson() throws Exception {
        String incompleteJson = """
            {"name": "test", "email": "test@example.com"
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(incompleteJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Malformed Request"))
                .andExpect(jsonPath("$.message").value("Invalid JSON format. Please check your request body."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("uri=/api/v1/auth/register"));
    }
}
