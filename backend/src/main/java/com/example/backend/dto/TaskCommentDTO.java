package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Klasa DTO (Data Transfer Object) dla komentarza do zadania.
 * <p>
 * Zawiera właściwości reprezentujące komentarz do zadania w systemie,
 * używane do przekazywania danych między warstwami aplikacji.
 * Implementuje walidację danych wejściowych.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCommentDTO {

    private Integer id;

    @NotNull(message = "Identyfikator zadania jest wymagany")
    private Integer taskId;

    @NotNull(message = "Identyfikator użytkownika jest wymagany")
    private Integer userId;

    @NotBlank(message = "Treść komentarza nie może być pusta")
    private String comment;

    private LocalDateTime createdAt;

    // Dodatkowe pola dla prezentacji w UI
    private String username;
    private String userFullName;
}