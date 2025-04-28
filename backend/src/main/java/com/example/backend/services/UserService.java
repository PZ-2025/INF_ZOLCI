package  com.example.backend.services;

import com.example.backend.models.User;
import com.example.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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


    public boolean deleteUser(Integer userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    userRepository.delete(user);
                    return true;
                })
                .orElse(false);
    }

    public List<User> findActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }
    @Transactional
    public User createUser(User user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);
        return userRepository.save(user);
    }

    @Transactional
    public Optional<User> updateUser(Integer userId, User userDetails) {
        return userRepository.findById(userId)
                .map(existingUser -> {
                    existingUser.setUsername(userDetails.getUsername());
                    existingUser.setEmail(userDetails.getEmail());
                    existingUser.setFirstName(userDetails.getFirstName());
                    existingUser.setLastName(userDetails.getLastName());
                    existingUser.setPhone(userDetails.getPhone());
                    existingUser.setRole(userDetails.getRole());

                    // Aktualizacja hasła bez enkodowania
                    if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                        existingUser.setPassword(userDetails.getPassword());
                    }

                    return userRepository.save(existingUser);
                });
    }

    @Transactional
    public Optional<User> deactivateUser(Integer userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setIsActive(false);
                    return userRepository.save(user);
                });
    }

    @Transactional
    public Optional<User> updateLastLogin(Integer userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setLastLogin(LocalDateTime.now()); // Upewnij się, że tutaj tworzona jest NOWA data
                    return userRepository.save(user);
                });
    }

}