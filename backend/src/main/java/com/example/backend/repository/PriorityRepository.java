package  com.example.backend.repository;

import com.example.backend.models.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Reposytorium dla encji {@link Priority}.
 * <p>
 * Interfejs ten dziedziczy po {@link JpaRepository}, co zapewnia podstawowe operacje CRUD (tworzenie, odczyt, aktualizacja, usuwanie)
 * oraz dostęp do zaawansowanych zapytań do bazy danych. Repozytorium jest oznaczone adnotacją {@link Repository},
 * co oznacza, że jest ono komponentem Spring, który zapewnia dostęp do danych dla aplikacji.
 * <p>
 * Dodatkowo, definiuje niestandardową metodę wyszukiwania priorytetu na podstawie jego nazwy.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface PriorityRepository extends JpaRepository<Priority, Integer>
{

  /**
   * Znajduje priorytet na podstawie jego nazwy.
   *
   * @param name Nazwa priorytetu.
   * @return Opcjonalny obiekt {@link Optional} zawierający znaleziony priorytet lub pusty, jeśli priorytet nie istnieje.
   */
  Optional<Priority> findByName(String name);
}