package  com.example.backend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Reprezentuje zadanie w systemie zarządzania zadaniami.
 * Zadania są jednostkami pracy, które są przypisywane do zespołów w ramach różnych projektów budowlanych.
 * Każde zadanie ma przypisany priorytet, status, daty rozpoczęcia, terminu i zakończenia.
 * <p>
 * Klasa jest encją bazy danych i odpowiada tabeli {@code tasks}.
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
@Table(name = "tasks")
public class Task {

    /**
     * Unikalny identyfikator zadania.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "task_id", referencedColumnName = "id")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    private Task task;

    /**
     * Tytuł zadania, np. "Przygotowanie fundamentów".
     * Maksymalna długość: 100 znaków.
     */
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    /**
     * Opcjonalny opis zadania, zawierający szczegóły na temat jego realizacji.
     */
    @Column(name = "description")
    private String description;

    /**
     * Zespół odpowiedzialny za wykonanie zadania.
     * Powiązany z encją {@link Team}.
     */
    @ManyToOne
    @JoinColumn(name = "team_id")
    @JsonManagedReference(value = "team-tasks")
    private Team team;

    /**
     * Priorytet zadania, który określa jego ważność.
     * Powiązany z encją {@link Priority}.
     */
    @ManyToOne
    @JoinColumn(name = "priority_id", nullable = false)
    @JsonBackReference(value = "priority-tasks")
    private Priority priority;

    /**
     * Status zadania, który określa jego aktualny stan (np. "W trakcie", "Zakończone").
     * Powiązany z encją {@link TaskStatus}.
     */
    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    @JsonBackReference(value = "status-tasks")
    private TaskStatus status;

    /**
     * Data rozpoczęcia zadania.
     * Może być null, jeśli zadanie jeszcze nie zostało rozpoczęte.
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * Data terminu zakończenia zadania.
     */
    @Column(name = "deadline")
    private LocalDate deadline;

    /**
     * Data faktycznego zakończenia zadania.
     * Może być null, jeśli zadanie nie zostało jeszcze zakończone.
     */
    @Column(name = "completed_date")
    private LocalDate completedDate;

    /**
     * Użytkownik, który stworzył zadanie.
     * Powiązany z encją {@link User}.
     */
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    @JsonManagedReference(value = "user-created-tasks")
    private User createdBy;

    /**
     * Data i czas utworzenia zadania.
     * Automatycznie ustawiana podczas tworzenia encji.
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Data i czas ostatniej aktualizacji zadania.
     * Automatycznie ustawiana podczas aktualizacji encji.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Zbiór komentarzy powiązanych z danym zadaniem.
     * Relacja jednokierunkowa - jedno {@code Task} może mieć wiele {@code TaskComment}.
     */
    @OneToMany(mappedBy = "task")
    @JsonBackReference(value = "task-comments")
    private Set<TaskComment> comments = new HashSet<>();

    /**
     * Zbiór historii zmian zadania.
     * Relacja jednokierunkowa - jedno {@code Task} może mieć wiele {@code TaskHistory}.
     */
    @OneToMany(mappedBy = "task")
    @JsonBackReference(value = "task-history")
    private Set<TaskHistory> history = new HashSet<>();

    /**
     * Domyślny konstruktor klasy {@link Task}.
     * Konstruktor bezparametrowy wymagany przez JPA.
     */
    public Task() {
        // Domyślny konstruktor, wymagany przez JPA do tworzenia nowych instancji encji.
    }


}
