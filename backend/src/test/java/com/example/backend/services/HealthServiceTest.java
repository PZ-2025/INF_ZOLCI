package com.example.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HealthServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private HealthService healthService;

    @BeforeEach
    void setUp() {
        // Nie potrzebujemy dodatkowej konfiguracji, ponieważ używamy adnotacji @Mock i @InjectMocks
    }

    @Test
    void isDatabaseConnected_ShouldReturnTrue_WhenDatabaseIsAvailable() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(1);

        // Act
        boolean result = healthService.isDatabaseConnected();

        // Assert
        assertTrue(result, "Metoda isDatabaseConnected powinna zwrócić true, gdy baza danych jest dostępna");
    }

    @Test
    void isDatabaseConnected_ShouldReturnFalse_WhenDatabaseReturnsWrongValue() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);

        // Act
        boolean result = healthService.isDatabaseConnected();

        // Assert
        assertFalse(result, "Metoda isDatabaseConnected powinna zwrócić false, gdy baza danych zwraca nieprawidłową wartość");
    }

    @Test
    void isDatabaseConnected_ShouldReturnFalse_WhenQueryReturnsNull() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(null);

        // Act
        boolean result = healthService.isDatabaseConnected();

        // Assert
        assertFalse(result, "Metoda isDatabaseConnected powinna zwrócić false, gdy zapytanie zwraca null");
    }

    @Test
    void isDatabaseConnected_ShouldReturnFalse_WhenDataAccessExceptionIsThrown() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenThrow(new DataAccessException("Błąd połączenia") {});

        // Act
        boolean result = healthService.isDatabaseConnected();

        // Assert
        assertFalse(result, "Metoda isDatabaseConnected powinna zwrócić false, gdy występuje wyjątek DataAccessException");
    }
}