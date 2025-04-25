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
        userDTO.setUsername("user1");
        userDTO.setEmail("user1@example.com");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setPhone("123-456-7890");
        userDTO.setRole("worker");
        userDTO.setIsActive(true);
        userDTO.setCreatedAt(LocalDateTime.now());
        userDTO.setLastLogin(LocalDateTime.now());

        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(2);
        userDTO2.setUsername("manager1");
        userDTO2.setEmail("manager1@example.com");
        userDTO2.setFirstName("Jane");
        userDTO2.setLastName("Smith");
        userDTO2.setPhone("987-654-3210");
        userDTO2.setRole("manager");
        userDTO2.setIsActive(true);
        userDTO2.setCreatedAt(LocalDateTime.now());
        userDTO2.setLastLogin(LocalDateTime.now());

        userDTOList = Arrays.asList(userDTO, userDTO2);
    }

    @Test
    public void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        // Arrange
        when(userService.getAllUsers()).thenReturn(userDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("manager1"));
    }

    @Test
    public void getUserById_WhenExists_ShouldReturnUser() throws Exception {
        // Arrange
        Optional<UserDTO> optionalUser = Optional.of(userDTO);
        when(userService.getUserById(1)).thenReturn(optionalUser);

        // Act & Assert
        mockMvc.perform(get("/database/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.email").value("user1@example.com"));
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
        Optional<UserDTO> optionalUser = Optional.of(userDTO);
        when(userService.getUserByUsername("user1")).thenReturn(optionalUser);

        // Act & Assert
        mockMvc.perform(get("/database/users/username/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user1@example.com"));
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
    public void getActiveUsers_ShouldReturnListOfActiveUsers() throws Exception {
        // Arrange
        when(userService.findActiveUsers()).thenReturn(userDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/users/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("manager1"));
    }

    @Test
    public void createUser_WithValidData_ShouldReturnCreatedUser() throws Exception {
        // Arrange
        when(userService.createUser(any(UserDTO.class))).thenReturn(userDTO);

        UserDTO newUser = new UserDTO();
        newUser.setUsername("newuser");
        newUser.setPassword("password123");
        newUser.setEmail("newuser@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setRole("worker");

        // Act & Assert
        mockMvc.perform(post("/database/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("user1")); // Mock returns our pre-defined userDTO
    }

    @Test
    public void createUser_WithInternalError_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(userService.createUser(any(UserDTO.class))).thenThrow(new RuntimeException("Internal error"));

        UserDTO newUser = new UserDTO();
        newUser.setUsername("newuser");
        newUser.setPassword("password123");
        newUser.setEmail("newuser@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setRole("worker");

        // Act & Assert
        mockMvc.perform(post("/database/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void updateUser_WhenExists_ShouldReturnUpdatedUser() throws Exception {
        // Arrange
        Optional<UserDTO> optionalUpdatedUser = Optional.of(userDTO);
        when(userService.updateUser(eq(1), any(UserDTO.class))).thenReturn(optionalUpdatedUser);

        UserDTO updatedUser = new UserDTO();
        updatedUser.setId(1);
        updatedUser.setUsername("user1");
        updatedUser.setEmail("user1updated@example.com");
        updatedUser.setFirstName("John");
        updatedUser.setLastName("Doe");
        updatedUser.setPhone("555-555-5555");
        updatedUser.setRole("worker");

        // Act & Assert
        mockMvc.perform(put("/database/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.email").value("user1@example.com")); // Mock returns our pre-defined userDTO
    }

    @Test
    public void updateUser_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(userService.updateUser(eq(99), any(UserDTO.class))).thenReturn(Optional.empty());

        UserDTO updatedUser = new UserDTO();
        updatedUser.setId(99);
        updatedUser.setUsername("nonexistent");
        updatedUser.setEmail("nonexistent@example.com");
        updatedUser.setFirstName("Non");
        updatedUser.setLastName("Existent");
        updatedUser.setRole("worker");

        // Act & Assert
        mockMvc.perform(put("/database/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteUser_WhenExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(userService.deleteUser(1)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/database/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteUser_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(userService.deleteUser(99)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/database/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deactivateUser_WhenExists_ShouldReturnDeactivatedUser() throws Exception {
        // Arrange
        UserDTO deactivatedUser = new UserDTO();
        deactivatedUser.setId(1);
        deactivatedUser.setUsername("user1");
        deactivatedUser.setEmail("user1@example.com");
        deactivatedUser.setFirstName("John");
        deactivatedUser.setLastName("Doe");
        deactivatedUser.setIsActive(false);

        Optional<UserDTO> optionalDeactivatedUser = Optional.of(deactivatedUser);
        when(userService.deactivateUser(1)).thenReturn(optionalDeactivatedUser);

        // Act & Assert
        mockMvc.perform(put("/database/users/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    public void deactivateUser_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(userService.deactivateUser(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/database/users/99/deactivate"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateLastLogin_WhenExists_ShouldReturnUpdatedUser() throws Exception {
        // Arrange
        Optional<UserDTO> optionalUpdatedUser = Optional.of(userDTO);
        when(userService.updateLastLogin(1)).thenReturn(optionalUpdatedUser);

        // Act & Assert
        mockMvc.perform(put("/database/users/1/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"));
    }

    @Test
    public void updateLastLogin_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(userService.updateLastLogin(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/database/users/99/login"))
                .andExpect(status().isNotFound());
    }
}