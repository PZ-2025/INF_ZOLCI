package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Klasa DTO (Data Transfer Object) dla żądania logowania.
 * <p>
 * Zawiera niezbędne dane do uwierzytelnienia użytkownika - nazwę użytkownika i hasło.
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
public class LoginRequestDTO {

    /**
     * Nazwa użytkownika do uwierzytelnienia.
     */
    private String username;

    /**
     * Hasło do uwierzytelnienia.
     */
    private String password;
}