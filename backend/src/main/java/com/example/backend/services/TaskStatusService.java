package com.example.backend.services;

import com.example.backend.dto.TaskStatusDTO;
import com.example.backend.models.TaskStatus;
import com.example.backend.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * Konstruktor wstrzykujący zależność do repozytorium statusów zadań.
     *
     * @param taskStatusRepository Repozytorium statusów zadań
     */
    @Autowired
    public TaskStatusService(TaskStatusRepository taskStatusRepository) {
        this.taskStatusRepository = taskStatusRepository;
    }

    /**
     * Mapuje encję TaskStatus na obiekt DTO.
     *
     * @param taskStatus Encja do mapowania
     * @return Obiekt DTO reprezentujący status zadania
     */
    private TaskStatusDTO mapToDTO(TaskStatus taskStatus) {
        if (taskStatus == null) return null;

        TaskStatusDTO dto = new TaskStatusDTO();
        dto.setId(taskStatus.getId());
        dto.setName(taskStatus.getName());
        dto.setProgressMin(taskStatus.getProgressMin());
        dto.setProgressMax(taskStatus.getProgressMax());
        dto.setDisplayOrder(taskStatus.getDisplayOrder());

        return dto;
    }

    /**
     * Mapuje obiekt DTO na encję TaskStatus.
     *
     * @param dto Obiekt DTO do mapowania
     * @return Encja reprezentująca status zadania
     */
    private TaskStatus mapToEntity(TaskStatusDTO dto) {
        if (dto == null) return null;

        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setId(dto.getId());
        taskStatus.setName(dto.getName());
        taskStatus.setProgressMin(dto.getProgressMin());
        taskStatus.setProgressMax(dto.getProgressMax());
        taskStatus.setDisplayOrder(dto.getDisplayOrder());

        return taskStatus;
    }

    /**
     * Pobiera wszystkie statusy zadań jako obiekty DTO.
     *
     * @return Lista wszystkich statusów zadań jako DTO
     */
    public List<TaskStatusDTO> getAllTaskStatuses() {
        return taskStatusRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera wszystkie statusy zadań posortowane według kolejności wyświetlania jako obiekty DTO.
     *
     * @return Lista statusów zadań posortowanych według kolejności wyświetlania jako DTO
     */
    public List<TaskStatusDTO> getAllTaskStatusesSorted() {
        return taskStatusRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera status zadania na podstawie jego identyfikatora jako obiekt DTO.
     *
     * @param id Identyfikator statusu zadania
     * @return Opcjonalny status zadania jako DTO, jeśli istnieje
     */
    public Optional<TaskStatusDTO> getTaskStatusById(Integer id) {
        return taskStatusRepository.findById(id)
                .map(this::mapToDTO);
    }

    /**
     * Pobiera status zadania na podstawie jego nazwy jako obiekt DTO.
     *
     * @param name Nazwa statusu zadania
     * @return Opcjonalny status zadania jako DTO, jeśli istnieje
     */
    public Optional<TaskStatusDTO> getTaskStatusByName(String name) {
        return taskStatusRepository.findByName(name)
                .map(this::mapToDTO);
    }

    /**
     * Zapisuje nowy status zadania lub aktualizuje istniejący na podstawie DTO.
     *
     * @param taskStatusDTO DTO statusu zadania do zapisania
     * @return Zapisany status zadania jako DTO
     */
    public TaskStatusDTO saveTaskStatus(TaskStatusDTO taskStatusDTO) {
        TaskStatus taskStatus = mapToEntity(taskStatusDTO);
        TaskStatus savedTaskStatus = taskStatusRepository.save(taskStatus);
        return mapToDTO(savedTaskStatus);
    }

    /**
     * Tworzy nowy status zadania z podanymi parametrami i zwraca go jako DTO.
     *
     * @param name          Nazwa statusu zadania
     * @param progressMin   Minimalny procent postępu dla tego statusu
     * @param progressMax   Maksymalny procent postępu dla tego statusu
     * @param displayOrder  Kolejność wyświetlania statusu
     * @return Utworzony status zadania jako DTO
     */
    public TaskStatusDTO createTaskStatus(String name, Integer progressMin,
                                             Integer progressMax, Integer displayOrder) {
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName(name);
        taskStatus.setProgressMin(progressMin);
        taskStatus.setProgressMax(progressMax);
        taskStatus.setDisplayOrder(displayOrder);

        TaskStatus savedTaskStatus = taskStatusRepository.save(taskStatus);
        return mapToDTO(savedTaskStatus);
    }

    /**
     * Usuwa status zadania na podstawie jego identyfikatora.
     *
     * @param id Identyfikator statusu zadania do usunięcia
     * @throws IllegalStateException jeśli do statusu są przypisane zadania
     */
    public void deleteTaskStatus(Integer id) {
        Optional<TaskStatus> statusOpt = taskStatusRepository.findById(id);

        if (statusOpt.isPresent()) {
            TaskStatus status = statusOpt.get();

            if (status.getTasks() != null && !status.getTasks().isEmpty()) {
                throw new IllegalStateException("Nie można usunąć statusu, do którego przypisane są zadania");
            }

            taskStatusRepository.deleteById(id);
        }
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
     * Aktualizuje kolejność wyświetlania statusu zadania i zwraca zaktualizowany status jako DTO.
     *
     * @param id           Identyfikator statusu zadania
     * @param displayOrder Nowa kolejność wyświetlania
     * @return Zaktualizowany status zadania jako DTO lub Optional.empty() jeśli status nie istnieje
     */
    public Optional<TaskStatusDTO> updateDisplayOrder(Integer id, Integer displayOrder) {
        return taskStatusRepository.findById(id)
                .map(status -> {
                    status.setDisplayOrder(displayOrder);
                    TaskStatus updatedStatus = taskStatusRepository.save(status);
                    return mapToDTO(updatedStatus);
                });
    }
}