package com.example.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Klasa DTO do operacji na użytkownikach systemu.
 * Zawiera pełną reprezentację użytkownika do operacji CRUD.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Integer id;

    @NotBlank(message = "Nazwa użytkownika nie może być pusta")
    @Size(min = 3, max = 50, message = "Nazwa użytkownika musi mieć od 3 do 50 znaków")
    private String username;

    @NotBlank(message = "Hasło nie może być puste")
    @Size(min = 6, message = "Hasło musi mieć przynajmniej 6 znaków")
    private String password;

    @NotBlank(message = "Email nie może być pusty")
    @Email(message = "Nieprawidłowy format adresu email")
    @Size(max = 100, message = "Email nie może przekraczać 100 znaków")
    private String email;

    @NotBlank(message = "Imię nie może być puste")
    @Size(max = 50, message = "Imię nie może przekraczać 50 znaków")
    private String firstName;

    @NotBlank(message = "Nazwisko nie może być puste")
    @Size(max = 50, message = "Nazwisko nie może przekraczać 50 znaków")
    private String lastName;

    @Size(max = 15, message = "Numer telefonu nie może przekraczać 15 znaków")
    private String phone;

    private String role;

    private Boolean isActive = true;

    private LocalDateTime createdAt;

    private LocalDateTime lastLogin;
}