package com.example.backend.services;

import com.example.backend.models.Task;
import com.example.backend.models.TaskComment;
import com.example.backend.models.User;
import com.example.backend.repository.TaskCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Serwis obsługujący operacje dla encji {@link TaskComment}.
 * <p>
 * Klasa zawiera logikę biznesową związaną z komentarzami do zadań.
 * Implementuje operacje tworzenia, odczytu, aktualizacji i usuwania komentarzy,
 * a także metody wyszukiwania komentarzy według różnych kryteriów.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@Transactional
public class TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;

    /**
     * Konstruktor wstrzykujący zależność do repozytorium komentarzy.
     *
     * @param taskCommentRepository Repozytorium komentarzy do zadań
     */
    @Autowired
    public TaskCommentService(TaskCommentRepository taskCommentRepository) {
        this.taskCommentRepository = taskCommentRepository;
    }

    /**
     * Pobiera wszystkie komentarze do zadań.
     *
     * @return Lista wszystkich komentarzy
     */
    public List<TaskComment> getAllTaskComments() {
        return taskCommentRepository.findAll();
    }

    /**
     * Pobiera komentarz na podstawie jego identyfikatora.
     *
     * @param id Identyfikator komentarza
     * @return Opcjonalny komentarz, jeśli istnieje
     */
    public Optional<TaskComment> getTaskCommentById(Integer id) {
        return taskCommentRepository.findById(id);
    }

    /**
     * Pobiera wszystkie komentarze do określonego zadania.
     *
     * @param task Zadanie, którego komentarze mają zostać pobrane
     * @return Lista komentarzy do zadania
     */
    public List<TaskComment> getCommentsByTask(Task task) {
        return taskCommentRepository.findByTask(task);
    }

    /**
     * Pobiera wszystkie komentarze dodane przez określonego użytkownika.
     *
     * @param user Użytkownik, którego komentarze mają zostać pobrane
     * @return Lista komentarzy dodanych przez użytkownika
     */
    public List<TaskComment> getCommentsByUser(User user) {
        return taskCommentRepository.findByUser(user);
    }

    /**
     * Zapisuje nowy komentarz do zadania lub aktualizuje istniejący.
     * <p>
     * Jeśli komentarz jest nowy (bez identyfikatora), automatycznie ustawia datę utworzenia.
     *
     * @param taskComment Komentarz do zapisania
     * @return Zapisany komentarz
     */
    public TaskComment saveTaskComment(TaskComment taskComment) {
        if (taskComment.getCreatedAt() == null) {
            taskComment.setCreatedAt(LocalDateTime.now());
        }
        return taskCommentRepository.save(taskComment);
    }

    /**
     * Dodaje nowy komentarz do zadania.
     *
     * @param task    Zadanie, do którego dodawany jest komentarz
     * @param user    Użytkownik dodający komentarz
     * @param comment Treść komentarza
     * @return Utworzony komentarz
     */
    public TaskComment addCommentToTask(Task task, User user, String comment) {
        TaskComment taskComment = new TaskComment();
        taskComment.setTask(task);
        taskComment.setUser(user);
        taskComment.setComment(comment);
        taskComment.setCreatedAt(LocalDateTime.now());

        return taskCommentRepository.save(taskComment);
    }

    /**
     * Usuwa komentarz na podstawie jego identyfikatora.
     *
     * @param id Identyfikator komentarza do usunięcia
     */
    public void deleteTaskComment(Integer id) {
        taskCommentRepository.deleteById(id);
    }

    /**
     * Usuwa wszystkie komentarze do określonego zadania.
     *
     * @param task Zadanie, którego komentarze mają zostać usunięte
     * @return Liczba usuniętych komentarzy
     */
    public int deleteAllCommentsForTask(Task task) {
        List<TaskComment> comments = taskCommentRepository.findByTask(task);
        taskCommentRepository.deleteAll(comments);
        return comments.size();
    }
}