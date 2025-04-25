package com.example.backend.controllers;

import com.example.backend.dto.UserDTO;
import com.example.backend.services.UserService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserDTO userDTO;
    private List<UserDTO> userDTOList;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDateTime serialization

        // Initialize test data
        userDTO = new UserDTO();
        userDTO.setId(1);
        userDTO.setUsername("testuser");
        userDTO.setPassword("password123");
        userDTO.setEmail("test@example.com");
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        userDTO.setPhone("123456789");
        userDTO.setRole("pracownik");
        userDTO.setIsActive(true);
        userDTO.setCreatedAt(LocalDateTime.now());

        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(2);
        userDTO2.setUsername("manager1");
        userDTO2.setPassword("password123");
        userDTO2.setEmail("manager@example.com");
        userDTO2.setFirstName("John");
        userDTO2.setLastName("Manager");
        userDTO2.setRole("kierownik");
        userDTO2.setIsActive(true);
        userDTO2.setCreatedAt(LocalDateTime.now());

        userDTOList = Arrays.asList(userDTO, userDTO2);
    }

    @Test
    public void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        // Arrange
        when(userService.getAllUsers()).thenReturn(userDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[1].username").value("manager1"));
    }

    @Test
    public void getUserById_WhenExists_ShouldReturnUser() throws Exception {
        // Arrange
        when(userService.getUserById(1)).thenReturn(Optional.of(userDTO));

        // Act & Assert
        mockMvc.perform(get("/database/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    public void getUserById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(userService.getUserById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getUserByUsername_WhenExists_ShouldReturnUser() throws Exception {
        // Arrange
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(userDTO));

        // Act & Assert
        mockMvc.perform(get("/database/users/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    public void getUserByUsername_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(userService.getUserByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/users/username/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getActiveUsers_ShouldReturnActiveUsers() throws Exception {
        // Arrange
        when(userService.findActiveUsers()).thenReturn(userDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/users/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[1].username").value("manager1"));
    }

    @Test
    public void createUser_WithValidData_ShouldReturnCreatedUser() throws Exception {
        // Arrange
        when(userService.createUser(any(UserDTO.class))).thenReturn(userDTO);

        // Act & Assert
        mockMvc.perform(post("/database/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    public void createUser_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(userService.createUser(any(UserDTO.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/database/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void updateUser_WhenUserExists_ShouldReturnUpdatedUser() throws Exception {
        // Arrange
        when(userService.updateUser(eq(1), any(UserDTO.class))).thenReturn(Optional.of(userDTO));

        // Update user data
        UserDTO updatedUser = new UserDTO();
        updatedUser.setId(1);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");
        updatedUser.setRole("pracownik");

        // Act & Assert
        mockMvc.perform(put("/database/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com")); // Mock returns original
    }

    @Test
    public void updateUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(userService.updateUser(eq(99), any(UserDTO.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/database/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteUser_WhenUserExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(userService.deleteUser(1)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/database/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(userService.deleteUser(99)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/database/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deactivateUser_WhenUserExists_ShouldReturnDeactivatedUser() throws Exception {
        // Arrange
        UserDTO deactivatedUser = new UserDTO();
        deactivatedUser.setId(1);
        deactivatedUser.setUsername("testuser");
        deactivatedUser.setEmail("test@example.com");
        deactivatedUser.setIsActive(false);

        when(userService.deactivateUser(1)).thenReturn(Optional.of(deactivatedUser));

        // Act & Assert
        mockMvc.perform(put("/database/users/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    public void deactivateUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(userService.deactivateUser(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/database/users/99/deactivate"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateLastLogin_WhenUserExists_ShouldReturnUserWithUpdatedLogin() throws Exception {
        // Arrange
        UserDTO userWithUpdatedLogin = new UserDTO();
        userWithUpdatedLogin.setId(1);
        userWithUpdatedLogin.setUsername("testuser");
        userWithUpdatedLogin.setLastLogin(LocalDateTime.now());

        when(userService.updateLastLogin(1)).thenReturn(Optional.of(userWithUpdatedLogin));

        // Act & Assert
        mockMvc.perform(put("/database/users/1/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastLogin").isNotEmpty());
    }

    @Test
    public void updateLastLogin_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(userService.updateLastLogin(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/database/users/99/login"))
                .andExpect(status().isNotFound());
    }
}