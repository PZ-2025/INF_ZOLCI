package com.example.database.repository;

import com.example.database.models.Task;
import com.example.database.models.TaskComment;
import com.example.database.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Reposytorium dla encji {@link TaskComment}.
 * <p>
 * Interfejs dziedziczy po {@link JpaRepository}, co zapewnia dostęp do operacji CRUD oraz metod do bardziej zaawansowanych zapytań.
 * Repozytorium jest oznaczone adnotacją {@link Repository}, co pozwala Springowi zarządzać tym komponentem i umożliwia wstrzykiwanie zależności.
 * <p>
 * Zawiera metody do wyszukiwania komentarzy przypisanych do konkretnego zadania lub użytkownika.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Integer>
{
    /**
     * Znajduje wszystkie komentarze przypisane do konkretnego zadania.
     *
     * @param task Zadanie, do którego komentarze mają zostać zwrócone.
     * @return Lista komentarzy przypisanych do podanego zadania.
     */
    List<TaskComment> findByTask(Task task);

    /**
     * Znajduje wszystkie komentarze przypisane do konkretnego użytkownika.
     *
     * @param user Użytkownik, którego komentarze mają zostać zwrócone.
     * @return Lista komentarzy przypisanych do podanego użytkownika.
     */
    List<TaskComment> findByUser(User user);

}