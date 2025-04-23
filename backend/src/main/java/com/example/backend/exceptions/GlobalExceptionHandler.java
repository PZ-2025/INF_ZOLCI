package com.example.backend.exceptions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.persistence.PersistenceException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Globalny handler wyjątków dla aplikacji BuildTask.
 * <p>
 * Przechwytuje wyjątki związane z bazą danych i inne błędy, aby zapewnić
 * jednolity format odpowiedzi API w przypadku błędów.
 *

 * @version 1.0.0
 * @since 1.0.0
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Value("${app.error.database.message:Wystapil blad serwera. Prosimy spróbowac ponownie pózniej.}")

    private String databaseErrorMessage;

    /**
     * Obsługuje niestandardowy wyjątek DatabaseConnectionException.
     *
     * @param ex złapany wyjątek
     * @return odpowiedź HTTP 503 z komunikatem błędu
     */
    @ExceptionHandler(DatabaseConnectionException.class)
    public ResponseEntity<Object> handleDatabaseConnectionException(DatabaseConnectionException ex) {
        logger.error("Błąd połączenia z bazą danych: {}", ex.getMessage(), ex);

        Map<String, String> response = new HashMap<>();
        response.put("message", databaseErrorMessage);
        response.put("status", "error");

        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Obsługuje standardowe wyjątki związane z dostępem do danych.
     * Obejmuje Spring DataAccessException i JPA PersistenceException.
     *
     * @param ex złapany wyjątek
     * @return odpowiedź HTTP 503 z komunikatem błędu
     */
    @ExceptionHandler({
            DataAccessException.class,
            PersistenceException.class
    })
    public ResponseEntity<Object> handleDataAccessExceptions(Exception ex) {
        logger.error("Błąd dostępu do bazy danych: {}", ex.getMessage(), ex);

        Map<String, String> response = new HashMap<>();
        response.put("status", String.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value()));
        response.put("exception", ex.getClass().getSimpleName());
        response.put("details", ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }
}