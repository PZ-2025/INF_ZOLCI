package com.example.backend.services;

import com.example.backend.dto.TaskHistoryDTO;
import com.example.backend.models.Task;
import com.example.backend.models.TaskHistory;
import com.example.backend.models.User;
import com.example.backend.repository.TaskHistoryRepository;
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
class TaskHistoryServiceTest {

    @Mock
    private TaskHistoryRepository taskHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskHistoryService taskHistoryService;

    private Task task;
    private User user;
    private TaskHistory taskHistory;
    private TaskHistoryDTO taskHistoryDTO;

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

        taskHistory = new TaskHistory();
        taskHistory.setId(1);
        taskHistory.setTask(task);
        taskHistory.setChangedBy(1);
        taskHistory.setFieldName("status");
        taskHistory.setOldValue("In Progress");
        taskHistory.setNewValue("Completed");
        taskHistory.setChangedAt(LocalDateTime.now());

        taskHistoryDTO = new TaskHistoryDTO();
        taskHistoryDTO.setId(1);
        taskHistoryDTO.setTaskId(1);
        taskHistoryDTO.setChangedById(1);
        taskHistoryDTO.setFieldName("status");
        taskHistoryDTO.setOldValue("In Progress");
        taskHistoryDTO.setNewValue("Completed");
        taskHistoryDTO.setChangedAt(LocalDateTime.now());
        taskHistoryDTO.setChangedByUsername("worker1");
        taskHistoryDTO.setChangedByFullName("John Doe");
    }

    @Test
    void getAllTaskHistory_ShouldReturnAllHistory() {
        // Arrange
        when(taskHistoryRepository.findAll()).thenReturn(Arrays.asList(taskHistory));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        // Act
        List<TaskHistoryDTO> result = taskHistoryService.getAllTaskHistory();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("status", result.get(0).getFieldName());
        assertEquals("In Progress", result.get(0).getOldValue());
        assertEquals("Completed", result.get(0).getNewValue());
    }

    @Test
    void getTaskHistoryById_WhenExists_ShouldReturnHistory() {
        // Arrange
        when(taskHistoryRepository.findById(1)).thenReturn(Optional.of(taskHistory));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        // Act
        Optional<TaskHistoryDTO> result = taskHistoryService.getTaskHistoryById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("status", result.get().getFieldName());
        assertEquals("In Progress", result.get().getOldValue());
        assertEquals("Completed", result.get().getNewValue());
    }

    @Test
    void getTaskHistoryById_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(taskHistoryRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<TaskHistoryDTO> result = taskHistoryService.getTaskHistoryById(999);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getHistoryByTask_ShouldReturnHistoryForTask() {
        // Arrange
        when(taskHistoryRepository.findByTask(task)).thenReturn(Arrays.asList(taskHistory));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        // Act
        List<TaskHistoryDTO> result = taskHistoryService.getHistoryByTask(task);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("status", result.get(0).getFieldName());
        assertEquals(1, result.get(0).getTaskId());
    }

    @Test
    void getHistoryByUser_ShouldReturnHistoryByUser() {
        // Arrange
        when(taskHistoryRepository.findByChangedBy(1)).thenReturn(Arrays.asList(taskHistory));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        // Act
        List<TaskHistoryDTO> result = taskHistoryService.getHistoryByUser(user);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("status", result.get(0).getFieldName());
        assertEquals(1, result.get(0).getChangedById());
    }

    @Test
    void saveTaskHistory_ShouldSaveAndReturnHistoryDTO() {
        // Arrange
        when(taskHistoryRepository.save(any(TaskHistory.class))).thenReturn(taskHistory);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        // Act
        TaskHistoryDTO result = taskHistoryService.saveTaskHistory(taskHistoryDTO, task);

        // Assert
        assertNotNull(result);
        assertEquals("status", result.getFieldName());
        assertEquals("In Progress", result.getOldValue());
        assertEquals("Completed", result.getNewValue());
        assertEquals(1, result.getTaskId());
        verify(taskHistoryRepository).save(any(TaskHistory.class));
    }

    @Test
    void saveTaskHistory_WithNullChangedAt_ShouldSetCurrentDateTime() {
        // Arrange
        when(taskHistoryRepository.save(any(TaskHistory.class))).thenAnswer(invocation -> {
            TaskHistory savedHistory = invocation.getArgument(0);
            savedHistory.setChangedAt(LocalDateTime.now());
            return savedHistory;
        });
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        // Set changedAt to null
        taskHistoryDTO.setChangedAt(null);

        // Act
        TaskHistoryDTO result = taskHistoryService.saveTaskHistory(taskHistoryDTO, task);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getChangedAt()); // Should be set automatically
    }

    @Test
    void logTaskChange_ShouldCreateAndSaveHistoryEntry() {
        // Arrange
        when(taskHistoryRepository.save(any(TaskHistory.class))).thenReturn(taskHistory);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        // Act
        TaskHistoryDTO result = taskHistoryService.logTaskChange(
                task, user, "status", "In Progress", "Completed");

        // Assert
        assertNotNull(result);
        assertEquals("status", result.getFieldName());
        assertEquals("In Progress", result.getOldValue());
        assertEquals("Completed", result.getNewValue());
        assertEquals(1, result.getTaskId());
        verify(taskHistoryRepository).save(any(TaskHistory.class));
    }

    @Test
    void deleteTaskHistory_ShouldCallRepositoryDelete() {
        // Act
        taskHistoryService.deleteTaskHistory(1);

        // Assert
        verify(taskHistoryRepository).deleteById(1);
    }

    @Test
    void deleteAllHistoryForTask_ShouldDeleteMatchingHistory() {
        // Arrange
        List<TaskHistory> history = Arrays.asList(taskHistory);
        when(taskHistoryRepository.findByTask(task)).thenReturn(history);

        // Act
        int result = taskHistoryService.deleteAllHistoryForTask(task);

        // Assert
        assertEquals(1, result); // Should return the number of deleted entries
        verify(taskHistoryRepository).findByTask(task);
        verify(taskHistoryRepository).deleteAll(history);
    }

    @Test
    void deleteAllHistoryForTask_WhenNoHistory_ShouldReturnZero() {
        // Arrange
        when(taskHistoryRepository.findByTask(task)).thenReturn(Arrays.asList());

        // Act
        int result = taskHistoryService.deleteAllHistoryForTask(task);

        // Assert
        assertEquals(0, result);
        verify(taskHistoryRepository).findByTask(task);
        verify(taskHistoryRepository).deleteAll(Arrays.asList());
    }
}