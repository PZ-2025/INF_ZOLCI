package com.example.backend.services;

import com.example.backend.dto.TeamMemberDTO;
import com.example.backend.models.Team;
import com.example.backend.models.TeamMember;
import com.example.backend.models.User;
import com.example.backend.repository.TeamMemberRepository;
import com.example.backend.repository.TeamRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serwis obsługujący operacje na encji {@link TeamMember}.
 */
@Service
@Transactional
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Autowired
    public TeamMemberService(TeamMemberRepository teamMemberRepository,
                             TeamRepository teamRepository,
                             UserRepository userRepository) {
        this.teamMemberRepository = teamMemberRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    /**
     * Mapuje encję TeamMember na obiekt DTO.
     */
    public TeamMemberDTO mapToDTO(TeamMember teamMember) {
        if (teamMember == null) return null;

        TeamMemberDTO dto = new TeamMemberDTO();
        dto.setId(teamMember.getId());
        dto.setTeamId(teamMember.getTeam().getId());
        dto.setUserId(teamMember.getUser().getId());
        dto.setJoinedAt(teamMember.getJoinedAt());
        dto.setIsActive(teamMember.getIsActive());

        // Dodatkowe pola dla prezentacji w UI
        dto.setTeamName(teamMember.getTeam().getName());
        dto.setUsername(teamMember.getUser().getUsername());
        dto.setUserFullName(teamMember.getUser().getFirstName() + " " + teamMember.getUser().getLastName());

        return dto;
    }

    /**
     * Mapuje obiekt DTO na encję TeamMember.
     */
    public TeamMember mapToEntity(TeamMemberDTO dto) {
        if (dto == null) return null;

        TeamMember teamMember = new TeamMember();
        teamMember.setId(dto.getId());

        // Pobieramy powiązane encje z bazy danych
        Team team = teamRepository.findById(dto.getTeamId())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono zespołu o ID: " + dto.getTeamId()));
        teamMember.setTeam(team);

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika o ID: " + dto.getUserId()));
        teamMember.setUser(user);

        teamMember.setJoinedAt(dto.getJoinedAt());
        teamMember.setIsActive(dto.getIsActive());

        return teamMember;
    }

    /**
     * Pobiera członków zespołu dla danego użytkownika jako DTO.
     */
    public List<TeamMemberDTO> getTeamMembersByUser(User user) {
        return teamMemberRepository.findByUser(user).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera członków zespołu dla danego zespołu jako DTO.
     */
    public List<TeamMemberDTO> getTeamMembersByTeam(Team team) {
        return teamMemberRepository.findByTeam(team).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera aktywnych członków zespołu dla danego zespołu jako DTO.
     */
    public List<TeamMemberDTO> getTeamMembersByTeamAndActiveStatus(Team team, boolean isActive) {
        return teamMemberRepository.findByTeamAndIsActive(team, isActive).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera członka zespołu dla danego użytkownika i zespołu jako DTO.
     */
    public Optional<TeamMemberDTO> getTeamMemberByUserAndTeam(User user, Team team) {
        return teamMemberRepository.findByUserAndTeam(user, team)
                .map(this::mapToDTO);
    }


    /**
     * Pobiera wszystkich członków danego zespołu jako DTO.
     */
    public List<TeamMemberDTO> getAllTeamMembersByTeam(Team team) {
        return teamMemberRepository.findAllByTeam(team).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Zapisuje członka zespołu i zwraca jako DTO.
     */
    public TeamMemberDTO saveTeamMember(TeamMemberDTO teamMemberDTO) {
        TeamMember teamMember = mapToEntity(teamMemberDTO);

        // Jeśli to nowy członek, ustaw datę dołączenia
        if (teamMember.getJoinedAt() == null) {
            teamMember.setJoinedAt(LocalDateTime.now());
        }

        // Jeśli isActive nie jest ustawione, domyślnie true
        if (teamMember.getIsActive() == null) {
            teamMember.setIsActive(true);
        }

        TeamMember savedMember = teamMemberRepository.save(teamMember);
        return mapToDTO(savedMember);
    }

    /**
     * Usuwa członka zespołu używając jego ID.
     */
    public boolean deleteTeamMemberById(Long id) {
        return teamMemberRepository.findById(id)
                .map(teamMember -> {
                    teamMemberRepository.delete(teamMember);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Pobiera członka zespołu po ID jako DTO.
     */
    public Optional<TeamMemberDTO> getTeamMemberById(Long id) {
        return teamMemberRepository.findById(id)
                .map(this::mapToDTO);
    }
}