package com.example.backend.services;

import com.example.backend.dto.TeamDTO;
import com.example.backend.dto.UserDTO;
import com.example.backend.models.Team;
import com.example.backend.models.User;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.TeamMemberRepository;
import com.example.backend.repository.TeamRepository;
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
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TeamService teamService;

    private Team team;
    private TeamDTO teamDTO;
    private User manager;
    private UserDTO managerDTO;

    @BeforeEach
    void setUp() {
        // Initialize test data
        manager = new User();
        manager.setId(1);
        manager.setUsername("manager");
        manager.setFirstName("Test");
        manager.setLastName("Manager");
        manager.setEmail("manager@example.com");

        managerDTO = new UserDTO();
        managerDTO.setId(1);
        managerDTO.setUsername("manager");
        managerDTO.setFirstName("Test");
        managerDTO.setLastName("Manager");
        managerDTO.setEmail("manager@example.com");
        managerDTO.setRole("kierownik");

        team = new Team();
        team.setId(1);
        team.setName("Construction Team Alpha");
        team.setManager(manager);
        team.setIsActive(true);
        team.setCreatedAt(LocalDateTime.now());

        teamDTO = new TeamDTO();
        teamDTO.setId(1);
        teamDTO.setName("Construction Team Alpha");
        teamDTO.setManagerId(1);
        teamDTO.setIsActive(true);
    }

    @Test
    void getAllTeams_ShouldReturnAllTeams() {
        // Arrange
        when(teamRepository.findAll()).thenReturn(Arrays.asList(team));

        // Act
        List<TeamDTO> result = teamService.getAllTeams();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Construction Team Alpha", result.get(0).getName());
        assertEquals(1, result.get(0).getManagerId());
    }

    @Test
    void getTeamById_WhenTeamExists_ShouldReturnTeam() {
        // Arrange
        when(teamRepository.findById(1)).thenReturn(Optional.of(team));

        // Act
        Optional<TeamDTO> result = teamService.getTeamById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Construction Team Alpha", result.get().getName());
        assertEquals(1, result.get().getManagerId());
    }

    @Test
    void getTeamById_WhenTeamDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(teamRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<TeamDTO> result = teamService.getTeamById(999);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getTeamEntityById_WhenTeamExists_ShouldReturnTeamEntity() {
        // Arrange
        when(teamRepository.findById(1)).thenReturn(Optional.of(team));

        // Act
        Optional<Team> result = teamService.getTeamEntityById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Construction Team Alpha", result.get().getName());
        assertEquals(1, result.get().getId());
    }

    @Test
    void getActiveTeams_ShouldReturnOnlyActiveTeams() {
        // Arrange
        when(teamRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(team));

        // Act
        List<TeamDTO> result = teamService.getActiveTeams();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Construction Team Alpha", result.get(0).getName());
        assertTrue(result.get(0).getIsActive());
    }

    @Test
    void getTeamsByActiveStatus_WhenActive_ShouldReturnActiveTeams() {
        // Arrange
        when(teamRepository.findByIsActive(true)).thenReturn(Arrays.asList(team));

        // Act
        List<TeamDTO> result = teamService.getTeamsByActiveStatus(true);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Construction Team Alpha", result.get(0).getName());
        assertTrue(result.get(0).getIsActive());
    }

    @Test
    void getTeamsByActiveStatus_WhenInactive_ShouldReturnInactiveTeams() {
        // Arrange
        Team inactiveTeam = new Team();
        inactiveTeam.setId(2);
        inactiveTeam.setName("Inactive Team");
        inactiveTeam.setManager(manager);
        inactiveTeam.setIsActive(false);

        when(teamRepository.findByIsActive(false)).thenReturn(Arrays.asList(inactiveTeam));

        // Act
        List<TeamDTO> result = teamService.getTeamsByActiveStatus(false);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Inactive Team", result.get(0).getName());
        assertFalse(result.get(0).getIsActive());
    }

    @Test
    void getTeamByName_WhenTeamExists_ShouldReturnTeam() {
        // Arrange
        when(teamRepository.findByName("Construction Team Alpha")).thenReturn(Optional.of(team));

        // Act
        Optional<TeamDTO> result = teamService.getTeamByName("Construction Team Alpha");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals(1, result.get().getManagerId());
    }

    @Test
    void getTeamByName_WhenTeamDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(teamRepository.findByName("Nonexistent Team")).thenReturn(Optional.empty());

        // Act
        Optional<TeamDTO> result = teamService.getTeamByName("Nonexistent Team");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void activateTeam_WhenTeamExists_ShouldSetIsActiveTrue() {
        // Arrange
        Team inactiveTeam = new Team();
        inactiveTeam.setId(1);
        inactiveTeam.setName("Construction Team Alpha");
        inactiveTeam.setManager(manager);
        inactiveTeam.setIsActive(false);

        when(teamRepository.findById(1)).thenReturn(Optional.of(inactiveTeam));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team savedTeam = invocation.getArgument(0);
            savedTeam.setIsActive(true);
            return savedTeam;
        });

        // Act
        Optional<TeamDTO> result = teamService.activateTeam(1);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().getIsActive());
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void activateTeam_WhenTeamDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(teamRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<TeamDTO> result = teamService.activateTeam(999);

        // Assert
        assertFalse(result.isPresent());
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    void deactivateTeam_WhenTeamExists_ShouldSetIsActiveFalse() {
        // Arrange
        when(teamRepository.findById(1)).thenReturn(Optional.of(team));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team savedTeam = invocation.getArgument(0);
            savedTeam.setIsActive(false);
            return savedTeam;
        });

        // Act
        Optional<TeamDTO> result = teamService.deactivateTeam(1);

        // Assert
        assertTrue(result.isPresent());
        assertFalse(result.get().getIsActive());
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void saveTeam_ShouldMapDTOAndSaveEntity() {
        // Arrange
        when(userService.getUserById(1)).thenReturn(Optional.of(managerDTO));
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        // Act
        TeamDTO result = teamService.saveTeam(teamDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Construction Team Alpha", result.getName());
        assertEquals(1, result.getManagerId());
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void updateTeam_WhenTeamExists_ShouldUpdateAndReturnDTO() {
        // Arrange
        when(teamRepository.findById(1)).thenReturn(Optional.of(team));
        when(userService.getUserById(1)).thenReturn(Optional.of(managerDTO));
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        // Update name in DTO
        teamDTO.setName("Updated Team Name");

        // Act
        TeamDTO result = teamService.updateTeam(teamDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Construction Team Alpha", result.getName()); // Mocked to return original team

        // Verify save was called with updated name
        verify(teamRepository).save(argThat(savedTeam ->
                savedTeam.getName().equals("Updated Team Name")));
    }

    @Test
    void updateTeam_WhenTeamDoesNotExist_ShouldThrowException() {
        // Arrange
        when(teamRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> teamService.updateTeam(teamDTO));

        assertEquals("Nie znaleziono zespołu o ID: 1", exception.getMessage());
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    void deleteTeam_WhenTeamExists_ShouldDeleteTeamAndRelatedEntities() {
        // Arrange
        when(teamRepository.findById(1)).thenReturn(Optional.of(team));

        // Act
        teamService.deleteTeam(1);

        // Assert
        verify(taskRepository).deleteAllByTeam(team);
        verify(teamMemberRepository).deleteAllByTeam(team);
        verify(teamRepository).delete(team);
    }

    @Test
    void deleteTeam_WhenTeamDoesNotExist_ShouldThrowException() {
        // Arrange
        when(teamRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> teamService.deleteTeam(1));

        assertEquals("Nie znaleziono zespołu o ID: 1", exception.getMessage());
        verify(taskRepository, never()).deleteAllByTeam(any(Team.class));
        verify(teamMemberRepository, never()).deleteAllByTeam(any(Team.class));
        verify(teamRepository, never()).delete(any(Team.class));
    }

    @Test
    void existsById_WhenTeamExists_ShouldReturnTrue() {
        // Arrange
        when(teamRepository.existsById(1)).thenReturn(true);

        // Act
        boolean result = teamService.existsById(1);

        // Assert
        assertTrue(result);
    }

    @Test
    void existsById_WhenTeamDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(teamRepository.existsById(999)).thenReturn(false);

        // Act
        boolean result = teamService.existsById(999);

        // Assert
        assertFalse(result);
    }
}