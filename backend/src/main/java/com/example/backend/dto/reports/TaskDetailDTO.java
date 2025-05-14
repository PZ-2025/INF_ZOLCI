package com.example.backend.dto.reports;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskDetailDTO {
    private Integer taskId;
    private String taskName;
    private String status;
    private String priority;
    private LocalDate startDate;
    private LocalDate deadlineDate;
    private LocalDate completedDate;
    private Double estimatedHours;
    private boolean isDelayed;
}