package com.example.backend.controllers;

import com.example.backend.dto.TeamDTO;
import com.example.backend.services.TeamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TeamControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TeamService teamService;

    @InjectMocks
    private TeamController teamController;

    private TeamDTO teamDTO;
    private List<TeamDTO> teamDTOList;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(teamController).build();
        objectMapper = new ObjectMapper();

        // Initialize test data
        teamDTO = new TeamDTO();
        teamDTO.setId(1);
        teamDTO.setName("Construction Team A");
        teamDTO.setManagerId(1);
        teamDTO.setIsActive(true);

        TeamDTO teamDTO2 = new TeamDTO();
        teamDTO2.setId(2);
        teamDTO2.setName("Electrical Team B");
        teamDTO2.setManagerId(2);
        teamDTO2.setIsActive(true);

        teamDTOList = Arrays.asList(teamDTO, teamDTO2);
    }

    @Test
    public void getAllTeams_ShouldReturnListOfTeams() throws Exception {
        // Arrange
        when(teamService.getAllTeams()).thenReturn(teamDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Construction Team A"))
                .andExpect(jsonPath("$[1].name").value("Electrical Team B"));
    }

    @Test
    public void getTeamById_WhenExists_ShouldReturnTeam() throws Exception {
        // Arrange
        Optional<TeamDTO> optionalTeam = Optional.of(teamDTO);
        when(teamService.getTeamById(1)).thenReturn(optionalTeam);

        // Act & Assert
        mockMvc.perform(get("/database/teams/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Construction Team A"))
                .andExpect(jsonPath("$.managerId").value(1));
    }

    @Test
    public void getTeamById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(teamService.getTeamById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/teams/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createTeam_WithValidData_ShouldReturnCreatedTeam() throws Exception {
        // Arrange
        when(teamService.saveTeam(any(TeamDTO.class))).thenReturn(teamDTO);

        TeamDTO newTeam = new TeamDTO();
        newTeam.setName("New Team");
        newTeam.setManagerId(1);
        newTeam.setIsActive(true);

        // Act & Assert
        mockMvc.perform(post("/database/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTeam)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Construction Team A")); // Mock returns our pre-defined teamDTO
    }

    @Test
    public void updateTeam_WhenExists_ShouldReturnUpdatedTeam() throws Exception {
        // Arrange
        Optional<TeamDTO> optionalTeam = Optional.of(teamDTO);
        when(teamService.getTeamById(1)).thenReturn(optionalTeam);
        when(teamService.updateTeam(any(TeamDTO.class))).thenReturn(teamDTO);

        TeamDTO updatedTeam = new TeamDTO();
        updatedTeam.setId(1);
        updatedTeam.setName("Updated Team");
        updatedTeam.setManagerId(2);
        updatedTeam.setIsActive(true);

        // Act & Assert
        mockMvc.perform(put("/database/teams/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTeam)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Construction Team A")); // Mock returns our pre-defined teamDTO
    }

    @Test
    public void updateTeam_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(teamService.getTeamById(99)).thenReturn(Optional.empty());

        TeamDTO updatedTeam = new TeamDTO();
        updatedTeam.setId(99);
        updatedTeam.setName("Updated Team");
        updatedTeam.setManagerId(2);
        updatedTeam.setIsActive(true);

        // Act & Assert
        mockMvc.perform(put("/database/teams/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTeam)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteTeam_WhenExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        Optional<TeamDTO> optionalTeam = Optional.of(teamDTO);
        when(teamService.getTeamById(1)).thenReturn(optionalTeam);
        doNothing().when(teamService).deleteTeam(1);

        // Act & Assert
        mockMvc.perform(delete("/database/teams/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteTeam_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(teamService.getTeamById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/database/teams/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getActiveTeams_ShouldReturnListOfActiveTeams() throws Exception {
        // Arrange
        when(teamService.getActiveTeams()).thenReturn(teamDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/teams/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Construction Team A"))
                .andExpect(jsonPath("$[1].name").value("Electrical Team B"));
    }

    @Test
    public void getTeamsByActiveStatus_ShouldReturnListOfTeams() throws Exception {
        // Arrange
        when(teamService.getTeamsByActiveStatus(true)).thenReturn(teamDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/teams/status")
                        .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Construction Team A"))
                .andExpect(jsonPath("$[1].name").value("Electrical Team B"));
    }

    @Test
    public void getTeamByName_WhenExists_ShouldReturnTeam() throws Exception {
        // Arrange
        Optional<TeamDTO> optionalTeam = Optional.of(teamDTO);
        when(teamService.getTeamByName("Construction Team A")).thenReturn(optionalTeam);

        // Act & Assert
        mockMvc.perform(get("/database/teams/name/Construction Team A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.managerId").value(1));
    }

    @Test
    public void getTeamByName_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(teamService.getTeamByName("Non-existent Team")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/teams/name/Non-existent Team"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void activateTeam_WhenExists_ShouldReturnActivatedTeam() throws Exception {
        // Arrange
        Optional<TeamDTO> optionalTeam = Optional.of(teamDTO);
        when(teamService.activateTeam(1)).thenReturn(optionalTeam);

        // Act & Assert
        mockMvc.perform(patch("/database/teams/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Construction Team A"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    public void activateTeam_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(teamService.activateTeam(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(patch("/database/teams/99/activate"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deactivateTeam_WhenExists_ShouldReturnDeactivatedTeam() throws Exception {
        // Arrange
        TeamDTO deactivatedTeam = new TeamDTO();
        deactivatedTeam.setId(1);
        deactivatedTeam.setName("Construction Team A");
        deactivatedTeam.setManagerId(1);
        deactivatedTeam.setIsActive(false);

        Optional<TeamDTO> optionalTeam = Optional.of(deactivatedTeam);
        when(teamService.deactivateTeam(1)).thenReturn(optionalTeam);

        // Act & Assert
        mockMvc.perform(patch("/database/teams/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Construction Team A"))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    public void deactivateTeam_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(teamService.deactivateTeam(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(patch("/database/teams/99/deactivate"))
                .andExpect(status().isNotFound());
    }
}