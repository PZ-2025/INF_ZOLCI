
package com.example.backend.dto.reports;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class TeamEfficiencyItemDTO {
    private String teamName;
    private Double avgCompletionHours;
    private Integer openIssues;
    private Integer closedIssues;

    // Nowe pola
    private Integer completedTasksCount;
    private Integer totalTasksCount;
    private Integer onTimeTasksCount;
    private Integer delayedTasksCount;
    private Double avgDelayDays;
    private Integer activeTeamMembersCount;
    private Double tasksPerMember;
    private Map<String, Integer> tasksByPriority;
    private Double efficiencyScore;
    private Boolean hasNoTasks = false;
}