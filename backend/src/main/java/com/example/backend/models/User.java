package  com.example.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Reprezentuje użytkownika systemu zarządzania zadaniami.
 * Klasa przechowuje dane użytkowników, takie jak dane logowania, dane osobowe, rola, oraz powiązania z zespołami, zadaniami, komentarzami i innymi encjami.
 * <p>
 * Klasa jest encją bazy danych i odpowiada tabeli {@code users}.
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
@Table(name = "users")
public class User {

    /**
     * Unikalny identyfikator użytkownika.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * Nazwa użytkownika, unikalna w systemie.
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Hasło użytkownika, wykorzystywane do logowania.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Adres email użytkownika, unikalny w systemie.
     */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Imię użytkownika.
     */
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    /**
     * Nazwisko użytkownika.
     */
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    /**
     * Numer telefonu użytkownika.
     */
    @Column(name = "phone", length = 15)
    private String phone;

    /**
     * Rola użytkownika w systemie (np. administrator, manager, użytkownik).
     */
    @Column(name = "role", length = 50)
    private String role;

    /**
     * Data i czas utworzenia konta użytkownika. Automatycznie ustawiana podczas tworzenia encji.
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Data i czas ostatniego logowania użytkownika.
     */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    /**
     * Określa, czy konto użytkownika jest aktywne. Wartość domyślna to {@code true}.
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Zespoły, które są zarządzane przez tego użytkownika. Powiązane z encją {@code Team}.
     */
    @OneToMany(mappedBy = "manager")
    private Set<Team> managedTeams = new HashSet<>();

    /**
     * Członkostwa użytkownika w zespołach. Powiązane z encją {@link TeamMember}.
     */
    @OneToMany(mappedBy = "user")
    private Set<TeamMember> teamMemberships = new HashSet<>();

    /**
     * Zadania utworzone przez użytkownika. Powiązane z encją {@link Task}.
     */
    @OneToMany(mappedBy = "createdBy")
    private Set<Task> createdTasks = new HashSet<>();

    /**
     * Komentarze użytkownika w zadaniach. Powiązane z encją {@link TaskComment}.
     */
    @OneToMany(mappedBy = "user")
    private Set<TaskComment> comments = new HashSet<>();

    /**
     * Historia zmian użytkownika w zadaniach. Powiązane z encją {@link TaskHistory}.
     */
    @OneToMany(mappedBy = "changedBy")
    private Set<TaskHistory> taskChanges = new HashSet<>();

    /**
     * Raporty utworzone przez użytkownika. Powiązane z encją {@link Report}.
     */
    @OneToMany(mappedBy = "createdBy")
    private Set<Report> reports = new HashSet<>();

    /**
     * Ustawienia systemowe zaktualizowane przez użytkownika. Powiązane z encją {@code SystemSetting}.
     */
    @OneToMany(mappedBy = "updatedBy")
    private Set<SystemSetting> updatedSettings = new HashSet<>();

    /**
     * Domyślny konstruktor klasy {@link User}.
     * Konstruktor bezparametrowy wymagany przez JPA.
     */
    public User() {
        // Domyślny konstruktor, wymagany przez JPA do tworzenia nowych instancji encji.
    }
}