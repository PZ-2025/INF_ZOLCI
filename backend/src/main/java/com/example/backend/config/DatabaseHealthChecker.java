package com.example.backend.config;

import com.example.backend.exceptions.DatabaseConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Komponent do sprawdzania stanu połączenia z bazą danych.
 * <p>
 * Umożliwia proaktywne wykrywanie problemów z bazą danych przed wykonaniem operacji.
 * Może być używany w różnych miejscach aplikacji do weryfikacji dostępności bazy.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class DatabaseHealthChecker {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseHealthChecker.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseHealthChecker(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Sprawdza, czy połączenie z bazą danych jest dostępne.
     *
     * @return true jeśli połączenie jest dostępne, false w przeciwnym razie
     */
    public boolean isDatabaseConnected() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (DataAccessException e) {
            logger.error("Błąd podczas sprawdzania połączenia z bazą danych", e);
            return false;
        }
    }

    /**
     * Sprawdza połączenie z bazą danych i rzuca wyjątek, jeśli jest niedostępne.
     *
     * @throws DatabaseConnectionException jeśli połączenie jest niedostępne
     */
    public void checkDatabaseConnection() {
        if (!isDatabaseConnected()) {
            throw new DatabaseConnectionException("Baza danych jest obecnie niedostępna");
        }
    }
}