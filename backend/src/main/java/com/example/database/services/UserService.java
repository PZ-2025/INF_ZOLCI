package com.example.database.services;

import com.example.database.models.User;
import com.example.database.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    /** Repozytorium użytkowników do wykonywania operacji bazodanowych. */
    private final UserRepository userRepository;

    /**
     * Pobiera listę wszystkich użytkowników z systemu.
     *
     * @return lista wszystkich użytkowników
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Wyszukuje użytkownika o podanym identyfikatorze.
     *
     * @param id unikalny identyfikator użytkownika
     * @return {@link Optional} zawierający użytkownika lub pusty, jeśli nie znaleziono
     */
    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    /**
     * Wyszukuje użytkownika o podanej nazwie użytkownika.
     *
     * @param username nazwa użytkownika do wyszukania
     * @return {@link Optional} zawierający użytkownika lub pusty, jeśli nie znaleziono
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Zapisuje lub aktualizuje użytkownika w systemie.
     *
     * @param user obiekt użytkownika do zapisu
     * @return zapisany obiekt użytkownika
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Usuwa użytkownika o podanym identyfikatorze.
     *
     * @param id identyfikator użytkownika do usunięcia
     */
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
}