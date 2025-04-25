package com.example.backend.controllers;

import com.example.backend.services.HealthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class HealthControllerTest {

    @Mock
    private HealthService healthService;

    @InjectMocks
    private HealthController healthController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(healthController).build();
    }

    @Test
    void checkHealth_ShouldReturnStatusOK_WhenDatabaseIsUp() {
        // Arrange
        when(healthService.isDatabaseConnected()).thenReturn(true);

        // Act
        ResponseEntity<Map<String, Object>> responseEntity = healthController.checkHealth();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode(), "Powinien zwrócić status OK, gdy baza danych działa");

        Map<String, Object> body = responseEntity.getBody();
        assertNotNull(body, "Odpowiedź nie powinna być null");
        assertEquals("UP", body.get("status"), "Status powinien być UP");
        assertEquals("UP", body.get("database"), "Status bazy danych powinien być UP");

        @SuppressWarnings("unchecked")
        Map<String, Object> components = (Map<String, Object>) body.get("components");
        assertNotNull(components, "Komponent powinien być obecny");

        @SuppressWarnings("unchecked")
        Map<String, Object> databaseComponent = (Map<String, Object>) components.get("database");
        assertEquals("UP", databaseComponent.get("status"), "Status bazy danych w komponentach powinien być UP");
    }

    @Test
    void checkHealth_ShouldReturnStatusServiceUnavailable_WhenDatabaseIsDown() {
        // Arrange
        when(healthService.isDatabaseConnected()).thenReturn(false);

        // Act
        ResponseEntity<Map<String, Object>> responseEntity = healthController.checkHealth();

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, responseEntity.getStatusCode(),
                "Powinien zwrócić status SERVICE_UNAVAILABLE, gdy baza danych nie działa");

        Map<String, Object> body = responseEntity.getBody();
        assertNotNull(body, "Odpowiedź nie powinna być null");
        assertEquals("DOWN", body.get("status"), "Status powinien być DOWN");
        assertEquals("DOWN", body.get("database"), "Status bazy danych powinien być DOWN");
        assertEquals("Database connection is not available", body.get("message"),
                "Powinien zwrócić komunikat o niedostępności bazy danych");
    }

    @Test
    void checkDatabaseHealth_ShouldReturnStatusOK_WhenDatabaseIsUp() {
        // Arrange
        when(healthService.isDatabaseConnected()).thenReturn(true);

        // Act
        ResponseEntity<Map<String, Object>> responseEntity = healthController.checkDatabaseHealth();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode(), "Powinien zwrócić status OK, gdy baza danych działa");

        Map<String, Object> body = responseEntity.getBody();
        assertNotNull(body, "Odpowiedź nie powinna być null");
        assertEquals("UP", body.get("status"), "Status powinien być UP");
        assertFalse(body.containsKey("message"), "Nie powinien zawierać komunikatu o błędzie");
    }

    @Test
    void checkDatabaseHealth_ShouldReturnStatusServiceUnavailable_WhenDatabaseIsDown() {
        // Arrange
        when(healthService.isDatabaseConnected()).thenReturn(false);

        // Act
        ResponseEntity<Map<String, Object>> responseEntity = healthController.checkDatabaseHealth();

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, responseEntity.getStatusCode(),
                "Powinien zwrócić status SERVICE_UNAVAILABLE, gdy baza danych nie działa");

        Map<String, Object> body = responseEntity.getBody();
        assertNotNull(body, "Odpowiedź nie powinna być null");
        assertEquals("DOWN", body.get("status"), "Status powinien być DOWN");
        assertEquals("Database connection is not available", body.get("message"),
                "Powinien zwrócić komunikat o niedostępności bazy danych");
    }

    @Test
    void checkHealth_WithMockMvc_WhenDatabaseIsUp() throws Exception {
        // Arrange
        when(healthService.isDatabaseConnected()).thenReturn(true);

        // Act and Assert
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.database").value("UP"))
                .andExpect(jsonPath("$.components.database.status").value("UP"));
    }

    @Test
    void checkHealth_WithMockMvc_WhenDatabaseIsDown() throws Exception {
        // Arrange
        when(healthService.isDatabaseConnected()).thenReturn(false);

        // Act and Assert
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.database").value("DOWN"))
                .andExpect(jsonPath("$.message").value("Database connection is not available"));
    }

    @Test
    void checkDatabaseHealth_WithMockMvc_WhenDatabaseIsUp() throws Exception {
        // Arrange
        when(healthService.isDatabaseConnected()).thenReturn(true);

        // Act and Assert
        mockMvc.perform(get("/api/health/database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void checkDatabaseHealth_WithMockMvc_WhenDatabaseIsDown() throws Exception {
        // Arrange
        when(healthService.isDatabaseConnected()).thenReturn(false);

        // Act and Assert
        mockMvc.perform(get("/api/health/database"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.message").value("Database connection is not available"));
    }
}