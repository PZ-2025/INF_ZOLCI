package com.example.database.repository;

import com.example.database.models.Team;
import com.example.database.models.TeamMember;
import com.example.database.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Integer> {
    List<TeamMember> findByUser(User user);
    List<TeamMember> findByTeam(Team team);
    List<TeamMember> findByTeamAndIsActive(Team team, boolean isActive);
}