
package com.example.backend.controllers;

import com.example.backend.models.TaskStatus;
import com.example.backend.services.TaskStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Kontroler REST dla operacji na statusach zadań.
 * <p>
 * Klasa zapewnia endpoints do zarządzania statusami zadań w systemie,
 * w tym tworzenie, odczyt, aktualizację i usuwanie statusów.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/database/task-statuses")
public class TaskStatusController {

    private final TaskStatusService taskStatusService;

    /**
     * Konstruktor wstrzykujący zależność do serwisu statusów zadań.
     *
     * @param taskStatusService Serwis statusów zadań
     */
    @Autowired
    public TaskStatusController(TaskStatusService taskStatusService) {
        this.taskStatusService = taskStatusService;
    }

    /**
     * Pobiera wszystkie statusy zadań.
     *
     * @return Lista wszystkich statusów zadań
     */
    @GetMapping
    public ResponseEntity<List<TaskStatus>> getAllTaskStatuses() {
        List<TaskStatus> statuses = taskStatusService.getAllTaskStatuses();
        return new ResponseEntity<>(statuses, HttpStatus.OK);
    }

    /**
     * Pobiera wszystkie statusy zadań posortowane według kolejności wyświetlania.
     *
     * @return Lista statusów zadań posortowanych według kolejności wyświetlania
     */
    @GetMapping("/sorted")
    public ResponseEntity<List<TaskStatus>> getAllTaskStatusesSorted() {
        List<TaskStatus> statuses = taskStatusService.getAllTaskStatusesSorted();
        return new ResponseEntity<>(statuses, HttpStatus.OK);
    }

    /**
     * Pobiera status zadania na podstawie jego identyfikatora.
     *
     * @param id Identyfikator statusu zadania
     * @return Status zadania lub status 404, jeśli nie istnieje
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskStatus> getTaskStatusById(@PathVariable Integer id) {
        return taskStatusService.getTaskStatusById(id)
                .map(status -> new ResponseEntity<>(status, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Pobiera status zadania na podstawie jego nazwy.
     *
     * @param name Nazwa statusu zadania
     * @return Status zadania lub status 404, jeśli nie istnieje
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<TaskStatus> getTaskStatusByName(@PathVariable String name) {
        return taskStatusService.getTaskStatusByName(name)
                .map(status -> new ResponseEntity<>(status, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Tworzy nowy status zadania.
     *
     * @param taskStatus Dane nowego statusu zadania
     * @return Utworzony status zadania
     */
    @PostMapping
    public ResponseEntity<TaskStatus> createTaskStatus(@RequestBody TaskStatus taskStatus) {
        if (taskStatusService.existsByName(taskStatus.getName())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        TaskStatus savedStatus = taskStatusService.saveTaskStatus(taskStatus);
        return new ResponseEntity<>(savedStatus, HttpStatus.CREATED);
    }

    /**
     * Tworzy nowy status zadania na podstawie podanych parametrów.
     *
     * @param payload Mapa zawierająca name, progressMin, progressMax, displayOrder
     * @return Utworzony status zadania lub status błędu
     */
    @PostMapping("/create")
    public ResponseEntity<TaskStatus> createTaskStatusFromParams(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        Integer progressMin = (Integer) payload.get("progressMin");
        Integer progressMax = (Integer) payload.get("progressMax");
        Integer displayOrder = (Integer) payload.get("displayOrder");

        if (name == null || progressMin == null || progressMax == null || displayOrder == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (taskStatusService.existsByName(name)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        TaskStatus newStatus = taskStatusService.createTaskStatus(
                name, progressMin, progressMax, displayOrder);

        return new ResponseEntity<>(newStatus, HttpStatus.CREATED);
    }

    /**
     * Aktualizuje istniejący status zadania.
     *
     * @param id         Identyfikator statusu zadania
     * @param taskStatus Zaktualizowane dane statusu zadania
     * @return Zaktualizowany status zadania lub status 404, jeśli nie istnieje
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskStatus> updateTaskStatus(@PathVariable Integer id,
                                                       @RequestBody TaskStatus taskStatus) {
        if (!taskStatusService.getTaskStatusById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        taskStatus.setId(id);
        TaskStatus updatedStatus = taskStatusService.saveTaskStatus(taskStatus);
        return new ResponseEntity<>(updatedStatus, HttpStatus.OK);
    }

    /**
     * Aktualizuje kolejność wyświetlania statusu zadania.
     *
     * @param id        Identyfikator statusu zadania
     * @param payload   Mapa zawierająca displayOrder
     * @return Zaktualizowany status zadania lub status 404, jeśli nie istnieje
     */
    @PatchMapping("/{id}/display-order")
    public ResponseEntity<TaskStatus> updateDisplayOrder(@PathVariable Integer id,
                                                         @RequestBody Map<String, Integer> payload) {
        Integer displayOrder = payload.get("displayOrder");

        if (displayOrder == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return taskStatusService.updateDisplayOrder(id, displayOrder)
                .map(status -> new ResponseEntity<>(status, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Usuwa status zadania na podstawie jego identyfikatora.
     *
     * @param id Identyfikator statusu zadania do usunięcia
     * @return Status 204 po pomyślnym usunięciu, 404 jeśli nie istnieje, lub 409 jeśli są przypisane zadania
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaskStatus(@PathVariable Integer id) {
        if (!taskStatusService.getTaskStatusById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            taskStatusService.deleteTaskStatus(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }
}