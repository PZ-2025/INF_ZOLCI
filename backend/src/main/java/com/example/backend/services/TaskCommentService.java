package com.example.backend.services;

import com.example.backend.dto.TaskCommentDTO;
import com.example.backend.models.Task;
import com.example.backend.models.TaskComment;
import com.example.backend.models.User;
import com.example.backend.repository.TaskCommentRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serwis obsługujący operacje dla encji {@link TaskComment}.
 * <p>
 * Klasa zawiera logikę biznesową związaną z komentarzami do zadań.
 * Implementuje operacje tworzenia, odczytu, aktualizacji i usuwania komentarzy,
 * a także metody wyszukiwania komentarzy według różnych kryteriów.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@Transactional
public class TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    /**
     * Konstruktor wstrzykujący zależności.
     *
     * @param taskCommentRepository Repozytorium komentarzy do zadań
     * @param taskRepository Repozytorium zadań
     * @param userRepository Repozytorium użytkowników
     */
    @Autowired
    public TaskCommentService(TaskCommentRepository taskCommentRepository,
                              TaskRepository taskRepository,
                              UserRepository userRepository) {
        this.taskCommentRepository = taskCommentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    /**
     * Mapuje encję TaskComment na obiekt DTO.
     *
     * @param taskComment Encja do mapowania
     * @return Obiekt DTO reprezentujący komentarz do zadania
     */
    private TaskCommentDTO mapToDTO(TaskComment taskComment) {
        if (taskComment == null) return null;

        TaskCommentDTO dto = new TaskCommentDTO();
        dto.setId(taskComment.getId());

        if (taskComment.getTask() != null) {
            dto.setTaskId(taskComment.getTask().getId());
        }

        if (taskComment.getUser() != null) {
            dto.setUserId(taskComment.getUser().getId());
            dto.setUsername(taskComment.getUser().getUsername());
            dto.setUserFullName(taskComment.getUser().getFirstName() + " " +
                    taskComment.getUser().getLastName());
        }

        dto.setComment(taskComment.getComment());
        dto.setCreatedAt(taskComment.getCreatedAt());

        return dto;
    }

    /**
     * Mapuje obiekt DTO na encję TaskComment.
     *
     * @param dto Obiekt DTO do mapowania
     * @return Encja reprezentująca komentarz do zadania
     */
    private TaskComment mapToEntity(TaskCommentDTO dto) {
        if (dto == null) return null;

        TaskComment entity = new TaskComment();
        entity.setId(dto.getId());
        entity.setComment(dto.getComment());
        entity.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());

        if (dto.getTaskId() != null) {
            taskRepository.findById(dto.getTaskId())
                    .ifPresent(entity::setTask);
        }

        if (dto.getUserId() != null) {
            userRepository.findById(dto.getUserId())
                    .ifPresent(entity::setUser);
        }

        return entity;
    }

    /**
     * Pobiera wszystkie komentarze do zadań jako DTO.
     *
     * @return Lista wszystkich komentarzy jako DTO
     */
    public List<TaskCommentDTO> getAllTaskComments() {
        return taskCommentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera komentarz na podstawie jego identyfikatora jako DTO.
     *
     * @param id Identyfikator komentarza
     * @return Opcjonalny komentarz jako DTO, jeśli istnieje
     */
    public Optional<TaskCommentDTO> getTaskCommentById(Integer id) {
        return taskCommentRepository.findById(id)
                .map(this::mapToDTO);
    }

    /**
     * Pobiera wszystkie komentarze do określonego zadania jako DTO.
     *
     * @param task Zadanie, którego komentarze mają zostać pobrane
     * @return Lista komentarzy do zadania jako DTO
     */
    public List<TaskCommentDTO> getCommentsByTask(Task task) {
        return taskCommentRepository.findByTask(task).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera wszystkie komentarze dodane przez określonego użytkownika jako DTO.
     *
     * @param user Użytkownik, którego komentarze mają zostać pobrane
     * @return Lista komentarzy dodanych przez użytkownika jako DTO
     */
    public List<TaskCommentDTO> getCommentsByUser(User user) {
        return taskCommentRepository.findByUser(user).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Zapisuje nowy komentarz do zadania lub aktualizuje istniejący na podstawie DTO.
     *
     * @param taskCommentDTO DTO komentarza do zapisania
     * @return Zapisany komentarz jako DTO
     */
    public TaskCommentDTO saveTaskComment(TaskCommentDTO taskCommentDTO) {
        TaskComment taskComment = mapToEntity(taskCommentDTO);
        if (taskComment.getCreatedAt() == null) {
            taskComment.setCreatedAt(LocalDateTime.now());
        }
        TaskComment savedComment = taskCommentRepository.save(taskComment);
        return mapToDTO(savedComment);
    }

    /**
     * Dodaje nowy komentarz do zadania.
     *
     * @param task    Zadanie, do którego dodawany jest komentarz
     * @param user    Użytkownik dodający komentarz
     * @param comment Treść komentarza
     * @return Utworzony komentarz jako DTO
     */
    public TaskCommentDTO addCommentToTask(Task task, User user, String comment) {
        TaskComment taskComment = new TaskComment();
        taskComment.setTask(task);
        taskComment.setUser(user);
        taskComment.setComment(comment);
        taskComment.setCreatedAt(LocalDateTime.now());

        TaskComment savedComment = taskCommentRepository.save(taskComment);
        return mapToDTO(savedComment);
    }

    /**
     * Usuwa komentarz na podstawie jego identyfikatora.
     *
     * @param id Identyfikator komentarza do usunięcia
     */
    public void deleteTaskComment(Integer id) {
        taskCommentRepository.deleteById(id);
    }

    /**
     * Usuwa wszystkie komentarze do określonego zadania.
     *
     * @param task Zadanie, którego komentarze mają zostać usunięte
     * @return Liczba usuniętych komentarzy
     */
    public int deleteAllCommentsForTask(Task task) {
        List<TaskComment> comments = taskCommentRepository.findByTask(task);
        taskCommentRepository.deleteAll(comments);
        return comments.size();
    }
}