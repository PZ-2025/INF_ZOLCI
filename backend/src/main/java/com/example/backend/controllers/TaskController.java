package com.example.backend.controllers;

import com.example.backend.dto.TaskDTO;
import com.example.backend.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * Kontroler REST dla operacji na zadaniach.
 * <p>
 * Klasa zapewnia endpoints do zarządzania zadaniami w systemie,
 * w tym tworzenie, odczyt, aktualizację i usuwanie zadań oraz
 * wyszukiwanie zadań według różnych kryteriów.
 *
 * @author Karol
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/database/tasks")
public class TaskController {

    private final TaskService taskService;

    /**
     * Konstruktor wstrzykujący zależności.
     *
     * @param taskService Serwis zadań
     */
    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Pobiera wszystkie zadania.
     *
     * @return Lista wszystkich zadań
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        List<TaskDTO> tasks = taskService.getAllTasks();
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    /**
     * Pobiera zadanie na podstawie jego identyfikatora.
     *
     * @param id Identyfikator zadania
     * @return Zadanie lub status 404, jeśli nie istnieje
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Integer id) {
        return taskService.getTaskById(id)
                .map(task -> new ResponseEntity<>(task, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Tworzy nowe zadanie.
     *
     * @param taskDTO Dane nowego zadania
     * @return Utworzone zadanie
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskDTO taskDTO) {
        TaskDTO savedTask = taskService.saveTask(taskDTO);
        return new ResponseEntity<>(savedTask, HttpStatus.CREATED);
    }

    /**
     * Aktualizuje istniejące zadanie.
     *
     * @param id     Identyfikator zadania
     * @param taskDTO Zaktualizowane dane zadania
     * @return Zaktualizowane zadanie lub status 404, jeśli nie istnieje
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Integer id, @Valid @RequestBody TaskDTO taskDTO) {
        return taskService.getTaskById(id)
                .map(existingTask -> {
                    taskDTO.setId(id);
                    TaskDTO updatedTask = taskService.updateTask(taskDTO);
                    return new ResponseEntity<>(updatedTask, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Usuwa zadanie na podstawie jego identyfikatora.
     *
     * @param id Identyfikator zadania do usunięcia
     * @return Status 204 po pomyślnym usunięciu lub 404, jeśli nie istnieje
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Integer id) {
        return taskService.getTaskById(id)
                .map(task -> {
                    taskService.deleteTask(id);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Pobiera zadania dla konkretnego zespołu na podstawie ID zespołu.
     *
     * @param teamId ID zespołu
     * @return Lista zadań przypisanych do zespołu
     */
    @GetMapping(value = "/team/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskDTO>> getTasksByTeamId(@PathVariable Integer teamId) {
        List<TaskDTO> tasks = taskService.getTasksByTeamId(teamId);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    /**
     * Pobiera zadania o określonym statusie na podstawie ID statusu.
     *
     * @param statusId ID statusu
     * @return Lista zadań o określonym statusie
     */
    @GetMapping(value = "/status/{statusId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskDTO>> getTasksByStatusId(@PathVariable Integer statusId) {
        List<TaskDTO> tasks = taskService.getTasksByStatusId(statusId);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    /**
     * Pobiera zadania o określonym priorytecie na podstawie ID priorytetu.
     *
     * @param priorityId ID priorytetu
     * @return Lista zadań o określonym priorytecie
     */
    @GetMapping(value = "/priority/{priorityId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskDTO>> getTasksByPriorityId(@PathVariable Integer priorityId) {
        List<TaskDTO> tasks = taskService.getTasksByPriorityId(priorityId);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    /**
     * Pobiera zadanie na podstawie tytułu.
     *
     * @param title Tytuł zadania
     * @return Zadanie lub status 404, jeśli nie istnieje
     */
    @GetMapping(value = "/title/{title}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskDTO> getTaskByTitle(@PathVariable String title) {
        return taskService.getTaskByTitle(title)
                .map(task -> new ResponseEntity<>(task, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Pobiera zadania z terminem wykonania przed podaną datą.
     *
     * @param date Data graniczna w formacie "yyyy-MM-dd"
     * @return Lista zadań z terminem przed podaną datą
     */
    @GetMapping(value = "/deadline-before/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskDTO>> getTasksWithDeadlineBefore(@PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        List<TaskDTO> tasks = taskService.getTasksWithDeadlineBefore(localDate);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }
}