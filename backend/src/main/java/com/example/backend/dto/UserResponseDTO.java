package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Klasa DTO (Data Transfer Object) dla odpowiedzi zawierającej dane użytkownika.
 * <p>
 * Zawiera dane użytkownika, które są bezpieczne do wysłania do klienta.
 * Nie zawiera wrażliwych danych, takich jak hasło.
 * Używa adnotacji Lombok do automatycznego generowania metod dostępowych,
 * konstruktorów i innych standardowych metod.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    /**
     * Identyfikator użytkownika.
     */
    private Integer id;

    /**
     * Nazwa użytkownika (login).
     */
    private String username;

    /**
     * Adres email użytkownika.
     */
    private String email;

    /**
     * Imię użytkownika.
     */
    private String firstName;

    /**
     * Nazwisko użytkownika.
     */
    private String lastName;

    /**
     * Numer telefonu użytkownika.
     */
    private String phone;

    /**
     * Rola użytkownika w systemie.
     */
    private String role;

    /**
     * Flaga określająca, czy konto użytkownika jest aktywne.
     */
    private Boolean isActive;

    /**
     * Data i czas utworzenia konta użytkownika.
     */
    private LocalDateTime createdAt;

    /**
     * Data i czas ostatniego logowania użytkownika.
     */
    private LocalDateTime lastLogin;
}