package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Klasa DTO (Data Transfer Object) dla żądania rejestracji.
 * <p>
 * Zawiera wszystkie dane niezbędne do utworzenia nowego konta użytkownika w systemie.
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
public class RegisterRequestDTO {

    /**
     * Nazwa użytkownika (login) - musi być unikalna w systemie.
     */
    private String username;

    /**
     * Hasło użytkownika.
     */
    private String password;

    /**
     * Adres email użytkownika - musi być unikalny w systemie.
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
     * Numer telefonu użytkownika (opcjonalny).
     */
    private String phone;
}