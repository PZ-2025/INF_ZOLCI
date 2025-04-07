package com.example.database.repository;

import com.example.database.models.Task;
import com.example.database.models.TaskHistory;
import com.example.database.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Reposytorium dla encji {@link TaskHistory}.
 * <p>
 * Interfejs dziedziczy po {@link JpaRepository}, co zapewnia dostęp do operacji CRUD oraz metod do bardziej zaawansowanych zapytań.
 * Repozytorium jest oznaczone adnotacją {@link Repository}, co pozwala Springowi zarządzać tym komponentem i umożliwia wstrzykiwanie zależności.
 * <p>
 * Zawiera metody do wyszukiwania historii zmian przypisanych do konkretnego zadania lub użytkownika.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Integer>
{
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
     * @param user Użytkownik, którego historia zmian ma zostać zwrócona.
     * @return Lista historii zmian wykonanych przez podanego użytkownika.
     */
    List<TaskHistory> findByChangedBy(User user);


}