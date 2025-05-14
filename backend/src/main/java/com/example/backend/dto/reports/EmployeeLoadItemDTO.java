package com.example.backend.dto.reports;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class EmployeeLoadItemDTO {
    private Integer employeeId;
    private String employeeName;
    private Integer taskCount;
    private Double totalHours;
    private Double fteEquivalent;
    private List<TaskDetailDTO> tasks;
    private Map<String, Integer> tasksByStatus;
}