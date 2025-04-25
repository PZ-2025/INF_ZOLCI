package com.example.backend.controllers;

import com.example.backend.dto.TeamMemberDTO;
import com.example.backend.models.Team;
import com.example.backend.services.TeamMemberService;
import com.example.backend.services.TeamService;
import com.example.backend.services.UserService;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
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

    @Mock
    private UserService userService;

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
        team.setName("Construction Team Alpha");

        teamMemberDTO = new TeamMemberDTO();
        teamMemberDTO.setId(1);
        teamMemberDTO.setTeamId(1);
        teamMemberDTO.setUserId(1);
        teamMemberDTO.setJoinedAt(LocalDateTime.now());
        teamMemberDTO.setIsActive(true);
        teamMemberDTO.setTeamName("Construction Team Alpha");
        teamMemberDTO.setUsername("worker1");
        teamMemberDTO.setUserFullName("John Doe");

        TeamMemberDTO teamMemberDTO2 = new TeamMemberDTO();
        teamMemberDTO2.setId(2);
        teamMemberDTO2.setTeamId(1);
        teamMemberDTO2.setUserId(2);
        teamMemberDTO2.setJoinedAt(LocalDateTime.now());
        teamMemberDTO2.setIsActive(true);
        teamMemberDTO2.setTeamName("Construction Team Alpha");
        teamMemberDTO2.setUsername("worker2");
        teamMemberDTO2.setUserFullName("Jane Smith");

        teamMemberDTOList = Arrays.asList(teamMemberDTO, teamMemberDTO2);
    }

    @Test
    public void getTeamMembersByTeam_WhenTeamExists_ShouldReturnMembers() throws Exception {
        // Arrange
        when(teamService.getTeamEntityById(1)).thenReturn(Optional.of(team));
        when(teamMemberService.getTeamMembersByTeam(any(Team.class))).thenReturn(teamMemberDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/team-members/team/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("worker1"))
                .andExpect(jsonPath("$[1].username").value("worker2"));
    }

    @Test
    public void getTeamMembersByTeam_WhenTeamDoesNotExist_ShouldThrowException() throws Exception {
        // Arrange
        when(teamService.getTeamEntityById(99)).thenThrow(new RuntimeException("Zespół o ID: 99 nie istnieje"));

        // Act & Assert
        mockMvc.perform(get("/database/team-members/team/99"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Zespół o ID: 99 nie istnieje")));
    }

    @Test
    public void getTeamMembersByTeamAndActiveStatus_WhenTeamExists_ShouldReturnFilteredMembers() throws Exception {
        // Arrange
        when(teamService.getTeamEntityById(1)).thenReturn(Optional.of(team));
        when(teamMemberService.getTeamMembersByTeamAndActiveStatus(any(Team.class), eq(true)))
                .thenReturn(teamMemberDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/team-members/team/1/active/true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("worker1"))
                .andExpect(jsonPath("$[1].username").value("worker2"));
    }

    @Test
    public void getTeamMembersByTeamAndActiveStatus_WithInactiveMembers_ShouldReturnFilteredMembers() throws Exception {
        // Arrange
        TeamMemberDTO inactiveMember = new TeamMemberDTO();
        inactiveMember.setId(3);
        inactiveMember.setTeamId(1);
        inactiveMember.setUserId(3);
        inactiveMember.setIsActive(false);
        inactiveMember.setUsername("inactiveworker");

        when(teamService.getTeamEntityById(1)).thenReturn(Optional.of(team));
        when(teamMemberService.getTeamMembersByTeamAndActiveStatus(any(Team.class), eq(false)))
                .thenReturn(Collections.singletonList(inactiveMember));

        // Act & Assert
        mockMvc.perform(get("/database/team-members/team/1/active/false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("inactiveworker"))
                .andExpect(jsonPath("$[0].isActive").value(false));
    }

    @Test
    public void createTeamMember_WithValidData_ShouldReturnCreatedMember() throws Exception {
        // Arrange
        when(teamMemberService.saveTeamMember(any(TeamMemberDTO.class))).thenReturn(teamMemberDTO);

        // Act & Assert
        mockMvc.perform(post("/database/team-members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamMemberDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.teamId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("worker1"));
    }

    @Test
    public void updateTeamMember_WhenMemberExists_ShouldReturnUpdatedMember() throws Exception {
        // Arrange
        when(teamMemberService.getTeamMemberById(1L)).thenReturn(Optional.of(teamMemberDTO));
        when(teamMemberService.saveTeamMember(any(TeamMemberDTO.class))).thenReturn(teamMemberDTO);

        // Update the team member
        TeamMemberDTO updatedMember = new TeamMemberDTO();
        updatedMember.setId(1);
        updatedMember.setTeamId(1);
        updatedMember.setUserId(1);
        updatedMember.setIsActive(false); // Deactivate the member

        // Act & Assert
        mockMvc.perform(put("/database/team-members/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedMember)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true)); // Mock returns original

        // Verify that the ID was set in the DTO before saving
        verify(teamMemberService).saveTeamMember(argThat(dto -> dto.getId() == 1));
    }

    @Test
    public void updateTeamMember_WhenMemberDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(teamMemberService.getTeamMemberById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/database/team-members/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamMemberDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteTeamMember_WhenMemberExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(teamMemberService.deleteTeamMemberById(1L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/database/team-members/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteTeamMember_WhenMemberDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(teamMemberService.deleteTeamMemberById(99L)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/database/team-members/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getTeamMemberById_WhenExists_ShouldReturnMember() throws Exception {
        // Arrange
        when(teamMemberService.getTeamMemberById(1L)).thenReturn(Optional.of(teamMemberDTO));

        // Act & Assert
        mockMvc.perform(get("/database/team-members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("worker1"));
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