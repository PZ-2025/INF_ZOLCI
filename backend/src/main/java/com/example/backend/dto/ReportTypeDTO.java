package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO dla typu raportu.
 * Zawiera tylko podstawowe pola bez relacji.
 */
@Getter
@Setter
public class ReportTypeDTO {
    
    private Integer id;
    
    @NotBlank(message = "Nazwa typu raportu nie może być pusta")
    @Size(max = 100, message = "Nazwa typu raportu nie może przekraczać 100 znaków")
    private String name;
    
    private String description;
    
    private String templatePath;
}