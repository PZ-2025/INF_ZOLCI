package com.example.backend.controllers;

import com.example.backend.dto.LoginRequestDTO;
import com.example.backend.dto.RegisterRequestDTO;
import com.example.backend.dto.UserResponseDTO;
import com.example.backend.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private UserResponseDTO userResponseDTO;
    private LoginRequestDTO loginRequestDTO;
    private RegisterRequestDTO registerRequestDTO;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For proper serialization of LocalDateTime

        // Initialize test data
        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1);
        userResponseDTO.setUsername("testuser");
        userResponseDTO.setEmail("test@example.com");
        userResponseDTO.setFirstName("Test");
        userResponseDTO.setLastName("User");
        userResponseDTO.setRole("użytkownik");
        userResponseDTO.setIsActive(true);
        userResponseDTO.setCreatedAt(LocalDateTime.now());

        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUsername("testuser");
        loginRequestDTO.setPassword("password");

        registerRequestDTO = new RegisterRequestDTO();
        registerRequestDTO.setUsername("newuser");
        registerRequestDTO.setPassword("password");
        registerRequestDTO.setEmail("new@example.com");
        registerRequestDTO.setFirstName("New");
        registerRequestDTO.setLastName("User");
    }

    @Test
    public void login_WithValidCredentials_ShouldReturnUserResponseDTO() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(Optional.of(userResponseDTO));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    public void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Niepoprawne dane logowania lub konto jest nieaktywne"));
    }

    @Test
    public void login_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequestDTO.class))).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Wystąpił błąd podczas logowania"))
                .andExpect(jsonPath("$.error").value("Service error"));
    }

    @Test
    public void register_WithValidData_ShouldReturnCreatedUser() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(userResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    public void register_WithExistingUsername_ShouldReturnConflict() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new RuntimeException("Nazwa użytkownika jest już zajęta"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Nie można zarejestrować użytkownika"))
                .andExpect(jsonPath("$.error").value("Nazwa użytkownika jest już zajęta"));
    }

    @Test
    public void register_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new Exception("General service error"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Wystąpił nieoczekiwany błąd podczas rejestracji"));
    }

    @Test
    public void checkUsernameAvailability_WhenAvailable_ShouldReturnTrue() throws Exception {
        // Arrange
        when(authService.userExists(anyString())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/auth/check/username/newuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    public void checkUsernameAvailability_WhenNotAvailable_ShouldReturnFalse() throws Exception {
        // Arrange
        when(authService.userExists(anyString())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/auth/check/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    public void checkEmailAvailability_WhenAvailable_ShouldReturnTrue() throws Exception {
        // Arrange
        when(authService.emailExists(anyString())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/auth/check/email/new@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    public void checkEmailAvailability_WhenNotAvailable_ShouldReturnFalse() throws Exception {
        // Arrange
        when(authService.emailExists(anyString())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/auth/check/email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }
}