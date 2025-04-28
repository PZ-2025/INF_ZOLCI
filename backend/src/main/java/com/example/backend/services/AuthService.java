package com.example.backend.services;

import com.example.backend.dto.LoginRequestDTO;
import com.example.backend.dto.RegisterRequestDTO;
import com.example.backend.dto.UserResponseDTO;
import com.example.backend.models.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Serwis obsługujący operacje uwierzytelniania i rejestracji użytkowników.
 * <p>
 * Zapewnia funkcjonalności logowania, rejestracji oraz weryfikacji poświadczeń.
 * Używa BCrypt do hashowania i weryfikacji haseł.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Konstruktor wstrzykujący zależności.
     *
     * @param userRepository repozytorium użytkowników
     * @param passwordEncoder enkoder haseł
     */
    @Autowired
    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Próbuje zalogować użytkownika na podstawie podanych danych.
     *
     * @param loginRequest obiekt zawierający nazwę użytkownika i hasło
     * @return Optional zawierający zalogowanego użytkownika lub pusty, jeśli logowanie się nie powiodło
     */
    public Optional<UserResponseDTO> login(LoginRequestDTO loginRequest) {
        return userRepository.findByUsername(loginRequest.getUsername())
                .filter(user -> user.getIsActive() && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
                .map(user -> {
                    // Aktualizacja czasu ostatniego logowania
                    user.setLastLogin(LocalDateTime.now());
                    userRepository.save(user);
                    return mapToDTO(user);
                });
    }

    /**
     * Rejestruje nowego użytkownika w systemie.
     *
     * @param registerRequest obiekt zawierający dane nowego użytkownika
     * @return zarejestrowanego użytkownika
     * @throws RuntimeException jeśli nazwa użytkownika lub email są już zajęte
     */
    public UserResponseDTO register(RegisterRequestDTO registerRequest) {
        // Sprawdzenie, czy nazwa użytkownika jest już zajęta
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Nazwa użytkownika jest już zajęta");
        }

        // Sprawdzenie, czy email jest już używany
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Adres email jest już używany");
        }

        // Tworzenie nowego użytkownika
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setEmail(registerRequest.getEmail());
        newUser.setFirstName(registerRequest.getFirstName());
        newUser.setLastName(registerRequest.getLastName());
        newUser.setPhone(registerRequest.getPhone());
        newUser.setRole("użytkownik"); // Domyślna rola
        newUser.setIsActive(true);
        newUser.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(newUser);
        return mapToDTO(savedUser);
    }

    /**
     * Konwertuje encję User na obiekt DTO.
     *
     * @param user encja użytkownika
     * @return obiekt DTO
     */
    private UserResponseDTO mapToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());
        return dto;
    }

    /**
     * Sprawdza, czy podane hasło zgadza się z hasłem użytkownika.
     *
     * @param rawPassword surowe hasło do sprawdzenia
     * @param encodedPassword zahashowane hasło z bazy danych
     * @return true jeśli hasła się zgadzają, false w przeciwnym przypadku
     */
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Hashuje podane hasło za pomocą BCrypt.
     *
     * @param rawPassword surowe hasło do zahashowania
     * @return zahashowane hasło
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Sprawdza, czy istnieje użytkownik o podanej nazwie użytkownika.
     *
     * @param username nazwa użytkownika do sprawdzenia
     * @return true jeśli użytkownik istnieje, false w przeciwnym przypadku
     */
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Sprawdza, czy istnieje użytkownik o podanym adresie email.
     *
     * @param email adres email do sprawdzenia
     * @return true jeśli email jest już używany, false w przeciwnym przypadku
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}