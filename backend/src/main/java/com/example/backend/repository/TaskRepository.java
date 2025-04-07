package  com.example.backend.repository;

import com.example.backend.models.Task;
import com.example.backend.models.TaskStatus;
import com.example.backend.models.User;
import com.example.backend.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Reposytorium dla encji {@link Task}.
 * <p>
 * Interfejs dziedziczy po {@link JpaRepository}, zapewniając dostęp do podstawowych operacji CRUD oraz do bardziej zaawansowanych zapytań
 * na encji {@link Task}. Repozytorium jest oznaczone adnotacją {@link Repository}, co pozwala Springowi zarządzać tym komponentem i
 * umożliwia wstrzykiwanie zależności.
 * <p>
 * Zawiera metody umożliwiające wyszukiwanie zadań na podstawie różnych kryteriów, takich jak zespół, status, użytkownik, termin
 * lub tytuł zadania.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    /**
     * Znajduje wszystkie zadania przypisane do konkretnego zespołu.
     *
     * @param team Zespół, do którego przypisane są zadania.
     * @return Lista zadań przypisanych do podanego zespołu.
     */
    List<Task> findByTeam(Team team);

    /**
     * Znajduje wszystkie zadania o określonym statusie.
     *
     * @param status Status, który ma zostać przypisany do zadań.
     * @return Lista zadań o określonym statusie.
     */
    List<Task> findByStatus(TaskStatus status);

    /**
     * Znajduje wszystkie zadania stworzone przez konkretnego użytkownika.
     *
     * @param user Użytkownik, który stworzył zadania.
     * @return Lista zadań stworzonych przez podanego użytkownika.
     */
    List<Task> findByCreatedBy(User user);

    /**
     * Znajduje wszystkie zadania, których termin wykonania jest wcześniejszy niż podana data.
     *
     * @param date Data, przed którą termin wykonania zadania musi być ustawiony.
     * @return Lista zadań, których termin wykonania jest przed podaną datą.
     */
    List<Task> findByDeadlineBefore(LocalDate date);

    /**
     * Znajduje zadanie o określonym tytule.
     *
     * @param title Tytuł zadania.
     * @return Opcjonalne zadanie o podanym tytule, jeśli istnieje.
     */
    Optional<Task> findByTitle(String title);

    /**
     * Znajduje zadania przypisane do konkretnego zespołu na podstawie ID zespołu.
     *
     * @param teamId ID zespołu.
     * @return Lista zadań przypisanych do zespołu.
     */
    List<Task> findByTeamId(Integer teamId);

    /**
     * Znajduje zadania o określonym statusie na podstawie ID statusu.
     *
     * @param statusId ID statusu.
     * @return Lista zadań o określonym statusie.
     */
    List<Task> findByStatusId(Integer statusId);

    /**
     * Znajduje zadania o określonym priorytecie na podstawie ID priorytetu.
     *
     * @param priorityId ID priorytetu.
     * @return Lista zadań o określonym priorytecie.
     */
    List<Task> findByPriorityId(Integer priorityId);
}
