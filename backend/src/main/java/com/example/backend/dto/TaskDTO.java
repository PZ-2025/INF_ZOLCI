package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Klasa DTO (Data Transfer Object) dla zadania.
 * <p>
 * Zawiera właściwości reprezentujące zadanie w systemie,
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
public class TaskDTO {

    private Integer id;
    
    @NotBlank(message = "Tytuł zadania nie może być pusty")
    private String title;

    private String description;

    private Integer teamId;

    @NotNull(message = "Priorytet zadania jest wymagany")
    private Integer priorityId;

    @NotNull(message = "Status zadania jest wymagany")
    private Integer statusId;

    private LocalDate startDate;

    private LocalDate deadline;

    private LocalDate completedDate;

    @NotNull(message = "Informacja o twórcy zadania jest wymagana")
    private Integer createdById;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}