package com.example.backend.dto.reports;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ConstructionProgressItemDTO {
    private String taskName;
    private String status;
    private LocalDate plannedEnd;
    private LocalDate actualEnd;
    private boolean isDelayed;
    private Integer delayInDays;
    private Integer completionPercentage;
}