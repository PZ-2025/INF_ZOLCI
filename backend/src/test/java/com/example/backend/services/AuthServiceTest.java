package com.example.backend.services;

import com.example.backend.dto.LoginRequestDTO;
import com.example.backend.dto.RegisterRequestDTO;
import com.example.backend.dto.UserResponseDTO;
import com.example.backend.models.User;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LocalDateTime now;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        passwordEncoder = new BCryptPasswordEncoder();
        now = LocalDateTime.now();

        // Inicjalizacja testowego użytkownika
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole("użytkownik");
        testUser.setIsActive(true);
        testUser.setCreatedAt(now);
        testUser.setLastLogin(now);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnUserDTO() {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "password123");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        Optional<UserResponseDTO> result = authService.login(loginRequest);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_WithInvalidUsername_ShouldReturnEmpty() {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO("wronguser", "password123");
        when(userRepository.findByUsername("wronguser")).thenReturn(Optional.empty());

        // When
        Optional<UserResponseDTO> result = authService.login(loginRequest);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername("wronguser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_WithInvalidPassword_ShouldReturnEmpty() {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "wrongpassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        Optional<UserResponseDTO> result = authService.login(loginRequest);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_WithInactiveUser_ShouldReturnEmpty() {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "password123");
        User inactiveUser = new User();
        inactiveUser.setUsername("testuser");
        inactiveUser.setPassword(passwordEncoder.encode("password123"));
        inactiveUser.setIsActive(false);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(inactiveUser));

        // When
        Optional<UserResponseDTO> result = authService.login(loginRequest);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_WithValidData_ShouldReturnUserDTO() {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("newpassword");
        registerRequest.setEmail("new@example.com");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(2); // Symulacja nadania ID przez bazę danych
            return savedUser;
        });

        // When
        UserResponseDTO result = authService.register(registerRequest);

        // Then
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("New", result.getFirstName());
        assertEquals("User", result.getLastName());
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("new@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_WithExistingUsername_ShouldThrowException() {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("newpassword");
        registerRequest.setEmail("new@example.com");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Nazwa użytkownika jest już zajęta", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("newpassword");
        registerRequest.setEmail("test@example.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Adres email jest już używany", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void validatePassword_WithCorrectPassword_ShouldReturnTrue() {
        // Given
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // When
        boolean result = authService.validatePassword(rawPassword, encodedPassword);

        // Then
        assertTrue(result);
    }

    @Test
    void validatePassword_WithIncorrectPassword_ShouldReturnFalse() {
        // Given
        String rawPassword = "password123";
        String wrongPassword = "wrongpassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // When
        boolean result = authService.validatePassword(wrongPassword, encodedPassword);

        // Then
        assertFalse(result);
    }

    @Test
    void encodePassword_ShouldReturnEncodedPassword() {
        // Given
        String rawPassword = "password123";

        // When
        String encodedPassword = authService.encodePassword(rawPassword);

        // Then
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void userExists_WhenUserExists_ShouldReturnTrue() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When
        boolean result = authService.userExists("testuser");

        // Then
        assertTrue(result);
        verify(userRepository, times(1)).existsByUsername("testuser");
    }

    @Test
    void userExists_WhenUserDoesNotExist_ShouldReturnFalse() {
        // Given
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // When
        boolean result = authService.userExists("nonexistent");

        // Then
        assertFalse(result);
        verify(userRepository, times(1)).existsByUsername("nonexistent");
    }

    @Test
    void emailExists_WhenEmailExists_ShouldReturnTrue() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        boolean result = authService.emailExists("test@example.com");

        // Then
        assertTrue(result);
        verify(userRepository, times(1)).existsByEmail("test@example.com");
    }

    @Test
    void emailExists_WhenEmailDoesNotExist_ShouldReturnFalse() {
        // Given
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // When
        boolean result = authService.emailExists("nonexistent@example.com");

        // Then
        assertFalse(result);
        verify(userRepository, times(1)).existsByEmail("nonexistent@example.com");
    }
}