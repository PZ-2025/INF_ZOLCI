package com.example.backend.services;

import com.example.backend.dto.TaskDTO;
import com.example.backend.models.*;
import com.example.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serwis obsługujący operacje dla encji {@link Task}.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;
    private final PriorityRepository priorityRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;

    /**
     * Konstruktor wstrzykujący zależności.
     */
    @Autowired
    public TaskService(TaskRepository taskRepository,
                       TeamRepository teamRepository,
                       PriorityRepository priorityRepository,
                       TaskStatusRepository taskStatusRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.teamRepository = teamRepository;
        this.priorityRepository = priorityRepository;
        this.taskStatusRepository = taskStatusRepository;
        this.userRepository = userRepository;
    }

    /**
     * Mapuje encję Task na obiekt DTO.
     */
    public TaskDTO mapToDTO(Task task) {
        if (task == null) return null;

        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());

        if (task.getTeam() != null) {
            dto.setTeamId(task.getTeam().getId());
        }

        dto.setPriorityId(task.getPriority().getId());
        dto.setStatusId(task.getStatus().getId());
        dto.setStartDate(task.getStartDate());
        dto.setDeadline(task.getDeadline());
        dto.setCompletedDate(task.getCompletedDate());
        dto.setCreatedById(task.getCreatedBy().getId());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        return dto;
    }

    /**
     * Mapuje obiekt DTO na encję Task.
     */
    public Task mapToEntity(TaskDTO dto) {
        if (dto == null) return null;

        Task task = new Task();
        task.setId(dto.getId());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());

        if (dto.getTeamId() != null) {
            teamRepository.findById(dto.getTeamId())
                    .ifPresent(task::setTeam);
        }

        priorityRepository.findById(dto.getPriorityId())
                .ifPresent(task::setPriority);

        taskStatusRepository.findById(dto.getStatusId())
                .ifPresent(task::setStatus);

        task.setStartDate(dto.getStartDate());
        task.setDeadline(dto.getDeadline());
        task.setCompletedDate(dto.getCompletedDate());

        userRepository.findById(dto.getCreatedById())
                .ifPresent(task::setCreatedBy);

        task.setCreatedAt(dto.getCreatedAt());
        task.setUpdatedAt(dto.getUpdatedAt());

        return task;
    }

    /**
     * Pobiera wszystkie zadania jako DTO.
     */
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera zadanie po ID jako DTO.
     */
    public Optional<TaskDTO> getTaskById(Integer id) {
        return taskRepository.findById(id)
                .map(this::mapToDTO);
    }

    /**
     * Zapisuje nowe zadanie jako DTO.
     */
    public TaskDTO saveTask(TaskDTO taskDTO) {
        Task task = mapToEntity(taskDTO);

        // Ustawiamy brakujące daty, jeśli nie zostały podane
        if (task.getCreatedAt() == null) {
            task.setCreatedAt(LocalDateTime.now());
        }

        Task savedTask = taskRepository.save(task);
        return mapToDTO(savedTask);
    }

    /**
     * Aktualizuje istniejące zadanie jako DTO.
     */
    public TaskDTO updateTask(TaskDTO taskDTO) {
        Task task = mapToEntity(taskDTO);

        // Aktualizujemy updatedAt
        task.setUpdatedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);
        return mapToDTO(updatedTask);
    }

    /**
     * Usuwa zadanie po ID.
     */
    public void deleteTask(Integer id) {
        taskRepository.deleteById(id);
    }

    /**
     * Pobiera zadania dla zespołu jako DTO.
     */
    public List<TaskDTO> getTasksByTeam(Team team) {
        return taskRepository.findByTeam(team).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera zadania o określonym statusie jako DTO.
     */
    public List<TaskDTO> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera zadania stworzone przez użytkownika jako DTO.
     */
    public List<TaskDTO> getTasksCreatedBy(User user) {
        return taskRepository.findByCreatedBy(user).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera zadania z terminem przed podaną datą jako DTO.
     */
    public List<TaskDTO> getTasksWithDeadlineBefore(LocalDate date) {
        return taskRepository.findByDeadlineBefore(date).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera zadanie po tytule jako DTO.
     */
    public Optional<TaskDTO> getTaskByTitle(String title) {
        return taskRepository.findByTitle(title)
                .map(this::mapToDTO);
    }

    /**
     * Pobiera zadania dla zespołu po ID jako DTO.
     */
    public List<TaskDTO> getTasksByTeamId(Integer teamId) {
        return taskRepository.findByTeamId(teamId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera zadania o określonym statusie po ID jako DTO.
     */
    public List<TaskDTO> getTasksByStatusId(Integer statusId) {
        return taskRepository.findByStatusId(statusId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera zadania o określonym priorytecie po ID jako DTO.
     */
    public List<TaskDTO> getTasksByPriorityId(Integer priorityId) {
        return taskRepository.findByPriorityId(priorityId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public static class TaskStatusService {
    }
}