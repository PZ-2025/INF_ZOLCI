package com.example.database.repository;

import com.example.database.models.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatus, Integer>
{
    Optional<TaskStatus> findByName(String name);
    List<TaskStatus> findAllByOrderByDisplayOrderAsc();
}