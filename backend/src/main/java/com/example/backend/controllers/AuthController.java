package com.example.backend.controllers;

import com.example.backend.dto.LoginRequestDTO;
import com.example.backend.dto.RegisterRequestDTO;
import com.example.backend.dto.UserResponseDTO;
import com.example.backend.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Kontroler REST obsługujący operacje uwierzytelniania i rejestracji.
 * <p>
 * Udostępnia endpointy logowania i rejestracji użytkowników w systemie.
 * Wykorzystuje serwis uwierzytelniania do wykonywania operacji biznesowych.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Konstruktor wstrzykujący zależności.
     *
     * @param authService serwis uwierzytelniania
     */
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint logowania użytkownika.
     *
     * @param loginRequest obiekt zawierający dane logowania
     * @return ResponseEntity z danymi zalogowanego użytkownika lub komunikatem o błędzie
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            Optional<UserResponseDTO> userOpt = authService.login(loginRequest);

            if (userOpt.isPresent()) {
                return ResponseEntity.ok(userOpt.get());
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Niepoprawne dane logowania lub konto jest nieaktywne");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Wystąpił błąd podczas logowania");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint rejestracji nowego użytkownika.
     *
     * @param registerRequest obiekt zawierający dane rejestracji
     * @return ResponseEntity z danymi zarejestrowanego użytkownika lub komunikatem o błędzie
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO registerRequest) {
        try {
            UserResponseDTO newUser = authService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (RuntimeException e) {
            // Tylko specyficzne RuntimeException związane z walidacją danych
            if (e.getMessage().contains("jest już zajęta") || e.getMessage().contains("jest już używany")) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Nie można zarejestrować użytkownika");
                response.put("error", e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            } else {
                // Inne RuntimeException traktujemy jako błędy serwera
                Map<String, String> response = new HashMap<>();
                response.put("message", "Wystąpił błąd podczas rejestracji");
                response.put("error", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Wystąpił nieoczekiwany błąd podczas rejestracji");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint sprawdzający dostępność nazwy użytkownika.
     *
     * @param username nazwa użytkownika do sprawdzenia
     * @return ResponseEntity z informacją o dostępności nazwy użytkownika
     */
    @GetMapping("/check/username/{username}")
    public ResponseEntity<Map<String, Boolean>> checkUsernameAvailability(@PathVariable String username) {
        boolean isAvailable = !authService.userExists(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint sprawdzający dostępność adresu email.
     *
     * @param email adres email do sprawdzenia
     * @return ResponseEntity z informacją o dostępności adresu email
     */
    @GetMapping("/check/email/{email}")
    public ResponseEntity<Map<String, Boolean>> checkEmailAvailability(@PathVariable String email) {
        boolean isAvailable = !authService.emailExists(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);
        return ResponseEntity.ok(response);
    }
}