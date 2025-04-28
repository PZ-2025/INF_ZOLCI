package  com.example.backend.controllers;

import com.example.backend.dto.TeamDTO;
import com.example.backend.models.Team;
import com.example.backend.models.TeamMember;
import com.example.backend.models.User;
import com.example.backend.services.TeamMemberService;
import com.example.backend.services.TeamService;
import com.example.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private final UserService userService;
    private final TeamService teamService;

    @Autowired
    public TeamMemberController(TeamMemberService teamMemberService, UserService userService, TeamService teamService) {
        this.teamMemberService = teamMemberService;
        this.userService = userService;
        this.teamService = teamService;
    }

    /**
     * Pobiera członków zespołu dla danego użytkownika.
     *
     * @param userId ID użytkownika
     * @return ResponseEntity z listą członków zespołu
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TeamMember>> getTeamMembersByUser(@PathVariable Long userId) {
        User user = userService.getUserById(Math.toIntExact(userId))
                .orElseThrow(() -> new RuntimeException("Użytkownik o ID: " + userId + " nie istnieje"));
        return ResponseEntity.ok(teamMemberService.getTeamMembersByUser(user));
    }

    /**
     * Pobiera członków zespołu dla danego zespołu.
     *
     * @param teamId ID zespołu
     * @return ResponseEntity z listą członków zespołu
     */
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<TeamMember>> getTeamMembersByTeam(@PathVariable Long teamId) {
        TeamDTO teamDTO = teamService.getTeamById(Math.toIntExact(teamId))
                .orElseThrow(() -> new RuntimeException("Zespół o ID: " + teamId + " nie istnieje"));
        Team team = teamService.getTeamEntityById(Math.toIntExact(teamId))
                .orElseThrow(() -> new RuntimeException("Zespół o ID: " + teamId + " nie istnieje"));
        return ResponseEntity.ok(teamMemberService.getTeamMembersByTeam(team));
    }

    /**
     * Pobiera aktywnych członków zespołu dla danego zespołu.
     *
     * @param teamId ID zespołu
     * @param isActive Status aktywności
     * @return ResponseEntity z listą członków zespołu
     */
    @GetMapping("/team/{teamId}/active/{isActive}")
    public ResponseEntity<List<TeamMember>> getTeamMembersByTeamAndActiveStatus(
            @PathVariable Long teamId,
            @PathVariable boolean isActive) {
        Team team = teamService.getTeamEntityById(Math.toIntExact(teamId))
                .orElseThrow(() -> new RuntimeException("Zespół o ID: " + teamId + " nie istnieje"));
        return ResponseEntity.ok(teamMemberService.getTeamMembersByTeamAndActiveStatus(team, isActive));
    }

    /**
     * Tworzy nowego członka zespołu.
     *
     * @param teamMember Dane nowego członka zespołu
     * @return ResponseEntity z utworzonym członkiem zespołu
     */
    @PostMapping
    public ResponseEntity<TeamMember> createTeamMember(@RequestBody TeamMember teamMember) {
        TeamMember createdTeamMember = teamMemberService.saveTeamMember(teamMember);
        return new ResponseEntity<>(createdTeamMember, HttpStatus.CREATED);
    }

    /**
     * Aktualizuje istniejącego członka zespołu.
     *
     * @param id ID członka zespołu
     * @param teamMemberDetails Dane do aktualizacji
     * @return ResponseEntity z zaktualizowanym członkiem zespołu
     */
    @PutMapping("/{id}")
    public ResponseEntity<TeamMember> updateTeamMember(
            @PathVariable Long id,
            @RequestBody TeamMember teamMemberDetails) {
        return teamMemberService.getTeamMemberById(id)
                .map(existingTeamMember -> {
                    // Aktualizacja tylko zmienionych pól
                    if (teamMemberDetails.getUser() != null) {
                        existingTeamMember.setUser(teamMemberDetails.getUser());
                    }
                    if (teamMemberDetails.getTeam() != null) {
                        existingTeamMember.setTeam(teamMemberDetails.getTeam());
                    }
                    if (teamMemberDetails.getIsActive() != null) {
                        existingTeamMember.setIsActive(teamMemberDetails.getIsActive());
                    }
                    // Dodaj aktualizację innych pól, jeśli istnieją

                    return ResponseEntity.ok(teamMemberService.saveTeamMember(existingTeamMember));
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Usuwa członka zespołu.
     *
     * @param id ID członka zespołu
     * @return ResponseEntity ze statusem operacji
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeamMember(@PathVariable Long id) {
        return teamMemberService.getTeamMemberById(id)
                .map(teamMember -> {
                    teamMemberService.deleteTeamMember(teamMember);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Pobiera członka zespołu po ID.
     *
     * @param id ID członka zespołu
     * @return ResponseEntity ze znalezionym członkiem zespołu
     */
    @GetMapping("/{id}")
    public ResponseEntity<TeamMember> getTeamMemberById(@PathVariable Long id) {
        return teamMemberService.getTeamMemberById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}