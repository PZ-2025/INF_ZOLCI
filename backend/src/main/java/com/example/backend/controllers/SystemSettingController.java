package com.example.backend.controllers;

import com.example.backend.models.SystemSetting;
import com.example.backend.models.User;
import com.example.backend.services.SystemSettingService;
import com.example.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
 * @version 1.0.0
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
     * Pobiera wszystkie ustawienia systemowe.
     *
     * @return Lista wszystkich ustawień systemowych
     */
    @GetMapping
    public ResponseEntity<List<SystemSetting>> getAllSystemSettings() {
        List<SystemSetting> settings = systemSettingService.getAllSystemSettings();
        return new ResponseEntity<>(settings, HttpStatus.OK);
    }

    /**
     * Pobiera ustawienie systemowe na podstawie jego identyfikatora.
     *
     * @param id Identyfikator ustawienia systemowego
     * @return Ustawienie systemowe lub status 404, jeśli nie istnieje
     */
    @GetMapping("/{id}")
    public ResponseEntity<SystemSetting> getSystemSettingById(@PathVariable Integer id) {
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
    @GetMapping("/key/{key}")
    public ResponseEntity<SystemSetting> getSystemSettingByKey(@PathVariable String key) {
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
    @GetMapping("/value/{key}")
    public ResponseEntity<String> getSettingValue(
            @PathVariable String key,
            @RequestParam(required = false, defaultValue = "") String defaultValue) {

        String value = systemSettingService.getStringValue(key, defaultValue);
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    /**
     * Tworzy nowe ustawienie systemowe.
     *
     * @param systemSetting Dane nowego ustawienia systemowego
     * @return Utworzone ustawienie systemowe
     */
    @PostMapping
    public ResponseEntity<SystemSetting> createSystemSetting(@RequestBody SystemSetting systemSetting) {
        if (systemSettingService.existsByKey(systemSetting.getKey())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        SystemSetting savedSetting = systemSettingService.saveSystemSetting(systemSetting);
        return new ResponseEntity<>(savedSetting, HttpStatus.CREATED);
    }

    /**
     * Tworzy nowe ustawienie systemowe na podstawie podanych parametrów.
     *
     * @param payload Mapa zawierająca key, value, description, updatedById
     * @return Utworzone ustawienie systemowe lub status błędu
     */
    @PostMapping("/create")
    public ResponseEntity<SystemSetting> createSystemSettingFromParams(@RequestBody Map<String, Object> payload) {
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

        Optional<User> userOpt = userService.getUserById(updatedById);
        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        SystemSetting newSetting = systemSettingService.createSystemSetting(
                key, value, description, userOpt.get());

        return new ResponseEntity<>(newSetting, HttpStatus.CREATED);
    }

    /**
     * Aktualizuje istniejące ustawienie systemowe.
     *
     * @param id            Identyfikator ustawienia systemowego
     * @param systemSetting Zaktualizowane dane ustawienia systemowego
     * @return Zaktualizowane ustawienie systemowe lub status 404, jeśli nie istnieje
     */
    @PutMapping("/{id}")
    public ResponseEntity<SystemSetting> updateSystemSetting(@PathVariable Integer id,
                                                             @RequestBody SystemSetting systemSetting) {
        if (!systemSettingService.getSystemSettingById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        systemSetting.setId(id);
        SystemSetting updatedSetting = systemSettingService.saveSystemSetting(systemSetting);
        return new ResponseEntity<>(updatedSetting, HttpStatus.OK);
    }

    /**
     * Aktualizuje wartość ustawienia systemowego na podstawie klucza.
     *
     * @param key      Klucz ustawienia systemowego
     * @param payload  Mapa zawierająca value, updatedById
     * @return Zaktualizowane ustawienie systemowe lub status 404, jeśli nie istnieje
     */
    @PatchMapping("/key/{key}")
    public ResponseEntity<SystemSetting> updateSettingValue(
            @PathVariable String key,
            @RequestBody Map<String, Object> payload) {

        String value = (String) payload.get("value");
        Integer updatedById = (Integer) payload.get("updatedById");

        if (value == null || updatedById == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<User> userOpt = userService.getUserById(updatedById);
        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return systemSettingService.updateValue(key, value, userOpt.get())
                .map(setting -> new ResponseEntity<>(setting, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Usuwa ustawienie systemowe na podstawie jego identyfikatora.
     *
     * @param id Identyfikator ustawienia systemowego do usunięcia
     * @return Status 204 po pomyślnym usunięciu lub 404, jeśli nie istnieje
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSystemSetting(@PathVariable Integer id) {
        if (!systemSettingService.getSystemSettingById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        systemSettingService.deleteSystemSetting(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}