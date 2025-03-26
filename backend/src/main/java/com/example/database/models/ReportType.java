package com.example.database.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

/**
 * Reprezentuje typ raportu w systemie zarządzania zadaniami.
 * Typy raportów definiują różne kategorie analiz, które można generować,
 * np. raporty postępu projektu itp.
 * <p>
 * Klasa jest encją bazy danych i odpowiada tabeli {@code report_types}.
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
@Table(name = "report_types")
public class ReportType {

    /**
     * Unikalny identyfikator typu raportu.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * Nazwa typu raportu, np. "Raport postępu".
     * Musi być unikalna i ma maksymalnie 100 znaków.
     */
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Opcjonalny opis typu raportu, zawierający szczegółowe informacje na temat jego przeznaczenia.
     */
    @Column(name = "description")
    private String description;

    /**
     * Ścieżka do szablonu raportu na serwerze.
     * Może wskazywać na plik używany do generowania raportów tego typu.
     */
    @Column(name = "template_path")
    private String templatePath;

    /**
     * Zbiór raportów, które należą do tego typu.
     * Relacja jednokierunkowa - jeden {@code ReportType} może mieć wiele {@code Report}.
     */
    @OneToMany(mappedBy = "type")
    private Set<Report> reports = new HashSet<>();

    /**
     * Domyślny konstruktor klasy {@link ReportType}.
     * Konstruktor bezparametrowy wymagany przez JPA.
     */
    public ReportType() {
        // Domyślny konstruktor, wymagany przez JPA do tworzenia nowych instancji encji.
    }
}