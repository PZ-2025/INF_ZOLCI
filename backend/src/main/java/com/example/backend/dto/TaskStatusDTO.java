package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO reprezentujące status zadania w API.
 * Zawiera tylko podstawowe pola bez relacji.
 */
@Getter
@Setter
public class TaskStatusDTO {
    
    private Integer id;
    
    @NotBlank(message = "Nazwa statusu nie może być pusta")
    private String name;
    
    @NotNull(message = "Minimalny postęp musi być określony")
    @Min(value = 0, message = "Minimalny postęp nie może być mniejszy niż 0")
    @Max(value = 100, message = "Minimalny postęp nie może być większy niż 100")
    private Integer progressMin;
    
    @NotNull(message = "Maksymalny postęp musi być określony")
    @Min(value = 0, message = "Maksymalny postęp nie może być mniejszy niż 0")
    @Max(value = 100, message = "Maksymalny postęp nie może być większy niż 100")
    private Integer progressMax;
    
    @NotNull(message = "Kolejność wyświetlania musi być określona")
    @Min(value = 0, message = "Kolejność wyświetlania nie może być ujemna")
    private Integer displayOrder;
}