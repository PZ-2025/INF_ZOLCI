package  com.example.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Reprezentuje członka zespołu w systemie zarządzania zadaniami "BuildTask".
 * Każdy członek zespołu jest przypisany do konkretnego zespołu i jest użytkownikiem systemu.
 * <p>
 * Klasa jest encją bazy danych i odpowiada tabeli {@code team_members}.
 *
 * <p>Używa adnotacji Lombok {@code @Getter} i {@code @Setter} do automatycznego generowania metod dostępnych.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@Setter
@Entity
@Table(name = "team_members")
public class TeamMember {

    /**
     * Unikalny identyfikator członka zespołu.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * Zespół, do którego należy członek. Powiązane z encją {@link Team}.
     */
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /**
     * Użytkownik, który jest członkiem zespołu. Powiązane z encją {@link User}.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Data i czas dołączenia użytkownika do zespołu.
     * Automatycznie ustawiana podczas tworzenia encji.
     */
    @CreationTimestamp
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    /**
     * Określa, czy członek zespołu jest aktywny.
     * Wartość domyślna to {@code true}.
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Domyślny konstruktor klasy {@link TeamMember}.
     * Konstruktor bezparametrowy wymagany przez JPA.
     */
    public TeamMember() {
        // Domyślny konstruktor, wymagany przez JPA do tworzenia nowych instancji encji.
    }

    public void setTeamId(Integer id) {
        // Metoda pomocnicza, używana w testach
    }

    public void setUserId(boolean managerId) {
        // Metoda pomocnicza, używana w testach
    }

    public void setUserId(Integer userId) {
        // Metoda pomocnicza, używana w testach
    }
}