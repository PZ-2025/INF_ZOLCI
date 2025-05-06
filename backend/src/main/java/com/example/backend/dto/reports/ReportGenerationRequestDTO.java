package com.example.backend.dto.reports;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ReportGenerationRequestDTO {
    private Integer reportTypeId;
    private String dateFrom;
    private String dateTo;
    private Integer targetId; // Team ID or Employee ID depending on report type
    private Map<String, Object> additionalParams;
}
