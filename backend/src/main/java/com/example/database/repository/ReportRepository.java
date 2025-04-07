package com.example.database.repository;

import com.example.database.models.Report;
import com.example.database.models.ReportType;
import com.example.database.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Reposytorium dla encji {@link Report}.
 * <p>
 * Interfejs ten dziedziczy po {@link JpaRepository}, co zapewnia podstawowe operacje CRUD (tworzenie, odczyt, aktualizacja, usuwanie)
 * oraz dostęp do zaawansowanych zapytań do bazy danych. Repozytorium jest oznaczone adnotacją {@link Repository},
 * co oznacza, że jest ono komponentem Spring, który zapewnia dostęp do danych dla aplikacji.
 * <p>
 * Dodatkowo, definiuje niestandardowe metody wyszukiwania raportów na podstawie typu raportu oraz użytkownika,
 * który je stworzył.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Integer>
{
    /**
     * Znajduje listę raportów na podstawie typu raportu.
     *
     * @param type Typ raportu.
     * @return Lista raportów o podanym typie.
     */
    List<Report> findByType(ReportType type);

    /**
     * Znajduje listę raportów stworzoną przez określonego użytkownika.
     *
     * @param user Użytkownik, który stworzył raporty.
     * @return Lista raportów stworzonych przez określonego użytkownika.
     */
    List<Report> findByCreatedBy(User user);

    Optional<Report> findById(Long id);
}