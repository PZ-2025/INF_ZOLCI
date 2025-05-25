package com.example.backend.services;

import com.example.backend.dto.reports.*;
import com.example.backend.models.*;
import com.example.backend.repository.*;
import org.example.reporting.model.TeamEfficiency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
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
        LocalDate currentDate = LocalDate.now();

        // Fetch the team
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        // ✅ NOWA LOGIKA: Fetch ALL tasks for the team within the date range
        List<Task> tasks = taskRepository.findByTeam(team).stream()
                .filter(task -> {
                    LocalDate taskDate = task.getStartDate();
                    return taskDate != null &&
                            !taskDate.isBefore(startDate) &&
                            !taskDate.isAfter(endDate);
                })
                // ✅ USUNIĘTO: Filtr wykluczający administratorów - bierzemy WSZYSTKIE zadania zespołu
                .collect(Collectors.toList());

        // ✅ Count completed tasks - sprawdź completed_date oraz status "Zakończone"
        long completedCount = tasks.stream()
                .filter(task -> {
                    // Zadanie ukończone jeśli ma completed_date lub status "Zakończone"
                    if (task.getCompletedDate() != null) {
                        return true;
                    }
                    if (task.getStatus() != null) {
                        return "zakończone".equalsIgnoreCase(task.getStatus().getName());
                    }
                    return false;
                })
                .count();

        // ✅ Count delayed tasks
        long delayedCount = tasks.stream()
                .filter(task -> {
                    if (task.getDeadline() == null) {
                        return false;
                    }

                    // Sprawdź czy zadanie jest opóźnione
                    if (task.getCompletedDate() == null) {
                        // Zadanie nie ukończone - sprawdź czy deadline minął
                        return currentDate.isAfter(task.getDeadline());
                    } else {
                        // Zadanie ukończone - sprawdź czy ukończono po deadline
                        return task.getCompletedDate().isAfter(task.getDeadline());
                    }
                })
                .count();

        // ✅ Calculate actual completed percentage based on task completion
        int totalCompletionSum = 0;
        for (Task task : tasks) {
            int taskCompletion;

            // Sprawdź czy zadanie ukończone
            if (task.getCompletedDate() != null ||
                    (task.getStatus() != null && "zakończone".equalsIgnoreCase(task.getStatus().getName()))) {
                taskCompletion = 100;
            } else if (task.getStatus() != null) {
                // Użyj progressMin ze statusu
                taskCompletion = task.getStatus().getProgressMin();
            } else {
                taskCompletion = 0;
            }
            totalCompletionSum += taskCompletion;
        }

        // Calculate average completion
        int completedPercentage = tasks.isEmpty() ? 0 : totalCompletionSum / tasks.size();

        // ✅ Count tasks by status
        Map<String, Long> tasksByStatus = tasks.stream()
                .filter(task -> task.getStatus() != null)
                .collect(Collectors.groupingBy(
                        task -> task.getStatus().getName(),
                        Collectors.counting()
                ));

        // ✅ Create data items for table
        List<ConstructionProgressItemDTO> items = tasks.stream()
                .map(task -> {
                    ConstructionProgressItemDTO item = new ConstructionProgressItemDTO();
                    item.setTaskName(task.getTitle());
                    item.setStatus(task.getStatus() != null ? task.getStatus().getName() : "Nieznany");
                    item.setPlannedEnd(task.getDeadline());
                    item.setActualEnd(task.getCompletedDate());

                    // ✅ Calculate delay information
                    boolean isDelayed = false;
                    int delayInDays = 0;

                    if (task.getDeadline() != null) {
                        if (task.getCompletedDate() == null) {
                            // Task not completed yet, check if current date is after deadline
                            if (currentDate.isAfter(task.getDeadline())) {
                                isDelayed = true;
                                delayInDays = (int) ChronoUnit.DAYS.between(task.getDeadline(), currentDate);
                            }
                        } else {
                            // Task completed, check if completion date is after deadline
                            if (task.getCompletedDate().isAfter(task.getDeadline())) {
                                isDelayed = true;
                                delayInDays = (int) ChronoUnit.DAYS.between(task.getDeadline(), task.getCompletedDate());
                            }
                        }
                    }

                    item.setDelayed(isDelayed);
                    item.setDelayInDays(delayInDays);

                    // Calculate completion percentage
                    int completionPercentage;
                    if (task.getCompletedDate() != null ||
                            (task.getStatus() != null && "zakończone".equalsIgnoreCase(task.getStatus().getName()))) {
                        completionPercentage = 100;
                    } else if (task.getStatus() != null) {
                        // Use progressMin for consistency
                        completionPercentage = task.getStatus().getProgressMin();
                    } else {
                        completionPercentage = 0;
                    }

                    item.setCompletionPercentage(completionPercentage);

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
        reportDTO.setTasksByStatus(tasksByStatus);

        return reportDTO;
    }

    public EmployeeLoadReportDTO collectEmployeeLoadData(Integer userId, String dateFrom, String dateTo) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(dateFrom, formatter);
        LocalDate endDate = LocalDate.parse(dateTo, formatter);
        LocalDate currentDate = LocalDate.now();

        // Get all users if userId is null, otherwise get specific user
        List<User> users;
        if (userId == null) {
            // Get all users excluding administrators
            users = userRepository.findAll().stream()
                    .filter(user -> !"administrator".equalsIgnoreCase(user.getRole()))
                    .collect(Collectors.toList());
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            users = List.of(user);
        }

        // Create data items
        List<EmployeeLoadItemDTO> items = new ArrayList<>();

        // Standard work hours per day
        final double WORK_HOURS_PER_DAY = 8.0;
        // Number of working days in the period
        long workingDays = ChronoUnit.DAYS.between(startDate, endDate.plusDays(1));
        // Simple adjustment for weekends (approximation)
        workingDays = workingDays - (workingDays * 2 / 7); // Subtract weekends

        // Ensure at least 1 day
        if (workingDays < 1) workingDays = 1;

        for (User user : users) {
            // Get tasks from user's teams instead of created by user
            List<Task> userTasks = getUserTeamTasks(user, startDate, endDate, currentDate);

            // Always create item for user (even without tasks)
            double totalHours = 0.0;
            Map<String, Integer> tasksByStatus = new HashMap<>();
            List<TaskDetailDTO> taskDetails = new ArrayList<>();

            for (Task task : userTasks) {
                // Collect status information - ensure it's never null
                String status = task.getStatus() != null ? task.getStatus().getName() : "Nieznany";
                tasksByStatus.merge(status, 1, Integer::sum);

                // Calculate estimated hours based on priority
                double priorityMultiplier = 1.0;
                if (task.getPriority() != null) {
                    int priorityValue = task.getPriority().getValue();
                    priorityMultiplier = 0.5 + (priorityValue * 0.25); // Scale from 0.75 to 1.5 based on priority
                }

                // Calculate task duration in days
                LocalDate taskStart = task.getStartDate() != null ? task.getStartDate() :
                        task.getCreatedAt().toLocalDate();
                LocalDate taskEnd = task.getCompletedDate() != null ? task.getCompletedDate() :
                        (task.getDeadline() != null ? task.getDeadline() : endDate);

                // Ensure dates are within report period
                if (taskStart.isBefore(startDate)) taskStart = startDate;
                if (taskEnd.isAfter(endDate)) taskEnd = endDate;

                // Calculate days between dates (simplified)
                long days = ChronoUnit.DAYS.between(taskStart, taskEnd.plusDays(1));
                // Simple adjustment for weekends (approximation)
                long businessDays = days - (days * 2 / 7); // Subtract weekends
                if (businessDays < 1) businessDays = 1; // At least 1 day

                // Calculate hours for this task
                double taskHours = businessDays * WORK_HOURS_PER_DAY * priorityMultiplier;
                totalHours += taskHours;

                // Determine if task is delayed
                boolean isDelayed = false;
                if (task.getDeadline() != null) {
                    if (task.getCompletedDate() == null) {
                        // Not completed yet, check if current date is past deadline
                        isDelayed = currentDate.isAfter(task.getDeadline());
                    } else {
                        // Completed, check if completion date is past deadline
                        isDelayed = task.getCompletedDate().isAfter(task.getDeadline());
                    }
                }

                // Add task details
                TaskDetailDTO detail = new TaskDetailDTO();
                detail.setTaskId(task.getId());
                detail.setTaskName(task.getTitle());
                detail.setStatus(status);
                detail.setPriority(task.getPriority() != null ? task.getPriority().getName() : "Standardowy");
                detail.setStartDate(task.getStartDate());
                detail.setDeadlineDate(task.getDeadline());
                detail.setCompletedDate(task.getCompletedDate());
                detail.setEstimatedHours(taskHours);
                detail.setDelayed(isDelayed);

                taskDetails.add(detail);
            }

            // Ensure we have status data - if not, create a default
            if (tasksByStatus.isEmpty()) {
                tasksByStatus.put("Brak zadań", 0);
            }

            // Calculate FTE equivalent (based on 8-hour workday)
            double totalPossibleHours = workingDays * WORK_HOURS_PER_DAY;
            double fteEquivalent;

            if (totalPossibleHours > 0) {
                fteEquivalent = totalHours / totalPossibleHours;
            } else {
                // Fallback if calculation fails
                fteEquivalent = totalHours / 160.0; // Assume standard month workload
            }

            // Guard against NaN or infinity
            if (Double.isNaN(fteEquivalent) || Double.isInfinite(fteEquivalent)) {
                fteEquivalent = 0.0;
            }

            // Create and add the employee load item
            EmployeeLoadItemDTO item = new EmployeeLoadItemDTO();
            item.setEmployeeId(user.getId());
            item.setEmployeeName(user.getFirstName() + " " + user.getLastName());
            item.setTaskCount(userTasks.size());
            item.setTotalHours(totalHours);
            item.setFteEquivalent(fteEquivalent);
            item.setTasks(taskDetails);
            item.setTasksByStatus(tasksByStatus);

            items.add(item);
        }

        // Create report DTO
        EmployeeLoadReportDTO reportDTO = new EmployeeLoadReportDTO();
        reportDTO.setItems(items);
        reportDTO.setDateFrom(dateFrom);
        reportDTO.setDateTo(dateTo);
        reportDTO.setWorkingDays((int) workingDays);

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

        // Liczniki do podsumowania ogólnego
        int teamsWithTasksCount = 0;
        int totalTasksCount = 0;
        int totalCompletedTasksCount = 0;

        for (Team team : teams) {
            // Get tasks for the team
            List<Task> teamTasks = taskRepository.findByTeam(team).stream()
                    .filter(task -> {
                        LocalDate taskDate = task.getCreatedAt().toLocalDate();
                        return !taskDate.isBefore(startDate) && !taskDate.isAfter(endDate);
                    })
                    .collect(Collectors.toList());

            TeamEfficiencyItemDTO item = new TeamEfficiencyItemDTO();
            item.setTeamName(team.getName());

            // Sprawdź czy zespół ma zadania
            if (teamTasks.isEmpty()) {
                // Zespół bez zadań - ustaw specjalny flag
                item.setHasNoTasks(true);
                item.setAvgCompletionHours(0.0);
                item.setOpenIssues(0);
                item.setClosedIssues(0);
                item.setCompletedTasksCount(0);
                item.setTotalTasksCount(0);
                item.setOnTimeTasksCount(0);
                item.setDelayedTasksCount(0);
                item.setAvgDelayDays(0.0);
                item.setActiveTeamMembersCount(teamMemberRepository.findByTeamAndIsActive(team, true).size());
                item.setTasksPerMember(0.0);
                item.setEfficiencyScore(0.0);

                // Dodaj pustą mapę priorytetów
                item.setTasksByPriority(new HashMap<>());
            } else {
                // Zespół z zadaniami - normalny przepływ
                teamsWithTasksCount++;
                totalTasksCount += teamTasks.size();

                // Count open and closed issues
                long openIssues = teamTasks.stream()
                        .filter(task -> task.getCompletedDate() == null)
                        .count();

                long closedIssues = teamTasks.stream()
                        .filter(task -> task.getCompletedDate() != null)
                        .count();

                totalCompletedTasksCount += closedIssues;

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

                // Policz zadania na czas i opóźnione
                long onTimeTasksCount = teamTasks.stream()
                        .filter(task -> {
                            if (task.getCompletedDate() == null || task.getDeadline() == null) {
                                return false;
                            }
                            return !task.getCompletedDate().isAfter(task.getDeadline());
                        })
                        .count();

                long delayedTasksCount = teamTasks.stream()
                        .filter(task -> {
                            if (task.getCompletedDate() == null || task.getDeadline() == null) {
                                return false;
                            }
                            return task.getCompletedDate().isAfter(task.getDeadline());
                        })
                        .count();

                // Oblicz średnie opóźnienie w dniach
                double avgDelayDays = teamTasks.stream()
                        .filter(task -> {
                            if (task.getCompletedDate() == null || task.getDeadline() == null) {
                                return false;
                            }
                            return task.getCompletedDate().isAfter(task.getDeadline());
                        })
                        .mapToLong(task ->
                                java.time.temporal.ChronoUnit.DAYS.between(task.getDeadline(), task.getCompletedDate())
                        )
                        .average()
                        .orElse(0);

                // Pobierz aktywnych członków zespołu
                int activeMembers = teamMemberRepository.findByTeamAndIsActive(team, true).size();

                // Zadania per członek zespołu
                double tasksPerMember = activeMembers > 0 ?
                        (double) teamTasks.size() / activeMembers : 0;

                // Grupuj zadania według priorytetu
                Map<String, Integer> tasksByPriority = teamTasks.stream()
                        .collect(Collectors.groupingBy(
                                task -> task.getPriority() != null ? task.getPriority().getName() : "Nieokreślony",
                                Collectors.summingInt(task -> 1)
                        ));

                // Oblicz wskaźnik efektywności (50% - zadania ukończone, 30% - zadania na czas, 20% - obciążenie członków)
                double completionRate = teamTasks.isEmpty() ? 0 :
                        (double) closedIssues / teamTasks.size() * 100;

                double onTimeRate = closedIssues > 0 ?
                        (double) onTimeTasksCount / closedIssues * 100 : 0;

                // Dla zespołów z 0 ukończonych zadań, pierwsza część (50%) i druga (30%) powinny być 0
                double efficiencyScore = 0;
                if (closedIssues > 0) {
                    efficiencyScore = (completionRate * 0.5) + (onTimeRate * 0.3);
                }

                // Dodaj tylko część związaną z obciążeniem zespołu
                double memberLoadBalance = activeMembers > 0 ?
                        Math.min(100, (teamTasks.size() / activeMembers) * 20) : 0;
                efficiencyScore += memberLoadBalance * 0.2;

                // Ogranicz do 100%
                efficiencyScore = Math.min(100, efficiencyScore);

                item.setAvgCompletionHours(avgCompletionHours);
                item.setOpenIssues((int) openIssues);
                item.setClosedIssues((int) closedIssues);
                item.setCompletedTasksCount((int) closedIssues);
                item.setTotalTasksCount(teamTasks.size());
                item.setOnTimeTasksCount((int) onTimeTasksCount);
                item.setDelayedTasksCount((int) delayedTasksCount);
                item.setAvgDelayDays(avgDelayDays);
                item.setActiveTeamMembersCount(activeMembers);
                item.setTasksPerMember(tasksPerMember);
                item.setTasksByPriority(tasksByPriority);
                item.setEfficiencyScore(efficiencyScore);
                item.setHasNoTasks(false);
            }

            items.add(item);
        }

        // Oblicz współczynnik ukończenia zadań
        double overallCompletionRate = totalTasksCount > 0 ?
                (double) totalCompletedTasksCount / totalTasksCount * 100 : 0;

        // Create report DTO
        TeamEfficiencyReportDTO reportDTO = new TeamEfficiencyReportDTO();
        reportDTO.setItems(items);
        reportDTO.setDateFrom(dateFrom);
        reportDTO.setDateTo(dateTo);

        // Dodaj parametry podsumowania do DTO
        Map<String, Object> summaryParameters = new HashMap<>();
        summaryParameters.put("teamsWithTasksCount", teamsWithTasksCount);
        summaryParameters.put("totalTeamsCount", teams.size());
        summaryParameters.put("totalTasksCount", totalTasksCount);
        summaryParameters.put("totalCompletedTasksCount", totalCompletedTasksCount);
        summaryParameters.put("overallCompletionRate", overallCompletionRate);
        reportDTO.setSummaryParameters(summaryParameters);

        return reportDTO;
    }

    /**
     * Pobiera wszystkie zadania zespołów w których użytkownik jest członkiem
     */
    private List<Task> getUserTeamTasks(User user, LocalDate startDate, LocalDate endDate, LocalDate currentDate) {
        List<Task> allUserTasks = new ArrayList<>();

        // Znajdź wszystkie zespoły użytkownika
        List<TeamMember> userTeamMemberships = teamMemberRepository.findByUser(user);

        for (TeamMember membership : userTeamMemberships) {
            if (membership.getIsActive()) {
                // Pobierz wszystkie zadania zespołu
                List<Task> teamTasks = taskRepository.findByTeam(membership.getTeam());

                // Filtruj zadania według dat
                List<Task> filteredTasks = teamTasks.stream()
                        .filter(task -> {
                            // Task is relevant if it overlaps with the date range
                            LocalDate taskStartDate = task.getStartDate() != null ?
                                    task.getStartDate() :
                                    task.getCreatedAt().toLocalDate();
                            LocalDate taskEndDate = task.getCompletedDate() != null ?
                                    task.getCompletedDate() : currentDate;

                            // Task is relevant if it overlaps with the date range
                            return (taskStartDate == null || !taskStartDate.isAfter(endDate)) &&
                                    (taskEndDate == null || !taskEndDate.isBefore(startDate));
                        })
                        .collect(Collectors.toList());

                allUserTasks.addAll(filteredTasks);
            }
        }

        // Usuń duplikaty (jeśli użytkownik jest w wielu zespołach z tym samym zadaniem)
        return allUserTasks.stream()
                .distinct()
                .collect(Collectors.toList());
    }
}