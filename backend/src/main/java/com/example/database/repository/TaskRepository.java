package com.example.database.repository;

import com.example.database.models.Task;
import com.example.database.models.TaskStatus;
import com.example.database.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.database.models.Team;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Integer>
{
    List<Task> findByTeam(Team team);
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByCreatedBy(User user);
    List<Task> findByDeadlineBefore(LocalDate date);
    Optional<Task> findByTitle(String title);
}