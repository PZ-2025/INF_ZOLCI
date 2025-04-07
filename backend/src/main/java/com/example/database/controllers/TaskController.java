package com.example.database.controllers;

import com.example.database.models.Task;
import com.example.database.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    /**
     * Pobiera zadanie na podstawie jego identyfikatora.
     *
     * @param id Identyfikator zadania
     * @return Zadanie lub status 404, jeśli nie istnieje
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Integer id) {
        return taskService.getTaskById(id)
                .map(task -> new ResponseEntity<>(task, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Tworzy nowe zadanie.
     *
     * @param task Dane nowego zadania
     * @return Utworzone zadanie
     */
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task savedTask = taskService.saveTask(task);
        return new ResponseEntity<>(savedTask, HttpStatus.CREATED);
    }

    /**
     * Aktualizuje istniejące zadanie.
     *
     * @param id   Identyfikator zadania
     * @param task Zaktualizowane dane zadania
     * @return Zaktualizowane zadanie lub status 404, jeśli nie istnieje
     */
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Integer id, @RequestBody Task task) {
        return taskService.getTaskById(id)
                .map(existingTask -> {
                    task.setId(id);
                    Task updatedTask = taskService.updateTask(task);
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
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<Task>> getTasksByTeamId(@PathVariable Integer teamId) {
        List<Task> tasks = taskService.getTasksByTeamId(teamId);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    /**
     * Pobiera zadania o określonym statusie na podstawie ID statusu.
     *
     * @param statusId ID statusu
     * @return Lista zadań o określonym statusie
     */
    @GetMapping("/status/{statusId}")
    public ResponseEntity<List<Task>> getTasksByStatusId(@PathVariable Integer statusId) {
        List<Task> tasks = taskService.getTasksByStatusId(statusId);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    /**
     * Pobiera zadania o określonym priorytecie na podstawie ID priorytetu.
     *
     * @param priorityId ID priorytetu
     * @return Lista zadań o określonym priorytecie
     */
    @GetMapping("/priority/{priorityId}")
    public ResponseEntity<List<Task>> getTasksByPriorityId(@PathVariable Integer priorityId) {
        List<Task> tasks = taskService.getTasksByPriorityId(priorityId);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    /**
     * Pobiera zadanie na podstawie tytułu.
     *
     * @param title Tytuł zadania
     * @return Zadanie lub status 404, jeśli nie istnieje
     */
    @GetMapping("/title/{title}")
    public ResponseEntity<Task> getTaskByTitle(@PathVariable String title) {
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
    @GetMapping("/deadline-before/{date}")
    public ResponseEntity<List<Task>> getTasksWithDeadlineBefore(@PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        List<Task> tasks = taskService.getTasksWithDeadlineBefore(localDate);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }
}