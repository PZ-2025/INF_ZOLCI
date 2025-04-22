package com.example.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Klasa DTO (Data Transfer Object) dla członka zespołu.
 * <p>
 * Zawiera właściwości reprezentujące członka zespołu w systemie,
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
public class TeamMemberDTO {

    private Integer id;

    @NotNull(message = "Identyfikator zespołu jest wymagany")
    private Integer teamId;

    @NotNull(message = "Identyfikator użytkownika jest wymagany")
    private Integer userId;

    private LocalDateTime joinedAt;

    private Boolean isActive = true;

    // Dodatkowe pola dla prezentacji w UI
    private String teamName;
    private String username;
    private String userFullName;
}