package com.example.backend.controllers;

import com.example.backend.dto.TaskHistoryDTO;
import com.example.backend.dto.UserDTO;
import com.example.backend.models.Task;
import com.example.backend.models.User;
import com.example.backend.services.TaskHistoryService;
import com.example.backend.services.TaskService;
import com.example.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskHistoryDTO>> getAllTaskHistory() {
        List<TaskHistoryDTO> history = taskHistoryService.getAllTaskHistory();
        return new ResponseEntity<>(history, HttpStatus.OK);
    }

    /**
     * Pobiera wpis historii na podstawie jego identyfikatora.
     *
     * @param id Identyfikator wpisu historii
     * @return Wpis historii lub status 404, jeśli nie istnieje
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskHistoryDTO> getTaskHistoryById(@PathVariable Integer id) {
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
    @GetMapping(value = "/task/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskHistoryDTO>> getHistoryByTask(@PathVariable Integer taskId) {
        Optional<Task> taskOpt = taskService.getTaskById(taskId)
                .map(dto -> {
                    Task task = new Task();
                    task.setId(dto.getId());
                    return task;
                });

        if (taskOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<TaskHistoryDTO> history = taskHistoryService.getHistoryByTask(taskOpt.get());
        return new ResponseEntity<>(history, HttpStatus.OK);
    }

    /**
     * Pobiera historię zmian dokonanych przez określonego użytkownika.
     *
     * @param userId Identyfikator użytkownika
     * @return Lista wpisów historii użytkownika lub status 404, jeśli użytkownik nie istnieje
     */
    @GetMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskHistoryDTO>> getHistoryByUser(@PathVariable Integer userId) {
        return userService.getUserById(userId)
                .map(dto -> {
                    User user = new User();
                    user.setId(dto.getId());
                    List<TaskHistoryDTO> history = taskHistoryService.getHistoryByUser(user);
                    return new ResponseEntity<>(history, HttpStatus.OK);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Tworzy nowy wpis historii zmian.
     *
     * @param taskHistoryDTO Dane nowego wpisu historii
     * @return Utworzony wpis historii
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskHistoryDTO> createTaskHistory(@Valid @RequestBody TaskHistoryDTO taskHistoryDTO) {
        // Sprawdź czy istnieje zadanie o podanym ID
        Optional<Task> taskOpt = taskService.getTaskEntityById(taskHistoryDTO.getTaskId());
        if (taskOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Sprawdź czy istnieje użytkownik o podanym ID
        Optional<UserDTO> userOpt = userService.getUserById(taskHistoryDTO.getChangedById());
        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        TaskHistoryDTO savedHistory = taskHistoryService.saveTaskHistory(taskHistoryDTO, taskOpt.get());
        return new ResponseEntity<>(savedHistory, HttpStatus.CREATED);
    }

    /**
     * Rejestruje zmianę pola w zadaniu.
     *
     * @param payload Mapa zawierająca taskId, userId, fieldName, oldValue, newValue
     * @return Utworzony wpis historii lub status błędu
     */
    @PostMapping(value = "/log", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskHistoryDTO> logTaskChange(@RequestBody Map<String, Object> payload) {
        // Pobieramy i konwertujemy dane z payloadu
        Integer taskId = null;
        Integer userId = null;
        String fieldName = null;
        String oldValue = null;
        String newValue = null;

        try {
            // Pobieramy wymagane pola
            taskId = Integer.valueOf(String.valueOf(payload.get("taskId")));
            userId = Integer.valueOf(String.valueOf(payload.get("changedById")));
            fieldName = (String) payload.get("fieldName");

            // Opcjonalne pola
            if (payload.containsKey("oldValue")) {
                oldValue = String.valueOf(payload.get("oldValue"));
            }
            if (payload.containsKey("newValue")) {
                newValue = String.valueOf(payload.get("newValue"));
            }
        } catch (NumberFormatException | ClassCastException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Walidacja wymaganych pól
        if (taskId == null || userId == null || fieldName == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Pobieramy zadanie
        Optional<Task> taskOpt = taskService.getTaskEntityById(taskId);
        if (taskOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Pobieramy użytkownika
        Optional<UserDTO> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        User user = new User();
        user.setId(userOpt.get().getId());

        // Tworzymy wpis historii
        TaskHistoryDTO historyEntry = taskHistoryService.logTaskChange(
                taskOpt.get(), user, fieldName, oldValue, newValue);

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
        Optional<Task> taskOpt = taskService.getTaskById(taskId)
                .map(dto -> {
                    Task task = new Task();
                    task.setId(dto.getId());
                    return task;
                });

        if (taskOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        taskHistoryService.deleteAllHistoryForTask(taskOpt.get());
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }
}