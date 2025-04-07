package  com.example.backend.controllers;

import com.example.backend.models.Team;
import com.example.backend.models.User;
import com.example.backend.services.TeamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeamControllerTest {

    @Mock
    private TeamService teamService;

    @InjectMocks
    private TeamController teamController;

    private AutoCloseable closeable;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User sampleManager;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        sampleManager = new User();
        sampleManager.setId(1);
        sampleManager.setFirstName("Anna");
        sampleManager.setLastName("Nowak");
        sampleManager.setEmail("anna.nowak@example.com");
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void getAllTeams() {
        Team team = new Team();
        team.setId(1);
        team.setName("Alpha");
        team.setManager(sampleManager);

        when(teamService.getAllTeams()).thenReturn(List.of(team));

        ResponseEntity<List<Team>> response = teamController.getAllTeams();
        assertEquals(1, response.getBody().size());
        assertEquals("Alpha", response.getBody().get(0).getName());
    }

    @Test
    void getTeamById() {
        Team team = new Team();
        team.setId(2);
        team.setName("Beta");
        team.setManager(sampleManager);

        when(teamService.getTeamById(2)).thenReturn(Optional.of(team));

        ResponseEntity<Team> response = teamController.getTeamById(2);
        assertEquals("Beta", response.getBody().getName());
        assertEquals(2, response.getBody().getId());
    }

    @Test
    void createTeam() {
        Team newTeam = new Team();
        newTeam.setName("Gamma");
        newTeam.setManager(sampleManager);

        Team saved = new Team();
        saved.setId(3);
        saved.setName("Gamma");
        saved.setManager(sampleManager);

        when(teamService.saveTeam(any(Team.class))).thenReturn(saved);

        ResponseEntity<Team> response = teamController.createTeam(newTeam);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Gamma", response.getBody().getName());
    }

    @Test
    void updateTeam() {
        Team existing = new Team();
        existing.setId(4);
        existing.setName("Old");
        existing.setManager(sampleManager);

        Team updated = new Team();
        updated.setId(4);
        updated.setName("Updated");
        updated.setManager(sampleManager);

        when(teamService.getTeamById(4)).thenReturn(Optional.of(existing));
        when(teamService.updateTeam(any(Team.class))).thenReturn(updated);

        ResponseEntity<Team> response = teamController.updateTeam(4, updated);
        assertEquals("Updated", response.getBody().getName());
    }

    @Test
    void deleteTeam() {
        Team team = new Team();
        team.setId(5);
        team.setName("ToDelete");
        team.setManager(sampleManager);

        when(teamService.getTeamById(5)).thenReturn(Optional.of(team));
        doNothing().when(teamService).deleteTeam(5);

        ResponseEntity<Void> response = teamController.deleteTeam(5);
        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void getActiveTeams() {
        Team activeTeam = new Team();
        activeTeam.setId(6);
        activeTeam.setName("Active");
        activeTeam.setIsActive(true);
        activeTeam.setManager(sampleManager);

        when(teamService.getActiveTeams()).thenReturn(List.of(activeTeam));

        ResponseEntity<List<Team>> response = teamController.getActiveTeams();
        assertTrue(response.getBody().get(0).getIsActive());
    }

    @Test
    void getTeamsByActiveStatus() {
        Team inactiveTeam = new Team();
        inactiveTeam.setId(7);
        inactiveTeam.setName("Inactive");
        inactiveTeam.setIsActive(false);
        inactiveTeam.setManager(sampleManager);

        when(teamService.getTeamsByActiveStatus(false)).thenReturn(List.of(inactiveTeam));

        ResponseEntity<List<Team>> response = teamController.getTeamsByActiveStatus(false);
        assertFalse(response.getBody().get(0).getIsActive());
    }

    @Test
    void getTeamByName() {
        Team team = new Team();
        team.setId(8);
        team.setName("Zespół X");
        team.setManager(sampleManager);

        when(teamService.getTeamByName("Zespół X")).thenReturn(Optional.of(team));

        ResponseEntity<Object> response = teamController.getTeamByName("Zespół X");
        assertEquals("Zespół X", team.getName());

    }

    @Test
    void activateTeam() {
        Team team = new Team();
        team.setId(9);
        team.setIsActive(true);
        team.setManager(sampleManager);

        when(teamService.activateTeam(9)).thenReturn(Optional.of(team));

        ResponseEntity<Team> response = teamController.activateTeam(9);
        assertTrue(response.getBody().getIsActive());
    }

    @Test
    void deactivateTeam() {
        Team team = new Team();
        team.setId(10);
        team.setIsActive(false);
        team.setManager(sampleManager);

        when(teamService.deactivateTeam(10)).thenReturn(Optional.of(team));

        ResponseEntity<Team> response = teamController.deactivateTeam(10);
        assertFalse(response.getBody().getIsActive());
    }
}
