package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Klasa DTO (Data Transfer Object) dla ustawień systemowych.
 * <p>
 * Zawiera właściwości reprezentujące ustawienie systemowe w systemie,
 * używane do przekazywania danych między warstwami aplikacji.
 * Implementuje walidację danych wejściowych.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingDTO {

    private Integer id;

    @NotBlank(message = "Klucz ustawienia nie może być pusty")
    private String key;

    private String value;

    private String description;

    private Integer updatedById;

    private LocalDateTime updatedAt;

    // Dodatkowe pola dla prezentacji w UI
    private String updatedByUsername;
    private String updatedByFullName;
}