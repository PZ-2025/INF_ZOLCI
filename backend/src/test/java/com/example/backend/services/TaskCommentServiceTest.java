package com.example.backend.services;

import com.example.backend.dto.TaskCommentDTO;
import com.example.backend.models.Task;
import com.example.backend.models.TaskComment;
import com.example.backend.models.User;
import com.example.backend.repository.TaskCommentRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskCommentServiceTest {

    @Mock
    private TaskCommentRepository taskCommentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskCommentService taskCommentService;

    private Task task;
    private User user;
    private TaskComment taskComment;
    private TaskCommentDTO taskCommentDTO;

    @BeforeEach
    void setUp() {
        // Initialize test data
        task = new Task();
        task.setId(1);
        task.setTitle("Build Foundation");

        user = new User();
        user.setId(1);
        user.setUsername("worker1");
        user.setFirstName("John");
        user.setLastName("Doe");

        taskComment = new TaskComment();
        taskComment.setId(1);
        taskComment.setTask(task);
        taskComment.setUser(user);
        taskComment.setComment("Need more concrete for this job");
        taskComment.setCreatedAt(LocalDateTime.now());

        taskCommentDTO = new TaskCommentDTO();
        taskCommentDTO.setId(1);
        taskCommentDTO.setTaskId(1);
        taskCommentDTO.setUserId(1);
        taskCommentDTO.setComment("Need more concrete for this job");
        taskCommentDTO.setCreatedAt(LocalDateTime.now());
        taskCommentDTO.setUsername("worker1");
        taskCommentDTO.setUserFullName("John Doe");
    }

    @Test
    void getAllTaskComments_ShouldReturnAllComments() {
        // Arrange
        when(taskCommentRepository.findAll()).thenReturn(Arrays.asList(taskComment));

        // Act
        List<TaskCommentDTO> result = taskCommentService.getAllTaskComments();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Need more concrete for this job", result.get(0).getComment());
        assertEquals(1, result.get(0).getTaskId());
        assertEquals(1, result.get(0).getUserId());
    }

    @Test
    void getTaskCommentById_WhenExists_ShouldReturnComment() {
        // Arrange
        when(taskCommentRepository.findById(1)).thenReturn(Optional.of(taskComment));

        // Act
        Optional<TaskCommentDTO> result = taskCommentService.getTaskCommentById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Need more concrete for this job", result.get().getComment());
        assertEquals(1, result.get().getTaskId());
        assertEquals(1, result.get().getUserId());
    }

    @Test
    void getTaskCommentById_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(taskCommentRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<TaskCommentDTO> result = taskCommentService.getTaskCommentById(999);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getCommentsByTask_ShouldReturnCommentsForTask() {
        // Arrange
        when(taskCommentRepository.findByTask(task)).thenReturn(Arrays.asList(taskComment));

        // Act
        List<TaskCommentDTO> result = taskCommentService.getCommentsByTask(task);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Need more concrete for this job", result.get(0).getComment());
        assertEquals(1, result.get(0).getTaskId());
    }

    @Test
    void getCommentsByUser_ShouldReturnCommentsForUser() {
        // Arrange
        when(taskCommentRepository.findByUser(user)).thenReturn(Arrays.asList(taskComment));

        // Act
        List<TaskCommentDTO> result = taskCommentService.getCommentsByUser(user);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Need more concrete for this job", result.get(0).getComment());
        assertEquals(1, result.get(0).getUserId());
    }

    @Test
    void saveTaskComment_ShouldMapAndSaveComment() {
        // Arrange
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(taskCommentRepository.save(any(TaskComment.class))).thenReturn(taskComment);

        // Act
        TaskCommentDTO result = taskCommentService.saveTaskComment(taskCommentDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Need more concrete for this job", result.getComment());
        assertEquals(1, result.getTaskId());
        assertEquals(1, result.getUserId());
        verify(taskCommentRepository).save(any(TaskComment.class));
    }

    @Test
    void saveTaskComment_WithNullCreatedAt_ShouldSetCurrentDateTime() {
        // Arrange
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(taskCommentRepository.save(any(TaskComment.class))).thenAnswer(invocation -> {
            TaskComment savedComment = invocation.getArgument(0);
            savedComment.setCreatedAt(LocalDateTime.now());
            return savedComment;
        });

        // Set createdAt to null
        taskCommentDTO.setCreatedAt(null);

        // Act
        TaskCommentDTO result = taskCommentService.saveTaskComment(taskCommentDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCreatedAt()); // Should be set automatically
    }

    @Test
    void addCommentToTask_ShouldCreateAndSaveComment() {
        // Arrange
        when(taskCommentRepository.save(any(TaskComment.class))).thenReturn(taskComment);

        // Act
        TaskCommentDTO result = taskCommentService.addCommentToTask(
                task, user, "Need more concrete for this job");

        // Assert
        assertNotNull(result);
        assertEquals("Need more concrete for this job", result.getComment());
        assertEquals(1, result.getTaskId());
        assertEquals(1, result.getUserId());
        verify(taskCommentRepository).save(any(TaskComment.class));
    }

    @Test
    void deleteTaskComment_ShouldCallRepositoryDelete() {
        // Act
        taskCommentService.deleteTaskComment(1);

        // Assert
        verify(taskCommentRepository).deleteById(1);
    }

    @Test
    void deleteAllCommentsForTask_ShouldDeleteMatchingComments() {
        // Arrange
        List<TaskComment> comments = Arrays.asList(taskComment);
        when(taskCommentRepository.findByTask(task)).thenReturn(comments);

        // Act
        int result = taskCommentService.deleteAllCommentsForTask(task);

        // Assert
        assertEquals(1, result); // Should return the number of deleted comments
        verify(taskCommentRepository).findByTask(task);
        verify(taskCommentRepository).deleteAll(comments);
    }

    @Test
    void deleteAllCommentsForTask_WhenNoComments_ShouldReturnZero() {
        // Arrange
        when(taskCommentRepository.findByTask(task)).thenReturn(Arrays.asList());

        // Act
        int result = taskCommentService.deleteAllCommentsForTask(task);

        // Assert
        assertEquals(0, result);
        verify(taskCommentRepository).findByTask(task);
        verify(taskCommentRepository).deleteAll(Arrays.asList());
    }
}