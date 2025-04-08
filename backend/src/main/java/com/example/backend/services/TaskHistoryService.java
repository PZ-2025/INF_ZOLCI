package com.example.backend.services;

import com.example.backend.models.Task;
import com.example.backend.models.TaskHistory;
import com.example.backend.models.User;
import com.example.backend.repository.TaskHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Serwis obsługujący operacje dla encji {@link TaskHistory}.
 * <p>
 * Klasa zawiera logikę biznesową związaną z historią zmian zadań.
 * Implementuje operacje tworzenia, odczytu, aktualizacji i usuwania wpisów historii,
 * a także metody wyszukiwania historii według różnych kryteriów.
 *
 * @author Jakub
 * @version 1.1.0
 * @since 1.0.0
 */
@Service
@Transactional
public class TaskHistoryService {

    private final TaskHistoryRepository taskHistoryRepository;

    /**
     * Konstruktor wstrzykujący zależność do repozytorium historii zadań.
     *
     * @param taskHistoryRepository Repozytorium historii zadań
     */
    @Autowired
    public TaskHistoryService(TaskHistoryRepository taskHistoryRepository) {
        this.taskHistoryRepository = taskHistoryRepository;
    }

    /**
     * Pobiera wszystkie wpisy historii zmian zadań.
     *
     * @return Lista wszystkich wpisów historii
     */
    public List<TaskHistory> getAllTaskHistory() {
        return taskHistoryRepository.findAll();
    }

    /**
     * Pobiera wpis historii na podstawie jego identyfikatora.
     *
     * @param id Identyfikator wpisu historii
     * @return Opcjonalny wpis historii, jeśli istnieje
     */
    public Optional<TaskHistory> getTaskHistoryById(Integer id) {
        return taskHistoryRepository.findById(id);
    }

    /**
     * Pobiera historię zmian dla określonego zadania.
     *
     * @param task Zadanie, którego historia ma zostać pobrana
     * @return Lista wpisów historii dla zadania
     */
    public List<TaskHistory> getHistoryByTask(Task task) {
        return taskHistoryRepository.findByTask(task);
    }

    /**
     * Pobiera historię zmian dokonanych przez określonego użytkownika.
     *
     * @param user Użytkownik, którego historia zmian ma zostać pobrana
     * @return Lista wpisów historii dokonanych przez użytkownika
     */
    public List<TaskHistory> getHistoryByUser(User user) {
        return taskHistoryRepository.findByChangedBy(user);
    }

    /**
     * Zapisuje nowy wpis historii lub aktualizuje istniejący.
     * <p>
     * Jeśli wpis jest nowy (bez identyfikatora), automatycznie ustawia datę zmiany.
     *
     * @param taskHistory Wpis historii do zapisania
     * @return Zapisany wpis historii
     */
    public TaskHistory saveTaskHistory(TaskHistory taskHistory) {
        if (taskHistory.getChangedAt() == null) {
            taskHistory.setChangedAt(LocalDateTime.now());
        }
        return taskHistoryRepository.save(taskHistory);
    }

    /**
     * Rejestruje zmianę pola w zadaniu.
     *
     * @param task      Zadanie, w którym dokonano zmiany
     * @param user      Użytkownik, który dokonał zmiany
     * @param fieldName Nazwa zmienionego pola
     * @param oldValue  Stara wartość pola
     * @param newValue  Nowa wartość pola
     * @return Utworzony wpis historii
     */
    public TaskHistory logTaskChange(Task task, User user, String fieldName,
                                     String oldValue, String newValue) {
        TaskHistory history = new TaskHistory();
        history.setTask(task);
        history.setChangedBy(user.getId());  // This is now correct since we changed the model field type
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setChangedAt(LocalDateTime.now());

        return taskHistoryRepository.save(history);
    }

    /**
     * Usuwa wpis historii na podstawie jego identyfikatora.
     *
     * @param id Identyfikator wpisu historii do usunięcia
     */
    public void deleteTaskHistory(Integer id) {
        taskHistoryRepository.deleteById(id);
    }

    /**
     * Usuwa całą historię dla określonego zadania.
     *
     * @param task Zadanie, którego historia ma zostać usunięta
     * @return Liczba usuniętych wpisów historii
     */
    public int deleteAllHistoryForTask(Task task) {
        List<TaskHistory> history = taskHistoryRepository.findByTask(task);
        taskHistoryRepository.deleteAll(history);
        return history.size();
    }
}