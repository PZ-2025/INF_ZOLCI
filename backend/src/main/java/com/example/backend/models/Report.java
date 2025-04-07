package  com.example.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Reprezentuje raport wygenerowany w systemie zarządzania zadaniami.
 * Raporty mogą zawierać różne analizy i statystyki.
 * <p>
 * Klasa jest encją bazy danych i odpowiada tabeli {@code reports}.
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
@Table(name = "reports")
public class Report {

    /**
     * Unikalny identyfikator raportu.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * Nazwa raportu, np. "Miesięczne zestawienie kosztów".
     * Maksymalna długość: 100 znaków.
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Typ raportu, powiązany z encją {@code ReportType}.
     * Każdy raport musi mieć określony typ.
     */
    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private ReportType type;

    /**
     * Użytkownik, który wygenerował raport.
     * Powiązany z encją {@link User}.
     */
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /**
     * Parametry użyte do wygenerowania raportu w formacie JSON.
     * Mogą zawierać np. zakres dat czy dodatkowe filtry.
     */
    @Column(name = "parameters")
    @JdbcTypeCode(SqlTypes.JSON)
    private String parameters;

    /**
     * Nazwa wygenerowanego pliku raportu.
     * Jest unikalna w systemie.
     */
    @Column(name = "file_name", unique = true)
    private String fileName;

    /**
     * Ścieżka do wygenerowanego pliku raportu na serwerze.
     */
    @Column(name = "file_path")
    private String filePath;

    /**
     * Data i czas utworzenia raportu.
     * Automatycznie ustawiana podczas tworzenia encji.
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Domyślny konstruktor klasy {@link Report}.
     * Konstruktor bezparametrowy wymagany przez JPA.
     */
    public Report() {
        // Domyślny konstruktor, wymagany przez JPA do tworzenia nowych instancji encji.
    }
}