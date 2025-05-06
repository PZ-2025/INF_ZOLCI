package com.example.backend.dto.reports;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeLoadItemDTO {
    private Integer employeeId;
    private String employeeName;
    private Integer taskCount;
    private Double totalHours;
}
