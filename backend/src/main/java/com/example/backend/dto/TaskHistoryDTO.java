package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Klasa DTO (Data Transfer Object) dla historii zmian zadania.
 * <p>
 * Zawiera właściwości reprezentujące wpis historii zmian zadania w systemie,
 * używane do przekazywania danych między warstwami aplikacji.
 * Implementuje walidację danych wejściowych.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskHistoryDTO {

    private Integer id;

    @NotNull(message = "Identyfikator zadania jest wymagany")
    private Integer taskId;

    @NotNull(message = "Identyfikator użytkownika, który dokonał zmiany, jest wymagany")
    private Integer changedById;

    @NotBlank(message = "Nazwa pola, które zostało zmienione, nie może być pusta")
    private String fieldName;

    private String oldValue;

    private String newValue;

    private LocalDateTime changedAt;

    // Dodatkowe pola dla prezentacji w UI
    private String changedByUsername;
    private String changedByFullName;
}