package com.example.backend.services;

import com.example.backend.dto.UserDTO;
import com.example.backend.models.User;
import com.example.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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
            user.setPassword(dto.getPassword());
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
        User user = mapToEntity(userDTO);
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);
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
                    // Aktualizujemy tylko dozwolone pola
                    existingUser.setUsername(userDTO.getUsername());
                    existingUser.setEmail(userDTO.getEmail());
                    existingUser.setFirstName(userDTO.getFirstName());
                    existingUser.setLastName(userDTO.getLastName());
                    existingUser.setPhone(userDTO.getPhone());
                    existingUser.setRole(userDTO.getRole());

                    // Aktualizacja hasła tylko jeśli zostało podane
                    if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                        existingUser.setPassword(userDTO.getPassword());
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
}