package com.example.backend.services;

import com.example.backend.models.SystemSetting;
import com.example.backend.models.User;
import com.example.backend.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Serwis obsługujący operacje dla encji {@link SystemSetting}.
 * <p>
 * Klasa zawiera logikę biznesową związaną z ustawieniami systemowymi.
 * Implementuje operacje tworzenia, odczytu, aktualizacji i usuwania ustawień systemowych.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@Transactional
public class SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;

    /**
     * Konstruktor wstrzykujący zależność do repozytorium ustawień systemowych.
     *
     * @param systemSettingRepository Repozytorium ustawień systemowych
     */
    @Autowired
    public SystemSettingService(SystemSettingRepository systemSettingRepository) {
        this.systemSettingRepository = systemSettingRepository;
    }

    /**
     * Pobiera wszystkie ustawienia systemowe.
     *
     * @return Lista wszystkich ustawień systemowych
     */
    public List<SystemSetting> getAllSystemSettings() {
        return systemSettingRepository.findAll();
    }

    /**
     * Pobiera ustawienie systemowe na podstawie jego identyfikatora.
     *
     * @param id Identyfikator ustawienia systemowego
     * @return Opcjonalne ustawienie systemowe, jeśli istnieje
     */
    public Optional<SystemSetting> getSystemSettingById(Integer id) {
        return systemSettingRepository.findById(id);
    }

    /**
     * Pobiera ustawienie systemowe na podstawie jego klucza.
     *
     * @param key Klucz ustawienia systemowego
     * @return Opcjonalne ustawienie systemowe, jeśli istnieje
     */
    public Optional<SystemSetting> getSystemSettingByKey(String key) {
        return systemSettingRepository.findByKey(key);
    }

    /**
     * Zapisuje nowe ustawienie systemowe lub aktualizuje istniejące.
     *
     * @param systemSetting Ustawienie systemowe do zapisania
     * @return Zapisane ustawienie systemowe
     */
    public SystemSetting saveSystemSetting(SystemSetting systemSetting) {
        return systemSettingRepository.save(systemSetting);
    }

    /**
     * Tworzy nowe ustawienie systemowe z podanymi parametrami.
     *
     * @param key         Klucz ustawienia systemowego
     * @param value       Wartość ustawienia systemowego
     * @param description Opis ustawienia systemowego
     * @param updatedBy   Użytkownik, który utworzył ustawienie
     * @return Utworzone ustawienie systemowe
     */
    public SystemSetting createSystemSetting(String key, String value,
                                             String description, User updatedBy) {
        SystemSetting setting = new SystemSetting();
        setting.setKey(key);
        setting.setValue(value);
        setting.setDescription(description);
        setting.setUpdatedBy(updatedBy);
        setting.setUpdatedAt(LocalDateTime.now());

        return systemSettingRepository.save(setting);
    }

    /**
     * Aktualizuje wartość ustawienia systemowego.
     *
     * @param key       Klucz ustawienia systemowego
     * @param value     Nowa wartość
     * @param updatedBy Użytkownik dokonujący aktualizacji
     * @return Zaktualizowane ustawienie systemowe lub Optional.empty() jeśli ustawienie nie istnieje
     */
    public Optional<SystemSetting> updateValue(String key, String value, User updatedBy) {
        return systemSettingRepository.findByKey(key)
                .map(setting -> {
                    setting.setValue(value);
                    setting.setUpdatedBy(updatedBy);
                    setting.setUpdatedAt(LocalDateTime.now());
                    return systemSettingRepository.save(setting);
                });
    }

    /**
     * Usuwa ustawienie systemowe na podstawie jego identyfikatora.
     *
     * @param id Identyfikator ustawienia systemowego do usunięcia
     */
    public void deleteSystemSetting(Integer id) {
        systemSettingRepository.deleteById(id);
    }

    /**
     * Sprawdza, czy ustawienie systemowe o podanym kluczu już istnieje.
     *
     * @param key Klucz ustawienia systemowego do sprawdzenia
     * @return true jeśli ustawienie systemowe istnieje, false w przeciwnym razie
     */
    public boolean existsByKey(String key) {
        return systemSettingRepository.findByKey(key).isPresent();
    }

    /**
     * Pobiera wartość ustawienia systemowego jako ciąg znaków.
     *
     * @param key          Klucz ustawienia systemowego
     * @param defaultValue Domyślna wartość, jeśli ustawienie nie istnieje
     * @return Wartość ustawienia lub domyślna wartość
     */
    public String getStringValue(String key, String defaultValue) {
        return systemSettingRepository.findByKey(key)
                .map(SystemSetting::getValue)
                .orElse(defaultValue);
    }

    /**
     * Pobiera wartość ustawienia systemowego jako liczbę całkowitą.
     *
     * @param key          Klucz ustawienia systemowego
     * @param defaultValue Domyślna wartość, jeśli ustawienie nie istnieje lub nie jest liczbą
     * @return Wartość ustawienia jako liczba całkowita lub domyślna wartość
     */
    public int getIntValue(String key, int defaultValue) {
        try {
            return Integer.parseInt(getStringValue(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Pobiera wartość ustawienia systemowego jako wartość logiczną.
     *
     * @param key          Klucz ustawienia systemowego
     * @param defaultValue Domyślna wartość, jeśli ustawienie nie istnieje
     * @return Wartość ustawienia jako wartość logiczna lub domyślna wartość
     */
    public boolean getBooleanValue(String key, boolean defaultValue) {
        String value = getStringValue(key, String.valueOf(defaultValue));
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }
}