package com.example.backend.services;

import com.example.backend.models.Team;
import com.example.backend.models.User;
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

    @InjectMocks
    private TeamService teamService;

    private User manager;
    private Team team1;
    private Team team2;
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
        manager.setRole("manager");
        manager.setIsActive(true);

        // Inicjalizacja zespołów testowych
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
    }

    @Test
    void getAllTeams_ShouldReturnAllTeams() {
        // Given
        List<Team> expectedTeams = Arrays.asList(team1, team2);
        when(teamRepository.findAll()).thenReturn(expectedTeams);

        // When
        List<Team> actualTeams = teamService.getAllTeams();

        // Then
        assertEquals(expectedTeams.size(), actualTeams.size());
        assertEquals(expectedTeams.get(0).getId(), actualTeams.get(0).getId());
        assertEquals(expectedTeams.get(1).getId(), actualTeams.get(1).getId());
        verify(teamRepository, times(1)).findAll();
    }

    @Test
    void getTeamById_WhenTeamExists_ShouldReturnTeam() {
        // Given
        when(teamRepository.findById(1)).thenReturn(Optional.of(team1));

        // When
        Optional<Team> result = teamService.getTeamById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(team1.getId(), result.get().getId());
        assertEquals(team1.getName(), result.get().getName());
        verify(teamRepository, times(1)).findById(1);
    }

    @Test
    void getTeamById_WhenTeamDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(teamRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<Team> result = teamService.getTeamById(99);

        // Then
        assertFalse(result.isPresent());
        verify(teamRepository, times(1)).findById(99);
    }

    @Test
    void saveTeam_ShouldSaveAndReturnTeam() {
        // Given
        Team newTeam = new Team();
        newTeam.setName("New Team");
        newTeam.setManager(manager);
        newTeam.setIsActive(true);

        when(teamRepository.save(any(Team.class))).thenReturn(newTeam);

        // When
        Team savedTeam = teamService.saveTeam(newTeam);

        // Then
        assertEquals(newTeam.getName(), savedTeam.getName());
        assertEquals(newTeam.getManager(), savedTeam.getManager());
        assertEquals(newTeam.getIsActive(), savedTeam.getIsActive());
        verify(teamRepository, times(1)).save(newTeam);
    }

    @Test
    void updateTeam_ShouldUpdateAndReturnTeam() {
        // Given
        Team updatedTeam = new Team();
        updatedTeam.setId(1);
        updatedTeam.setName("Updated Team Alpha");
        updatedTeam.setManager(manager);
        updatedTeam.setIsActive(true);
        updatedTeam.setCreatedAt(team1.getCreatedAt());

        when(teamRepository.save(any(Team.class))).thenReturn(updatedTeam);

        // When
        Team result = teamService.updateTeam(updatedTeam);

        // Then
        assertEquals(updatedTeam.getId(), result.getId());
        assertEquals(updatedTeam.getName(), result.getName());
        verify(teamRepository, times(1)).save(updatedTeam);
    }

    @Test
    void deleteTeam_ShouldCallRepository() {
        // When
        teamService.deleteTeam(1);

        // Then
        verify(teamRepository, times(1)).deleteById(1);
    }

    @Test
    void getTeamsByManager_ShouldReturnTeamsForManager() {
        // Given
        List<Team> expectedTeams = Arrays.asList(team1, team2);
        when(teamRepository.findByManager(manager)).thenReturn(expectedTeams);

        // When
        List<Team> actualTeams = teamService.getTeamsByManager(manager);

        // Then
        assertEquals(expectedTeams.size(), actualTeams.size());
        assertEquals(expectedTeams.get(0).getId(), actualTeams.get(0).getId());
        assertEquals(expectedTeams.get(1).getId(), actualTeams.get(1).getId());
        verify(teamRepository, times(1)).findByManager(manager);
    }

    @Test
    void getTeamsByActiveStatus_WithActiveTrue_ShouldReturnActiveTeams() {
        // Given
        List<Team> activeTeams = Arrays.asList(team1);
        when(teamRepository.findByIsActive(true)).thenReturn(activeTeams);

        // When
        List<Team> actualTeams = teamService.getTeamsByActiveStatus(true);

        // Then
        assertEquals(activeTeams.size(), actualTeams.size());
        assertEquals(activeTeams.get(0).getId(), actualTeams.get(0).getId());
        assertTrue(actualTeams.get(0).getIsActive());
        verify(teamRepository, times(1)).findByIsActive(true);
    }

    @Test
    void getTeamsByActiveStatus_WithActiveFalse_ShouldReturnInactiveTeams() {
        // Given
        List<Team> inactiveTeams = Arrays.asList(team2);
        when(teamRepository.findByIsActive(false)).thenReturn(inactiveTeams);

        // When
        List<Team> actualTeams = teamService.getTeamsByActiveStatus(false);

        // Then
        assertEquals(inactiveTeams.size(), actualTeams.size());
        assertEquals(inactiveTeams.get(0).getId(), actualTeams.get(0).getId());
        assertFalse(actualTeams.get(0).getIsActive());
        verify(teamRepository, times(1)).findByIsActive(false);
    }

    @Test
    void getActiveTeams_ShouldReturnActiveTeams() {
        // Given
        List<Team> activeTeams = Arrays.asList(team1);
        when(teamRepository.findByIsActiveTrue()).thenReturn(activeTeams);

        // When
        List<Team> actualTeams = teamService.getActiveTeams();

        // Then
        assertEquals(activeTeams.size(), actualTeams.size());
        assertEquals(activeTeams.get(0).getId(), actualTeams.get(0).getId());
        assertTrue(actualTeams.get(0).getIsActive());
        verify(teamRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void getTeamByName_WhenTeamExists_ShouldReturnTeam() {
        // Given
        when(teamRepository.findByName("Team Alpha")).thenReturn(Optional.of(team1));

        // When
        Optional<Object> result = teamService.getTeamByName("Team Alpha");

        // Then
        assertTrue(result.isPresent());
        assertEquals(team1, result.get());
        verify(teamRepository, times(1)).findByName("Team Alpha");
    }

    @Test
    void getTeamByName_WhenTeamDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(teamRepository.findByName("Nonexistent Team")).thenReturn(Optional.empty());

        // When
        Optional<Object> result = teamService.getTeamByName("Nonexistent Team");

        // Then
        assertFalse(result.isPresent());
        verify(teamRepository, times(1)).findByName("Nonexistent Team");
    }

    @Test
    void activateTeam_WhenTeamExists_ShouldActivateAndReturnTeam() {
        // Given
        when(teamRepository.findById(2)).thenReturn(Optional.of(team2));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team savedTeam = invocation.getArgument(0);
            return savedTeam;
        });

        // When
        Optional<Team> result = teamService.activateTeam(2);

        // Then
        assertTrue(result.isPresent());
        assertTrue(result.get().getIsActive());
        verify(teamRepository, times(1)).findById(2);
        verify(teamRepository, times(1)).save(any(Team.class));
    }

    @Test
    void activateTeam_WhenTeamDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(teamRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<Team> result = teamService.activateTeam(99);

        // Then
        assertFalse(result.isPresent());
        verify(teamRepository, times(1)).findById(99);
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    void deactivateTeam_WhenTeamExists_ShouldDeactivateAndReturnTeam() {
        // Given
        when(teamRepository.findById(1)).thenReturn(Optional.of(team1));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team savedTeam = invocation.getArgument(0);
            return savedTeam;
        });

        // When
        Optional<Team> result = teamService.deactivateTeam(1);

        // Then
        assertTrue(result.isPresent());
        assertFalse(result.get().getIsActive());
        verify(teamRepository, times(1)).findById(1);
        verify(teamRepository, times(1)).save(any(Team.class));
    }

    @Test
    void deactivateTeam_WhenTeamDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(teamRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<Team> result = teamService.deactivateTeam(99);

        // Then
        assertFalse(result.isPresent());
        verify(teamRepository, times(1)).findById(99);
        verify(teamRepository, never()).save(any(Team.class));
    }
}