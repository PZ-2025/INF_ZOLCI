package com.example.backend.controllers;

import com.example.backend.services.HealthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Kontroler REST dostarczający endpointy do sprawdzania stanu systemu.
 * <p>
 * Umożliwia sprawdzenie stanu aplikacji, bazy danych i innych komponentów.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    private final HealthService healthService;

    @Autowired
    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    /**
     * Endpoint sprawdzający ogólny stan aplikacji.
     * <p>
     * Zwraca status 200 OK, jeśli aplikacja działa poprawnie.
     * Zwraca status 503 Service Unavailable, jeśli baza danych jest niedostępna.
     *
     * @return Odpowiedź HTTP ze statusem i danymi o stanie systemu
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> checkHealth() {
        logger.debug("Sprawdzanie stanu systemu");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());

        // Sprawdź połączenie z bazą danych
        boolean databaseStatus = healthService.isDatabaseConnected();
        response.put("database", databaseStatus ? "UP" : "DOWN");

        // Jeśli baza danych nie działa, zwróć 503 Service Unavailable
        HttpStatus httpStatus = databaseStatus ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;

        // Dodaj szczegóły tylko, gdy baza nie działa
        if (!databaseStatus) {
            response.put("status", "DOWN");
            response.put("message", "Database connection is not available");
            logger.warn("Baza danych niedostępna podczas sprawdzania stanu systemu");
        }

        // Dodaj inne komponenty, które mogą być sprawdzane
        Map<String, Object> components = new HashMap<>();
        components.put("database", Map.of(
                "status", databaseStatus ? "UP" : "DOWN",
                "timestamp", System.currentTimeMillis()
        ));
        response.put("components", components);

        return new ResponseEntity<>(response, httpStatus);
    }

    /**
     * Endpoint sprawdzający tylko stan bazy danych.
     *
     * @return Odpowiedź HTTP ze statusem bazy danych
     */
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> checkDatabaseHealth() {
        logger.debug("Sprawdzanie stanu bazy danych");

        boolean databaseStatus = healthService.isDatabaseConnected();

        Map<String, Object> response = new HashMap<>();
        response.put("status", databaseStatus ? "UP" : "DOWN");
        response.put("timestamp", System.currentTimeMillis());

        // Jeśli baza danych nie działa, zwróć 503 Service Unavailable
        HttpStatus httpStatus = databaseStatus ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;

        if (!databaseStatus) {
            response.put("message", "Database connection is not available");
            logger.warn("Baza danych niedostępna");
        }

        return new ResponseEntity<>(response, httpStatus);
    }
}