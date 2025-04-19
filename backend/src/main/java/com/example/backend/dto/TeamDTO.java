package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeamDTO {
    
    private Integer id;
    
    @NotBlank(message = "Nazwa zespołu nie może być pusta")
    private String name;
    
    @NotNull(message = "ID menedżera jest wymagane")
    private Integer managerId;
    
    private Boolean isActive = true;
}