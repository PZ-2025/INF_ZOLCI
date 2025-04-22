package com.example.backend.controllers;

import com.example.backend.dto.TeamMemberDTO;
import com.example.backend.models.Team;
import com.example.backend.models.TeamMember;
import com.example.backend.models.User;
import com.example.backend.services.TeamMemberService;
import com.example.backend.services.TeamService;
import com.example.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Kontroler REST dla encji {@link TeamMember}.
 *
 * Klasa obsługuje żądania HTTP związane z zarządzaniem członkami zespołu.
 *
 * @author YourName
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/database/team-members")
public class TeamMemberController {

    private final TeamMemberService teamMemberService;
    private final TeamService teamService;

    @Autowired
    public TeamMemberController(TeamMemberService teamMemberService, UserService userService, TeamService teamService) {
        this.teamMemberService = teamMemberService;
        this.teamService = teamService;
    }

    /**
     * Pobiera członków zespołu dla danego zespołu.
     */
    @GetMapping(value = "/team/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TeamMemberDTO>> getTeamMembersByTeam(@PathVariable Long teamId) {
        Team team = teamService.getTeamEntityById(Math.toIntExact(teamId))
                .orElseThrow(() -> new RuntimeException("Zespół o ID: " + teamId + " nie istnieje"));
        return ResponseEntity.ok(teamMemberService.getTeamMembersByTeam(team));
    }

    /**
     * Pobiera aktywnych członków zespołu dla danego zespołu.
     */
    @GetMapping(value = "/team/{teamId}/active/{isActive}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TeamMemberDTO>> getTeamMembersByTeamAndActiveStatus(
            @PathVariable Long teamId,
            @PathVariable boolean isActive) {
        Team team = teamService.getTeamEntityById(Math.toIntExact(teamId))
                .orElseThrow(() -> new RuntimeException("Zespół o ID: " + teamId + " nie istnieje"));
        return ResponseEntity.ok(teamMemberService.getTeamMembersByTeamAndActiveStatus(team, isActive));
    }

    /**
     * Tworzy nowego członka zespołu.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TeamMemberDTO> createTeamMember(@Valid @RequestBody TeamMemberDTO teamMemberDTO) {
        TeamMemberDTO createdTeamMember = teamMemberService.saveTeamMember(teamMemberDTO);
        return new ResponseEntity<>(createdTeamMember, HttpStatus.CREATED);
    }

    /**
     * Aktualizuje istniejącego członka zespołu.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TeamMemberDTO> updateTeamMember(
            @PathVariable Long id,
            @Valid @RequestBody TeamMemberDTO teamMemberDTO) {
        return teamMemberService.getTeamMemberById(id)
                .map(existingTeamMember -> {
                    // Zachowujemy ID
                    teamMemberDTO.setId(Math.toIntExact(id));

                    // Zapisujemy zaktualizowanego członka zespołu
                    TeamMemberDTO updatedMember = teamMemberService.saveTeamMember(teamMemberDTO);
                    return ResponseEntity.ok(updatedMember);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Usuwa członka zespołu.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeamMember(@PathVariable Long id) {
        boolean deleted = teamMemberService.deleteTeamMemberById(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Pobiera członka zespołu po ID.
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TeamMemberDTO> getTeamMemberById(@PathVariable Long id) {
        return teamMemberService.getTeamMemberById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}