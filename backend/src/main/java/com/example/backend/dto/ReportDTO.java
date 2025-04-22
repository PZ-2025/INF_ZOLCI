package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Klasa DTO (Data Transfer Object) dla raportu.
 * <p>
 * Zawiera właściwości reprezentujące raport w systemie,
 * używane do przekazywania danych między warstwami aplikacji.
 * Implementuje walidację danych wejściowych.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {

    private Integer id;

    @NotBlank(message = "Nazwa raportu nie może być pusta")
    private String name;

    @NotNull(message = "Typ raportu jest wymagany")
    private Integer typeId;

    @NotNull(message = "Informacja o twórcy raportu jest wymagana")
    private Integer createdById;

    private String parameters;

    private String fileName;

    private String filePath;

    private LocalDateTime createdAt;

    // Dodatkowe pola dla prezentacji w UI
    private String typeName;
    private String createdByUsername;
    private String createdByFullName;
}