package com.example.database.repository;

import com.example.database.models.Team;
import com.example.database.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Integer>
{
    List<Team> findByManager(User manager);
    List<Team> findByIsActive(boolean isActive);
}