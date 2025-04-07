package  com.example.backend.repository;

import com.example.backend.models.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Reposytorium dla encji {@link TaskStatus}.
 * <p>
 * Interfejs dziedziczy po {@link JpaRepository}, zapewniając dostęp do podstawowych operacji CRUD oraz do bardziej zaawansowanych zapytań
 * na encji {@link TaskStatus}. Repozytorium jest oznaczone adnotacją {@link Repository}, co pozwala Springowi zarządzać tym komponentem i
 * umożliwia wstrzykiwanie zależności.
 * <p>
 * Zawiera metody umożliwiające wyszukiwanie statusów zadań na podstawie nazwy oraz uzyskiwanie wszystkich statusów posortowanych według
 * kolejności wyświetlania.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatus, Integer>
{
    /**
     * Znajduje status zadania o określonej nazwie.
     *
     * @param name Nazwa statusu zadania.
     * @return Opcjonalny status zadania o podanej nazwie, jeśli istnieje.
     */
    Optional<TaskStatus> findByName(String name);

    /**
     * Znajduje wszystkie statusy zadań i zwraca je posortowane według pola displayOrder w porządku rosnącym.
     *
     * @return Lista statusów zadań posortowanych według kolejności wyświetlania.
     */
    List<TaskStatus> findAllByOrderByDisplayOrderAsc();
}