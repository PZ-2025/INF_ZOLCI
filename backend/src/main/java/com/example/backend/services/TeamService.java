package com.example.backend.services;

import com.example.backend.models.Team;
import com.example.backend.models.User;
import com.example.backend.dto.TeamDTO;
import com.example.backend.repository.TeamRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.TeamMemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserService userService;
    private final TaskRepository taskRepository;
    private final TeamMemberRepository teamMemberRepository;

    /**
     * Konstruktor wstrzykujący zależności.
     *
     * @param teamRepository Repozytorium zespołów
     * @param userService Serwis użytkowników
     * @param taskRepository Repozytorium zadań
     * @param teamMemberRepository Repozytorium członków zespołu
     */
    @Autowired
    public TeamService(TeamRepository teamRepository, 
                       UserService userService, 
                       TaskRepository taskRepository, 
                       TeamMemberRepository teamMemberRepository) {
        this.teamRepository = teamRepository;
        this.userService = userService;
        this.taskRepository = taskRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    // Metoda pomocnicza do mapowania Entity -> DTO
    private TeamDTO mapToDTO(Team team) {
        if (team == null) return null;
        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setManagerId(team.getManager().getId());
        dto.setIsActive(team.getIsActive());
        return dto;
    }

    // Metoda pomocnicza do mapowania DTO -> Entity
    private Team mapToEntity(TeamDTO dto) {
        if (dto == null) return null;
        Team team = new Team();
        team.setId(dto.getId());
        team.setName(dto.getName());
        team.setIsActive(dto.getIsActive());

        User manager = userService.getUserById(dto.getManagerId())
            .orElseThrow(() -> new RuntimeException("Nie znaleziono menedżera o ID: " + dto.getManagerId()));
        team.setManager(manager);

        return team;
    }

    /**
     * Pobiera wszystkie zespoły z bazy danych.
     *
     * @return Lista wszystkich zespołów jako DTO
     */
    public List<TeamDTO> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera zespół na podstawie jego identyfikatora.
     *
     * @param id Identyfikator zespołu
     * @return Opcjonalny zespół jako DTO, jeśli istnieje
     */
    public Optional<TeamDTO> getTeamById(Integer id) {
        return teamRepository.findById(id)
                .map(this::mapToDTO);
    }

    /**
     * Pobiera wszystkie aktywne zespoły.
     *
     * @return Lista aktywnych zespołów jako DTO
     */
    public List<TeamDTO> getActiveTeams() {
        return teamRepository.findByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera zespoły o określonym statusie aktywności.
     *
     * @param isActive Status aktywności (true = aktywne, false = nieaktywne)
     * @return Lista zespołów o określonym statusie aktywności jako DTO
     */
    public List<TeamDTO> getTeamsByActiveStatus(boolean isActive) {
        return teamRepository.findByIsActive(isActive).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Znajduje zespół na podstawie nazwy.
     *
     * @param teamName Nazwa zespołu
     * @return Opcjonalny zespół o podanej nazwie jako DTO
     */
    public Optional<TeamDTO> getTeamByName(String teamName) {
        return teamRepository.findByName(teamName)
                .map(this::mapToDTO);
    }

    /**
     * Aktywuje zespół o podanym ID.
     *
     * @param id Identyfikator zespołu
     * @return Zaktualizowany zespół jako DTO lub Optional.empty() jeśli zespół nie istnieje
     */
    public Optional<TeamDTO> activateTeam(Integer id) {
        return teamRepository.findById(id)
                .map(team -> {
                    team.setIsActive(true);
                    return mapToDTO(teamRepository.save(team));
                });
    }

    /**
     * Dezaktywuje zespół o podanym ID.
     *
     * @param id Identyfikator zespołu
     * @return Zaktualizowany zespół jako DTO lub Optional.empty() jeśli zespół nie istnieje
     */
    public Optional<TeamDTO> deactivateTeam(Integer id) {
        return teamRepository.findById(id)
                .map(team -> {
                    team.setIsActive(false);
                    return mapToDTO(teamRepository.save(team));
                });
    }

    public Optional<Team> getTeamEntityById(Integer id) {
        return teamRepository.findById(id);
    }

    /**
     * Zapisuje nowy zespół.
     *
     * @param teamDTO Dane zespołu do zapisania
     * @return Zapisany zespół jako DTO
     */
    public TeamDTO saveTeam(TeamDTO teamDTO) {
        Team team = mapToEntity(teamDTO);
        Team savedTeam = teamRepository.save(team);
        return mapToDTO(savedTeam);
    }

    /**
     * Aktualizuje istniejący zespół.
     *
     * @param teamDTO Dane zespołu do aktualizacji
     * @return Zaktualizowany zespół jako DTO
     * @throws RuntimeException gdy zespół o podanym ID nie istnieje
     */
    public TeamDTO updateTeam(TeamDTO teamDTO) {
        if (teamDTO.getId() == null) {
            throw new RuntimeException("ID zespołu nie może być null przy aktualizacji");
        }
        
        // Sprawdź czy zespół istnieje
        teamRepository.findById(teamDTO.getId())
            .orElseThrow(() -> new RuntimeException("Nie znaleziono zespołu o ID: " + teamDTO.getId()));
        
        Team team = mapToEntity(teamDTO);
        Team updatedTeam = teamRepository.save(team);
        return mapToDTO(updatedTeam);
    }

    /**
     * Usuwa zespół o podanym ID.
     *
     * @param id ID zespołu do usunięcia
     * @throws RuntimeException gdy zespół o podanym ID nie istnieje
     */
    public void deleteTeam(Integer id) {
        Team team = teamRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Nie znaleziono zespołu o ID: " + id));

        // Najpierw usuń wszystkie zadania zespołu
        taskRepository.deleteAllByTeam(team);

        // Następnie usuń wszystkich członków zespołu
        teamMemberRepository.deleteAllByTeam(team);

        // Na końcu usuń sam zespół
        teamRepository.delete(team);
    }

    /**
     * Sprawdza czy zespół o podanym ID istnieje.
     *
     * @param id ID zespołu do sprawdzenia
     * @return true jeśli zespół istnieje, false w przeciwnym razie
     */
    public boolean existsById(Integer id) {
        return teamRepository.existsById(id);
    }
}