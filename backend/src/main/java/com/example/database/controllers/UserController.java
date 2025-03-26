package com.example.database.controllers;

import com.example.database.models.User;
import com.example.database.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

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
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Endpoint zwracający użytkownika o podanym identyfikatorze.
     *
     * @param id unikalny identyfikator użytkownika
     * @return ResponseEntity z użytkownikiem (kod 200 OK) lub brakiem użytkownika (kod 404 Not Found)
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
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
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}