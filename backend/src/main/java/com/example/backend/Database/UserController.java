package com.example.backend.Database;

import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for managing user-related requests.
 * Provides an endpoint to retrieve all users from the database.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3306")
public class UserController {

    private final UserRepository userRepository;

    /**
     * Constructor to inject the UserRepository.
     *
     * @param userRepository The repository handling user database operations.
     */
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Handles GET requests to retrieve all users from the database.
     *
     * @return A list of all users stored in the database.
     */
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
