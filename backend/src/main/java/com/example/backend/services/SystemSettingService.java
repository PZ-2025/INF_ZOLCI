package com.example.backend.services;

import com.example.backend.dto.SystemSettingDTO;
import com.example.backend.models.SystemSetting;
import com.example.backend.models.User;
import com.example.backend.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * Mapuje encję SystemSetting na obiekt DTO.
     *
     * @param systemSetting Encja do mapowania
     * @return Obiekt DTO reprezentujący ustawienie systemowe
     */
    private SystemSettingDTO mapToDTO(SystemSetting systemSetting) {
        if (systemSetting == null) return null;

        SystemSettingDTO dto = new SystemSettingDTO();
        dto.setId(systemSetting.getId());
        dto.setKey(systemSetting.getKey());
        dto.setValue(systemSetting.getValue());
        dto.setDescription(systemSetting.getDescription());

        if (systemSetting.getUpdatedBy() != null) {
            dto.setUpdatedById(systemSetting.getUpdatedBy().getId());
            dto.setUpdatedByUsername(systemSetting.getUpdatedBy().getUsername());
            dto.setUpdatedByFullName(systemSetting.getUpdatedBy().getFirstName() + " " +
                    systemSetting.getUpdatedBy().getLastName());
        }

        dto.setUpdatedAt(systemSetting.getUpdatedAt());

        return dto;
    }

    /**
     * Mapuje obiekt DTO na encję SystemSetting.
     *
     * @param dto Obiekt DTO do mapowania
     * @param updatedBy Użytkownik, który zaktualizował ustawienie (może być null)
     * @return Encja reprezentująca ustawienie systemowe
     */
    private SystemSetting mapToEntity(SystemSettingDTO dto, User updatedBy) {
        if (dto == null) return null;

        SystemSetting entity = null;

        // Jeśli ID istnieje, próbujemy znaleźć istniejącą encję
        if (dto.getId() != null) {
            entity = systemSettingRepository.findById(dto.getId()).orElse(new SystemSetting());
        } else {
            entity = new SystemSetting();
        }

        entity.setKey(dto.getKey());
        entity.setValue(dto.getValue());
        entity.setDescription(dto.getDescription());
        entity.setUpdatedBy(updatedBy);

        // Aktualizuj datę tylko jeśli nie jest ustawiona lub jesteśmy w trybie aktualizacji
        if (dto.getUpdatedAt() != null) {
            entity.setUpdatedAt(dto.getUpdatedAt());
        } else {
            entity.setUpdatedAt(LocalDateTime.now());
        }

        return entity;
    }

    /**
     * Pobiera wszystkie ustawienia systemowe jako DTO.
     *
     * @return Lista wszystkich ustawień systemowych jako DTO
     */
    public List<SystemSettingDTO> getAllSystemSettings() {
        return systemSettingRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera ustawienie systemowe na podstawie jego identyfikatora jako DTO.
     *
     * @param id Identyfikator ustawienia systemowego
     * @return Opcjonalne ustawienie systemowe jako DTO, jeśli istnieje
     */
    public Optional<SystemSettingDTO> getSystemSettingById(Integer id) {
        return systemSettingRepository.findById(id)
                .map(this::mapToDTO);
    }

    /**
     * Pobiera ustawienie systemowe na podstawie jego klucza jako DTO.
     *
     * @param key Klucz ustawienia systemowego
     * @return Opcjonalne ustawienie systemowe jako DTO, jeśli istnieje
     */
    public Optional<SystemSettingDTO> getSystemSettingByKey(String key) {
        return systemSettingRepository.findByKey(key)
                .map(this::mapToDTO);
    }

    /**
     * Zapisuje nowe ustawienie systemowe lub aktualizuje istniejące.
     *
     * @param systemSettingDTO Ustawienie systemowe jako DTO do zapisania
     * @return Zapisane ustawienie systemowe jako DTO
     */
    public SystemSettingDTO saveSystemSetting(SystemSettingDTO systemSettingDTO) {
        User updatedBy = null;

        // Próbujemy znaleźć istniejące ustawienie, jeśli podano ID
        SystemSetting existingEntity = null;
        if (systemSettingDTO.getId() != null) {
            existingEntity = systemSettingRepository.findById(systemSettingDTO.getId()).orElse(null);
        }

        // Jeśli nie znaleziono i klucz istnieje, szukamy po kluczu
        if (existingEntity == null && systemSettingDTO.getKey() != null) {
            existingEntity = systemSettingRepository.findByKey(systemSettingDTO.getKey()).orElse(null);
        }

        // Jeśli istnieje i ma updatedBy, zachowujemy tę wartość
        if (existingEntity != null && existingEntity.getUpdatedBy() != null) {
            updatedBy = existingEntity.getUpdatedBy();
        }

        SystemSetting entity = mapToEntity(systemSettingDTO, updatedBy);
        SystemSetting savedEntity = systemSettingRepository.save(entity);
        return mapToDTO(savedEntity);
    }

    /**
     * Tworzy nowe ustawienie systemowe z podanymi parametrami.
     *
     * @param key         Klucz ustawienia systemowego
     * @param value       Wartość ustawienia systemowego
     * @param description Opis ustawienia systemowego
     * @param updatedBy   Użytkownik, który utworzył ustawienie
     * @return Utworzone ustawienie systemowe jako DTO
     */
    public SystemSettingDTO createSystemSetting(String key, String value,
                                                String description, User updatedBy) {
        SystemSetting setting = new SystemSetting();
        setting.setKey(key);
        setting.setValue(value);
        setting.setDescription(description);
        setting.setUpdatedBy(updatedBy);
        setting.setUpdatedAt(LocalDateTime.now());

        SystemSetting savedSetting = systemSettingRepository.save(setting);
        return mapToDTO(savedSetting);
    }

    /**
     * Aktualizuje wartość ustawienia systemowego.
     *
     * @param key       Klucz ustawienia systemowego
     * @param value     Nowa wartość
     * @param updatedBy Użytkownik dokonujący aktualizacji
     * @return Zaktualizowane ustawienie systemowe jako DTO lub Optional.empty() jeśli ustawienie nie istnieje
     */
    public Optional<SystemSettingDTO> updateValue(String key, String value, User updatedBy) {
        return systemSettingRepository.findByKey(key)
                .map(setting -> {
                    setting.setValue(value);
                    setting.setUpdatedBy(updatedBy);
                    setting.setUpdatedAt(LocalDateTime.now());
                    return mapToDTO(systemSettingRepository.save(setting));
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