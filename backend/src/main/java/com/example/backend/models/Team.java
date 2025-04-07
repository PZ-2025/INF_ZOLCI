package  com.example.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Reprezentuje zespół w systemie zarządzania zadaniami "BuildTask".
 * Zespół składa się z członków, którzy realizują zadania. Każdy zespół ma przypisanego menedżera
 * oraz może mieć przypisane różne zadania.
 * <p>
 * Klasa jest encją bazy danych i odpowiada tabeli {@code teams}.
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
@Table(name = "teams")
public class Team {

    /**
     * Unikalny identyfikator zespołu.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * Nazwa zespołu, np. "Zespół A", "Zespół B".
     * Nazwa jest obowiązkowa i nie może być pusta.
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Menedżer zespołu, który jest odpowiedzialny za zarządzanie członkami zespołu oraz zadaniami.
     * Powiązane z encją {@link User}.
     */
    @ManyToOne
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    /**
     * Data i czas utworzenia zespołu.
     * Automatycznie ustawiana podczas tworzenia encji.
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Określa, czy zespół jest aktywny.
     * Wartość domyślna to {@code true}.
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Zbiór członków zespołu. Każdy członek zespołu jest powiązany z tym zespołem.
     * Powiązane z encją {@link TeamMember}.
     */
    @OneToMany(mappedBy = "team")
    private Set<TeamMember> members = new HashSet<>();

    /**
     * Zbiór zadań przypisanych do tego zespołu.
     * Powiązane z encją {@link Task}.
     */
    @OneToMany(mappedBy = "team")
    private Set<Task> tasks = new HashSet<>();

    /**
     * Domyślny konstruktor klasy {@link Team}.
     * Konstruktor bezparametrowy wymagany przez JPA.
     */
    public Team() {
        // Domyślny konstruktor, wymagany przez JPA do tworzenia nowych instancji encji.
    }

//    public void getManagerId() {
//    }

    public void setManagerId(boolean managerId) {
    }
}