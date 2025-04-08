package com.example.backend.services;

import com.example.backend.models.TaskStatus;
import com.example.backend.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
     * Pobiera wszystkie statusy zadań.
     *
     * @return Lista wszystkich statusów zadań
     */
    public List<TaskStatus> getAllTaskStatuses() {
        return taskStatusRepository.findAll();
    }

    /**
     * Pobiera wszystkie statusy zadań posortowane według kolejności wyświetlania.
     *
     * @return Lista statusów zadań posortowanych według kolejności wyświetlania
     */
    public List<TaskStatus> getAllTaskStatusesSorted() {
        return taskStatusRepository.findAllByOrderByDisplayOrderAsc();
    }

    /**
     * Pobiera status zadania na podstawie jego identyfikatora.
     *
     * @param id Identyfikator statusu zadania
     * @return Opcjonalny status zadania, jeśli istnieje
     */
    public Optional<TaskStatus> getTaskStatusById(Integer id) {
        return taskStatusRepository.findById(id);
    }

    /**
     * Pobiera status zadania na podstawie jego nazwy.
     *
     * @param name Nazwa statusu zadania
     * @return Opcjonalny status zadania, jeśli istnieje
     */
    public Optional<TaskStatus> getTaskStatusByName(String name) {
        return taskStatusRepository.findByName(name);
    }

    /**
     * Zapisuje nowy status zadania lub aktualizuje istniejący.
     *
     * @param taskStatus Status zadania do zapisania
     * @return Zapisany status zadania
     */
    public TaskStatus saveTaskStatus(TaskStatus taskStatus) {
        return taskStatusRepository.save(taskStatus);
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
    public TaskStatus createTaskStatus(String name, Integer progressMin,
                                       Integer progressMax, Integer displayOrder) {
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName(name);
        taskStatus.setProgressMin(progressMin);
        taskStatus.setProgressMax(progressMax);
        taskStatus.setDisplayOrder(displayOrder);

        return taskStatusRepository.save(taskStatus);
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
     * Aktualizuje kolejność wyświetlania statusu zadania.
     *
     * @param id           Identyfikator statusu zadania
     * @param displayOrder Nowa kolejność wyświetlania
     * @return Zaktualizowany status zadania lub Optional.empty() jeśli status nie istnieje
     */
    public Optional<TaskStatus> updateDisplayOrder(Integer id, Integer displayOrder) {
        return taskStatusRepository.findById(id)
                .map(status -> {
                    status.setDisplayOrder(displayOrder);
                    return taskStatusRepository.save(status);
                });
    }
}