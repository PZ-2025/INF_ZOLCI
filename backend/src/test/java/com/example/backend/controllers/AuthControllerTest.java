package com.example.backend.controllers;

import com.example.backend.dto.LoginRequestDTO;
import com.example.backend.dto.RegisterRequestDTO;
import com.example.backend.dto.UserResponseDTO;
import com.example.backend.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;
    private UserResponseDTO userResponse;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Dla obsługi LocalDateTime

        now = LocalDateTime.now();

        // Inicjalizacja odpowiedzi użytkownika
        userResponse = new UserResponseDTO();
        userResponse.setId(1);
        userResponse.setUsername("testuser");
        userResponse.setEmail("test@example.com");
        userResponse.setFirstName("Test");
        userResponse.setLastName("User");
        userResponse.setRole("użytkownik");
        userResponse.setIsActive(true);
        userResponse.setCreatedAt(now);
        userResponse.setLastLogin(now);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnOkWithUserDTO() throws Exception {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "password123");
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(Optional.of(userResponse));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "wrongpassword");
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());

        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    @Test
    void login_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "password123");
        when(authService.login(any(LoginRequestDTO.class))).thenThrow(new RuntimeException("Test exception"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.error").exists());

        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    @Test
    void register_WithValidData_ShouldReturnCreatedWithUserDTO() throws Exception {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("newpassword");
        registerRequest.setEmail("new@example.com");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");

        UserResponseDTO newUserResponse = new UserResponseDTO();
        newUserResponse.setId(2);
        newUserResponse.setUsername("newuser");
        newUserResponse.setEmail("new@example.com");
        newUserResponse.setFirstName("New");
        newUserResponse.setLastName("User");
        newUserResponse.setRole("użytkownik");
        newUserResponse.setIsActive(true);
        newUserResponse.setCreatedAt(now);

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(newUserResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("new@example.com"));

        verify(authService, times(1)).register(any(RegisterRequestDTO.class));
    }

    @Test
    void register_WithExistingUsername_ShouldReturnConflict() throws Exception {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("existinguser");
        registerRequest.setPassword("password");
        registerRequest.setEmail("new@example.com");

        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new RuntimeException("Nazwa użytkownika jest już zajęta"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.error").value("Nazwa użytkownika jest już zajęta"));

        verify(authService, times(1)).register(any(RegisterRequestDTO.class));
    }

    @Test
    void register_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password");
        registerRequest.setEmail("new@example.com");

        // Używamy niekontrolowanego wyjątku, który nie jest RuntimeException ani jego podklasą
        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new RuntimeException("Nieoczekiwany błąd")); // Error jest niekontrolowanym wyjątkiem, ale nie rozszerza RuntimeException

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isInternalServerError())  // Teraz powinno zwrócić 500
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.error").exists());

        verify(authService, times(1)).register(any(RegisterRequestDTO.class));
    }

    @Test
    void checkUsernameAvailability_WhenAvailable_ShouldReturnTrue() throws Exception {
        // Given
        when(authService.userExists("newuser")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/auth/check/username/newuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        verify(authService, times(1)).userExists("newuser");
    }

    @Test
    void checkUsernameAvailability_WhenNotAvailable_ShouldReturnFalse() throws Exception {
        // Given
        when(authService.userExists("existinguser")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/auth/check/username/existinguser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        verify(authService, times(1)).userExists("existinguser");
    }

    @Test
    void checkEmailAvailability_WhenAvailable_ShouldReturnTrue() throws Exception {
        // Given
        when(authService.emailExists("new@example.com")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/auth/check/email/new@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        verify(authService, times(1)).emailExists("new@example.com");
    }

    @Test
    void checkEmailAvailability_WhenNotAvailable_ShouldReturnFalse() throws Exception {
        // Given
        when(authService.emailExists("existing@example.com")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/auth/check/email/existing@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        verify(authService, times(1)).emailExists("existing@example.com");
    }
}