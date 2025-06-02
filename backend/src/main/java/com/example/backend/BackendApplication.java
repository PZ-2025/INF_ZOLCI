package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Główna klasa startowa aplikacji BuildTask - systemu zarządzania zadaniami dla firmy budowlanej.
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

    public static void main(final String[] args) {
        List<String> requiredEnvVars = Arrays.asList(
                "MYSQL_ROOT_PASSWORD",
                "MYSQL_DATABASE",
                "MYSQL_USER",
                "MYSQL_PASSWORD",
                "MYSQL_CONNECTION_STRING",
                "SPRING_PROFILES_ACTIVE",
                "REPORTS_STORAGE_PATH"
        );

        boolean dotenvLoaded = false;
        Dotenv dotenv = null;

        // Sprawdź, czy istnieje plik .env w bieżącym katalogu
        if (new File(".env").exists()) {
            dotenv = Dotenv.configure().ignoreIfMissing().load();
            dotenvLoaded = true;
            System.out.println(".env file found — loading environment variables from it.");
        } else {
            System.out.println(".env file not found — falling back to system environment variables.");
        }

        boolean allEnvVarsSet = true;

        for (String envVar : requiredEnvVars) {
            String value = null;

            if (dotenvLoaded) {
                value = dotenv.get(envVar);
            }

            if (value == null || value.isEmpty()) {
                value = System.getenv(envVar);
            }

            if (value == null || value.isEmpty()) {
                System.err.println("UWAGA: Zmienna środowiskowa " + envVar + " nie jest ustawiona!");
                allEnvVarsSet = false;
            } else {
                // Nadpisuj tylko jeśli zmienna nie jest jeszcze ustawiona jako systemowa
                if (System.getProperty(envVar) == null) {
                    System.setProperty(envVar, value);
                }
            }
        }

        if (!allEnvVarsSet) {
            System.err.println("Nie wszystkie wymagane zmienne środowiskowe są ustawione.");
            System.err.println("Upewnij się, że plik .env istnieje lub że skrypt setup-env.sh/.bat został uruchomiony.");
            System.err.println("Kontynuuję uruchamianie, ale aplikacja może nie działać poprawnie...");
        } else {
            System.out.println("Wszystkie wymagane zmienne środowiskowe zostały poprawnie ustawione.");
            System.out.println("Profil aktywny: " + System.getProperty("SPRING_PROFILES_ACTIVE"));
            System.out.println("Ścieżka do raportów: " + System.getProperty("REPORTS_STORAGE_PATH"));
        }

        SpringApplication.run(BackendApplication.class, args);
    }
}
