package com.example.database.repository;

import com.example.database.models.Task;
import com.example.database.models.TaskHistory;
import com.example.database.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Integer>
{
    List<TaskHistory> findByTask(Task task);
    List<TaskHistory> findByChangedBy(User user);
}