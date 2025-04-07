package com.example.database.services;

import com.example.database.models.Team;
import com.example.database.models.User;
import com.example.database.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Serwis obsługujący operacje dla encji {@link Team}.
 * <p>
 * Klasa ta zawiera logikę biznesową związaną z zespołami w systemie.
 * Implementuje operacje tworzenia, odczytu, aktualizacji i usuwania zespołów,
 * a także bardziej złożone operacje.
 *
 * @author Karol
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
public class TeamService {

    private final TeamRepository teamRepository;

    /**
     * Konstruktor wstrzykujący zależności.
     *
     * @param teamRepository Repozytorium zespołów
     */
    @Autowired
    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    /**
     * Pobiera wszystkie zespoły z bazy danych.
     *
     * @return Lista wszystkich zespołów
     */
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    /**
     * Pobiera zespół na podstawie jego identyfikatora.
     *
     * @param id Identyfikator zespołu
     * @return Opcjonalny zespół, jeśli istnieje
     */
    public Optional<Team> getTeamById(Integer id) {
        return teamRepository.findById(id);
    }

    /**
     * Zapisuje nowy zespół w bazie danych.
     *
     * @param team Zespół do zapisania
     * @return Zapisany zespół
     */
    public Team saveTeam(Team team) {
        return teamRepository.save(team);
    }

    /**
     * Aktualizuje istniejący zespół.
     *
     * @param team Zespół ze zaktualizowanymi danymi
     * @return Zaktualizowany zespół
     */
    public Team updateTeam(Team team) {
        return teamRepository.save(team);
    }

    /**
     * Usuwa zespół na podstawie jego identyfikatora.
     *
     * @param id Identyfikator zespołu do usunięcia
     */
    public void deleteTeam(Integer id) {
        teamRepository.deleteById(id);
    }

    /**
     * Pobiera zespoły zarządzane przez określonego menedżera.
     *
     * @param manager Menedżer zespołów
     * @return Lista zespołów zarządzanych przez podanego menedżera
     */
    public List<Team> getTeamsByManager(User manager) {
        return teamRepository.findByManager(manager);
    }

    /**
     * Pobiera zespoły o określonym statusie aktywności.
     *
     * @param isActive Status aktywności (true = aktywne, false = nieaktywne)
     * @return Lista zespołów o określonym statusie aktywności
     */
    public List<Team> getTeamsByActiveStatus(boolean isActive) {
        return teamRepository.findByIsActive(isActive);
    }

    /**
     * Pobiera wszystkie aktywne zespoły.
     *
     * @return Lista aktywnych zespołów
     */
    public List<Team> getActiveTeams() {
        return teamRepository.findByIsActiveTrue();
    }

    /**
     * Znajduje zespół na podstawie nazwy.
     *
     * @param teamName Nazwa zespołu
     * @return Opcjonalny zespół o podanej nazwie
     */
    public Optional<Object> getTeamByName(String teamName) {
        return teamRepository.findByName(teamName);
    }

    /**
     * Aktywuje zespół o podanym ID.
     *
     * @param id Identyfikator zespołu
     * @return Zaktualizowany zespół lub Optional.empty() jeśli zespół nie istnieje
     */
    public Optional<Team> activateTeam(Integer id) {
        return teamRepository.findById(id)
                .map(team -> {
                    team.setIsActive(true);
                    return teamRepository.save(team);
                });
    }

    /**
     * Dezaktywuje zespół o podanym ID.
     *
     * @param id Identyfikator zespołu
     * @return Zaktualizowany zespół lub Optional.empty() jeśli zespół nie istnieje
     */
    public Optional<Team> deactivateTeam(Integer id) {
        return teamRepository.findById(id)
                .map(team -> {
                    team.setIsActive(false);
                    return teamRepository.save(team);
                });
    }
}