package com.example.backend.controllers;

import com.example.backend.dto.TeamMemberDTO;
import com.example.backend.models.Team;
import com.example.backend.services.TeamMemberService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TeamMemberControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TeamMemberService teamMemberService;

    @Mock
    private TeamService teamService;

    @InjectMocks
    private TeamMemberController teamMemberController;

    private TeamMemberDTO teamMemberDTO;
    private List<TeamMemberDTO> teamMemberDTOList;
    private Team team;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(teamMemberController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDateTime serialization

        // Initialize test data
        team = new Team();
        team.setId(1);
        team.setName("Construction Team A");

        teamMemberDTO = new TeamMemberDTO();
        teamMemberDTO.setId(1);
        teamMemberDTO.setTeamId(1);
        teamMemberDTO.setUserId(2);
        teamMemberDTO.setJoinedAt(LocalDateTime.now());
        teamMemberDTO.setIsActive(true);
        teamMemberDTO.setTeamName("Construction Team A");
        teamMemberDTO.setUsername("worker1");
        teamMemberDTO.setUserFullName("John Doe");

        TeamMemberDTO teamMemberDTO2 = new TeamMemberDTO();
        teamMemberDTO2.setId(2);
        teamMemberDTO2.setTeamId(1);
        teamMemberDTO2.setUserId(3);
        teamMemberDTO2.setJoinedAt(LocalDateTime.now());
        teamMemberDTO2.setIsActive(true);
        teamMemberDTO2.setTeamName("Construction Team A");
        teamMemberDTO2.setUsername("worker2");
        teamMemberDTO2.setUserFullName("Jane Smith");

        teamMemberDTOList = Arrays.asList(teamMemberDTO, teamMemberDTO2);
    }

    @Test
    public void getTeamMembersByTeam_WhenTeamExists_ShouldReturnTeamMembers() throws Exception {
        // Arrange
        when(teamService.getTeamEntityById(1)).thenReturn(Optional.of(team));
        when(teamMemberService.getTeamMembersByTeam(team)).thenReturn(teamMemberDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/team-members/team/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("worker1"))
                .andExpect(jsonPath("$[1].username").value("worker2"));
    }

    @Test
    public void getTeamMembersByTeam_WhenTeamDoesNotExist_ShouldThrowException() throws Exception {
        // Arrange
        when(teamService.getTeamEntityById(99)).thenThrow(new RuntimeException("Team not found"));

        // Act & Assert
        mockMvc.perform(get("/database/team-members/team/99"))
                .andExpect(status().isInternalServerError()); // Because the controller doesn't handle exceptions
    }

    @Test
    public void getTeamMembersByTeamAndActiveStatus_WhenTeamExists_ShouldReturnActiveTeamMembers() throws Exception {
        // Arrange
        when(teamService.getTeamEntityById(1)).thenReturn(Optional.of(team));
        when(teamMemberService.getTeamMembersByTeamAndActiveStatus(team, true)).thenReturn(teamMemberDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/team-members/team/1/active/true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("worker1"))
                .andExpect(jsonPath("$[1].username").value("worker2"));
    }

    @Test
    public void createTeamMember_WithValidData_ShouldReturnCreatedTeamMember() throws Exception {
        // Arrange
        when(teamMemberService.saveTeamMember(any(TeamMemberDTO.class))).thenReturn(teamMemberDTO);

        TeamMemberDTO newTeamMember = new TeamMemberDTO();
        newTeamMember.setTeamId(1);
        newTeamMember.setUserId(4);
        newTeamMember.setIsActive(true);

        // Act & Assert
        mockMvc.perform(post("/database/team-members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTeamMember)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("worker1")); // Mock returns our pre-defined teamMemberDTO
    }

    @Test
    public void updateTeamMember_WhenExists_ShouldReturnUpdatedTeamMember() throws Exception {
        // Arrange
        Optional<TeamMemberDTO> optionalTeamMember = Optional.of(teamMemberDTO);
        when(teamMemberService.getTeamMemberById(1L)).thenReturn(optionalTeamMember);
        when(teamMemberService.saveTeamMember(any(TeamMemberDTO.class))).thenReturn(teamMemberDTO);

        TeamMemberDTO updatedTeamMember = new TeamMemberDTO();
        updatedTeamMember.setId(1);
        updatedTeamMember.setTeamId(1);
        updatedTeamMember.setUserId(2);
        updatedTeamMember.setIsActive(false); // Changing active status

        // Act & Assert
        mockMvc.perform(put("/database/team-members/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTeamMember)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("worker1")); // Mock returns our pre-defined teamMemberDTO
    }

    @Test
    public void updateTeamMember_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(teamMemberService.getTeamMemberById(99L)).thenReturn(Optional.empty());

        TeamMemberDTO updatedTeamMember = new TeamMemberDTO();
        updatedTeamMember.setId(99);
        updatedTeamMember.setTeamId(1);
        updatedTeamMember.setUserId(2);
        updatedTeamMember.setIsActive(false);

        // Act & Assert
        mockMvc.perform(put("/database/team-members/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTeamMember)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteTeamMember_WhenExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(teamMemberService.deleteTeamMemberById(1L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/database/team-members/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteTeamMember_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(teamMemberService.deleteTeamMemberById(99L)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/database/team-members/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getTeamMemberById_WhenExists_ShouldReturnTeamMember() throws Exception {
        // Arrange
        Optional<TeamMemberDTO> optionalTeamMember = Optional.of(teamMemberDTO);
        when(teamMemberService.getTeamMemberById(1L)).thenReturn(optionalTeamMember);

        // Act & Assert
        mockMvc.perform(get("/database/team-members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("worker1"))
                .andExpect(jsonPath("$.teamName").value("Construction Team A"));
    }

    @Test
    public void getTeamMemberById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(teamMemberService.getTeamMemberById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/team-members/99"))
                .andExpect(status().isNotFound());
    }
}