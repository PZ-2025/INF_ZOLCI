package com.example.database.repository;

import com.example.database.models.Task;
import com.example.database.models.TaskComment;
import com.example.database.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Integer>
{
    List<TaskComment> findByTask(Task task);
    List<TaskComment> findByUser(User user);
}