package com.example.backend.services;

import com.example.backend.dto.TeamMemberDTO;
import com.example.backend.models.Team;
import com.example.backend.models.TeamMember;
import com.example.backend.models.User;
import com.example.backend.repository.TeamMemberRepository;
import com.example.backend.repository.TeamRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamMemberServiceTest {

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeamMemberService teamMemberService;

    private Team team;
    private User user;
    private TeamMember teamMember;
    private TeamMemberDTO teamMemberDTO;

    @BeforeEach
    void setUp() {
        // Initialize test data
        team = new Team();
        team.setId(1);
        team.setName("Construction Team Alpha");

        user = new User();
        user.setId(1);
        user.setUsername("worker1");
        user.setFirstName("John");
        user.setLastName("Doe");

        teamMember = new TeamMember();
        teamMember.setId(1);
        teamMember.setTeam(team);
        teamMember.setUser(user);
        teamMember.setJoinedAt(LocalDateTime.now());
        teamMember.setIsActive(true);

        teamMemberDTO = new TeamMemberDTO();
        teamMemberDTO.setId(1);
        teamMemberDTO.setTeamId(1);
        teamMemberDTO.setUserId(1);
        teamMemberDTO.setJoinedAt(LocalDateTime.now());
        teamMemberDTO.setIsActive(true);
        teamMemberDTO.setTeamName("Construction Team Alpha");
        teamMemberDTO.setUsername("worker1");
        teamMemberDTO.setUserFullName("John Doe");
    }

    @Test
    void getTeamMembersByUser_ShouldReturnTeamMembersForUser() {
        // Arrange
        when(teamMemberRepository.findByUser(user)).thenReturn(Arrays.asList(teamMember));

        // Act
        List<TeamMemberDTO> result = teamMemberService.getTeamMembersByUser(user);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getTeamId());
        assertEquals(1, result.get(0).getUserId());
    }

    @Test
    void getTeamMembersByTeam_ShouldReturnTeamMembersForTeam() {
        // Arrange
        when(teamMemberRepository.findByTeam(team)).thenReturn(Arrays.asList(teamMember));

        // Act
        List<TeamMemberDTO> result = teamMemberService.getTeamMembersByTeam(team);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getTeamId());
        assertEquals(1, result.get(0).getUserId());
    }

    @Test
    void getTeamMembersByTeamAndActiveStatus_WhenActive_ShouldReturnActiveTeamMembers() {
        // Arrange
        when(teamMemberRepository.findByTeamAndIsActive(team, true)).thenReturn(Arrays.asList(teamMember));

        // Act
        List<TeamMemberDTO> result = teamMemberService.getTeamMembersByTeamAndActiveStatus(team, true);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getTeamId());
        assertTrue(result.get(0).getIsActive());
    }

    @Test
    void getTeamMembersByTeamAndActiveStatus_WhenInactive_ShouldReturnInactiveTeamMembers() {
        // Arrange
        TeamMember inactiveTeamMember = new TeamMember();
        inactiveTeamMember.setId(2);
        inactiveTeamMember.setTeam(team);
        inactiveTeamMember.setUser(user);
        inactiveTeamMember.setIsActive(false);

        when(teamMemberRepository.findByTeamAndIsActive(team, false)).thenReturn(Arrays.asList(inactiveTeamMember));

        // Act
        List<TeamMemberDTO> result = teamMemberService.getTeamMembersByTeamAndActiveStatus(team, false);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getTeamId());
        assertFalse(result.get(0).getIsActive());
    }

    @Test
    void getTeamMemberByUserAndTeam_WhenExists_ShouldReturnTeamMember() {
        // Arrange
        when(teamMemberRepository.findByUserAndTeam(user, team)).thenReturn(Optional.of(teamMember));

        // Act
        Optional<TeamMemberDTO> result = teamMemberService.getTeamMemberByUserAndTeam(user, team);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals(1, result.get().getTeamId());
        assertEquals(1, result.get().getUserId());
    }

    @Test
    void getTeamMemberByUserAndTeam_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(teamMemberRepository.findByUserAndTeam(user, team)).thenReturn(Optional.empty());

        // Act
        Optional<TeamMemberDTO> result = teamMemberService.getTeamMemberByUserAndTeam(user, team);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getAllTeamMembersByTeam_ShouldReturnAllTeamMembers() {
        // Arrange
        when(teamMemberRepository.findAllByTeam(team)).thenReturn(Arrays.asList(teamMember));

        // Act
        List<TeamMemberDTO> result = teamMemberService.getAllTeamMembersByTeam(team);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(1, result.get(0).getTeamId());
    }

    @Test
    void saveTeamMember_WithValidDTO_ShouldSaveAndReturnDTO() {
        // Arrange
        when(teamRepository.findById(1)).thenReturn(Optional.of(team));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(teamMember);

        // Act
        TeamMemberDTO result = teamMemberService.saveTeamMember(teamMemberDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(1, result.getTeamId());
        assertEquals(1, result.getUserId());
        verify(teamMemberRepository).save(any(TeamMember.class));
    }

    @Test
    void saveTeamMember_WhenTeamNotFound_ShouldThrowException() {
        // Arrange
        when(teamRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> teamMemberService.saveTeamMember(teamMemberDTO));

        assertEquals("Nie znaleziono zespołu o ID: 1", exception.getMessage());
        verify(teamMemberRepository, never()).save(any(TeamMember.class));
    }

    @Test
    void saveTeamMember_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(teamRepository.findById(1)).thenReturn(Optional.of(team));
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> teamMemberService.saveTeamMember(teamMemberDTO));

        assertEquals("Nie znaleziono użytkownika o ID: 1", exception.getMessage());
        verify(teamMemberRepository, never()).save(any(TeamMember.class));
    }

    @Test
    void saveTeamMember_WithNullJoinedAt_ShouldSetCurrentDateTime() {
        // Arrange
        when(teamRepository.findById(1)).thenReturn(Optional.of(team));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(teamMemberRepository.save(any(TeamMember.class))).thenAnswer(invocation -> {
            TeamMember savedMember = invocation.getArgument(0);
            savedMember.setId(1);
            return savedMember;
        });

        // Set joinedAt to null
        teamMemberDTO.setJoinedAt(null);

        // Act
        TeamMemberDTO result = teamMemberService.saveTeamMember(teamMemberDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getJoinedAt()); // Should be set automatically
    }

    @Test
    void deleteTeamMemberById_WhenExists_ShouldReturnTrue() {
        // Arrange
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(teamMember));

        // Act
        boolean result = teamMemberService.deleteTeamMemberById(1L);

        // Assert
        assertTrue(result);
        verify(teamMemberRepository).delete(teamMember);
    }

    @Test
    void deleteTeamMemberById_WhenNotExists_ShouldReturnFalse() {
        // Arrange
        when(teamMemberRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        boolean result = teamMemberService.deleteTeamMemberById(999L);

        // Assert
        assertFalse(result);
        verify(teamMemberRepository, never()).delete(any(TeamMember.class));
    }

    @Test
    void getTeamMemberById_WhenExists_ShouldReturnTeamMember() {
        // Arrange
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(teamMember));

        // Act
        Optional<TeamMemberDTO> result = teamMemberService.getTeamMemberById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals(1, result.get().getTeamId());
        assertEquals(1, result.get().getUserId());
    }

    @Test
    void getTeamMemberById_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(teamMemberRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<TeamMemberDTO> result = teamMemberService.getTeamMemberById(999L);

        // Assert
        assertFalse(result.isPresent());
    }
}