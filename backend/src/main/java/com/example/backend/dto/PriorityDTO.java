package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Klasa DTO (Data Transfer Object) dla priorytetu zadania.
 * <p>
 * Zawiera właściwości reprezentujące priorytet zadania w systemie,
 * używane do przekazywania danych między warstwami aplikacji.
 * Implementuje walidację danych wejściowych.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriorityDTO {

    private Integer id;

    @NotBlank(message = "Nazwa priorytetu nie może być pusta")
    private String name;

    @NotNull(message = "Wartość priorytetu jest wymagana")
    private Integer value;

    private String colorCode;
}