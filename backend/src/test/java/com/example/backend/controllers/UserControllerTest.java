package com.example.backend.controllers;

import com.example.backend.dto.UserDTO;
import com.example.backend.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserDTO testUser;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        testUser = new UserDTO();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("password"); // W rzeczywistości hasło nie powinno być przekazywane w odpowiedzi
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhone("123456789");
        testUser.setRole("user");
        testUser.setIsActive(true);
        testUser.setCreatedAt(now);
        testUser.setLastLogin(now);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // given
        UserDTO user1 = new UserDTO();
        user1.setId(1);
        user1.setUsername("user1");

        UserDTO user2 = new UserDTO();
        user2.setId(2);
        user2.setUsername("user2");

        when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2));

        // when
        ResponseEntity<List<UserDTO>> response = userController.getAllUsers();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getUsername()).isEqualTo("user1");
        assertThat(response.getBody().get(1).getUsername()).isEqualTo("user2");
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // given
        testUser.setPassword(null); // Hasło nie powinno być zwracane w odpowiedzi
        when(userService.getUserById(1)).thenReturn(Optional.of(testUser));

        // when
        ResponseEntity<UserDTO> response = userController.getUserById(1);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1);
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
        assertThat(response.getBody().getPassword()).isNull(); // Hasło nie powinno być w odpowiedzi
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldReturnNotFound() {
        // given
        when(userService.getUserById(999)).thenReturn(Optional.empty());

        // when
        ResponseEntity<UserDTO> response = userController.getUserById(999);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getUserByUsername_WhenUserExists_ShouldReturnUser() {
        // given
        testUser.setPassword(null); // Hasło nie powinno być zwracane w odpowiedzi
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));

        // when
        ResponseEntity<UserDTO> response = userController.getUserByUsername("testuser");

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1);
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
    }

    @Test
    void getActiveUsers_ShouldReturnOnlyActiveUsers() {
        // given
        UserDTO activeUser = new UserDTO();
        activeUser.setId(1);
        activeUser.setUsername("active");
        activeUser.setIsActive(true);

        when(userService.findActiveUsers()).thenReturn(Arrays.asList(activeUser));

        // when
        ResponseEntity<List<UserDTO>> response = userController.getActiveUsers();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getUsername()).isEqualTo("active");
        assertThat(response.getBody().get(0).getIsActive()).isTrue();
    }

    @Test
    void createUser_WithValidData_ShouldCreateAndReturnUser() {
        // given
        UserDTO inputUser = new UserDTO();
        inputUser.setUsername("newuser");
        inputUser.setPassword("password123");
        inputUser.setEmail("new@example.com");
        inputUser.setFirstName("New");
        inputUser.setLastName("User");

        UserDTO createdUser = new UserDTO();
        createdUser.setId(2);
        createdUser.setUsername("newuser");
        createdUser.setEmail("new@example.com");
        createdUser.setFirstName("New");
        createdUser.setLastName("User");
        createdUser.setIsActive(true);

        when(userService.createUser(any(UserDTO.class))).thenReturn(createdUser);

        // when
        ResponseEntity<UserDTO> response = userController.createUser(inputUser);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(2);
        assertThat(response.getBody().getUsername()).isEqualTo("newuser");

        verify(userService).createUser(any(UserDTO.class));
    }

    @Test
    void updateUser_WithPassword_ShouldUpdateUserAndHashPassword() {
        // given
        UserDTO updateUser = new UserDTO();
        updateUser.setUsername("updateduser");
        updateUser.setPassword("newpassword");
        updateUser.setEmail("updated@example.com");

        UserDTO updatedUser = new UserDTO();
        updatedUser.setId(1);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setIsActive(true);

        when(userService.updateUser(eq(1), any(UserDTO.class))).thenReturn(Optional.of(updatedUser));

        // when
        ResponseEntity<UserDTO> response = userController.updateUser(1, updateUser);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1);
        assertThat(response.getBody().getUsername()).isEqualTo("updateduser");
        assertThat(response.getBody().getEmail()).isEqualTo("updated@example.com");

        // Weryfikujemy, że serwis został wywołany z DTO zawierającym hasło
        verify(userService).updateUser(eq(1), argThat(dto ->
                "newpassword".equals(dto.getPassword()) &&
                        "updateduser".equals(dto.getUsername()) &&
                        "updated@example.com".equals(dto.getEmail())
        ));
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldReturnNotFound() {
        // given
        UserDTO updateUser = new UserDTO();
        updateUser.setUsername("updateduser");

        when(userService.updateUser(eq(999), any(UserDTO.class))).thenReturn(Optional.empty());

        // when
        ResponseEntity<UserDTO> response = userController.updateUser(999, updateUser);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteAndReturnNoContent() {
        // given
        when(userService.deleteUser(1)).thenReturn(true);

        // when
        ResponseEntity<Void> response = userController.deleteUser(1);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService).deleteUser(1);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldReturnNotFound() {
        // given
        when(userService.deleteUser(999)).thenReturn(false);

        // when
        ResponseEntity<Void> response = userController.deleteUser(999);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deactivateUser_WhenUserExists_ShouldDeactivateAndReturnUser() {
        // given
        UserDTO deactivatedUser = new UserDTO();
        deactivatedUser.setId(1);
        deactivatedUser.setUsername("testuser");
        deactivatedUser.setIsActive(false);

        when(userService.deactivateUser(1)).thenReturn(Optional.of(deactivatedUser));

        // when
        ResponseEntity<UserDTO> response = userController.deactivateUser(1);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1);
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
        assertThat(response.getBody().getIsActive()).isFalse();
    }

    @Test
    void updateLastLogin_WhenUserExists_ShouldUpdateAndReturnUser() {
        // given
        UserDTO updatedUser = new UserDTO();
        updatedUser.setId(1);
        updatedUser.setUsername("testuser");
        updatedUser.setLastLogin(LocalDateTime.now());

        when(userService.updateLastLogin(1)).thenReturn(Optional.of(updatedUser));

        // when
        ResponseEntity<UserDTO> response = userController.updateLastLogin(1);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1);
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
        assertThat(response.getBody().getLastLogin()).isNotNull();
    }
}