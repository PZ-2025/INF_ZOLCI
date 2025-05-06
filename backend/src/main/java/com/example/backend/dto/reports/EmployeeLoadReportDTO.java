package com.example.backend.dto.reports;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EmployeeLoadReportDTO {
    private List<EmployeeLoadItemDTO> items;
    private String dateFrom;
    private String dateTo;
}
