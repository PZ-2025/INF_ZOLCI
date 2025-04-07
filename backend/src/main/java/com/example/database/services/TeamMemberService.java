package com.example.database.services;

import com.example.database.models.Team;
import com.example.database.models.TeamMember;
import com.example.database.models.User;
import com.example.database.repository.TeamMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Serwis obsługujący operacje na encji {@link TeamMember}.
 *
 * Klasa zawiera logikę biznesową związaną z zarządzaniem członkami zespołu,
 * wykorzystując {@link TeamMemberRepository} do komunikacji z bazą danych.
 *
 * @author Karol
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@Transactional
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;

    @Autowired
    public TeamMemberService(TeamMemberRepository teamMemberRepository) {
        this.teamMemberRepository = teamMemberRepository;
    }

    /**
     * Pobiera wszystkich członków zespołu dla danego użytkownika.
     *
     * @param user Użytkownik
     * @return Lista członków zespołu
     */
    public List<TeamMember> getTeamMembersByUser(User user) {
        return teamMemberRepository.findByUser(user);
    }

    /**
     * Pobiera wszystkich członków danego zespołu.
     *
     * @param team Zespół
     * @return Lista członków zespołu
     */
    public List<TeamMember> getTeamMembersByTeam(Team team) {
        return teamMemberRepository.findByTeam(team);
    }

    /**
     * Pobiera wszystkich aktywnych lub nieaktywnych członków zespołu.
     *
     * @param team Zespół
     * @param isActive Status aktywności
     * @return Lista członków zespołu o określonym statusie aktywności
     */
    public List<TeamMember> getTeamMembersByTeamAndActiveStatus(Team team, boolean isActive) {
        return teamMemberRepository.findByTeamAndIsActive(team, isActive);
    }

    /**
     * Pobiera członka zespołu dla konkretnego użytkownika i zespołu.
     *
     * @param user Użytkownik
     * @param team Zespół
     * @return Optional zawierający członka zespołu, jeśli istnieje
     */
    public Optional<TeamMember> getTeamMemberByUserAndTeam(User user, Team team) {
        return teamMemberRepository.findByUserAndTeam(user, team);
    }

    /**
     * Pobiera wszystkich członków danego zespołu.
     *
     * @param team Zespół
     * @return Lista wszystkich członków zespołu
     */
    public List<TeamMember> getAllTeamMembersByTeam(Team team) {
        return teamMemberRepository.findAllByTeam(team);
    }

    /**
     * Zapisuje członka zespołu.
     *
     * @param teamMember Członek zespołu do zapisania
     * @return Zapisany członek zespołu
     */
    public TeamMember saveTeamMember(TeamMember teamMember) {
        return teamMemberRepository.save(teamMember);
    }

    /**
     * Usuwa członka zespołu.
     *
     * @param teamMember Członek zespołu do usunięcia
     */
    public void deleteTeamMember(TeamMember teamMember) {
        teamMemberRepository.delete(teamMember);
    }

    /**
     * Pobiera członka zespołu po identyfikatorze.
     *
     * @param id Identyfikator członka zespołu
     * @return Optional zawierający członka zespołu, jeśli istnieje
     */
    public Optional<TeamMember> getTeamMemberById(Long id) {
        return teamMemberRepository.findById(id);
    }
}