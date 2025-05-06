package com.example.backend.dto.reports;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TeamEfficiencyReportDTO {
    private List<TeamEfficiencyItemDTO> items;
    private String dateFrom;
    private String dateTo;
}
