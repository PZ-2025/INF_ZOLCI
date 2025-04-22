package com.example.backend.controllers;

import com.example.backend.dto.TeamDTO;
import com.example.backend.services.TeamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
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
    private TeamDTO teamDTO1;
    private TeamDTO teamDTO2;
    private TeamDTO teamDTO3;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Przygotowanie przykładowych DTO
        teamDTO1 = new TeamDTO();
        teamDTO1.setId(1);
        teamDTO1.setName("Alpha");
        teamDTO1.setManagerId(1);
        teamDTO1.setIsActive(true);

        teamDTO2 = new TeamDTO();
        teamDTO2.setId(2);
        teamDTO2.setName("Beta");
        teamDTO2.setManagerId(1);
        teamDTO2.setIsActive(true);

        teamDTO3 = new TeamDTO();
        teamDTO3.setId(3);
        teamDTO3.setName("Gamma");
        teamDTO3.setManagerId(1);
        teamDTO3.setIsActive(false);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void getAllTeams_ShouldReturnListOfTeamDTOs() {
        // Given
        when(teamService.getAllTeams()).thenReturn(List.of(teamDTO1, teamDTO2, teamDTO3));

        // When
        ResponseEntity<List<TeamDTO>> response = teamController.getAllTeams();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        assertEquals("Alpha", response.getBody().get(0).getName());
        assertEquals("Beta", response.getBody().get(1).getName());
        assertEquals("Gamma", response.getBody().get(2).getName());
        verify(teamService).getAllTeams();
    }

    @Test
    void getTeamById_WhenTeamExists_ShouldReturnTeamDTO() {
        // Given
        when(teamService.getTeamById(1)).thenReturn(Optional.of(teamDTO1));

        // When
        ResponseEntity<TeamDTO> response = teamController.getTeamById(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Alpha", response.getBody().getName());
        verify(teamService).getTeamById(1);
    }

    @Test
    void getTeamById_WhenTeamNotExists_ShouldReturnNotFound() {
        // Given
        when(teamService.getTeamById(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<TeamDTO> response = teamController.getTeamById(999);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(teamService).getTeamById(999);
    }

    @Test
    void createTeam_ShouldReturnCreatedTeamDTO() {
        // Given
        TeamDTO newTeam = new TeamDTO();
        newTeam.setName("New Team");
        newTeam.setManagerId(1);
        newTeam.setIsActive(true);

        when(teamService.saveTeam(any(TeamDTO.class))).thenReturn(teamDTO1);

        // When
        ResponseEntity<TeamDTO> response = teamController.createTeam(newTeam);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(teamService).saveTeam(any(TeamDTO.class));
    }

    @Test
    void updateTeam_WhenTeamExists_ShouldReturnUpdatedTeamDTO() {
        // Given
        TeamDTO updateDTO = new TeamDTO();
        updateDTO.setName("Updated Alpha");
        updateDTO.setManagerId(1);
        updateDTO.setIsActive(true);

        when(teamService.getTeamById(1)).thenReturn(Optional.of(teamDTO1));
        when(teamService.updateTeam(any(TeamDTO.class))).thenReturn(updateDTO);

        // When
        ResponseEntity<TeamDTO> response = teamController.updateTeam(1, updateDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Alpha", response.getBody().getName());
        verify(teamService).updateTeam(any(TeamDTO.class));
    }

    @Test
    void updateTeam_WhenTeamNotExists_ShouldReturnNotFound() {
        // Given
        when(teamService.getTeamById(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<TeamDTO> response = teamController.updateTeam(999, new TeamDTO());

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteTeam_WhenTeamExists_ShouldReturnNoContent() {
        // Given
        when(teamService.getTeamById(1)).thenReturn(Optional.of(teamDTO1));
        doNothing().when(teamService).deleteTeam(1);

        // When
        ResponseEntity<Void> response = teamController.deleteTeam(1);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(teamService).deleteTeam(1);
    }

    @Test
    void getActiveTeams_ShouldReturnOnlyActiveTeams() {
        // Given
        List<TeamDTO> activeTeams = Arrays.asList(teamDTO1, teamDTO2);
        when(teamService.getActiveTeams()).thenReturn(activeTeams);

        // When
        ResponseEntity<List<TeamDTO>> response = teamController.getActiveTeams();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().stream().allMatch(TeamDTO::getIsActive));
        verify(teamService).getActiveTeams();
    }

    @Test
    void getTeamsByActiveStatus_ShouldReturnFilteredTeams() {
        // Given
        when(teamService.getTeamsByActiveStatus(false)).thenReturn(List.of(teamDTO3));

        // When
        ResponseEntity<List<TeamDTO>> response = teamController.getTeamsByActiveStatus(false);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().get(0).getIsActive());
        verify(teamService).getTeamsByActiveStatus(false);
    }

    @Test
    void getTeamByName_WhenTeamExists_ShouldReturnTeamDTO() {
        // Given
        when(teamService.getTeamByName("Alpha")).thenReturn(Optional.of(teamDTO1));

        // When
        ResponseEntity<TeamDTO> response = teamController.getTeamByName("Alpha");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Alpha", response.getBody().getName());
        verify(teamService).getTeamByName("Alpha");
    }

    @Test
    void activateTeam_WhenTeamExists_ShouldReturnActivatedTeam() {
        // Given
        TeamDTO activatedTeam = new TeamDTO();
        activatedTeam.setId(1);
        activatedTeam.setIsActive(true);
        when(teamService.activateTeam(1)).thenReturn(Optional.of(activatedTeam));

        // When
        ResponseEntity<TeamDTO> response = teamController.activateTeam(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getIsActive());
        verify(teamService).activateTeam(1);
    }

    @Test
    void deactivateTeam_WhenTeamExists_ShouldReturnDeactivatedTeam() {
        // Given
        TeamDTO deactivatedTeam = new TeamDTO();
        deactivatedTeam.setId(1);
        deactivatedTeam.setIsActive(false);
        when(teamService.deactivateTeam(1)).thenReturn(Optional.of(deactivatedTeam));

        // When
        ResponseEntity<TeamDTO> response = teamController.deactivateTeam(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().getIsActive());
        verify(teamService).deactivateTeam(1);
    }
}
