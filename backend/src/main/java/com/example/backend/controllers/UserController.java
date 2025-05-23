package com.example.backend.controllers;

import com.example.backend.dto.UserDTO;
import com.example.backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kontroler REST obsługujący operacje związane z użytkownikami w systemie BuildTask.
 *
 * <p>Udostępnia punkty końcowe API do:</p>
 * <ul>
 *   <li>Pobierania listy wszystkich użytkowników</li>
 *   <li>Wyszukiwania użytkownika po identyfikatorze</li>
 *   <li>Wyszukiwania użytkownika po nazwie użytkownika</li>
 * </ul>
 *
 * <p>Wszystkie endpoints są mapowane pod ścieżką bazowej {@code /database/users}.</p>
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/database/users")
@RequiredArgsConstructor
public class UserController {

    /** Serwis użytkowników do obsługi operacji biznesowych. */
    private final UserService userService;

    /**
     * Endpoint zwracający listę wszystkich użytkowników w systemie.
     *
     * @return ResponseEntity z listą użytkowników i kodem statusu 200 OK
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Endpoint zwracający użytkownika o podanym identyfikatorze.
     *
     * @param id unikalny identyfikator użytkownika
     * @return ResponseEntity z użytkownikiem (kod 200 OK) lub brakiem użytkownika (kod 404 Not Found)
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint zwracający użytkownika o podanej nazwie użytkownika.
     *
     * @param username nazwa użytkownika do wyszukania
     * @return ResponseEntity z użytkownikiem (kod 200 OK) lub brakiem użytkownika (kod 404 Not Found)
     */
    @GetMapping(value = "/username/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint zwracający aktywnych użytkowników.
     */
    @GetMapping(value = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDTO>> getActiveUsers() {
        return ResponseEntity.ok(userService.findActiveUsers());
    }

    /**
     * Endpoint do tworzenia nowego użytkownika.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        try {
            UserDTO createdUser = userService.createUser(userDTO);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint do aktualizacji użytkownika.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> updateUser(@PathVariable("id") Integer id, @Valid @RequestBody UserDTO userDTO) {
        return userService.updateUser(id, userDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint do usuwania użytkownika.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Integer id) {
        return userService.deleteUser(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /**
     * Endpoint do dezaktywacji użytkownika.
     */
    @PutMapping(value = "/{id}/deactivate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> deactivateUser(@PathVariable("id") Integer id) {
        return userService.deactivateUser(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint do aktualizacji czasu ostatniego logowania.
     */
    @PutMapping(value = "/{id}/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> updateLastLogin(@PathVariable("id") Integer id) {
        return userService.updateLastLogin(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint do częściowej aktualizacji użytkownika.
     * Pozwala na aktualizację wybranych pól użytkownika bez konieczności wysyłania całego obiektu.
     */
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> partialUpdateUser(@PathVariable("id") Integer id, @RequestBody Map<String, Object> updates) {
        try {
            return userService.partialUpdateUser(id, updates)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Wystąpił błąd podczas aktualizacji użytkownika");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}