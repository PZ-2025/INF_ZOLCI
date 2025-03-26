package com.example.database.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Reprezentuje ustawienie systemowe w aplikacji.
 * Ustawienia systemowe są wykorzystywane do przechowywania różnych konfiguracji,
 * takich jak wartości konfiguracyjne czy metadane dotyczące działania systemu.
 * <p>
 * Klasa jest encją bazy danych i odpowiada tabeli {@code system_settings}.
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
@Table(name = "system_settings")
public class SystemSetting {

    /**
     * Unikalny identyfikator ustawienia systemowego.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * Klucz identyfikujący ustawienie systemowe, np. "system_name".
     * Musi być unikalny i ma maksymalnie 100 znaków.
     */
    @Column(name = "key", nullable = false, unique = true, length = 100)
    private String key;

    /**
     * Wartość przypisana do danego ustawienia systemowego.
     * Może być wartością liczbową, tekstową lub innym typem danych.
     */
    @Column(name = "value")
    private String value;

    /**
     * Opcjonalny opis ustawienia systemowego.
     * Zawiera dodatkowe informacje na temat tego, do czego służy konkretne ustawienie.
     */
    @Column(name = "description")
    private String description;

    /**
     * Użytkownik, który dokonał ostatniej aktualizacji ustawienia systemowego.
     * Powiązany z encją {@link User}.
     */
    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    /**
     * Data i czas ostatniej aktualizacji ustawienia systemowego.
     * Automatycznie ustawiana podczas aktualizacji encji.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Domyślny konstruktor klasy {@link SystemSetting}.
     * Konstruktor bezparametrowy wymagany przez JPA.
     */
    public SystemSetting() {
        // Domyślny konstruktor, wymagany przez JPA do tworzenia nowych instancji encji.
    }
}