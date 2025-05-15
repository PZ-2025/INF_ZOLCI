package com.example.backend.controllers;

import com.example.backend.dto.LoginRequestDTO;
import com.example.backend.dto.RegisterRequestDTO;
import com.example.backend.dto.UserResponseDTO;
import com.example.backend.models.User;
import com.example.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test integracyjny dla AuthController.
 * <p>
 * Test weryfikuje rzeczywistą integrację kontrolera uwierzytelniania z bazą danych
 * i innymi komponentami systemu. Sprawdza poprawność działania endpointów logowania
 * i rejestracji w kontekście całej aplikacji.
 * <p>
 * Test używa profilu 'test', który powinien być skonfigurowany do pracy z bazą H2 w pamięci.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("deploy")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Czyść bazę danych przed każdym testem
        userRepository.deleteAll();

        passwordEncoder = new BCryptPasswordEncoder();

        // Dodaj testowego użytkownika do bazy danych
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole("użytkownik");
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());

        userRepository.save(testUser);
    }

    @Test
    void register_WithValidData_ShouldCreateNewUserAndReturnData() throws Exception {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("newpassword");
        registerRequest.setEmail("new@example.com");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andReturn();

        // Verify that user is in the database
        User savedUser = userRepository.findByUsername("newuser").orElse(null);
        assertNotNull(savedUser, "Użytkownik powinien być zapisany w bazie danych");
        assertEquals("new@example.com", savedUser.getEmail());
        assertTrue(passwordEncoder.matches("newpassword", savedUser.getPassword()), "Hasło powinno być zahashowane");
    }

    @Test
    void register_WithExistingUsername_ShouldReturnConflict() throws Exception {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("testuser"); // Już istnieje
        registerRequest.setPassword("newpassword");
        registerRequest.setEmail("another@example.com");
        registerRequest.setFirstName("Another");
        registerRequest.setLastName("User");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.error").value("Nazwa użytkownika jest już zajęta"));

        // Verify that no new user is in the database
        assertEquals(1, userRepository.count(), "Nie powinien zostać dodany nowy użytkownik");
    }

    @Test
    void register_WithExistingEmail_ShouldReturnConflict() throws Exception {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("anotheruser");
        registerRequest.setPassword("newpassword");
        registerRequest.setEmail("test@example.com"); // Już istnieje
        registerRequest.setFirstName("Another");
        registerRequest.setLastName("User");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.error").value("Adres email jest już używany"));

        // Verify that no new user is in the database
        assertEquals(1, userRepository.count(), "Nie powinien zostać dodany nowy użytkownik");
    }

    @Test
    void login_WithValidCredentials_ShouldReturnUserData() throws Exception {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.isActive").value(true));

        // Verify that last login time is updated
        User user = userRepository.findByUsername("testuser").orElseThrow();
        assertNotNull(user.getLastLogin(), "Czas ostatniego logowania powinien być zaktualizowany");
    }

    @Test
    void login_WithInvalidUsername_ShouldReturnUnauthorized() throws Exception {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO("wronguser", "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Niepoprawne dane logowania lub konto jest nieaktywne"));
    }

    @Test
    void login_WithInvalidPassword_ShouldReturnUnauthorized() throws Exception {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Niepoprawne dane logowania lub konto jest nieaktywne"));
    }

    @Test
    void login_WithInactiveAccount_ShouldReturnUnauthorized() throws Exception {
        // Given
        // Dezaktywuj konto testowe
        User user = userRepository.findByUsername("testuser").orElseThrow();
        user.setIsActive(false);
        userRepository.save(user);

        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Niepoprawne dane logowania lub konto jest nieaktywne"));
    }

    @Test
    void checkUsernameAvailability_ForAvailableUsername_ShouldReturnTrue() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/check/username/newusername"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void checkUsernameAvailability_ForTakenUsername_ShouldReturnFalse() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/check/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void checkEmailAvailability_ForAvailableEmail_ShouldReturnTrue() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/check/email/new@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void checkEmailAvailability_ForTakenEmail_ShouldReturnFalse() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/check/email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }
}