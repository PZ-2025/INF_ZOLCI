package  com.example.backend.controllers;

import com.example.backend.models.Team;
import com.example.backend.models.TeamMember;
import com.example.backend.models.User;
import com.example.backend.services.TeamMemberService;
import com.example.backend.services.TeamService;
import com.example.backend.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeamMemberControllerTest {

    @InjectMocks
    private TeamMemberController teamMemberController;

    @Mock
    private TeamMemberService teamMemberService;

    @Mock
    private UserService userService;

    @Mock
    private TeamService teamService;

    private User user;
    private Team team;
    private TeamMember teamMember;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1);
        user.setUsername("john_doe");

        team = new Team();
        team.setId(1);
        team.setName("Team A");

        teamMember = new TeamMember();
        teamMember.setId(1);
        teamMember.setUser(user);
        teamMember.setTeam(team);
        teamMember.setIsActive(true);
    }

    @Test
    void getTeamMembersByUser_returnsList() {
        when(userService.getUserById(1)).thenReturn(Optional.of(user));
        when(teamMemberService.getTeamMembersByUser(user)).thenReturn(List.of(teamMember));

        ResponseEntity<List<TeamMember>> response = teamMemberController.getTeamMembersByUser(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        verify(userService).getUserById(1);
    }

    @Test
    void getTeamMembersByTeam_returnsList() {
        when(teamService.getTeamById(1)).thenReturn(Optional.of(team));
        when(teamMemberService.getTeamMembersByTeam(team)).thenReturn(List.of(teamMember));

        ResponseEntity<List<TeamMember>> response = teamMemberController.getTeamMembersByTeam(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        verify(teamService).getTeamById(1);
    }

    @Test
    void getTeamMembersByTeamAndActiveStatus_returnsList() {
        when(teamService.getTeamById(1)).thenReturn(Optional.of(team));
        when(teamMemberService.getTeamMembersByTeamAndActiveStatus(team, true)).thenReturn(List.of(teamMember));

        ResponseEntity<List<TeamMember>> response = teamMemberController.getTeamMembersByTeamAndActiveStatus(1L, true);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getIsActive());
    }

    @Test
    void createTeamMember_returnsCreated() {
        when(teamMemberService.saveTeamMember(teamMember)).thenReturn(teamMember);

        ResponseEntity<TeamMember> response = teamMemberController.createTeamMember(teamMember);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(teamMember, response.getBody());
    }

    @Test
    void updateTeamMember_success() {
        TeamMember updateData = new TeamMember();
        updateData.setIsActive(false);

        when(teamMemberService.getTeamMemberById(1L)).thenReturn(Optional.of(teamMember));
        when(teamMemberService.saveTeamMember(any())).thenReturn(teamMember);

        ResponseEntity<TeamMember> response = teamMemberController.updateTeamMember(1L, updateData);

        assertEquals(200, response.getStatusCodeValue());
        verify(teamMemberService).saveTeamMember(teamMember);
    }

    @Test
    void updateTeamMember_notFound() {
        when(teamMemberService.getTeamMemberById(1L)).thenReturn(Optional.empty());

        ResponseEntity<TeamMember> response = teamMemberController.updateTeamMember(1L, teamMember);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void deleteTeamMember_success() {
        when(teamMemberService.getTeamMemberById(1L)).thenReturn(Optional.of(teamMember));

        ResponseEntity<Void> response = teamMemberController.deleteTeamMember(1L);

        assertEquals(204, response.getStatusCodeValue());
        verify(teamMemberService).deleteTeamMember(teamMember);
    }

    @Test
    void deleteTeamMember_notFound() {
        when(teamMemberService.getTeamMemberById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = teamMemberController.deleteTeamMember(1L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void getTeamMemberById_found() {
        when(teamMemberService.getTeamMemberById(1L)).thenReturn(Optional.of(teamMember));

        ResponseEntity<TeamMember> response = teamMemberController.getTeamMemberById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(teamMember, response.getBody());
    }

    @Test
    void getTeamMemberById_notFound() {
        when(teamMemberService.getTeamMemberById(1L)).thenReturn(Optional.empty());

        ResponseEntity<TeamMember> response = teamMemberController.getTeamMemberById(1L);

        assertEquals(404, response.getStatusCodeValue());
    }
}
