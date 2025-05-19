package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import java.util.Arrays;
import java.util.List;

/**
 * Główna klasa startowa aplikacji BuildTask - systemu zarządzania zadaniami dla firmy budowlanej.
 *
 * <p>Klasa konfiguruje środowisko Spring Boot, inicjalizuje komponenty, repozytoria i modele
 * z określonych pakietów oraz wykorzystuje zmienne środowiskowe ustawione przez skrypty.</p>
 *
 * @author Jakub
 * @version 1.2.0
 * @since 1.0.0
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.backend.repository")
@EntityScan(basePackages = "com.example.backend.models")
@ComponentScan(basePackages = {
        "com.example.backend",
        "com.example.backend.controllers",
        "com.example.backend.services"
})
public class BackendApplication {

    /**
     * Główna metoda startowa aplikacji BuildTask.
     *
     * <p>Odpowiada za:</p>
     * <ul>
     *   <li>Sprawdzenie i wykorzystanie zmiennych środowiskowych ustawionych przez skrypty</li>
     *   <li>Ustawienie zmiennych środowiskowych jako właściwości systemowe (jeśli jeszcze nie są)</li>
     *   <li>Uruchomienie aplikacji Spring Boot</li>
     * </ul>
     *
     * @param args argumenty wiersza poleceń przekazywane podczas uruchamiania aplikacji
     */
    public static void main(final String[] args) {
        // Lista kluczowych zmiennych środowiskowych, które muszą być ustawione
        List<String> requiredEnvVars = Arrays.asList(
                "MYSQL_ROOT_PASSWORD",
                "MYSQL_DATABASE",
                "MYSQL_USER",
                "MYSQL_PASSWORD",
                "MYSQL_CONNECTION_STRING",
                "SPRING_PROFILES_ACTIVE",
                "REPORTS_STORAGE_PATH"
        );

        // Sprawdź, czy wszystkie wymagane zmienne środowiskowe są ustawione
        boolean allEnvVarsSet = true;
        for (String envVar : requiredEnvVars) {
            String value = System.getenv(envVar);
            if (value == null || value.isEmpty()) {
                System.err.println("UWAGA: Zmienna środowiskowa " + envVar + " nie jest ustawiona!");
                allEnvVarsSet = false;
            } else {
                // Ustaw zmienną jako właściwość systemową (jeśli nie jest już ustawiona)
                if (System.getProperty(envVar) == null) {
                    System.setProperty(envVar, value);
                }
            }
        }

        if (!allEnvVarsSet) {
            System.err.println("Nie wszystkie wymagane zmienne środowiskowe są ustawione.");
            System.err.println("Uruchom najpierw skrypt konfiguracyjny setup-env.sh (Linux) lub setup-env.bat/setup-env.ps1 (Windows).");
            System.err.println("Kontynuuję uruchamianie, ale aplikacja może nie działać poprawnie...");
        } else {
            System.out.println("Wszystkie wymagane zmienne środowiskowe zostały poprawnie ustawione.");
            System.out.println("Profil aktywny: " + System.getProperty("SPRING_PROFILES_ACTIVE"));
            System.out.println("Ścieżka do raportów: " + System.getProperty("REPORTS_STORAGE_PATH"));
        }

        // Uruchom aplikację Spring Boot
        SpringApplication.run(BackendApplication.class, args);
    }
}