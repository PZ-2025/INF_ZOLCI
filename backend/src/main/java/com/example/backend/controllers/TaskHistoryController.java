package com.example.backend.controllers;

import com.example.backend.models.Task;
import com.example.backend.models.TaskHistory;
import com.example.backend.models.User;
import com.example.backend.services.TaskHistoryService;
import com.example.backend.services.TaskService;
import com.example.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Kontroler REST dla operacji na historii zmian zadań.
 * <p>
 * Klasa zapewnia endpoints do zarządzania historią zmian zadań w systemie,
 * w tym tworzenie, odczyt i usuwanie wpisów historii oraz
 * wyszukiwanie historii według różnych kryteriów.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/database/task-history")
public class TaskHistoryController {

    private final TaskHistoryService taskHistoryService;
    private final TaskService taskService;
    private final UserService userService;

    /**
     * Konstruktor wstrzykujący zależności.
     *
     * @param taskHistoryService Serwis historii zmian zadań
     * @param taskService        Serwis zadań
     * @param userService        Serwis użytkowników
     */
    @Autowired
    public TaskHistoryController(TaskHistoryService taskHistoryService,
                                 TaskService taskService,
                                 UserService userService) {
        this.taskHistoryService = taskHistoryService;
        this.taskService = taskService;
        this.userService = userService;
    }

    /**
     * Pobiera całą historię zmian zadań.
     *
     * @return Lista wszystkich wpisów historii zmian
     */
    @GetMapping
    public ResponseEntity<List<TaskHistory>> getAllTaskHistory() {
        List<TaskHistory> history = taskHistoryService.getAllTaskHistory();
        return new ResponseEntity<>(history, HttpStatus.OK);
    }

    /**
     * Pobiera wpis historii na podstawie jego identyfikatora.
     *
     * @param id Identyfikator wpisu historii
     * @return Wpis historii lub status 404, jeśli nie istnieje
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskHistory> getTaskHistoryById(@PathVariable Integer id) {
        return taskHistoryService.getTaskHistoryById(id)
                .map(history -> new ResponseEntity<>(history, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Pobiera historię zmian dla określonego zadania.
     *
     * @param taskId Identyfikator zadania
     * @return Lista wpisów historii dla zadania lub status 404, jeśli zadanie nie istnieje
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<TaskHistory>> getHistoryByTask(@PathVariable Integer taskId) {
        return taskService.getTaskById(taskId)
                .map(task -> {
                    List<TaskHistory> history = taskHistoryService.getHistoryByTask(task);
                    return new ResponseEntity<>(history, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Pobiera historię zmian dokonanych przez określonego użytkownika.
     *
     * @param userId Identyfikator użytkownika
     * @return Lista wpisów historii użytkownika lub status 404, jeśli użytkownik nie istnieje
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskHistory>> getHistoryByUser(@PathVariable Integer userId) {
        return userService.getUserById(userId)
                .map(user -> {
                    List<TaskHistory> history = taskHistoryService.getHistoryByUser(user);
                    return new ResponseEntity<>(history, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Tworzy nowy wpis historii zmian.
     *
     * @param taskHistory Dane nowego wpisu historii
     * @return Utworzony wpis historii
     */
    @PostMapping
    public ResponseEntity<TaskHistory> createTaskHistory(@RequestBody TaskHistory taskHistory) {
        TaskHistory savedHistory = taskHistoryService.saveTaskHistory(taskHistory);
        return new ResponseEntity<>(savedHistory, HttpStatus.CREATED);
    }

    /**
     * Rejestruje zmianę pola w zadaniu.
     *
     * @param payload Mapa zawierająca taskId, userId, fieldName, oldValue, newValue
     * @return Utworzony wpis historii lub status błędu
     */
    @PostMapping("/log")
    public ResponseEntity<TaskHistory> logTaskChange(@RequestBody Map<String, Object> payload) {
        Integer taskId = (Integer) payload.get("taskId");
        Integer userId = (Integer) payload.get("userId");
        String fieldName = (String) payload.get("fieldName");
        String oldValue = (String) payload.get("oldValue");
        String newValue = (String) payload.get("newValue");

        if (taskId == null || userId == null || fieldName == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Task> taskOpt = taskService.getTaskById(taskId);
        Optional<User> userOpt = userService.getUserById(userId);

        if (taskOpt.isEmpty() || userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        TaskHistory historyEntry = taskHistoryService.logTaskChange(
                taskOpt.get(), userOpt.get(), fieldName, oldValue, newValue);

        return new ResponseEntity<>(historyEntry, HttpStatus.CREATED);
    }

    /**
     * Usuwa wpis historii na podstawie jego identyfikatora.
     *
     * @param id Identyfikator wpisu historii do usunięcia
     * @return Status 204 po pomyślnym usunięciu lub 404, jeśli nie istnieje
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaskHistory(@PathVariable Integer id) {
        return taskHistoryService.getTaskHistoryById(id)
                .map(history -> {
                    taskHistoryService.deleteTaskHistory(id);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Usuwa całą historię zmian dla określonego zadania.
     *
     * @param taskId Identyfikator zadania
     * @return Status 204 po pomyślnym usunięciu lub 404, jeśli zadanie nie istnieje
     */
    @DeleteMapping("/task/{taskId}")
    public ResponseEntity<Void> deleteAllHistoryForTask(@PathVariable Integer taskId) {
        return taskService.getTaskById(taskId)
                .map(task -> {
                    taskHistoryService.deleteAllHistoryForTask(task);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}