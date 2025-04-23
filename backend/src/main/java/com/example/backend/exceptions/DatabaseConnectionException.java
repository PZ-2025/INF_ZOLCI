package com.example.backend.exceptions;

/**
 * Wyjątek reprezentujący błędy związane z połączeniem do bazy danych.
 * <p>
 * Jest używany do sygnalizowania problemów z bazą danych, które wymagają
 * obsługi na poziomie całej aplikacji.
 *

 * @version 1.0.0
 * @since 1.0.0
 */
public class DatabaseConnectionException extends RuntimeException {


    /**
     * Konstruktor z komunikatem błędu i przyczyną.
     *
     * @param message komunikat opisujący błąd
     * @param cause przyczyna wyjątku
     */
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}