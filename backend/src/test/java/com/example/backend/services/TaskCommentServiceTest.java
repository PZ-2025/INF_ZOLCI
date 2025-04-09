package com.example.backend.services;

import com.example.backend.models.Task;
import com.example.backend.models.TaskComment;
import com.example.backend.models.User;
import com.example.backend.repository.TaskCommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskCommentServiceTest {

    @Mock
    private TaskCommentRepository taskCommentRepository;

    @InjectMocks
    private TaskCommentService taskCommentService;

    private Task task;
    private User user;
    private TaskComment taskComment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Inicjalizacja obiektów testowych
        task = new Task();
        task.setId(1);
        task.setTitle("Test Task");

        user = new User();
        user.setId(1);
        user.setUsername("testuser");

        taskComment = new TaskComment();
        taskComment.setId(1);
        taskComment.setTask(task);
        taskComment.setUser(user);
        taskComment.setComment("Test comment");
        taskComment.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getAllTaskComments_ShouldReturnAllComments() {
        // Given
        List<TaskComment> expectedComments = Arrays.asList(taskComment);
        when(taskCommentRepository.findAll()).thenReturn(expectedComments);

        // When
        List<TaskComment> actualComments = taskCommentService.getAllTaskComments();

        // Then
        assertEquals(expectedComments.size(), actualComments.size());
        assertEquals(expectedComments.get(0).getId(), actualComments.get(0).getId());
        assertEquals(expectedComments.get(0).getComment(), actualComments.get(0).getComment());
        verify(taskCommentRepository, times(1)).findAll();
    }

    @Test
    void getTaskCommentById_WhenCommentExists_ShouldReturnComment() {
        // Given
        when(taskCommentRepository.findById(1)).thenReturn(Optional.of(taskComment));

        // When
        Optional<TaskComment> result = taskCommentService.getTaskCommentById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(taskComment.getId(), result.get().getId());
        assertEquals(taskComment.getComment(), result.get().getComment());
        verify(taskCommentRepository, times(1)).findById(1);
    }

    @Test
    void getTaskCommentById_WhenCommentDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(taskCommentRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<TaskComment> result = taskCommentService.getTaskCommentById(99);

        // Then
        assertFalse(result.isPresent());
        verify(taskCommentRepository, times(1)).findById(99);
    }

    @Test
    void getCommentsByTask_ShouldReturnTaskComments() {
        // Given
        List<TaskComment> expectedComments = Arrays.asList(taskComment);
        when(taskCommentRepository.findByTask(task)).thenReturn(expectedComments);

        // When
        List<TaskComment> actualComments = taskCommentService.getCommentsByTask(task);

        // Then
        assertEquals(expectedComments.size(), actualComments.size());
        assertEquals(expectedComments.get(0).getId(), actualComments.get(0).getId());
        verify(taskCommentRepository, times(1)).findByTask(task);
    }

    @Test
    void getCommentsByUser_ShouldReturnUserComments() {
        // Given
        List<TaskComment> expectedComments = Arrays.asList(taskComment);
        when(taskCommentRepository.findByUser(user)).thenReturn(expectedComments);

        // When
        List<TaskComment> actualComments = taskCommentService.getCommentsByUser(user);

        // Then
        assertEquals(expectedComments.size(), actualComments.size());
        assertEquals(expectedComments.get(0).getId(), actualComments.get(0).getId());
        verify(taskCommentRepository, times(1)).findByUser(user);
    }

    @Test
    void saveTaskComment_WithNewComment_ShouldSetCreatedAtAndSave() {
        // Given
        TaskComment newComment = new TaskComment();
        newComment.setTask(task);
        newComment.setUser(user);
        newComment.setComment("New comment");
        // Brak ustawienia createdAt, powinno być ustawione przez serwis

        when(taskCommentRepository.save(any(TaskComment.class))).thenReturn(newComment);

        // When
        TaskComment savedComment = taskCommentService.saveTaskComment(newComment);

        // Then
        assertNotNull(savedComment.getCreatedAt());
        verify(taskCommentRepository, times(1)).save(newComment);
    }

    @Test
    void saveTaskComment_WithExistingComment_ShouldNotChangeCreatedAt() {
        // Given
        LocalDateTime originalTime = taskComment.getCreatedAt();
        when(taskCommentRepository.save(taskComment)).thenReturn(taskComment);

        // When
        TaskComment savedComment = taskCommentService.saveTaskComment(taskComment);

        // Then
        assertEquals(originalTime, savedComment.getCreatedAt());
        verify(taskCommentRepository, times(1)).save(taskComment);
    }

    @Test
    void addCommentToTask_ShouldCreateAndSaveComment() {
        // Given
        when(taskCommentRepository.save(any(TaskComment.class))).thenAnswer(invocation -> {
            TaskComment saved = invocation.getArgument(0);
            saved.setId(2); // Symulacja nadania ID przez bazę danych
            return saved;
        });

        // When
        TaskComment newComment = taskCommentService.addCommentToTask(task, user, "New test comment");

        // Then
        assertNotNull(newComment.getId());
        assertEquals(task, newComment.getTask());
        assertEquals(user, newComment.getUser());
        assertEquals("New test comment", newComment.getComment());
        assertNotNull(newComment.getCreatedAt());
        verify(taskCommentRepository, times(1)).save(any(TaskComment.class));
    }

    @Test
    void deleteTaskComment_ShouldCallRepository() {
        // When
        taskCommentService.deleteTaskComment(1);

        // Then
        verify(taskCommentRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteAllCommentsForTask_ShouldDeleteAllAndReturnCount() {
        // Given
        List<TaskComment> commentsToDelete = Arrays.asList(taskComment, new TaskComment());
        when(taskCommentRepository.findByTask(task)).thenReturn(commentsToDelete);
        doNothing().when(taskCommentRepository).deleteAll(commentsToDelete);

        // When
        int deletedCount = taskCommentService.deleteAllCommentsForTask(task);

        // Then
        assertEquals(commentsToDelete.size(), deletedCount);
        verify(taskCommentRepository, times(1)).findByTask(task);
        verify(taskCommentRepository, times(1)).deleteAll(commentsToDelete);
    }
}