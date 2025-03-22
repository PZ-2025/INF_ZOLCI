package com.example.database.repository;

import com.example.database.models.Team;
import com.example.database.models.TeamMember;
import com.example.database.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Integer> {
    List<TeamMember> findByUser(User user);
    List<TeamMember> findByTeam(Team team);
    List<TeamMember> findByTeamAndIsActive(Team team, boolean isActive);
    Optional<TeamMember> findByUserAndTeam(User user, Team team);

    List<TeamMember> findAllByTeam(Team team);
}