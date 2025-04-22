package com.example.backend.services;

import com.example.backend.dto.TaskStatusDTO;
import com.example.backend.models.TaskStatus;
import com.example.backend.repository.TaskStatusRepository;
import com.example.backend.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serwis obsługujący operacje dla encji {@link TaskStatus}.
 * <p>
 * Klasa zawiera logikę biznesową związaną ze statusami zadań.
 * Implementuje operacje tworzenia, odczytu, aktualizacji i usuwania statusów,
 * a także metody wyszukiwania i sortowania statusów.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@Transactional
public class TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskRepository taskRepository; // dodaj tę zależność

    @Value("${task.default-status-id:1}") // dodaj właściwość w application.properties
    private Integer defaultStatusId;

    /**
     * Konstruktor wstrzykujący zależność do repozytorium statusów zadań.
     *
     * @param taskStatusRepository Repozytorium statusów zadań
     */
    @Autowired
    public TaskStatusService(TaskStatusRepository taskStatusRepository) {
        this.taskStatusRepository = taskStatusRepository;
    }

    private TaskStatusDTO mapToDTO(TaskStatus status) {
        if (status == null) return null;
        TaskStatusDTO dto = new TaskStatusDTO();
        dto.setId(status.getId());
        dto.setName(status.getName());
        dto.setProgressMin(status.getProgressMin());
        dto.setProgressMax(status.getProgressMax());
        dto.setDisplayOrder(status.getDisplayOrder());
        return dto;
    }

    private TaskStatus mapToEntity(TaskStatusDTO dto) {
        if (dto == null) return null;
        TaskStatus status = new TaskStatus();
        status.setId(dto.getId());
        status.setName(dto.getName());
        status.setProgressMin(dto.getProgressMin());
        status.setProgressMax(dto.getProgressMax());
        status.setDisplayOrder(dto.getDisplayOrder());
        return status;
    }

    /**
     * Pobiera wszystkie statusy zadań.
     *
     * @return Lista wszystkich statusów zadań
     */
    public List<TaskStatusDTO> getAllTaskStatuses() {
        return taskStatusRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera wszystkie statusy zadań posortowane według kolejności wyświetlania.
     *
     * @return Lista statusów zadań posortowanych według kolejności wyświetlania
     */
    public List<TaskStatusDTO> getAllTaskStatusesSorted() {
        return taskStatusRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera status zadania na podstawie jego identyfikatora.
     *
     * @param id Identyfikator statusu zadania
     * @return Opcjonalny status zadania, jeśli istnieje
     */
    public Optional<TaskStatusDTO> getTaskStatusById(Integer id) {
        return taskStatusRepository.findById(id).map(this::mapToDTO);
    }

    /**
     * Pobiera status zadania na podstawie jego nazwy.
     *
     * @param name Nazwa statusu zadania
     * @return Opcjonalny status zadania, jeśli istnieje
     */
    public Optional<TaskStatusDTO> getTaskStatusByName(String name) {
        return taskStatusRepository.findByName(name).map(this::mapToDTO);
    }

    /**
     * Zapisuje nowy status zadania lub aktualizuje istniejący.
     *
     * @param dto Status zadania do zapisania
     * @return Zapisany status zadania
     */
    public TaskStatusDTO saveTaskStatus(TaskStatusDTO dto) {
        TaskStatus entity = mapToEntity(dto);
        TaskStatus saved = taskStatusRepository.save(entity);
        return mapToDTO(saved);
    }

    /**
     * Tworzy nowy status zadania z podanymi parametrami.
     *
     * @param name          Nazwa statusu zadania
     * @param progressMin   Minimalny procent postępu dla tego statusu
     * @param progressMax   Maksymalny procent postępu dla tego statusu
     * @param displayOrder  Kolejność wyświetlania statusu
     * @return Utworzony status zadania
     */
    public TaskStatusDTO createTaskStatus(String name, Integer progressMin,
                                          Integer progressMax, Integer displayOrder) {
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName(name);
        taskStatus.setProgressMin(progressMin);
        taskStatus.setProgressMax(progressMax);
        taskStatus.setDisplayOrder(displayOrder);

        TaskStatus saved = taskStatusRepository.save(taskStatus);
        return mapToDTO(saved);
    }

    /**
     * Usuwa status zadania na podstawie jego identyfikatora.
     *
     * @param id Identyfikator statusu zadania do usunięcia
     * @throws IllegalStateException jeśli do statusu są przypisane zadania
     */
    @Transactional
    public void deleteTaskStatus(Integer id) {
        TaskStatus statusToDelete = taskStatusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Status not found"));

        // Nie pozwól usunąć domyślnego statusu
        if (id.equals(defaultStatusId)) {
            throw new IllegalStateException("Cannot delete default status");
        }

        // Pobierz domyślny status
        TaskStatus defaultStatus = taskStatusRepository.findById(defaultStatusId)
                .orElseThrow(() -> new RuntimeException("Default status not found"));

        // Zaktualizuj wszystkie zadania ze statusem do usunięcia
        statusToDelete.getTasks().forEach(task -> {
            task.setStatus(defaultStatus);
            taskRepository.save(task);
        });

        // Teraz możemy bezpiecznie usunąć status
        taskStatusRepository.delete(statusToDelete);
    }

    /**
     * Sprawdza, czy status zadania o podanej nazwie już istnieje.
     *
     * @param name Nazwa statusu zadania do sprawdzenia
     * @return true jeśli status zadania istnieje, false w przeciwnym razie
     */
    public boolean existsByName(String name) {
        return taskStatusRepository.findByName(name).isPresent();
    }

    /**
     * Aktualizuje kolejność wyświetlania statusu zadania.
     *
     * @param id           Identyfikator statusu zadania
     * @param displayOrder Nowa kolejność wyświetlania
     * @return Zaktualizowany status zadania lub Optional.empty() jeśli status nie istnieje
     */
    public Optional<TaskStatusDTO> updateDisplayOrder(Integer id, Integer displayOrder) {
        return taskStatusRepository.findById(id)
                .map(status -> {
                    status.setDisplayOrder(displayOrder);
                    return mapToDTO(taskStatusRepository.save(status));
                });
    }
}