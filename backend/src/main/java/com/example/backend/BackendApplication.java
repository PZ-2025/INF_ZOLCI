package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * Główna klasa startowa aplikacji BuildTask - systemu zarządzania zadaniami dla firmy budowlanej.
 *
 * <p>Klasa konfiguruje środowisko Spring Boot, inicjalizuje komponenty, repozytoria i modele
 * z określonych pakietów oraz ładuje zmienne środowiskowe przed uruchomieniem aplikacji.</p>
 *
 * @author Jakub
 * @version 1.1.0
 * @since 1.0.0
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.database.repository")
@EntityScan(basePackages = "com.example.database.models")
@ComponentScan(basePackages = {
        "com.example.database",
        "com.example.database.controllers",
        "com.example.database.services"
})

/**
 * Główna metoda startowa aplikacji BuildTask.
 *
 * <p>Odpowiada za:</p>
 * <ul>
 *   <li>Ładowanie zmiennych środowiskowych z pliku .env</li>
 *   <li>Ustawienie zmiennych środowiskowych jako właściwości systemowe</li>
 *   <li>Uruchomienie aplikacji Spring Boot</li>
 * </ul>
 *
 * @param args argumenty wiersza poleceń przekazywane podczas uruchamiania aplikacji
 */
public class BackendApplication {

    /**
     * Główna metoda startowa aplikacji BuildTask.
     *
     * <p>Odpowiada za:</p>
     * <ul>
     *   <li>Ładowanie zmiennych środowiskowych z pliku .env</li>
     *   <li>Ustawienie zmiennych środowiskowych jako właściwości systemowe</li>
     *   <li>Uruchomienie aplikacji Spring Boot</li>
     * </ul>
     *
     * @param args argumenty wiersza poleceń przekazywane podczas uruchamiania aplikacji
     */
    public static void main(String[] args)
    {
        // Ładowanie zmiennych środowiskowych
        Dotenv dotenv = Dotenv.configure().load();

        // Ustaw je jako właściwości systemowe
        dotenv.entries().forEach(e ->
                System.setProperty(e.getKey(), e.getValue())
        );


        SpringApplication.run(BackendApplication.class, args);
    }
}
