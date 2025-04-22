package com.example.backend.services;

import com.example.backend.dto.TaskHistoryDTO;
import com.example.backend.models.Task;
import com.example.backend.models.TaskHistory;
import com.example.backend.models.User;
import com.example.backend.repository.TaskHistoryRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serwis obsługujący operacje dla encji {@link TaskHistory}.
 * <p>
 * Klasa zawiera logikę biznesową związaną z historią zmian zadań.
 * Implementuje operacje tworzenia, odczytu, aktualizacji i usuwania wpisów historii,
 * a także metody wyszukiwania historii według różnych kryteriów.
 *
 * @author Jakub
 * @version 1.1.0
 * @since 1.0.0
 */
@Service
@Transactional
public class TaskHistoryService {

    private final TaskHistoryRepository taskHistoryRepository;
    private final UserRepository userRepository;

    /**
     * Konstruktor wstrzykujący zależność do repozytorium historii zadań.
     *
     * @param taskHistoryRepository Repozytorium historii zadań
     * @param userRepository Repozytorium użytkowników
     */
    @Autowired
    public TaskHistoryService(TaskHistoryRepository taskHistoryRepository,
                              UserRepository userRepository) {
        this.taskHistoryRepository = taskHistoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * Mapuje encję TaskHistory na obiekt DTO.
     *
     * @param taskHistory Encja do mapowania
     * @return Obiekt DTO reprezentujący historię zmian zadania
     */
    private TaskHistoryDTO mapToDTO(TaskHistory taskHistory) {
        if (taskHistory == null) return null;

        TaskHistoryDTO dto = new TaskHistoryDTO();
        dto.setId(taskHistory.getId());

        if (taskHistory.getTask() != null) {
            dto.setTaskId(taskHistory.getTask().getId());
        }

        dto.setChangedById(taskHistory.getChangedBy());
        dto.setFieldName(taskHistory.getFieldName());
        dto.setOldValue(taskHistory.getOldValue());
        dto.setNewValue(taskHistory.getNewValue());
        dto.setChangedAt(taskHistory.getChangedAt());

        // Add user-related display fields if available
        userRepository.findById(taskHistory.getChangedBy()).ifPresent(user -> {
            dto.setChangedByUsername(user.getUsername());
            dto.setChangedByFullName(user.getFirstName() + " " + user.getLastName());
        });

        return dto;
    }

    /**
     * Mapuje obiekt DTO na encję TaskHistory.
     *
     * @param dto Obiekt DTO do mapowania
     * @param task Zadanie, którego dotyczy historia
     * @return Encja reprezentująca historię zmian zadania
     */
    private TaskHistory mapToEntity(TaskHistoryDTO dto, Task task) {
        if (dto == null) return null;

        TaskHistory entity = new TaskHistory();
        entity.setId(dto.getId());
        entity.setTask(task);
        entity.setChangedBy(dto.getChangedById());
        entity.setFieldName(dto.getFieldName());
        entity.setOldValue(dto.getOldValue());
        entity.setNewValue(dto.getNewValue());
        entity.setChangedAt(dto.getChangedAt() != null ? dto.getChangedAt() : LocalDateTime.now());

        return entity;
    }

    /**
     * Pobiera wszystkie wpisy historii zmian zadań jako DTO.
     *
     * @return Lista wszystkich wpisów historii jako DTO
     */
    public List<TaskHistoryDTO> getAllTaskHistory() {
        return taskHistoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera wpis historii na podstawie jego identyfikatora jako DTO.
     *
     * @param id Identyfikator wpisu historii
     * @return Opcjonalny wpis historii jako DTO, jeśli istnieje
     */
    public Optional<TaskHistoryDTO> getTaskHistoryById(Integer id) {
        return taskHistoryRepository.findById(id)
                .map(this::mapToDTO);
    }

    /**
     * Pobiera historię zmian dla określonego zadania jako DTO.
     *
     * @param task Zadanie, którego historia ma zostać pobrana
     * @return Lista wpisów historii dla zadania jako DTO
     */
    public List<TaskHistoryDTO> getHistoryByTask(Task task) {
        return taskHistoryRepository.findByTask(task).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera historię zmian dokonanych przez określonego użytkownika jako DTO.
     *
     * @param user Użytkownik, którego historia zmian ma zostać pobrana
     * @return Lista wpisów historii dokonanych przez użytkownika jako DTO
     */
    public List<TaskHistoryDTO> getHistoryByUser(User user) {
        return taskHistoryRepository.findByChangedBy(user).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Zapisuje nowy wpis historii lub aktualizuje istniejący na podstawie DTO.
     *
     * @param taskHistoryDTO DTO wpisu historii do zapisania
     * @param task Zadanie, którego dotyczy wpis historii
     * @return Zapisany wpis historii jako DTO
     */
    public TaskHistoryDTO saveTaskHistory(TaskHistoryDTO taskHistoryDTO, Task task) {
        TaskHistory taskHistory = mapToEntity(taskHistoryDTO, task);
        if (taskHistory.getChangedAt() == null) {
            taskHistory.setChangedAt(LocalDateTime.now());
        }
        TaskHistory savedHistory = taskHistoryRepository.save(taskHistory);
        return mapToDTO(savedHistory);
    }

    /**
     * Rejestruje zmianę pola w zadaniu.
     *
     * @param task      Zadanie, w którym dokonano zmiany
     * @param user      Użytkownik, który dokonał zmiany
     * @param fieldName Nazwa zmienionego pola
     * @param oldValue  Stara wartość pola
     * @param newValue  Nowa wartość pola
     * @return Utworzony wpis historii jako DTO
     */
    public TaskHistoryDTO logTaskChange(Task task, User user, String fieldName,
                                        String oldValue, String newValue) {
        TaskHistory history = new TaskHistory();
        history.setTask(task);
        history.setChangedBy(user.getId());
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setChangedAt(LocalDateTime.now());

        TaskHistory savedHistory = taskHistoryRepository.save(history);
        return mapToDTO(savedHistory);
    }

    /**
     * Usuwa wpis historii na podstawie jego identyfikatora.
     *
     * @param id Identyfikator wpisu historii do usunięcia
     */
    public void deleteTaskHistory(Integer id) {
        taskHistoryRepository.deleteById(id);
    }

    /**
     * Usuwa całą historię dla określonego zadania.
     *
     * @param task Zadanie, którego historia ma zostać usunięta
     * @return Liczba usuniętych wpisów historii
     */
    public int deleteAllHistoryForTask(Task task) {
        List<TaskHistory> history = taskHistoryRepository.findByTask(task);
        taskHistoryRepository.deleteAll(history);
        return history.size();
    }
}