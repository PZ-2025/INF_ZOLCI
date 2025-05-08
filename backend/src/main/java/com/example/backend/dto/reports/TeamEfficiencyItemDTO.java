package com.example.backend.dto.reports;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamEfficiencyItemDTO {
    private String teamName;
    private Double avgCompletionHours;
    private Integer openIssues;
    private Integer closedIssues;
}
