package  com.example.backend.repository;

import com.example.backend.models.Team;
import com.example.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Reposytorium dla encji {@link Team}.
 * <p>
 * Interfejs dziedziczy po {@link JpaRepository}, zapewniając dostęp do podstawowych operacji CRUD oraz do bardziej zaawansowanych zapytań
 * na encji {@link Team}. Repozytorium jest oznaczone adnotacją {@link Repository}, co pozwala Springowi zarządzać tym komponentem i
 * umożliwia wstrzykiwanie zależności.
 * <p>
 * Zawiera metody umożliwiające wyszukiwanie zespołów na podstawie menedżera, aktywności oraz nazwy zespołu.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Integer>
{
    /**
     * Znajduje wszystkie zespoły zarządzane przez określonego menedżera.
     *
     * @param manager Menedżer, którego zespoły mają zostać znalezione.
     * @return Lista zespołów zarządzanych przez podanego menedżera.
     */
    List<Team> findByManager(User manager);

    /**
     * Znajduje wszystkie zespoły o określonym statusie aktywności.
     *
     * @param isActive Status aktywności zespołów (true = aktywne, false = nieaktywne).
     * @return Lista zespołów o określonym statusie aktywności.
     */
    List<Team> findByIsActive(boolean isActive);

    /**
     * Znajduje zespół o podanej nazwie.
     *
     * @param teamName Nazwa zespołu, który ma zostać znaleziony.
     * @return Opcjonalny zespół o podanej nazwie.
     */
    Optional<Team> findByName(String teamName);

    List<Team> findByIsActiveTrue();
}