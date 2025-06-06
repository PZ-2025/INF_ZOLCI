package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class TeamDTO {
    
    private Integer id;
    
    @NotBlank(message = "Nazwa zespołu nie może być pusta")
    @Size(min = 2, max = 100, message = "Nazwa zespołu musi mieć od 2 do 100 znaków")
    private String name;

    @NotNull(message = "ID menedżera jest wymagane")
    private Integer managerId;
    
    private Boolean isActive = true;
}