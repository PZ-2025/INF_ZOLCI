package com.example.database.controllers;

import com.example.database.models.User;
import com.example.database.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = {UserController.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser1;
    private User testUser2;
    private List<User> userList;

    @BeforeEach
    void setUp() {
        testUser1 = new User();
        testUser1.setId(1);
        testUser1.setUsername("testuser");
        testUser1.setEmail("test@example.com");
        testUser1.setIsActive(true);
        testUser1.setCreatedAt(LocalDateTime.now());
        testUser1.setLastLogin(LocalDateTime.now());

        testUser2 = new User();
        testUser2.setId(2);
        testUser2.setUsername("anotheruser");
        testUser2.setEmail("another@example.com");
        testUser2.setIsActive(true);
        testUser2.setCreatedAt(LocalDateTime.now());
        testUser2.setLastLogin(LocalDateTime.now());

        userList = Arrays.asList(testUser1, testUser2);
    }

    @Test
    void getAllUsers() throws Exception {
        // Given
        when(userService.getAllUsers()).thenReturn(userList);

        // When & Then
        mockMvc.perform(get("/database/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].username", is("testuser")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].username", is("anotheruser")));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUserById() throws Exception {
        // Given
        when(userService.getUserById(1)).thenReturn(Optional.of(testUser1));
        when(userService.getUserById(999)).thenReturn(Optional.empty());

        // When & Then - successful case
        mockMvc.perform(get("/database/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")));

        // When & Then - not found case
        mockMvc.perform(get("/database/users/999"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(1);
        verify(userService, times(1)).getUserById(999);
    }

    @Test
    void getUserByUsername() throws Exception {
        // Given
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser1));
        when(userService.getUserByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then - successful case
        mockMvc.perform(get("/database/users/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")));

        // When & Then - not found case
        mockMvc.perform(get("/database/users/username/nonexistent"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserByUsername("testuser");
        verify(userService, times(1)).getUserByUsername("nonexistent");
    }

    @Test
    void getActiveUsers() throws Exception {
        // Given
        when(userService.findActiveUsers()).thenReturn(userList);

        // When & Then
        mockMvc.perform(get("/database/users/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].active", is(true)))
                .andExpect(jsonPath("$[1].active", is(true)));

        verify(userService, times(1)).findActiveUsers();
    }

    @Test
    void createUser() throws Exception {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setIsActive(true);

        User createdUser = new User();
        createdUser.setId(3);
        createdUser.setUsername("newuser");
        createdUser.setEmail("new@example.com");
        createdUser.setIsActive(true);
        createdUser.setCreatedAt(LocalDateTime.now());

        when(userService.createUser(any(User.class))).thenReturn(createdUser);
        when(userService.createUser(argThat(user -> user == null || user.getUsername() == null)))
                .thenThrow(new RuntimeException("Invalid user data"));

        // When & Then - successful case
        mockMvc.perform(post("/database/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.username", is("newuser")));

        // When & Then - error case
        User invalidUser = new User();
        mockMvc.perform(post("/database/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isInternalServerError());

        verify(userService, times(2)).createUser(any(User.class));
    }

    @Test
    void updateUser() throws Exception {
        // Given
        User updatedUserData = new User();
        updatedUserData.setUsername("updateduser");
        updatedUserData.setEmail("updated@example.com");

        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setIsActive(true);
        updatedUser.setCreatedAt(testUser1.getCreatedAt());
        updatedUser.setLastLogin(testUser1.getLastLogin());

        when(userService.updateUser(eq(1), any(User.class))).thenReturn(Optional.of(updatedUser));
        when(userService.updateUser(eq(999), any(User.class))).thenReturn(Optional.empty());

        // When & Then - successful case
        mockMvc.perform(put("/database/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("updateduser")))
                .andExpect(jsonPath("$.email", is("updated@example.com")));

        // When & Then - not found case
        mockMvc.perform(put("/database/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserData)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUser(eq(1), any(User.class));
        verify(userService, times(1)).updateUser(eq(999), any(User.class));
    }

    @Test
    void deleteUser() throws Exception {
        // Given
        when(userService.deleteUser(1)).thenReturn(true);
        when(userService.deleteUser(999)).thenReturn(false);

        // When & Then - successful case
        mockMvc.perform(delete("/database/users/1"))
                .andExpect(status().isNoContent());

        // When & Then - not found case
        mockMvc.perform(delete("/database/users/999"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser(1);
        verify(userService, times(1)).deleteUser(999);
    }

    @Test
    void deactivateUser() throws Exception {
        // Given
        User deactivatedUser = new User();
        deactivatedUser.setId(1);
        deactivatedUser.setUsername("testuser");
        deactivatedUser.setEmail("test@example.com");
        deactivatedUser.setIsActive(false);
        deactivatedUser.setCreatedAt(testUser1.getCreatedAt());
        deactivatedUser.setLastLogin(testUser1.getLastLogin());

        when(userService.deactivateUser(1)).thenReturn(Optional.of(deactivatedUser));
        when(userService.deactivateUser(999)).thenReturn(Optional.empty());

        // When & Then - successful case
        mockMvc.perform(put("/database/users/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.active", is(false)));

        // When & Then - not found case
        mockMvc.perform(put("/database/users/999/deactivate"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).deactivateUser(1);
        verify(userService, times(1)).deactivateUser(999);
    }

    @Test
    void updateLastLogin() throws Exception {
        // Given
        LocalDateTime newLoginTime = LocalDateTime.now();
        User userWithUpdatedLogin = new User();
        userWithUpdatedLogin.setId(1);
        userWithUpdatedLogin.setUsername("testuser");
        userWithUpdatedLogin.setEmail("test@example.com");
        userWithUpdatedLogin.setIsActive(true);
        userWithUpdatedLogin.setCreatedAt(testUser1.getCreatedAt());
        userWithUpdatedLogin.setLastLogin(newLoginTime);

        when(userService.updateLastLogin(1)).thenReturn(Optional.of(userWithUpdatedLogin));
        when(userService.updateLastLogin(999)).thenReturn(Optional.empty());

        // When & Then - successful case
        mockMvc.perform(put("/database/users/1/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")));

        // When & Then - not found case
        mockMvc.perform(put("/database/users/999/login"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateLastLogin(1);
        verify(userService, times(1)).updateLastLogin(999);
    }
}