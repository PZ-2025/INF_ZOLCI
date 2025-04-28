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

    /**
     * Pobiera bardziej szczegółowe informacje o stanie bazy danych.
     * <p>
     * Sprawdza połączenie i dodatkowo pobiera informacje diagnostyczne.
     *
     * @return Obiekt zawierający stan bazy i dodatkowe informacje
     */
    public DatabaseHealthInfo getDatabaseHealthInfo() {
        DatabaseHealthInfo info = new DatabaseHealthInfo();

        try {
            long startTime = System.currentTimeMillis();

            // Sprawdź połączenie z bazą danych
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            boolean isConnected = result != null && result == 1;

            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            info.setConnected(isConnected);
            info.setResponseTime(responseTime);

            if (isConnected) {
                // Pobierz dodatkowe informacje o bazie danych, jeśli dostępne
                try {
                    String dbVersion = jdbcTemplate.queryForObject(
                            "SELECT VERSION()", String.class);
                    info.setVersion(dbVersion);
                } catch (Exception e) {
                    logger.debug("Nie można pobrać wersji bazy danych", e);
                }
            }
        } catch (DataAccessException e) {
            logger.error("Błąd podczas pobierania informacji o stanie bazy danych", e);
            info.setConnected(false);
            info.setErrorMessage(e.getMessage());
        }

        return info;
    }

    /**
     * Klasa wewnętrzna reprezentująca informacje o stanie bazy danych.
     */
    public static class DatabaseHealthInfo {
        private boolean connected;
        private long responseTime;
        private String version;
        private String errorMessage;

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        public long getResponseTime() {
            return responseTime;
        }

        public void setResponseTime(long responseTime) {
            this.responseTime = responseTime;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}