package  com.example.backend.services;

import com.example.backend.models.Task;
import com.example.backend.models.TaskStatus;
import com.example.backend.models.User;
import com.example.backend.models.Team;
import com.example.backend.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Serwis obsługujący operacje dla encji {@link Task}.
 * <p>
 * Klasa ta zawiera logikę biznesową związaną z zadaniami w systemie.
 * Implementuje operacje tworzenia, odczytu, aktualizacji i usuwania zadań,
 * a także bardziej złożone operacje biznesowe.
 *
 * @author Karol
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;

    /**
     * Konstruktor wstrzykujący zależności.
     *
     * @param taskRepository Repozytorium zadań
     */
    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Pobiera wszystkie zadania z bazy danych.
     *
     * @return Lista wszystkich zadań
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Pobiera zadanie na podstawie jego identyfikatora.
     *
     * @param id Identyfikator zadania
     * @return Opcjonalne zadanie, jeśli istnieje
     */
    public Optional<Task> getTaskById(Integer id) {
        return taskRepository.findById(id);
    }

    /**
     * Zapisuje nowe zadanie w bazie danych.
     *
     * @param task Zadanie do zapisania
     * @return Zapisane zadanie
     */
    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    /**
     * Aktualizuje istniejące zadanie.
     *
     * @param task Zadanie ze zaktualizowanymi danymi
     * @return Zaktualizowane zadanie
     */
    public Task updateTask(Task task) {
        return taskRepository.save(task);
    }

    /**
     * Usuwa zadanie na podstawie jego identyfikatora.
     *
     * @param id Identyfikator zadania do usunięcia
     */
    public void deleteTask(Integer id) {
        taskRepository.deleteById(id);
    }

    /**
     * Pobiera zadania dla konkretnego zespołu.
     *
     * @param team Zespół, dla którego pobierane są zadania
     * @return Lista zadań przypisanych do zespołu
     */
    public List<Task> getTasksByTeam(Team team) {
        return taskRepository.findByTeam(team);
    }

    /**
     * Pobiera zadania o określonym statusie.
     *
     * @param status Status zadań
     * @return Lista zadań o określonym statusie
     */
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    /**
     * Pobiera zadania stworzone przez konkretnego użytkownika.
     *
     * @param user Użytkownik, który stworzył zadania
     * @return Lista zadań stworzonych przez użytkownika
     */
    public List<Task> getTasksCreatedBy(User user) {
        return taskRepository.findByCreatedBy(user);
    }

    /**
     * Pobiera zadania, których termin wykonania jest przed podaną datą.
     *
     * @param date Data graniczna
     * @return Lista zadań z terminem przed podaną datą
     */
    public List<Task> getTasksWithDeadlineBefore(LocalDate date) {
        return taskRepository.findByDeadlineBefore(date);
    }

    /**
     * Znajduje zadanie o określonym tytule.
     *
     * @param title Tytuł zadania
     * @return Opcjonalne zadanie o podanym tytule
     */
    public Optional<Task> getTaskByTitle(String title) {
        return taskRepository.findByTitle(title);
    }

    /**
     * Pobiera zadania dla konkretnego zespołu na podstawie ID zespołu.
     *
     * @param teamId ID zespołu
     * @return Lista zadań przypisanych do zespołu
     */
    public List<Task> getTasksByTeamId(Integer teamId) {
        return taskRepository.findByTeamId(teamId);
    }

    /**
     * Pobiera zadania o określonym statusie na podstawie ID statusu.
     *
     * @param statusId ID statusu
     * @return Lista zadań o określonym statusie
     */
    public List<Task> getTasksByStatusId(Integer statusId) {
        return taskRepository.findByStatusId(statusId);
    }

    /**
     * Pobiera zadania o określonym priorytecie na podstawie ID priorytetu.
     *
     * @param priorityId ID priorytetu
     * @return Lista zadań o określonym priorytecie
     */
    public List<Task> getTasksByPriorityId(Integer priorityId) {
        return taskRepository.findByPriorityId(priorityId);
    }

    public static class TaskStatusService {
    }
}