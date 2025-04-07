package  com.example.backend.controllers;

import com.example.backend.models.Team;
import com.example.backend.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kontroler REST dla operacji na zespołach.
 * <p>
 * Klasa zapewnia endpoints do zarządzania zespołami w systemie,
 * w tym tworzenie, odczyt, aktualizację i usuwanie zespołów oraz
 * wyszukiwanie zespołów według różnych kryteriów.
 *
 * @author Karol
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/database/teams")
public class TeamController {

    private final TeamService teamService;

    /**
     * Konstruktor wstrzykujący zależności.
     *
     * @param teamService Serwis zespołów
     */
    @Autowired
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * Pobiera wszystkie zespoły.
     *
     * @return Lista wszystkich zespołów
     */
    @GetMapping
    public ResponseEntity<List<Team>> getAllTeams() {
        List<Team> teams = teamService.getAllTeams();
        return new ResponseEntity<>(teams, HttpStatus.OK);
    }

    /**
     * Pobiera zespół na podstawie jego identyfikatora.
     *
     * @param id Identyfikator zespołu
     * @return Zespół lub status 404, jeśli nie istnieje
     */
    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable Integer id) {
        return teamService.getTeamById(id)
                .map(team -> new ResponseEntity<>(team, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Tworzy nowy zespół.
     *
     * @param team Dane nowego zespołu
     * @return Utworzony zespół
     */
    @PostMapping
    public ResponseEntity<Team> createTeam(@RequestBody Team team) {
        Team savedTeam = teamService.saveTeam(team);
        return new ResponseEntity<>(savedTeam, HttpStatus.CREATED);
    }

    /**
     * Aktualizuje istniejący zespół.
     *
     * @param id   Identyfikator zespołu
     * @param team Zaktualizowane dane zespołu
     * @return Zaktualizowany zespół lub status 404, jeśli nie istnieje
     */
    @PutMapping("/{id}")
    public ResponseEntity<Team> updateTeam(@PathVariable Integer id, @RequestBody Team team) {
        return teamService.getTeamById(id)
                .map(existingTeam -> {
                    team.setId(id);
                    Team updatedTeam = teamService.updateTeam(team);
                    return new ResponseEntity<>(updatedTeam, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Usuwa zespół na podstawie jego identyfikatora.
     *
     * @param id Identyfikator zespołu do usunięcia
     * @return Status 204 po pomyślnym usunięciu lub 404, jeśli nie istnieje
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Integer id) {
        return teamService.getTeamById(id)
                .map(team -> {
                    teamService.deleteTeam(id);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Pobiera wszystkie aktywne zespoły.
     *
     * @return Lista aktywnych zespołów
     */
    @GetMapping("/active")
    public ResponseEntity<List<Team>> getActiveTeams() {
        List<Team> activeTeams = teamService.getActiveTeams();
        return new ResponseEntity<>(activeTeams, HttpStatus.OK);
    }

    /**
     * Pobiera zespoły o określonym statusie aktywności.
     *
     * @param isActive Status aktywności (true = aktywne, false = nieaktywne)
     * @return Lista zespołów o określonym statusie aktywności
     */
    @GetMapping("/status")
    public ResponseEntity<List<Team>> getTeamsByActiveStatus(@RequestParam boolean isActive) {
        List<Team> teams = teamService.getTeamsByActiveStatus(isActive);
        return new ResponseEntity<>(teams, HttpStatus.OK);
    }

    /**
     * Pobiera zespół na podstawie nazwy.
     *
     * @param name Nazwa zespołu
     * @return Zespół lub status 404, jeśli nie istnieje
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<Object> getTeamByName(@PathVariable String name) {
        return teamService.getTeamByName(name)
                .map(team -> new ResponseEntity<>(team, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Aktywuje zespół o podanym ID.
     *
     * @param id Identyfikator zespołu
     * @return Zaktualizowany zespół lub status 404, jeśli nie istnieje
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Team> activateTeam(@PathVariable Integer id) {
        return teamService.activateTeam(id)
                .map(team -> new ResponseEntity<>(team, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Dezaktywuje zespół o podanym ID.
     *
     * @param id Identyfikator zespołu
     * @return Zaktualizowany zespół lub status 404, jeśli nie istnieje
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Team> deactivateTeam(@PathVariable Integer id) {
        return teamService.deactivateTeam(id)
                .map(team -> new ResponseEntity<>(team, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}