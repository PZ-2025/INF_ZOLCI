package com.example.backend.repository;

import com.example.backend.models.Task;
import com.example.backend.models.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Reposytorium dla encji {@link TaskHistory}.
 */
@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Integer> {
    /**
     * Znajduje wszystkie historie zmian przypisane do konkretnego zadania.
     *
     * @param task Zadanie, do którego historia zmian ma zostać zwrócona.
     * @return Lista historii zmian przypisanych do podanego zadania.
     */
    List<TaskHistory> findByTask(Task task);

    /**
     * Znajduje wszystkie historie zmian wykonane przez konkretnego użytkownika.
     *
     * @param changedBy ID użytkownika, który dokonał zmian.
     * @return Lista historii zmian wykonanych przez użytkownika o podanym ID.
     */
    List<TaskHistory> findByChangedBy(Integer changedBy);
}