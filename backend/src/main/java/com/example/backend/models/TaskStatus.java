package  com.example.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Reprezentuje statusy zadań w systemie zarządzania zadaniami.
 * Statusy pozwalają na śledzenie etapu, na którym znajduje się zadanie, oraz określają zakres postępu w zadaniach.
 * <p>
 * Klasa jest encją bazy danych i odpowiada tabeli {@code task_statuses}.
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
@Table(name = "task_statuses")
public class TaskStatus {

    /**
     * Unikalny identyfikator statusu zadania.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * Nazwa statusu, np. "W trakcie", "Zakończone".
     * Musi być unikalna i nie może być pusta.
     */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Minimalny postęp zadania dla tego statusu (np. 0% dla statusu "W trakcie").
     */
    @Column(name = "progress_min", nullable = false)
    private Integer progressMin;

    /**
     * Maksymalny postęp zadania dla tego statusu (np. 100% dla statusu "Zakończone").
     */
    @Column(name = "progress_max", nullable = false)
    private Integer progressMax;

    /**
     * Kolejność wyświetlania statusu w interfejsie użytkownika.
     * Określa, w jakiej kolejności będą wyświetlane statusy w liście.
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    /**
     * Zbiór zadań powiązanych z danym statusem.
     * Każde zadanie może mieć tylko jeden status.
     * Powiązane z encją {@link Task}.
     */
    @OneToMany(mappedBy = "status")
    @JsonIgnore
    private Set<Task> tasks = new HashSet<>();

    /**
     * Domyślny konstruktor klasy {@link TaskStatus}.
     * Konstruktor bezparametrowy wymagany przez JPA.
     */
    public TaskStatus() {
        // Domyślny konstruktor, wymagany przez JPA do tworzenia nowych instancji encji.
    }
}