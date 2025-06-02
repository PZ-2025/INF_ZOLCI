package com.example.backend.services;

import com.example.backend.dto.TeamDTO;
import com.example.backend.dto.UserDTO;
import com.example.backend.dto.UserResponseDTO;
import com.example.backend.models.Team;
import com.example.backend.models.User;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.TeamMemberRepository;
import com.example.backend.repository.TeamRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serwis obsługujący operacje dla encji {@link Team}.
 */
@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;

    /**
     * Konstruktor wstrzykujący zależności.
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

    /**
     * Mapuje encję Team na obiekt DTO.
     */
    public TeamDTO mapToDTO(Team team) {
        if (team == null) return null;

        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setManagerId(team.getManager().getId());
        dto.setIsActive(team.getIsActive());

        return dto;
    }

    /**
     * Mapuje obiekt DTO na encję Team.
     */
    public Team mapToEntity(TeamDTO dto) {
        if (dto == null) return null;

        Team team = new Team();
        team.setId(dto.getId());
        team.setName(dto.getName());
        team.setIsActive(dto.getIsActive());

        // Pobieramy DTO menedżera, a następnie konwertujemy je na encję
        UserDTO managerDto = userService.getUserById(dto.getManagerId())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono menedżera o ID: " + dto.getManagerId()));

        // Konwersja UserDTO na User
        User manager = new User();
        manager.setId(managerDto.getId());
        manager.setUsername(managerDto.getUsername());
        manager.setEmail(managerDto.getEmail());
        manager.setFirstName(managerDto.getFirstName());
        manager.setLastName(managerDto.getLastName());
        manager.setPhone(managerDto.getPhone());
        manager.setRole(managerDto.getRole());
        manager.setIsActive(managerDto.getIsActive());
        manager.setCreatedAt(managerDto.getCreatedAt());
        manager.setLastLogin(managerDto.getLastLogin());

        team.setManager(manager);

        return team;
    }

    /**
     * Pobiera wszystkie zespoły jako DTO.
     */
    public List<TeamDTO> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera zespół po ID jako DTO.
     */
    public Optional<TeamDTO> getTeamById(Integer id) {
        return teamRepository.findById(id)
                .map(this::mapToDTO);
    }

    /**
     * Pobiera encję zespołu po ID.
     */
    public Optional<Team> getTeamEntityById(Integer id) {
        return teamRepository.findById(id);
    }

    /**
     * Pobiera aktywne zespoły jako DTO.
     */
    public List<TeamDTO> getActiveTeams() {
        return teamRepository.findByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera zespoły o określonym statusie aktywności jako DTO.
     */
    public List<TeamDTO> getTeamsByActiveStatus(boolean isActive) {
        return teamRepository.findByIsActive(isActive).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera zespół po nazwie jako DTO.
     */
    public Optional<TeamDTO> getTeamByName(String teamName) {
        return teamRepository.findByName(teamName)
                .map(this::mapToDTO);
    }

    /**
     * Aktywuje zespół o podanym ID.
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
     */
    public Optional<TeamDTO> deactivateTeam(Integer id) {
        return teamRepository.findById(id)
                .map(team -> {
                    team.setIsActive(false);
                    return mapToDTO(teamRepository.save(team));
                });
    }

    /**
     * Zapisuje nowy zespół.
     */
    public TeamDTO saveTeam(TeamDTO teamDTO) {
        Team team = mapToEntity(teamDTO);
        Team savedTeam = teamRepository.save(team);
        return mapToDTO(savedTeam);
    }

    /**
     * Aktualizuje istniejący zespół.
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
     */
    public boolean existsById(Integer id) {
        return teamRepository.existsById(id);
    }

    /**
     * Pobiera dane kierownika zespołu jako DTO.
     *
     * @param teamId Identyfikator zespołu
     * @return Opcjonalne dane kierownika zespołu, jeśli zespół istnieje
     */
    public Optional<UserResponseDTO> getTeamManager(Integer teamId) {
        return teamRepository.findById(teamId)
                .map(team -> mapUserToResponseDTO(team.getManager()));
    }

    private UserResponseDTO mapUserToResponseDTO(User user) {
        if (user == null) return null;

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());

        return dto;
    }
}