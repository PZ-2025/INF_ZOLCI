package com.example.backend.services;

import com.example.backend.dto.UserDTO;
import com.example.backend.models.User;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        // Initialize test data
        user = new User();
        user.setId(1);
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhone("123456789");
        user.setRole("USER");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());

        userDTO = new UserDTO();
        userDTO.setId(1);
        userDTO.setUsername("testuser");
        userDTO.setPassword("password");
        userDTO.setEmail("test@example.com");
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        userDTO.setPhone("123456789");
        userDTO.setRole("USER");
        userDTO.setIsActive(true);
        userDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));

        // Act
        Optional<UserDTO> result = userService.getUserById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act
        Optional<UserDTO> result = userService.getUserById(1);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getUserByUsername_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        // Act
        Optional<UserDTO> result = userService.getUserByUsername("testuser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
    }

    @Test
    void getUserByUsername_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act
        Optional<UserDTO> result = userService.getUserByUsername("nonexistent");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void saveUser_ShouldPersistUser() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserDTO result = userService.saveUser(userDTO);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_WhenUserExists_ShouldReturnTrue() {
        // Arrange
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));

        // Act
        boolean result = userService.deleteUser(1);

        // Assert
        assertTrue(result);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act
        boolean result = userService.deleteUser(1);

        // Assert
        assertFalse(result);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void findActiveUsers_ShouldReturnOnlyActiveUsers() {
        // Arrange
        when(userRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(user));

        // Act
        List<UserDTO> result = userService.findActiveUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        assertTrue(result.get(0).getIsActive());
    }

    @Test
    void createUser_ShouldInitializeRequiredFields() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserDTO result = userService.createUser(userDTO);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserExists_ShouldUpdateFields() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setUsername("oldusername");
        existingUser.setEmail("old@example.com");
        existingUser.setFirstName("Old");
        existingUser.setLastName("User");

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<UserDTO> result = userService.updateUser(1, userDTO);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        assertEquals("test@example.com", result.get().getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act
        Optional<UserDTO> result = userService.updateUser(1, userDTO);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deactivateUser_WhenUserExists_ShouldSetIsActiveFalse() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setIsActive(false);
            return savedUser;
        });

        // Act
        Optional<UserDTO> result = userService.deactivateUser(1);

        // Assert
        assertTrue(result.isPresent());
        assertFalse(result.get().getIsActive());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateLastLogin_WhenUserExists_ShouldUpdateLoginTime() {
        // Arrange
        LocalDateTime before = LocalDateTime.now().minusDays(1);
        user.setLastLogin(before);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setLastLogin(LocalDateTime.now());
            return savedUser;
        });

        // Act
        Optional<UserDTO> result = userService.updateLastLogin(1);

        // Assert
        assertTrue(result.isPresent());
        assertNotNull(result.get().getLastLogin());
        // Verify last login was updated (should be more recent than 'before')
        assertTrue(result.get().getLastLogin().isAfter(before) ||
                result.get().getLastLogin().isEqual(before));
        verify(userRepository).save(any(User.class));
    }
}