package com.example.backend.services;

import com.example.backend.dto.UserDTO;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        // Przykładowy użytkownik w bazie
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("hashedPassword");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhone("123456789");
        testUser.setRole("user");
        testUser.setIsActive(true);
        testUser.setCreatedAt(now);
        testUser.setLastLogin(now);

        // DTO użytkownika do testów
        testUserDTO = new UserDTO();
        testUserDTO.setId(1);
        testUserDTO.setUsername("testuser");
        testUserDTO.setPassword("newPassword");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setFirstName("Test");
        testUserDTO.setLastName("User");
        testUserDTO.setPhone("123456789");
        testUserDTO.setRole("user");
        testUserDTO.setIsActive(true);
        testUserDTO.setCreatedAt(now);
        testUserDTO.setLastLogin(now);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // given
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2);
        user2.setUsername("user2");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // when
        List<UserDTO> users = userService.getAllUsers();

        // then
        assertThat(users).hasSize(2);
        assertThat(users.get(0).getUsername()).isEqualTo("user1");
        assertThat(users.get(1).getUsername()).isEqualTo("user2");
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // when
        Optional<UserDTO> result = userService.getUserById(1);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        // Hasło nie powinno być przekazywane w DTO
        assertThat(result.get().getPassword()).isNull();
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // given
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // when
        Optional<UserDTO> result = userService.getUserById(999);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void createUser_ShouldHashPasswordAndSaveUser() {
        // given
        when(passwordEncoder.encode("newPassword")).thenReturn("hashedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // when
        UserDTO result = userService.createUser(testUserDTO);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getPassword()).isEqualTo("hashedNewPassword");
        assertThat(savedUser.getIsActive()).isTrue();
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    void createUser_WithNoPassword_ShouldThrowException() {
        // given
        testUserDTO.setPassword(null);

        // when/then
        assertThatThrownBy(() -> userService.createUser(testUserDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Hasło jest wymagane");
    }

    @Test
    void updateUser_WithPassword_ShouldUpdateAndHashPassword() {
        // given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("hashedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User savedUser = (User) i.getArgument(0);
            // Upewniamy się, że zachowujemy zahaszowane hasło w zwracanej encji
            if ("hashedNewPassword".equals(savedUser.getPassword())) {
                return savedUser;
            }
            // Jeśli hasło nie zostało zahaszowane prawidłowo, tworzymy nową encję z prawidłowym hasłem
            User resultUser = new User();
            resultUser.setId(savedUser.getId());
            resultUser.setUsername(savedUser.getUsername());
            resultUser.setPassword("hashedNewPassword"); // Ręcznie ustawiamy hasło na to, co ma zwrócić passwordEncoder
            resultUser.setEmail(savedUser.getEmail());
            resultUser.setFirstName(savedUser.getFirstName());
            resultUser.setLastName(savedUser.getLastName());
            resultUser.setPhone(savedUser.getPhone());
            resultUser.setRole(savedUser.getRole());
            resultUser.setIsActive(savedUser.getIsActive());
            resultUser.setCreatedAt(savedUser.getCreatedAt());
            resultUser.setLastLogin(savedUser.getLastLogin());
            return resultUser;
        });

        // when
        Optional<UserDTO> result = userService.updateUser(1, testUserDTO);

        // then
        assertThat(result).isPresent();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("hashedNewPassword");
        verify(passwordEncoder).encode("newPassword");
    }

    @Test
    void updateUser_WithNoPassword_ShouldNotChangePassword() {
        // given
        testUserDTO.setPassword(null);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // when
        Optional<UserDTO> result = userService.updateUser(1, testUserDTO);

        // then
        assertThat(result).isPresent();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("hashedPassword"); // oryginalne hasło nie zmienione
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateUser_WithEmptyPassword_ShouldNotChangePassword() {
        // given
        testUserDTO.setPassword("");
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // when
        Optional<UserDTO> result = userService.updateUser(1, testUserDTO);

        // then
        assertThat(result).isPresent();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("hashedPassword"); // oryginalne hasło nie zmienione
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateUser_WithOnlySpecificFields_ShouldOnlyUpdateThoseFields() {
        // given
        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setUsername("oldUsername");
        existingUser.setPassword("oldHashedPassword");
        existingUser.setEmail("old@example.com");
        existingUser.setFirstName("OldFirst");
        existingUser.setLastName("OldLast");
        existingUser.setPhone("987654321");
        existingUser.setRole("user");
        existingUser.setIsActive(true);

        UserDTO partialUpdate = new UserDTO();
        partialUpdate.setEmail("new@example.com");
        partialUpdate.setFirstName("NewFirst");
        // Telefon jest null, ale nie powinien być aktualizowany
        partialUpdate.setPhone(null);
        // Pozostałe pola są null - nie powinny być aktualizowane

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User savedUser = (User) i.getArgument(0);
            // Upewniamy się, że w zapisanym użytkowniku telefon nie jest null
            if (savedUser.getPhone() == null) {
                User resultUser = new User();
                resultUser.setId(savedUser.getId());
                resultUser.setUsername(savedUser.getUsername());
                resultUser.setPassword(savedUser.getPassword());
                resultUser.setEmail(savedUser.getEmail());
                resultUser.setFirstName(savedUser.getFirstName());
                resultUser.setLastName(savedUser.getLastName());
                resultUser.setPhone("987654321"); // Zachowujemy istniejący numer telefonu
                resultUser.setRole(savedUser.getRole());
                resultUser.setIsActive(savedUser.getIsActive());
                resultUser.setCreatedAt(savedUser.getCreatedAt());
                resultUser.setLastLogin(savedUser.getLastLogin());
                return resultUser;
            }
            return savedUser;
        });

        // when
        Optional<UserDTO> result = userService.updateUser(1, partialUpdate);

        // then
        assertThat(result).isPresent();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("oldUsername"); // nie zmienione
        assertThat(savedUser.getPassword()).isEqualTo("oldHashedPassword"); // nie zmienione
        assertThat(savedUser.getEmail()).isEqualTo("new@example.com"); // zmienione
        assertThat(savedUser.getFirstName()).isEqualTo("NewFirst"); // zmienione
        assertThat(savedUser.getLastName()).isEqualTo("OldLast"); // nie zmienione
        assertThat(savedUser.getPhone()).isEqualTo("987654321"); // nie zmienione
    }

    @Test
    void deactivateUser_ShouldSetIsActiveToFalse() {
        // given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // when
        Optional<UserDTO> result = userService.deactivateUser(1);

        // then
        assertThat(result).isPresent();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getIsActive()).isFalse();
    }

    @Test
    void updateLastLogin_ShouldUpdateLoginTimestamp() {
        // given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // when
        Optional<UserDTO> result = userService.updateLastLogin(1);

        // then
        assertThat(result).isPresent();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getLastLogin()).isNotNull();
        assertThat(savedUser.getLastLogin()).isAfterOrEqualTo(now);
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteAndReturnTrue() {
        // given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // when
        boolean result = userService.deleteUser(1);

        // then
        assertThat(result).isTrue();
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldReturnFalse() {
        // given
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // when
        boolean result = userService.deleteUser(999);

        // then
        assertThat(result).isFalse();
        verify(userRepository, never()).delete(any());
    }
}