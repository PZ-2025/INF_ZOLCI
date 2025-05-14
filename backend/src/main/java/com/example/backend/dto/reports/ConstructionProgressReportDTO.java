package com.example.backend.dto.reports;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ConstructionProgressReportDTO {
    private List<ConstructionProgressItemDTO> items;
    private String dateFrom;
    private String dateTo;
    private Integer completedPercentage;
    private Integer delayedCount;
    private Map<String, Long> tasksByStatus; // Dodane pole dla wykresu kołowego
}