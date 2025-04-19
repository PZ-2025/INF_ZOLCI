package com.example.backend.controllers;

import com.example.backend.dto.TeamDTO;
import com.example.backend.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
@RequestMapping(value = "/database/teams", produces = MediaType.APPLICATION_JSON_VALUE)
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
    public ResponseEntity<List<TeamDTO>> getAllTeams() {
        List<TeamDTO> teams = teamService.getAllTeams();
        return new ResponseEntity<>(teams, HttpStatus.OK);
    }

    /**
     * Pobiera zespół na podstawie jego identyfikatora.
     *
     * @param id Identyfikator zespołu
     * @return Zespół lub status 404, jeśli nie istnieje
     */
    @GetMapping("/{id}")
    public ResponseEntity<TeamDTO> getTeamById(@PathVariable Integer id) {
        return teamService.getTeamById(id)
                .map(team -> new ResponseEntity<>(team, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Tworzy nowy zespół.
     *
     * @param teamDTO Dane nowego zespołu
     * @return Utworzony zespół
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TeamDTO> createTeam(@Valid @RequestBody TeamDTO teamDTO) {
        TeamDTO savedTeam = teamService.saveTeam(teamDTO);
        return new ResponseEntity<>(savedTeam, HttpStatus.CREATED);
    }

    /**
     * Aktualizuje istniejący zespół.
     *
     * @param id      Identyfikator zespołu
     * @param teamDTO Zaktualizowane dane zespołu
     * @return Zaktualizowany zespół lub status 404, jeśli nie istnieje
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TeamDTO> updateTeam(@PathVariable Integer id, @Valid @RequestBody TeamDTO teamDTO) {
        teamDTO.setId(id);
        return teamService.getTeamById(id)
                .map(existingTeam -> {
                    TeamDTO updatedTeam = teamService.updateTeam(teamDTO);
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
    public ResponseEntity<List<TeamDTO>> getActiveTeams() {
        List<TeamDTO> activeTeams = teamService.getActiveTeams();
        return new ResponseEntity<>(activeTeams, HttpStatus.OK);
    }

    /**
     * Pobiera zespoły o określonym statusie aktywności.
     *
     * @param isActive Status aktywności (true = aktywne, false = nieaktywne)
     * @return Lista zespołów o określonym statusie aktywności
     */
    @GetMapping("/status")
    public ResponseEntity<List<TeamDTO>> getTeamsByActiveStatus(@RequestParam boolean isActive) {
        List<TeamDTO> teams = teamService.getTeamsByActiveStatus(isActive);
        return new ResponseEntity<>(teams, HttpStatus.OK);
    }

    /**
     * Pobiera zespół na podstawie nazwy.
     *
     * @param name Nazwa zespołu
     * @return Zespół lub status 404, jeśli nie istnieje
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<TeamDTO> getTeamByName(@PathVariable String name) {
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
    public ResponseEntity<TeamDTO> activateTeam(@PathVariable Integer id) {
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
    public ResponseEntity<TeamDTO> deactivateTeam(@PathVariable Integer id) {
        return teamService.deactivateTeam(id)
                .map(team -> new ResponseEntity<>(team, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}