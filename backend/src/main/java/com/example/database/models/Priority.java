package com.example.database.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Reprezentuje priorytet zadania w systemie zarządzania zadaniami.
 * Priorytety pozwalają określić ważność poszczególnych zadań,
 * co ułatwia organizację pracy w firmie budowlanej.
 * <p>
 * Klasa jest encją bazy danych i odpowiada tabeli {@code priorities}.
 *
 * <p>Używa adnotacji Lombok {@code @Getter} i {@code @Setter} do generowania
 * odpowiednich metod dostępnych.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@Setter
@Entity
@Table(name = "priorities")
public class Priority {

    /**
     * Unikalny identyfikator priorytetu.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * Nazwa priorytetu, np. "Wysoki", "Średni", "Niski".
     * Musi być unikalna i ma maksymalnie 50 znaków.
     */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Wartość liczbową określająca poziom priorytetu.
     * Im wyższa wartość, tym większy priorytet.
     */
    @Column(name = "value", nullable = false)
    private Integer value;

    /**
     * Kod koloru w formacie HEX, np. "#FF0000" dla czerwonego.
     * Może być używany do wizualnej reprezentacji priorytetów w UI.
     */
    @Column(name = "color_code", length = 7)
    private String colorCode;

    /**
     * Zbiór zadań powiązanych z danym priorytetem.
     * Relacja jednokierunkowa - jedno {@code Priority} może być powiązane z wieloma {@code Task}.
     */
    @OneToMany(mappedBy = "priority")
    private Set<Task> tasks = new HashSet<>();

    /**
     * Domyślny konstruktor klasy {@link Priority}.
     * Konstruktor bezparametrowy wymagany przez JPA.
     */
    public Priority() {
        // Domyślny konstruktor, wymagany przez JPA do tworzenia nowych instancji encji.
    }
}