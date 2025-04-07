package  com.example.backend.repository;

import com.example.backend.models.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Reposytorium dla encji {@link SystemSetting}.
 * <p>
 * Interfejs ten dziedziczy po {@link JpaRepository}, co zapewnia podstawowe operacje CRUD (tworzenie, odczyt, aktualizacja, usuwanie)
 * oraz dostęp do zaawansowanych zapytań do bazy danych. Repozytorium jest oznaczone adnotacją {@link Repository},
 * co oznacza, że jest ono komponentem Spring, który zapewnia dostęp do danych w aplikacji.
 * <p>
 * Definiuje metodę do wyszukiwania ustawienia systemowego na podstawie jego klucza.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Integer>
{
    /**
     * Znajduje ustawienie systemowe na podstawie klucza.
     *
     * @param key Klucz ustawienia systemowego.
     * @return Opcjonalne ustawienie systemowe o podanym kluczu.
     */
    Optional<SystemSetting> findByKey(String key);
}