package com.example.backend.services;

import com.example.backend.dto.TeamDTO;
import com.example.backend.models.Team;
import com.example.backend.models.User;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.TeamMemberRepository;
import com.example.backend.repository.TeamRepository;
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

    private User manager;
    private Team team1;
    private Team team2;
    private TeamDTO teamDTO1;
    private TeamDTO teamDTO2;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        now = LocalDateTime.now();

        // Inicjalizacja menedżera testowego
        manager = new User();
        manager.setId(1);
        manager.setUsername("manager");
        manager.setEmail("manager@example.com");
        manager.setFirstName("Manager");
        manager.setLastName("User");
        manager.setRole("MANAGER");
        manager.setIsActive(true);

        // Inicjalizacja zespołów testowych - encje
        team1 = new Team();
        team1.setId(1);
        team1.setName("Team Alpha");
        team1.setManager(manager);
        team1.setCreatedAt(now);
        team1.setIsActive(true);

        team2 = new Team();
        team2.setId(2);
        team2.setName("Team Beta");
        team2.setManager(manager);
        team2.setCreatedAt(now);
        team2.setIsActive(false);

        // Inicjalizacja zespołów testowych - DTO
        teamDTO1 = new TeamDTO();
        teamDTO1.setId(1);
        teamDTO1.setName("Team Alpha");
        teamDTO1.setManagerId(1);
        teamDTO1.setIsActive(true);

        teamDTO2 = new TeamDTO();
        teamDTO2.setId(2);
        teamDTO2.setName("Team Beta");
        teamDTO2.setManagerId(1);
        teamDTO2.setIsActive(false);

        // Konfiguracja UserService mock
        when(userService.getUserById(1)).thenReturn(Optional.of(manager));
    }

    @Test
    void getAllTeams_ShouldReturnAllTeamsAsDTO() {
        // Given
        when(teamRepository.findAll()).thenReturn(Arrays.asList(team1, team2));

        // When
        List<TeamDTO> actualTeams = teamService.getAllTeams();

        // Then
        assertEquals(2, actualTeams.size());
        assertEquals(teamDTO1.getName(), actualTeams.get(0).getName());
        assertEquals(teamDTO1.getManagerId(), actualTeams.get(0).getManagerId());
        verify(teamRepository).findAll();
    }

    @Test
    void getTeamById_WhenTeamExists_ShouldReturnTeamDTO() {
        // Given
        when(teamRepository.findById(1)).thenReturn(Optional.of(team1));

        // When
        Optional<TeamDTO> result = teamService.getTeamById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(teamDTO1.getName(), result.get().getName());
        assertEquals(teamDTO1.getManagerId(), result.get().getManagerId());
        verify(teamRepository).findById(1);
    }

    @Test
    void saveTeam_ShouldSaveAndReturnTeamDTO() {
        // Given
        when(teamRepository.save(any(Team.class))).thenReturn(team1);

        // When
        TeamDTO savedTeam = teamService.saveTeam(teamDTO1);

        // Then
        assertEquals(teamDTO1.getName(), savedTeam.getName());
        assertEquals(teamDTO1.getManagerId(), savedTeam.getManagerId());
        verify(teamRepository).save(any(Team.class));
        verify(userService).getUserById(teamDTO1.getManagerId());
    }

    @Test
    void updateTeam_ShouldUpdateAndReturnTeamDTO() {
        // Given
        TeamDTO updateDTO = new TeamDTO();
        updateDTO.setId(1);
        updateDTO.setName("Updated Team Alpha");
        updateDTO.setManagerId(1);
        updateDTO.setIsActive(true);

        Team updatedTeam = new Team();
        updatedTeam.setId(1);
        updatedTeam.setName("Updated Team Alpha");
        updatedTeam.setManager(manager);
        updatedTeam.setIsActive(true);

        when(teamRepository.findById(1)).thenReturn(Optional.of(team1));
        when(teamRepository.save(any(Team.class))).thenReturn(updatedTeam);
        when(userService.getUserById(1)).thenReturn(Optional.of(manager));

        // When
        TeamDTO result = teamService.updateTeam(updateDTO);

        // Then
        assertEquals(updateDTO.getName(), result.getName());
        assertEquals(updateDTO.getManagerId(), result.getManagerId());
        verify(teamRepository).save(any(Team.class));
        verify(userService).getUserById(updateDTO.getManagerId());
    }

    @Test
    void getActiveTeams_ShouldReturnActiveTeamsAsDTO() {
        // Given
        when(teamRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(team1));

        // When
        List<TeamDTO> actualTeams = teamService.getActiveTeams();

        // Then
        assertEquals(1, actualTeams.size());
        assertTrue(actualTeams.get(0).getIsActive());
        assertEquals(teamDTO1.getName(), actualTeams.get(0).getName());
        verify(teamRepository).findByIsActiveTrue();
    }

    @Test
    void getTeamsByActiveStatus_ShouldReturnTeamsAsDTO() {
        // Given
        when(teamRepository.findByIsActive(false)).thenReturn(Arrays.asList(team2));

        // When
        List<TeamDTO> actualTeams = teamService.getTeamsByActiveStatus(false);

        // Then
        assertEquals(1, actualTeams.size());
        assertFalse(actualTeams.get(0).getIsActive());
        assertEquals(teamDTO2.getName(), actualTeams.get(0).getName());
        verify(teamRepository).findByIsActive(false);
    }

    @Test
    void getTeamByName_WhenTeamExists_ShouldReturnTeamDTO() {
        // Given
        when(teamRepository.findByName("Team Alpha")).thenReturn(Optional.of(team1));

        // When
        Optional<TeamDTO> result = teamService.getTeamByName("Team Alpha");

        // Then
        assertTrue(result.isPresent());
        assertEquals(teamDTO1.getName(), result.get().getName());
        assertEquals(teamDTO1.getManagerId(), result.get().getManagerId());
        verify(teamRepository).findByName("Team Alpha");
    }

    @Test
    void activateTeam_WhenTeamExists_ShouldReturnActivatedTeamDTO() {
        // Given
        Team inactiveTeam = team2;
        when(teamRepository.findById(2)).thenReturn(Optional.of(inactiveTeam));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team savedTeam = invocation.getArgument(0);
            savedTeam.setIsActive(true);
            return savedTeam;
        });

        // When
        Optional<TeamDTO> result = teamService.activateTeam(2);

        // Then
        assertTrue(result.isPresent());
        assertTrue(result.get().getIsActive());
        verify(teamRepository).findById(2);
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void deactivateTeam_WhenTeamExists_ShouldReturnDeactivatedTeamDTO() {
        // Given
        when(teamRepository.findById(1)).thenReturn(Optional.of(team1));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team savedTeam = invocation.getArgument(0);
            savedTeam.setIsActive(false);
            return savedTeam;
        });

        // When
        Optional<TeamDTO> result = teamService.deactivateTeam(1);

        // Then
        assertTrue(result.isPresent());
        assertFalse(result.get().getIsActive());
        verify(teamRepository).findById(1);
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void deleteTeam_ShouldVerifyRepositoryCalls() {
        // Given
        when(teamRepository.findById(1)).thenReturn(Optional.of(team1));
        doNothing().when(taskRepository).deleteAllByTeam(team1);
        doNothing().when(teamMemberRepository).deleteAllByTeam(team1);

        // When
        teamService.deleteTeam(1);

        // Then
        verify(teamRepository).findById(1);
        verify(taskRepository).deleteAllByTeam(team1);
        verify(teamMemberRepository).deleteAllByTeam(team1);
        verify(teamRepository).delete(team1);
    }

    @Test
    void updateTeam_WithNullId_ShouldThrowException() {
        // Given
        TeamDTO invalidDTO = new TeamDTO();
        invalidDTO.setName("Test");
        invalidDTO.setManagerId(1);

        // When & Then
        assertThrows(RuntimeException.class, () -> teamService.updateTeam(invalidDTO));
    }
}