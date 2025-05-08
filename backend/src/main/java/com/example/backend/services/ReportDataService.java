package com.example.backend.services;

import com.example.backend.dto.reports.*;
import com.example.backend.models.*;
import com.example.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportDataService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TaskCommentRepository taskCommentRepository;

    @Autowired
    public ReportDataService(TaskRepository taskRepository,
                             UserRepository userRepository,
                             TeamRepository teamRepository,
                             TeamMemberRepository teamMemberRepository,
                             TaskCommentRepository taskCommentRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.taskCommentRepository = taskCommentRepository;
    }

    public ConstructionProgressReportDTO collectConstructionProgressData(Integer teamId, String dateFrom, String dateTo) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(dateFrom, formatter);
        LocalDate endDate = LocalDate.parse(dateTo, formatter);

        // Fetch the team
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        // Fetch tasks for the team within the date range
        List<Task> tasks = taskRepository.findByTeam(team).stream()
                .filter(task -> {
                    LocalDate taskDate = task.getStartDate();
                    return taskDate != null &&
                            !taskDate.isBefore(startDate) &&
                            !taskDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        // Count completed and delayed tasks
        long completedCount = tasks.stream()
                .filter(task -> task.getCompletedDate() != null)
                .count();

        long delayedCount = tasks.stream()
                .filter(task -> {
                    if (task.getDeadline() == null || task.getCompletedDate() == null) {
                        return false;
                    }
                    return task.getCompletedDate().isAfter(task.getDeadline());
                })
                .count();

        int completedPercentage = tasks.isEmpty() ? 0 :
                (int) Math.round((double) completedCount / tasks.size() * 100);

        // Create data items
        List<ConstructionProgressItemDTO> items = tasks.stream()
                .map(task -> {
                    ConstructionProgressItemDTO item = new ConstructionProgressItemDTO();
                    item.setTaskName(task.getTitle());
                    item.setStatus(task.getStatus().getName());
                    item.setPlannedEnd(task.getDeadline());
                    item.setActualEnd(task.getCompletedDate());
                    return item;
                })
                .collect(Collectors.toList());

        // Create report DTO
        ConstructionProgressReportDTO reportDTO = new ConstructionProgressReportDTO();
        reportDTO.setItems(items);
        reportDTO.setDateFrom(dateFrom);
        reportDTO.setDateTo(dateTo);
        reportDTO.setCompletedPercentage(completedPercentage);
        reportDTO.setDelayedCount((int) delayedCount);

        return reportDTO;
    }

    public EmployeeLoadReportDTO collectEmployeeLoadData(Integer userId, String dateFrom, String dateTo) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(dateFrom, formatter);
        LocalDate endDate = LocalDate.parse(dateTo, formatter);

        // Get all users if userId is null, otherwise get specific user
        List<User> users;
        if (userId == null) {
            users = userRepository.findAll();
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            users = List.of(user);
        }

        // Create data items
        List<EmployeeLoadItemDTO> items = new ArrayList<>();

        for (User user : users) {
            // Get tasks created by the user
            List<Task> userTasks = taskRepository.findByCreatedBy(user).stream()
                    .filter(task -> {
                        LocalDate taskDate = task.getCreatedAt().toLocalDate();
                        return !taskDate.isBefore(startDate) && !taskDate.isAfter(endDate);
                    })
                    .collect(Collectors.toList());

            // Calculate total hours (here we assume 8 hours per task as an example)
            double totalHours = userTasks.size() * 8.0;

            EmployeeLoadItemDTO item = new EmployeeLoadItemDTO();
            item.setEmployeeId(user.getId());
            item.setEmployeeName(user.getFirstName() + " " + user.getLastName());
            item.setTaskCount(userTasks.size());
            item.setTotalHours(totalHours);
            items.add(item);
        }

        // Create report DTO
        EmployeeLoadReportDTO reportDTO = new EmployeeLoadReportDTO();
        reportDTO.setItems(items);
        reportDTO.setDateFrom(dateFrom);
        reportDTO.setDateTo(dateTo);

        return reportDTO;
    }

    public TeamEfficiencyReportDTO collectTeamEfficiencyData(String dateFrom, String dateTo) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(dateFrom, formatter);
        LocalDate endDate = LocalDate.parse(dateTo, formatter);

        // Get all teams
        List<Team> teams = teamRepository.findAll();

        // Create data items
        List<TeamEfficiencyItemDTO> items = new ArrayList<>();

        for (Team team : teams) {
            // Get tasks for the team
            List<Task> teamTasks = taskRepository.findByTeam(team).stream()
                    .filter(task -> {
                        LocalDate taskDate = task.getCreatedAt().toLocalDate();
                        return !taskDate.isBefore(startDate) && !taskDate.isAfter(endDate);
                    })
                    .collect(Collectors.toList());

            // Count open and closed issues
            long openIssues = teamTasks.stream()
                    .filter(task -> task.getCompletedDate() == null)
                    .count();

            long closedIssues = teamTasks.stream()
                    .filter(task -> task.getCompletedDate() != null)
                    .count();

            // Calculate average completion hours (assuming 8 hours per day between start and completion)
            double avgCompletionHours = teamTasks.stream()
                    .filter(task -> task.getStartDate() != null && task.getCompletedDate() != null)
                    .mapToLong(task -> {
                        long days = java.time.temporal.ChronoUnit.DAYS.between(
                                task.getStartDate(), task.getCompletedDate());
                        return days * 8; // 8 hours per day
                    })
                    .average()
                    .orElse(0);

            TeamEfficiencyItemDTO item = new TeamEfficiencyItemDTO();
            item.setTeamName(team.getName());
            item.setAvgCompletionHours(avgCompletionHours);
            item.setOpenIssues((int) openIssues);
            item.setClosedIssues((int) closedIssues);
            items.add(item);
        }

        // Create report DTO
        TeamEfficiencyReportDTO reportDTO = new TeamEfficiencyReportDTO();
        reportDTO.setItems(items);
        reportDTO.setDateFrom(dateFrom);
        reportDTO.setDateTo(dateTo);

        return reportDTO;
    }
}