package com.example.backend.services;

import com.example.backend.dto.UserDTO;
import com.example.backend.models.User;
import com.example.backend.repository.UserRepository;
import io.micrometer.observation.ObservationFilter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serwis obsługujący operacje na obiektach użytkowników w systemie BuildTask.
 *
 * <p>Zapewnia podstawowe operacje CRUD (Create, Read, Update, Delete)
 * dla encji użytkowników, takie jak:</p>
 * <ul>
 *   <li>Pobieranie wszystkich użytkowników</li>
 *   <li>Wyszukiwanie użytkowników po ID lub nazwie użytkownika</li>
 *   <li>Zapis nowych użytkowników</li>
 *   <li>Usuwanie użytkowników</li>
 * </ul>
 *
 * <p>Wykorzystuje {@link UserRepository} do interakcji z bazą danych.</p>
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Mapuje encję User na obiekt DTO.
     */
    public UserDTO mapToDTO(User user) {
        if (user == null) return null;

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPassword(null); // Nie przekazujemy hasła w DTO
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
     * Mapuje obiekt DTO na encję User.
     */
    public User mapToEntity(UserDTO dto) {
        if (dto == null) return null;

        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());

        // Zachowujemy hasło tylko jeśli jest ustawione w DTO
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());
        user.setIsActive(dto.getIsActive());
        user.setCreatedAt(dto.getCreatedAt());
        user.setLastLogin(dto.getLastLogin());

        return user;
    }

    /**
     * Pobiera listę wszystkich użytkowników z systemu jako DTO.
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Wyszukuje użytkownika o podanym identyfikatorze jako DTO.
     */
    public Optional<UserDTO> getUserById(Integer id) {
        return userRepository.findById(id)
                .map(this::mapToDTO);
    }

    /**
     * Wyszukuje użytkownika o podanej nazwie użytkownika jako DTO.
     */
    public Optional<UserDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::mapToDTO);
    }

    /**
     * Zapisuje lub aktualizuje użytkownika w systemie i zwraca jako DTO.
     */
    public UserDTO saveUser(UserDTO userDTO) {
        User user = mapToEntity(userDTO);

        // Jeśli to nowy użytkownik, ustawiamy createdAt
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }

        // Jeśli isActive nie jest ustawione, domyślnie true
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }

        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    /**
     * Usuwa użytkownika na podstawie ID.
     */
    public boolean deleteUser(Integer userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    userRepository.delete(user);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Zwraca listę aktywnych użytkowników jako DTO.
     */
    public List<UserDTO> findActiveUsers() {
        return userRepository.findByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tworzy nowego użytkownika i zwraca jako DTO.
     */
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        // Sprawdzamy czy hasło jest podane
        if (userDTO.getPassword() == null || userDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Hasło jest wymagane dla nowych użytkowników");
        }

        User user = mapToEntity(userDTO);
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);

        // Hasło jest już hashowane w mapToEntity

        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    /**
     * Aktualizuje istniejącego użytkownika i zwraca jako DTO.
     */
    @Transactional
    public Optional<UserDTO> updateUser(Integer userId, UserDTO userDTO) {
        return userRepository.findById(userId)
                .map(existingUser -> {
                    // Aktualizujemy tylko pola, które nie są null w DTO
                    if (userDTO.getUsername() != null) {
                        existingUser.setUsername(userDTO.getUsername());
                    }
                    if (userDTO.getEmail() != null) {
                        existingUser.setEmail(userDTO.getEmail());
                    }
                    if (userDTO.getFirstName() != null) {
                        existingUser.setFirstName(userDTO.getFirstName());
                    }
                    if (userDTO.getLastName() != null) {
                        existingUser.setLastName(userDTO.getLastName());
                    }
                    // Aktualizujemy telefon tylko jeśli nie jest null
                    if (userDTO.getPhone() != null) {
                        existingUser.setPhone(userDTO.getPhone());
                    }

                    if (userDTO.getRole() != null) {
                        existingUser.setRole(userDTO.getRole());
                    }
                    if (userDTO.getIsActive() != null) {
                        existingUser.setIsActive(userDTO.getIsActive());
                    }

                    // Aktualizacja hasła tylko jeśli zostało explicite podane
                    if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                    }

                    return mapToDTO(userRepository.save(existingUser));
                });
    }

    /**
     * Dezaktywuje użytkownika i zwraca jako DTO.
     */
    @Transactional
    public Optional<UserDTO> deactivateUser(Integer userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setIsActive(false);
                    return mapToDTO(userRepository.save(user));
                });
    }

    /**
     * Aktualizuje czas ostatniego logowania i zwraca użytkownika jako DTO.
     */
    @Transactional
    public Optional<UserDTO> updateLastLogin(Integer userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setLastLogin(LocalDateTime.now());
                    return mapToDTO(userRepository.save(user));
                });
    }

    /**
     * Metoda aktualizująca częściowo dane użytkownika (PATCH).
     * Zmienia tylko te pola, które zostały przekazane w żądaniu.
     *
     * @param id      Identyfikator użytkownika do aktualizacji
     * @param updates Mapa zawierająca pola do aktualizacji i ich nowe wartości
     * @return Optional zawierający zaktualizowany obiekt UserDTO lub pusty Optional, jeśli użytkownik nie istnieje
     */
    public Optional<UserDTO> partialUpdateUser(Integer id, Map<String, Object> updates) {
        return userRepository.findById(id)
                .map(user -> {

                    if(updates.containsKey("username")) {
                       user.setUsername((String) updates.get("username"));
                    }
                    // Aktualizacja imienia
                    if (updates.containsKey("firstName")) {
                        user.setFirstName((String) updates.get("firstName"));
                    }

                    // Aktualizacja nazwiska
                    if (updates.containsKey("lastName")) {
                        user.setLastName((String) updates.get("lastName"));
                    }

                    // Aktualizacja e-maila
                    if (updates.containsKey("email")) {
                        String newEmail = (String) updates.get("email");
                        // Sprawdź czy e-mail nie jest już używany przez innego użytkownika
                        if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                            throw new RuntimeException("Email jest już używany przez innego użytkownika");
                        }
                        user.setEmail(newEmail);
                    }

                    // Aktualizacja numeru telefonu
                    if (updates.containsKey("phone")) {
                        user.setPhone((String) updates.get("phone"));
                    }

                    // Aktualizacja hasła - wymaga weryfikacji aktualnego hasła
                    if (updates.containsKey("password") && updates.containsKey("currentPassword")) {
                        String currentPassword = (String) updates.get("currentPassword");
                        String newPassword = (String) updates.get("password");

                        // Weryfikacja aktualnego hasła
                        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                            throw new RuntimeException("Aktualne hasło jest nieprawidłowe");
                        }

                        // Walidacja nowego hasła
                        if (newPassword.length() < 6) {
                            throw new RuntimeException("Hasło musi mieć co najmniej 6 znaków");
                        }

                        // Zakodowanie i ustawienie nowego hasła
                        user.setPassword(passwordEncoder.encode(newPassword));
                    }

                    // Aktualizacja roli (jeśli dozwolona)
                    if (updates.containsKey("role")) {
                        // Tu można dodać dodatkową logikę weryfikującą uprawnienia
                        user.setRole((String) updates.get("role"));
                    }

                    // Aktualizacja statusu aktywności
                    if (updates.containsKey("isActive")) {
                        user.setIsActive((Boolean) updates.get("isActive"));
                    }

                    // Zapisz zaktualizowanego użytkownika
                    User savedUser = userRepository.save(user);
                    return mapToDTO(savedUser);
                });
    }
}