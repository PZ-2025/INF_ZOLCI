package com.example.backend.services;

import com.example.backend.dto.LoginRequestDTO;
import com.example.backend.dto.RegisterRequestDTO;
import com.example.backend.dto.UserResponseDTO;
import com.example.backend.models.User;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User user;
    private LoginRequestDTO loginRequest;
    private RegisterRequestDTO registerRequest;

    @BeforeEach
    void setUp() {
        // Initialize test data
        user = new User();
        user.setId(1);
        user.setUsername("testuser");
        user.setPassword("$2a$10$hashedpassword");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole("użytkownik");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());

        loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password");
        registerRequest.setEmail("new@example.com");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
        registerRequest.setPhone("987654321");
    }

    @Test
    void login_WithValidCredentials_ShouldReturnUserResponseDTO() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        Optional<UserResponseDTO> result = authService.login(loginRequest);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        assertEquals("test@example.com", result.get().getEmail());
        verify(userRepository).save(any(User.class)); // Verify last login is updated
    }

    @Test
    void login_WithInvalidPassword_ShouldReturnEmpty() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", user.getPassword())).thenReturn(false);

        // Act
        loginRequest.setPassword("wrongpassword");
        Optional<UserResponseDTO> result = authService.login(loginRequest);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_WithNonexistentUser_ShouldReturnEmpty() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        loginRequest.setUsername("nonexistent");
        Optional<UserResponseDTO> result = authService.login(loginRequest);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_WithInactiveUser_ShouldReturnEmpty() {
        // Arrange
        user.setIsActive(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act
        Optional<UserResponseDTO> result = authService.login(loginRequest);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_WithValidData_ShouldCreateNewUser() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("$2a$10$encodedpassword");

        // Configure save to return a new user with the assigned id
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(2);
            return savedUser;
        });

        // Act
        UserResponseDTO result = authService.register(registerRequest);

        // Assert
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("New", result.getFirstName());
        assertEquals("User", result.getLastName());

        // Verify the saved user contains expected data
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("newuser", savedUser.getUsername());
        assertEquals("$2a$10$encodedpassword", savedUser.getPassword());
        assertEquals("użytkownik", savedUser.getRole()); // Verify default role
    }

    @Test
    void register_WithExistingUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(registerRequest));

        assertEquals("Nazwa użytkownika jest już zajęta", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(registerRequest));

        assertEquals("Adres email jest już używany", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void validatePassword_WithCorrectPassword_ShouldReturnTrue() {
        // Arrange
        when(passwordEncoder.matches("password", "$2a$10$hashedpassword")).thenReturn(true);

        // Act
        boolean result = authService.validatePassword("password", "$2a$10$hashedpassword");

        // Assert
        assertTrue(result);
    }

    @Test
    void validatePassword_WithIncorrectPassword_ShouldReturnFalse() {
        // Arrange
        when(passwordEncoder.matches("wrongpassword", "$2a$10$hashedpassword")).thenReturn(false);

        // Act
        boolean result = authService.validatePassword("wrongpassword", "$2a$10$hashedpassword");

        // Assert
        assertFalse(result);
    }

    @Test
    void encodePassword_ShouldReturnEncodedPassword() {
        // Arrange
        when(passwordEncoder.encode("password")).thenReturn("$2a$10$encodedpassword");

        // Act
        String result = authService.encodePassword("password");

        // Assert
        assertEquals("$2a$10$encodedpassword", result);
    }

    @Test
    void userExists_WithExistingUsername_ShouldReturnTrue() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act
        boolean result = authService.userExists("testuser");

        // Assert
        assertTrue(result);
    }

    @Test
    void userExists_WithNonexistentUsername_ShouldReturnFalse() {
        // Arrange
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // Act
        boolean result = authService.userExists("nonexistent");

        // Assert
        assertFalse(result);
    }

    @Test
    void emailExists_WithExistingEmail_ShouldReturnTrue() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act
        boolean result = authService.emailExists("test@example.com");

        // Assert
        assertTrue(result);
    }

    @Test
    void emailExists_WithNonexistentEmail_ShouldReturnFalse() {
        // Arrange
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // Act
        boolean result = authService.emailExists("nonexistent@example.com");

        // Assert
        assertFalse(result);
    }
}