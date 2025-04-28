package com.example.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Serwis do sprawdzania stanu systemu i jego komponentów.
 * <p>
 * Umożliwia sprawdzenie dostępności bazy danych i innych istotnych
 * elementów infrastruktury.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
public class HealthService {

    private static final Logger logger = LoggerFactory.getLogger(HealthService.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public HealthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Sprawdza, czy połączenie z bazą danych jest dostępne.
     * <p>
     * Wykonuje proste zapytanie testowe, aby zweryfikować dostępność bazy danych.
     *
     * @return true jeśli baza danych jest dostępna, false w przeciwnym razie
     */
    public boolean isDatabaseConnected() {
        try {
            // Wykonaj proste zapytanie testowe
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return result != null && result == 1;
        } catch (DataAccessException e) {
            logger.error("Błąd podczas sprawdzania połączenia z bazą danych", e);
            return false;
        }
    }
}