package  com.example.backend.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Reprezentuje historię zmian w zadaniu w systemie zarządzania zadaniami.
 * Historia zmian przechowuje informacje o modyfikacjach, które miały miejsce w zadaniu,
 * w tym o zmianach w polach zadania oraz o użytkowniku, który dokonał tych zmian.
 * <p>
 * Klasa jest encją bazy danych i odpowiada tabeli {@code task_history}.
 *
 * <p>Używa adnotacji Lombok {@code @Getter} i {@code @Setter} do automatycznego generowania metod dostępnych.
 *
 * @author Jakub
 * @version 1.2.0
 * @since 1.0.0
 */
@Getter
@Setter
@Entity
@Table(name = "task_history")
public class TaskHistory {

    /**
     * Unikalny identyfikator historii zmiany zadania.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * Zadanie, którego dotyczy historia zmian.
     * Powiązane z encją {@link Task}.
     */
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    @JsonManagedReference(value = "task-history")
    private Task task;

    /**
     * ID użytkownika, który dokonał zmiany w zadaniu.
     */
    @Column(name = "changed_by", nullable = false)
    private Integer changedBy;

    /**
     * Nazwa pola, które zostało zmienione w zadaniu (np. "status", "priority").
     */
    @Column(name = "field_name", nullable = false, length = 50)
    private String fieldName;

    /**
     * Stara wartość pola przed zmianą.
     * Może być null, jeśli pole nie miało poprzedniej wartości.
     */
    @Column(name = "old_value")
    private String oldValue;

    /**
     * Nowa wartość pola po zmianie.
     */
    @Column(name = "new_value")
    private String newValue;

    /**
     * Data i czas, kiedy zmiana została wprowadzona.
     * Automatycznie ustawiana podczas tworzenia encji.
     */
    @CreationTimestamp
    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    /**
     * Domyślny konstruktor klasy {@link TaskHistory}.
     * Konstruktor bezparametrowy wymagany przez JPA.
     */
    public TaskHistory() {
        // Domyślny konstruktor, wymagany przez JPA do tworzenia nowych instancji encji.
    }

    /**
     * Ustawia ID zadania, którego dotyczy historia zmian.
     *
     * @param id Identyfikator zadania
     */
    public void setTaskId(Integer id) {
    }

    /**
     * Ustawia ID użytkownika, który dokonał zmiany w zadaniu.
     *
     * @param userId Identyfikator użytkownika
     */
    public void setChangedBy(Integer userId) {
        this.changedBy = userId;
    }
}