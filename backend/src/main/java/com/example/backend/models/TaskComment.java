package  com.example.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Reprezentuje komentarz do zadania w systemie zarządzania zadaniami.
 * Komentarze umożliwiają użytkownikom dodawanie notatek lub uwag do zadań,
 * co pomaga w lepszym śledzeniu postępów lub komunikacji w zespole.
 * <p>
 * Klasa jest encją bazy danych i odpowiada tabeli {@code task_comments}.
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
@Table(name = "task_comments")
public class TaskComment {

    /**
     * Unikalny identyfikator komentarza do zadania.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * Zadanie, do którego dodano komentarz.
     * Powiązane z encją {@link Task}.
     */
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    @JsonManagedReference(value = "task-comments")
    private Task task;

    /**
     * Użytkownik, który dodał komentarz.
     * Powiązane z encją {@link User}.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    /**
     * Treść komentarza dodanego do zadania.
     * Komentarz musi być podany (nie może być pusty).
     */
    @Column(name = "comment", nullable = false)
    private String comment;

    /**
     * Data i czas utworzenia komentarza.
     * Automatycznie ustawiana podczas tworzenia encji.
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Domyślny konstruktor klasy {@link TaskComment}.
     * Konstruktor bezparametrowy wymagany przez JPA.
     */
    public TaskComment() {
        // Domyślny konstruktor, wymagany przez JPA do tworzenia nowych instancji encji.
    }

    public void setTaskId(Integer taskId) {
    }

    public void setUserId(Integer userId) {

    }
}