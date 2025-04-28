package com.example.backend.controllers;

import com.example.backend.dto.SystemSettingDTO;
import com.example.backend.dto.UserDTO;
import com.example.backend.models.User;
import com.example.backend.services.SystemSettingService;
import com.example.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Kontroler REST dla operacji na ustawieniach systemowych.
 * <p>
 * Klasa zapewnia endpoints do zarządzania ustawieniami systemowymi w systemie,
 * w tym tworzenie, odczyt, aktualizację i usuwanie ustawień.
 *
 * @author Jakub
 * @version 1.0.1
 * @since 1.0.0
 */
@RestController
@RequestMapping("/database/system-settings")
public class SystemSettingController {

    private final SystemSettingService systemSettingService;
    private final UserService userService;

    /**
     * Konstruktor wstrzykujący zależności.
     *
     * @param systemSettingService Serwis ustawień systemowych
     * @param userService          Serwis użytkowników
     */
    @Autowired
    public SystemSettingController(SystemSettingService systemSettingService,
                                   UserService userService) {
        this.systemSettingService = systemSettingService;
        this.userService = userService;
    }

    /**
     * Mapuje UserDTO na encję User.
     *
     * @param userDTO DTO użytkownika
     * @return encja User
     */
    private User mapUserDtoToEntity(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setRole(userDTO.getRole());
        user.setIsActive(userDTO.getIsActive());
        // Pomijamy hasło i inne wrażliwe dane
        // user.setCreatedAt() i user.setLastLogin() również pomijamy
        return user;
    }

    /**
     * Pobiera wszystkie ustawienia systemowe.
     *
     * @return Lista wszystkich ustawień systemowych
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SystemSettingDTO>> getAllSystemSettings() {
        List<SystemSettingDTO> settings = systemSettingService.getAllSystemSettings();
        return new ResponseEntity<>(settings, HttpStatus.OK);
    }

    /**
     * Pobiera ustawienie systemowe na podstawie jego identyfikatora.
     *
     * @param id Identyfikator ustawienia systemowego
     * @return Ustawienie systemowe lub status 404, jeśli nie istnieje
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SystemSettingDTO> getSystemSettingById(@PathVariable Integer id) {
        return systemSettingService.getSystemSettingById(id)
                .map(setting -> new ResponseEntity<>(setting, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Pobiera ustawienie systemowe na podstawie jego klucza.
     *
     * @param key Klucz ustawienia systemowego
     * @return Ustawienie systemowe lub status 404, jeśli nie istnieje
     */
    @GetMapping(value = "/key/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SystemSettingDTO> getSystemSettingByKey(@PathVariable String key) {
        return systemSettingService.getSystemSettingByKey(key)
                .map(setting -> new ResponseEntity<>(setting, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Pobiera wartość ustawienia systemowego jako ciąg znaków.
     *
     * @param key          Klucz ustawienia systemowego
     * @param defaultValue Domyślna wartość, jeśli ustawienie nie istnieje
     * @return Wartość ustawienia lub domyślna wartość
     */
    @GetMapping(value = "/value/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSettingValue(
            @PathVariable String key,
            @RequestParam(required = false, defaultValue = "") String defaultValue) {

        String value = systemSettingService.getStringValue(key, defaultValue);
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    /**
     * Tworzy nowe ustawienie systemowe.
     *
     * @param systemSettingDTO Dane nowego ustawienia systemowego
     * @return Utworzone ustawienie systemowe
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SystemSettingDTO> createSystemSetting(@Valid @RequestBody SystemSettingDTO systemSettingDTO) {
        // Sprawdzanie, czy klucz już istnieje
        if (systemSettingService.existsByKey(systemSettingDTO.getKey())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        // Sprawdzanie, czy podany użytkownik istnieje
        if (systemSettingDTO.getUpdatedById() != null) {
            Optional<UserDTO> userDtoOpt = userService.getUserById(systemSettingDTO.getUpdatedById());
            if (userDtoOpt.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        try {
            SystemSettingDTO savedSetting = systemSettingService.saveSystemSetting(systemSettingDTO);
            return new ResponseEntity<>(savedSetting, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Tworzy nowe ustawienie systemowe na podstawie podanych parametrów.
     *
     * @param payload Mapa zawierająca key, value, description, updatedById
     * @return Utworzone ustawienie systemowe lub status błędu
     */
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SystemSettingDTO> createSystemSettingFromParams(@RequestBody Map<String, Object> payload) {
        String key = (String) payload.get("key");
        String value = (String) payload.get("value");
        String description = (String) payload.get("description");
        Integer updatedById = (Integer) payload.get("updatedById");

        if (key == null || updatedById == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (systemSettingService.existsByKey(key)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        Optional<UserDTO> userDtoOpt = userService.getUserById(updatedById);
        if (userDtoOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        User user = mapUserDtoToEntity(userDtoOpt.get());

        try {
            SystemSettingDTO newSetting = systemSettingService.createSystemSetting(
                    key, value, description, user);
            return new ResponseEntity<>(newSetting, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Aktualizuje istniejące ustawienie systemowe.
     *
     * @param id            Identyfikator ustawienia systemowego
     * @param systemSettingDTO Zaktualizowane dane ustawienia systemowego
     * @return Zaktualizowane ustawienie systemowe lub status 404, jeśli nie istnieje
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SystemSettingDTO> updateSystemSetting(@PathVariable Integer id,
                                                                @Valid @RequestBody SystemSettingDTO systemSettingDTO) {
        if (systemSettingService.getSystemSettingById(id).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Sprawdzanie, czy podany użytkownik istnieje
        if (systemSettingDTO.getUpdatedById() != null) {
            Optional<UserDTO> userDtoOpt = userService.getUserById(systemSettingDTO.getUpdatedById());
            if (userDtoOpt.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        systemSettingDTO.setId(id);
        try {
            SystemSettingDTO updatedSetting = systemSettingService.saveSystemSetting(systemSettingDTO);
            return new ResponseEntity<>(updatedSetting, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Aktualizuje wartość ustawienia systemowego na podstawie klucza.
     *
     * @param key      Klucz ustawienia systemowego
     * @param payload  Mapa zawierająca value, updatedById
     * @return Zaktualizowane ustawienie systemowe lub status 404, jeśli nie istnieje
     */
    @PatchMapping(value = "/key/{key}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SystemSettingDTO> updateSettingValue(
            @PathVariable String key,
            @RequestBody Map<String, Object> payload) {

        String value = (String) payload.get("value");
        Integer updatedById = (Integer) payload.get("updatedById");

        if (value == null || updatedById == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<UserDTO> userDtoOpt = userService.getUserById(updatedById);
        if (userDtoOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        User user = mapUserDtoToEntity(userDtoOpt.get());

        try {
            return systemSettingService.updateValue(key, value, user)
                    .map(setting -> new ResponseEntity<>(setting, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Usuwa ustawienie systemowe na podstawie jego identyfikatora.
     *
     * @param id Identyfikator ustawienia systemowego do usunięcia
     * @return Status 204 po pomyślnym usunięciu lub 404, jeśli nie istnieje
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSystemSetting(@PathVariable Integer id) {
        if (systemSettingService.getSystemSettingById(id).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            systemSettingService.deleteSystemSetting(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}