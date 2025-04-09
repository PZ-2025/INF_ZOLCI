package com.example.backend.services;

import com.example.backend.models.User;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User adminUser;
    private User regularUser;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testTime = LocalDateTime.now();

        // Inicjalizacja użytkowników testowych
        adminUser = new User();
        adminUser.setId(1);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setPassword("hashedPassword");
        adminUser.setRole("admin");
        adminUser.setIsActive(true);
        adminUser.setCreatedAt(testTime);
        adminUser.setLastLogin(testTime);

        regularUser = new User();
        regularUser.setId(2);
        regularUser.setUsername("user");
        regularUser.setEmail("user@example.com");
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setPassword("hashedPassword");
        regularUser.setRole("user");
        regularUser.setIsActive(true);
        regularUser.setCreatedAt(testTime);
        regularUser.setLastLogin(testTime);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        List<User> expectedUsers = Arrays.asList(adminUser, regularUser);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // When
        List<User> actualUsers = userService.getAllUsers();

        // Then
        assertEquals(expectedUsers.size(), actualUsers.size());
        assertEquals(expectedUsers.get(0).getId(), actualUsers.get(0).getId());
        assertEquals(expectedUsers.get(1).getId(), actualUsers.get(1).getId());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));

        // When
        Optional<User> result = userService.getUserById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(adminUser.getId(), result.get().getId());
        assertEquals(adminUser.getUsername(), result.get().getUsername());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserById(99);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(99);
    }

    @Test
    void getUserByUsername_WhenUserExists_ShouldReturnUser() {
        // Given
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        // When
        Optional<User> result = userService.getUserByUsername("admin");

        // Then
        assertTrue(result.isPresent());
        assertEquals(adminUser.getId(), result.get().getId());
        assertEquals("admin", result.get().getUsername());
        verify(userRepository, times(1)).findByUsername("admin");
    }

    @Test
    void getUserByUsername_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserByUsername("nonexistent");

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    void saveUser_ShouldSaveAndReturnUser() {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setPassword("password");
        newUser.setRole("user");

        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // When
        User savedUser = userService.saveUser(newUser);

        // Then
        assertEquals(newUser.getUsername(), savedUser.getUsername());
        assertEquals(newUser.getEmail(), savedUser.getEmail());
        verify(userRepository, times(1)).save(newUser);
    }

    @Test
    void deleteUser_WhenUserExists_ShouldReturnTrue() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));
        doNothing().when(userRepository).delete(adminUser);

        // When
        boolean result = userService.deleteUser(1);

        // Then
        assertTrue(result);
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).delete(adminUser);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldReturnFalse() {
        // Given
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // When
        boolean result = userService.deleteUser(99);

        // Then
        assertFalse(result);
        verify(userRepository, times(1)).findById(99);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void findActiveUsers_ShouldReturnActiveUsers() {
        // Given
        List<User> activeUsers = Arrays.asList(adminUser, regularUser);
        when(userRepository.findByIsActiveTrue()).thenReturn(activeUsers);

        // When
        List<User> result = userService.findActiveUsers();

        // Then
        assertEquals(activeUsers.size(), result.size());
        assertEquals(activeUsers.get(0).getId(), result.get(0).getId());
        assertEquals(activeUsers.get(1).getId(), result.get(1).getId());
        verify(userRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void createUser_ShouldSetCreatedAtAndActiveAndSaveUser() {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setPassword("password");
        newUser.setRole("user");
        // Note: createdAt and isActive are not set

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(3); // Simulate database giving an ID
            return savedUser;
        });

        // When
        User createdUser = userService.createUser(newUser);

        // Then
        assertNotNull(createdUser.getId());
        assertNotNull(createdUser.getCreatedAt());
        assertTrue(createdUser.getIsActive());
        assertEquals(newUser.getUsername(), createdUser.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserExists_ShouldUpdateAndReturnUser() {
        // Given
        User updatedDetails = new User();
        updatedDetails.setUsername("admin_updated");
        updatedDetails.setEmail("admin_updated@example.com");
        updatedDetails.setFirstName("Admin Updated");
        updatedDetails.setLastName("User Updated");
        updatedDetails.setPhone("123456789");
        updatedDetails.setRole("admin");
        updatedDetails.setPassword("newPassword");

        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<User> result = userService.updateUser(1, updatedDetails);

        // Then
        assertTrue(result.isPresent());
        assertEquals(updatedDetails.getUsername(), result.get().getUsername());
        assertEquals(updatedDetails.getEmail(), result.get().getEmail());
        assertEquals(updatedDetails.getFirstName(), result.get().getFirstName());
        assertEquals(updatedDetails.getLastName(), result.get().getLastName());
        assertEquals(updatedDetails.getPhone(), result.get().getPhone());
        assertEquals(updatedDetails.getRole(), result.get().getRole());
        assertEquals(updatedDetails.getPassword(), result.get().getPassword());
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Given
        User updatedDetails = new User();
        updatedDetails.setUsername("nonexistent_updated");

        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.updateUser(99, updatedDetails);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(99);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deactivateUser_WhenUserExists_ShouldDeactivateAndReturnUser() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            return savedUser;
        });

        // When
        Optional<User> result = userService.deactivateUser(1);

        // Then
        assertTrue(result.isPresent());
        assertFalse(result.get().getIsActive());
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deactivateUser_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.deactivateUser(99);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(99);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateLastLogin_WhenUserExists_ShouldUpdateAndReturnUser() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            return savedUser;
        });

        LocalDateTime oldLoginTime = adminUser.getLastLogin();

        // When
        Optional<User> result = userService.updateLastLogin(1);

        // Then
        assertTrue(result.isPresent());
        assertNotEquals(oldLoginTime, result.get().getLastLogin());
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateLastLogin_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.updateLastLogin(99);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(99);
        verify(userRepository, never()).save(any(User.class));
    }
}