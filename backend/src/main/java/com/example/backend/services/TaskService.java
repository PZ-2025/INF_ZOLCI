package com.example.backend.services;

import com.example.backend.dto.TaskDTO;
import com.example.backend.models.*;
import com.example.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serwis obsługujący operacje dla encji {@link Task}.
 */
@Service
@Transactional
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

        if (task.getPriority() != null) {
            dto.setPriorityId(task.getPriority().getId());
        }

        if (task.getStatus() != null) {
            dto.setStatusId(task.getStatus().getId());
        }

        if (task.getCreatedBy() != null) {
            dto.setCreatedById(task.getCreatedBy().getId());
        }

        dto.setStartDate(task.getStartDate());
        dto.setDeadline(task.getDeadline());
        dto.setCompletedDate(task.getCompletedDate());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        return dto;
    }

    /**
     * Mapuje obiekt DTO na encję Task, zachowując istniejące wartości dla pól, które są null w DTO.
     */
    public Task mapToEntity(TaskDTO dto, Task existingTask) {
        if (dto == null) return null;

        Task task = existingTask != null ? existingTask : new Task();

        // Aktualizujemy tylko pola które są ustawione w DTO
        if (dto.getTitle() != null) {
            task.setTitle(dto.getTitle());
        }

        if (dto.getDescription() != null) {
            task.setDescription(dto.getDescription());
        }

        // Aktualizujemy zespół tylko jeśli teamId jest ustawione w DTO
        if (dto.getTeamId() != null) {
            teamRepository.findById(dto.getTeamId())
                    .ifPresent(task::setTeam);
        }

        if (dto.getPriorityId() != null) {
            priorityRepository.findById(dto.getPriorityId())
                    .ifPresent(task::setPriority);
        }

        if (dto.getStatusId() != null) {
            taskStatusRepository.findById(dto.getStatusId())
                    .ifPresent(task::setStatus);
        }

        if (dto.getStartDate() != null) {
            task.setStartDate(dto.getStartDate());
        }

        if (dto.getDeadline() != null) {
            task.setDeadline(dto.getDeadline());
        }

        if (dto.getCompletedDate() != null) {
            task.setCompletedDate(dto.getCompletedDate());
        }

        if (dto.getCreatedById() != null) {
            userRepository.findById(dto.getCreatedById())
                    .ifPresent(task::setCreatedBy);
        }

        // Dla nowej encji ustawiamy createdAt
        if (existingTask == null && dto.getCreatedAt() == null) {
            task.setCreatedAt(LocalDateTime.now());
        } else if (dto.getCreatedAt() != null) {
            task.setCreatedAt(dto.getCreatedAt());
        }

        // Zawsze aktualizujemy updatedAt podczas modyfikacji
        task.setUpdatedAt(LocalDateTime.now());

        return task;
    }

    /**
     * Stara wersja mapToEntity - pozostawiona dla zachowania kompatybilności.
     */
    public Task mapToEntity(TaskDTO dto) {
        return mapToEntity(dto, null);
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
     * Pobiera encję zadania po ID.
     */
    public Optional<Task> getTaskEntityById(Integer id) {
        return taskRepository.findById(id);
    }

    /**
     * Zapisuje nowe zadanie jako DTO.
     */
    public TaskDTO saveTask(TaskDTO taskDTO) {
        Task task = mapToEntity(taskDTO, null);

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
        // Pobieramy istniejące zadanie, aby zachować wartości które nie zostały zmienione
        Optional<Task> existingTaskOpt = taskRepository.findById(taskDTO.getId());

        if (existingTaskOpt.isPresent()) {
            Task existingTask = existingTaskOpt.get();

            // Zachowujemy relację do zespołu, jeśli nie jest ustawiona w DTO
            if (taskDTO.getTeamId() == null && existingTask.getTeam() != null) {
                taskDTO.setTeamId(existingTask.getTeam().getId());
            }

            // Zachowujemy relację do priorytetu, jeśli nie jest ustawiona w DTO
            if (taskDTO.getPriorityId() == null && existingTask.getPriority() != null) {
                taskDTO.setPriorityId(existingTask.getPriority().getId());
            }

            // Zachowujemy relację do statusu, jeśli nie jest ustawiona w DTO
            if (taskDTO.getStatusId() == null && existingTask.getStatus() != null) {
                taskDTO.setStatusId(existingTask.getStatus().getId());
            }

            // Zachowujemy relację do twórcy, jeśli nie jest ustawiona w DTO
            if (taskDTO.getCreatedById() == null && existingTask.getCreatedBy() != null) {
                taskDTO.setCreatedById(existingTask.getCreatedBy().getId());
            }

            // Zachowujemy oryginalną datę utworzenia
            if (taskDTO.getCreatedAt() == null && existingTask.getCreatedAt() != null) {
                taskDTO.setCreatedAt(existingTask.getCreatedAt());
            }

            // Użyj istniejącej encji jako bazy przy mapowaniu
            Task task = mapToEntity(taskDTO, existingTask);

            // Aktualizujemy updatedAt
            task.setUpdatedAt(LocalDateTime.now());

            Task updatedTask = taskRepository.save(task);
            return mapToDTO(updatedTask);
        } else {
            // Jeśli zadanie nie istnieje, traktujemy to jako utworzenie nowego
            return saveTask(taskDTO);
        }
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
}