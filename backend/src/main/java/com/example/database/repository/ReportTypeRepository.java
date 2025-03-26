package com.example.database.repository;

import com.example.database.models.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Reposytorium dla encji {@link ReportType}.
 * <p>
 * Interfejs ten dziedziczy po {@link JpaRepository}, co zapewnia podstawowe operacje CRUD (tworzenie, odczyt, aktualizacja, usuwanie)
 * oraz dostęp do zaawansowanych zapytań do bazy danych. Repozytorium jest oznaczone adnotacją {@link Repository},
 * co oznacza, że jest ono komponentem Spring, który zapewnia dostęp do danych dla aplikacji.
 * <p>
 * Dodatkowo, definiuje niestandardową metodę wyszukiwania raportu na podstawie nazwy typu raportu.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface ReportTypeRepository extends JpaRepository<ReportType, Integer>
{
    /**
     * Znajduje typ raportu na podstawie jego nazwy.
     *
     * @param name Nazwa typu raportu.
     * @return Opcjonalny typ raportu o podanej nazwie.
     */
    Optional<ReportType> findByName(String name);
}