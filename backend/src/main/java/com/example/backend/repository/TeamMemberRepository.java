package  com.example.backend.repository;

import com.example.backend.models.Team;
import com.example.backend.models.TeamMember;
import com.example.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Reposytorium dla encji {@link TeamMember}.
 * <p>
 * Interfejs dziedziczy po {@link JpaRepository}, zapewniając dostęp do podstawowych operacji CRUD oraz do bardziej zaawansowanych zapytań
 * na encji {@link TeamMember}. Repozytorium jest oznaczone adnotacją {@link Repository}, co pozwala Springowi zarządzać tym komponentem i
 * umożliwia wstrzykiwanie zależności.
 * <p>
 * Zawiera metody umożliwiające wyszukiwanie członków zespołu na podstawie użytkownika, zespołu, aktywności członka oraz kombinacji tych
 * parametrów.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Integer> {

    /**
     * Znajduje wszystkich członków zespołu przypisanych do konkretnego użytkownika.
     *
     * @param user Użytkownik, którego członkowie zespołu mają zostać znalezieni.
     * @return Lista członków zespołu przypisanych do użytkownika.
     */
    List<TeamMember> findByUser(User user);

    /**
     * Znajduje wszystkich członków zespołu przypisanych do konkretnego zespołu.
     *
     * @param team Zespół, którego członkowie mają zostać znalezieni.
     * @return Lista członków zespołu przypisanych do zespołu.
     */
    List<TeamMember> findByTeam(Team team);

    /**
     * Znajduje wszystkich członków zespołu przypisanych do konkretnego zespołu i o określonym statusie aktywności.
     *
     * @param team     Zespół, którego członkowie mają zostać znalezieni.
     * @param isActive Status aktywności członków (true = aktywni, false = nieaktywni).
     * @return Lista członków zespołu o określonym statusie aktywności.
     */
    List<TeamMember> findByTeamAndIsActive(Team team, boolean isActive);

    /**
     * Znajduje członka zespołu przypisanego do konkretnego użytkownika i zespołu.
     *
     * @param user Użytkownik, którego członkowie zespołu mają zostać znalezieni.
     * @param team Zespół, którego członkowie mają zostać znalezieni.
     * @return Opcjonalny członek zespołu przypisany do użytkownika i zespołu.
     */
    Optional<TeamMember> findByUserAndTeam(User user, Team team);

    /**
     * Znajduje wszystkich członków zespołu przypisanych do konkretnego zespołu.
     *
     * @param team Zespół, którego członkowie mają zostać znalezieni.
     * @return Lista wszystkich członków zespołu.
     */
    List<TeamMember> findAllByTeam(Team team);

    Optional<TeamMember> findById(Long id);
}

