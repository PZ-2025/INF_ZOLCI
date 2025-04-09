package com.example.backend.services;

import com.example.backend.models.Team;
import com.example.backend.models.TeamMember;
import com.example.backend.models.User;
import com.example.backend.repository.TeamMemberRepository;
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

class TeamMemberServiceTest {

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private TeamMemberService teamMemberService;

    private User user1;
    private User user2;
    private Team team;
    private TeamMember teamMember1;
    private TeamMember teamMember2;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        now = LocalDateTime.now();

        // Inicjalizacja użytkowników testowych
        user1 = new User();
        user1.setId(1);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setIsActive(true);

        user2 = new User();
        user2.setId(2);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setIsActive(true);

        // Inicjalizacja zespołu testowego
        team = new Team();
        team.setId(1);
        team.setName("Test Team");
        team.setIsActive(true);

        // Inicjalizacja członków zespołu testowych
        teamMember1 = new TeamMember();
        teamMember1.setId(1);
        teamMember1.setUser(user1);
        teamMember1.setTeam(team);
        teamMember1.setJoinedAt(now);
        teamMember1.setIsActive(true);

        teamMember2 = new TeamMember();
        teamMember2.setId(2);
        teamMember2.setUser(user2);
        teamMember2.setTeam(team);
        teamMember2.setJoinedAt(now);
        teamMember2.setIsActive(false);
    }

    @Test
    void getTeamMembersByUser_ShouldReturnMembershipsForUser() {
        // Given
        List<TeamMember> expectedMembers = Arrays.asList(teamMember1);
        when(teamMemberRepository.findByUser(user1)).thenReturn(expectedMembers);

        // When
        List<TeamMember> actualMembers = teamMemberService.getTeamMembersByUser(user1);

        // Then
        assertEquals(expectedMembers.size(), actualMembers.size());
        assertEquals(expectedMembers.get(0).getId(), actualMembers.get(0).getId());
        verify(teamMemberRepository, times(1)).findByUser(user1);
    }

    @Test
    void getTeamMembersByTeam_ShouldReturnMembersForTeam() {
        // Given
        List<TeamMember> expectedMembers = Arrays.asList(teamMember1, teamMember2);
        when(teamMemberRepository.findByTeam(team)).thenReturn(expectedMembers);

        // When
        List<TeamMember> actualMembers = teamMemberService.getTeamMembersByTeam(team);

        // Then
        assertEquals(expectedMembers.size(), actualMembers.size());
        assertEquals(expectedMembers.get(0).getId(), actualMembers.get(0).getId());
        assertEquals(expectedMembers.get(1).getId(), actualMembers.get(1).getId());
        verify(teamMemberRepository, times(1)).findByTeam(team);
    }

    @Test
    void getTeamMembersByTeamAndActiveStatus_WithActiveTrue_ShouldReturnActiveMembers() {
        // Given
        List<TeamMember> activeMembers = Arrays.asList(teamMember1);
        when(teamMemberRepository.findByTeamAndIsActive(team, true)).thenReturn(activeMembers);

        // When
        List<TeamMember> actualMembers = teamMemberService.getTeamMembersByTeamAndActiveStatus(team, true);

        // Then
        assertEquals(activeMembers.size(), actualMembers.size());
        assertEquals(activeMembers.get(0).getId(), actualMembers.get(0).getId());
        assertTrue(actualMembers.get(0).getIsActive());
        verify(teamMemberRepository, times(1)).findByTeamAndIsActive(team, true);
    }

    @Test
    void getTeamMembersByTeamAndActiveStatus_WithActiveFalse_ShouldReturnInactiveMembers() {
        // Given
        List<TeamMember> inactiveMembers = Arrays.asList(teamMember2);
        when(teamMemberRepository.findByTeamAndIsActive(team, false)).thenReturn(inactiveMembers);

        // When
        List<TeamMember> actualMembers = teamMemberService.getTeamMembersByTeamAndActiveStatus(team, false);

        // Then
        assertEquals(inactiveMembers.size(), actualMembers.size());
        assertEquals(inactiveMembers.get(0).getId(), actualMembers.get(0).getId());
        assertFalse(actualMembers.get(0).getIsActive());
        verify(teamMemberRepository, times(1)).findByTeamAndIsActive(team, false);
    }

    @Test
    void getTeamMemberByUserAndTeam_WhenMemberExists_ShouldReturnMember() {
        // Given
        when(teamMemberRepository.findByUserAndTeam(user1, team)).thenReturn(Optional.of(teamMember1));

        // When
        Optional<TeamMember> result = teamMemberService.getTeamMemberByUserAndTeam(user1, team);

        // Then
        assertTrue(result.isPresent());
        assertEquals(teamMember1.getId(), result.get().getId());
        assertEquals(user1, result.get().getUser());
        assertEquals(team, result.get().getTeam());
        verify(teamMemberRepository, times(1)).findByUserAndTeam(user1, team);
    }

    @Test
    void getTeamMemberByUserAndTeam_WhenMemberDoesNotExist_ShouldReturnEmpty() {
        // Given
        User nonMemberUser = new User();
        nonMemberUser.setId(3);
        nonMemberUser.setUsername("nonmember");

        when(teamMemberRepository.findByUserAndTeam(nonMemberUser, team)).thenReturn(Optional.empty());

        // When
        Optional<TeamMember> result = teamMemberService.getTeamMemberByUserAndTeam(nonMemberUser, team);

        // Then
        assertFalse(result.isPresent());
        verify(teamMemberRepository, times(1)).findByUserAndTeam(nonMemberUser, team);
    }

    @Test
    void getAllTeamMembersByTeam_ShouldReturnAllMembers() {
        // Given
        List<TeamMember> expectedMembers = Arrays.asList(teamMember1, teamMember2);
        when(teamMemberRepository.findAllByTeam(team)).thenReturn(expectedMembers);

        // When
        List<TeamMember> actualMembers = teamMemberService.getAllTeamMembersByTeam(team);

        // Then
        assertEquals(expectedMembers.size(), actualMembers.size());
        assertEquals(expectedMembers.get(0).getId(), actualMembers.get(0).getId());
        assertEquals(expectedMembers.get(1).getId(), actualMembers.get(1).getId());
        verify(teamMemberRepository, times(1)).findAllByTeam(team);
    }

    @Test
    void saveTeamMember_ShouldSaveAndReturnMember() {
        // Given
        User newUser = new User();
        newUser.setId(3);
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setIsActive(true);

        TeamMember newMember = new TeamMember();
        newMember.setUser(newUser);
        newMember.setTeam(team);
        newMember.setIsActive(true);

        when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(newMember);

        // When
        TeamMember savedMember = teamMemberService.saveTeamMember(newMember);

        // Then
        assertEquals(newMember.getUser(), savedMember.getUser());
        assertEquals(newMember.getTeam(), savedMember.getTeam());
        assertEquals(newMember.getIsActive(), savedMember.getIsActive());
        verify(teamMemberRepository, times(1)).save(newMember);
    }

    @Test
    void deleteTeamMember_ShouldCallRepository() {
        // When
        teamMemberService.deleteTeamMember(teamMember1);

        // Then
        verify(teamMemberRepository, times(1)).delete(teamMember1);
    }

    @Test
    void getTeamMemberById_WhenMemberExists_ShouldReturnMember() {
        // Given
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(teamMember1));

        // When
        Optional<TeamMember> result = teamMemberService.getTeamMemberById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(teamMember1.getId(), result.get().getId());
        assertEquals(teamMember1.getUser(), result.get().getUser());
        assertEquals(teamMember1.getTeam(), result.get().getTeam());
        verify(teamMemberRepository, times(1)).findById(1L);
    }

    @Test
    void getTeamMemberById_WhenMemberDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(teamMemberRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<TeamMember> result = teamMemberService.getTeamMemberById(99L);

        // Then
        assertFalse(result.isPresent());
        verify(teamMemberRepository, times(1)).findById(99L);
    }
}