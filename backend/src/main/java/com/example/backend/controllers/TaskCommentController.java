package com.example.backend.controllers;

import com.example.backend.models.Task;
import com.example.backend.models.TaskComment;
import com.example.backend.models.User;
import com.example.backend.services.TaskCommentService;
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
 * Kontroler REST dla operacji na komentarzach do zadań.
 * <p>
 * Klasa zapewnia endpoints do zarządzania komentarzami do zadań w systemie,
 * w tym tworzenie, odczyt, aktualizację i usuwanie komentarzy oraz
 * wyszukiwanie komentarzy według różnych kryteriów.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/database/task-comments")
public class TaskCommentController {

    private final TaskCommentService taskCommentService;
    private final TaskService taskService;
    private final UserService userService;

    /**
     * Konstruktor wstrzykujący zależności.
     *
     * @param taskCommentService Serwis komentarzy do zadań
     * @param taskService        Serwis zadań
     * @param userService        Serwis użytkowników
     */
    @Autowired
    public TaskCommentController(TaskCommentService taskCommentService,
                                 TaskService taskService,
                                 UserService userService) {
        this.taskCommentService = taskCommentService;
        this.taskService = taskService;
        this.userService = userService;
    }

    /**
     * Pobiera wszystkie komentarze do zadań.
     *
     * @return Lista wszystkich komentarzy
     */
    @GetMapping
    public ResponseEntity<List<TaskComment>> getAllTaskComments() {
        List<TaskComment> comments = taskCommentService.getAllTaskComments();
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    /**
     * Pobiera komentarz na podstawie jego identyfikatora.
     *
     * @param id Identyfikator komentarza
     * @return Komentarz lub status 404, jeśli nie istnieje
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskComment> getTaskCommentById(@PathVariable Integer id) {
        return taskCommentService.getTaskCommentById(id)
                .map(comment -> new ResponseEntity<>(comment, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Pobiera komentarze do określonego zadania.
     *
     * @param taskId Identyfikator zadania
     * @return Lista komentarzy do zadania lub status 404, jeśli zadanie nie istnieje
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<TaskComment>> getCommentsByTask(@PathVariable Integer taskId) {
        return taskService.getTaskById(taskId)
                .map(task -> {
                    List<TaskComment> comments = taskCommentService.getCommentsByTask(task);
                    return new ResponseEntity<>(comments, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Pobiera komentarze dodane przez określonego użytkownika.
     *
     * @param userId Identyfikator użytkownika
     * @return Lista komentarzy użytkownika lub status 404, jeśli użytkownik nie istnieje
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskComment>> getCommentsByUser(@PathVariable Integer userId) {
        return userService.getUserById(userId)
                .map(user -> {
                    List<TaskComment> comments = taskCommentService.getCommentsByUser(user);
                    return new ResponseEntity<>(comments, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Tworzy nowy komentarz do zadania.
     *
     * @param taskComment Dane nowego komentarza
     * @return Utworzony komentarz
     */
    @PostMapping
    public ResponseEntity<TaskComment> createTaskComment(@RequestBody TaskComment taskComment) {
        TaskComment savedComment = taskCommentService.saveTaskComment(taskComment);
        return new ResponseEntity<>(savedComment, HttpStatus.CREATED);
    }

    /**
     * Dodaje komentarz do zadania na podstawie identyfikatorów i treści.
     *
     * @param payload Mapa zawierająca taskId, userId i comment
     * @return Utworzony komentarz lub status błędu
     */
    @PostMapping("/add")
    public ResponseEntity<TaskComment> addCommentToTask(@RequestBody Map<String, Object> payload) {
        Integer taskId = (Integer) payload.get("taskId");
        Integer userId = (Integer) payload.get("userId");
        String comment = (String) payload.get("comment");

        if (taskId == null || userId == null || comment == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Task> taskOpt = taskService.getTaskById(taskId);
        Optional<User> userOpt = userService.getUserById(userId);

        if (taskOpt.isEmpty() || userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        TaskComment newComment = taskCommentService.addCommentToTask(
                taskOpt.get(), userOpt.get(), comment);

        return new ResponseEntity<>(newComment, HttpStatus.CREATED);
    }

    /**
     * Aktualizuje istniejący komentarz.
     *
     * @param id           Identyfikator komentarza
     * @param taskComment Zaktualizowane dane komentarza
     * @return Zaktualizowany komentarz lub status 404, jeśli nie istnieje
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskComment> updateTaskComment(@PathVariable Integer id,
                                                         @RequestBody TaskComment taskComment) {
        return taskCommentService.getTaskCommentById(id)
                .map(existingComment -> {
                    taskComment.setId(id);
                    TaskComment updatedComment = taskCommentService.saveTaskComment(taskComment);
                    return new ResponseEntity<>(updatedComment, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Usuwa komentarz na podstawie jego identyfikatora.
     *
     * @param id Identyfikator komentarza do usunięcia
     * @return Status 204 po pomyślnym usunięciu lub 404, jeśli nie istnieje
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaskComment(@PathVariable Integer id) {
        return taskCommentService.getTaskCommentById(id)
                .map(comment -> {
                    taskCommentService.deleteTaskComment(id);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Usuwa wszystkie komentarze do określonego zadania.
     *
     * @param taskId Identyfikator zadania
     * @return Status 204 po pomyślnym usunięciu lub 404, jeśli zadanie nie istnieje
     */
    @DeleteMapping("/task/{taskId}")
    public ResponseEntity<Void> deleteAllCommentsForTask(@PathVariable Integer taskId) {
        return taskService.getTaskById(taskId)
                .map(task -> {
                    taskCommentService.deleteAllCommentsForTask(task);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}