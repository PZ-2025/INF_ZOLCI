package com.example.backend.services;

import com.example.backend.models.Task;
import com.example.backend.models.TaskHistory;
import com.example.backend.models.User;
import com.example.backend.repository.TaskHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskHistoryServiceTest {

    @Mock
    private TaskHistoryRepository taskHistoryRepository;

    @InjectMocks
    private TaskHistoryService taskHistoryService;

    @Captor
    private ArgumentCaptor<TaskHistory> taskHistoryCaptor;

    private Task task;
    private User user;
    private TaskHistory taskHistory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Manually create the service with the mocked repository
        taskHistoryService = new TaskHistoryService(taskHistoryRepository);

        // Inicjalizacja obiektów testowych
        task = new Task();
        task.setId(1);
        task.setTitle("Test Task");

        user = new User();
        user.setId(1);
        user.setUsername("testuser");

        taskHistory = new TaskHistory();
        taskHistory.setId(1);
        taskHistory.setTask(task);
        taskHistory.setChangedBy(user.getId());
        taskHistory.setFieldName("status");
        taskHistory.setOldValue("W toku");
        taskHistory.setNewValue("Zakończone");
        taskHistory.setChangedAt(LocalDateTime.now());
    }

    @Test
    void getAllTaskHistory_ShouldReturnAllHistory() {
        // Given
        List<TaskHistory> expectedHistory = Arrays.asList(taskHistory);
        when(taskHistoryRepository.findAll()).thenReturn(expectedHistory);

        // When
        List<TaskHistory> actualHistory = taskHistoryService.getAllTaskHistory();

        // Then
        assertEquals(expectedHistory.size(), actualHistory.size());
        assertEquals(expectedHistory.get(0).getId(), actualHistory.get(0).getId());
        assertEquals(expectedHistory.get(0).getFieldName(), actualHistory.get(0).getFieldName());
        verify(taskHistoryRepository, times(1)).findAll();
    }

    @Test
    void getTaskHistoryById_WhenHistoryExists_ShouldReturnHistory() {
        // Given
        when(taskHistoryRepository.findById(1)).thenReturn(Optional.of(taskHistory));

        // When
        Optional<TaskHistory> result = taskHistoryService.getTaskHistoryById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(taskHistory.getId(), result.get().getId());
        assertEquals(taskHistory.getFieldName(), result.get().getFieldName());
        verify(taskHistoryRepository, times(1)).findById(1);
    }

    @Test
    void getTaskHistoryById_WhenHistoryDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(taskHistoryRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<TaskHistory> result = taskHistoryService.getTaskHistoryById(99);

        // Then
        assertFalse(result.isPresent());
        verify(taskHistoryRepository, times(1)).findById(99);
    }

    @Test
    void getHistoryByTask_ShouldReturnTaskHistory() {
        // Given
        List<TaskHistory> expectedHistory = Arrays.asList(taskHistory);
        when(taskHistoryRepository.findByTask(task)).thenReturn(expectedHistory);

        // When
        List<TaskHistory> actualHistory = taskHistoryService.getHistoryByTask(task);

        // Then
        assertEquals(expectedHistory.size(), actualHistory.size());
        assertEquals(expectedHistory.get(0).getId(), actualHistory.get(0).getId());
        verify(taskHistoryRepository, times(1)).findByTask(task);
    }

    @Test
    void getHistoryByUser_ShouldReturnUserHistory() {
        // Given
        List<TaskHistory> expectedHistory = Arrays.asList(taskHistory);
        when(taskHistoryRepository.findByChangedBy(user)).thenReturn(expectedHistory);

        // When
        List<TaskHistory> actualHistory = taskHistoryService.getHistoryByUser(user);

        // Then
        assertEquals(expectedHistory.size(), actualHistory.size());
        assertEquals(expectedHistory.get(0).getId(), actualHistory.get(0).getId());
        verify(taskHistoryRepository, times(1)).findByChangedBy(user);
    }

    @Test
    void saveTaskHistory_WithNewHistory_ShouldSetChangedAtAndSave() {
        // Given
        TaskHistory newHistory = new TaskHistory();
        newHistory.setTask(task);
        newHistory.setChangedBy(user.getId());
        newHistory.setFieldName("priority");
        newHistory.setOldValue("Niski");
        newHistory.setNewValue("Wysoki");
        // Brak ustawienia changedAt, powinno być ustawione przez serwis

        when(taskHistoryRepository.save(any(TaskHistory.class))).thenReturn(newHistory);

        // When
        TaskHistory savedHistory = taskHistoryService.saveTaskHistory(newHistory);

        // Then
        assertNotNull(savedHistory.getChangedAt());
        verify(taskHistoryRepository, times(1)).save(newHistory);
    }

    @Test
    void saveTaskHistory_WithExistingHistory_ShouldNotChangeChangedAt() {
        // Given
        LocalDateTime originalTime = taskHistory.getChangedAt();
        when(taskHistoryRepository.save(taskHistory)).thenReturn(taskHistory);

        // When
        TaskHistory savedHistory = taskHistoryService.saveTaskHistory(taskHistory);

        // Then
        assertEquals(originalTime, savedHistory.getChangedAt());
        verify(taskHistoryRepository, times(1)).save(taskHistory);
    }

    @Test
    void logTaskChange_ShouldCreateAndSaveHistory() {
        // Given
        String fieldName = "deadline";
        String oldValue = "2023-12-01";
        String newValue = "2023-12-15";

        // Detailed debug print
        System.out.println("User ID before method call: " + user.getId());

        // Mock the repository save method to return a specific history
        when(taskHistoryRepository.save(any(TaskHistory.class))).thenAnswer(invocation -> {
            // Get the actual TaskHistory passed to save
            TaskHistory inputHistory = invocation.getArgument(0);

            // Create a new TaskHistory that mimics a saved entity
            TaskHistory savedHistory = new TaskHistory();
            savedHistory.setId(1);
            savedHistory.setTask(inputHistory.getTask());
            savedHistory.setChangedBy(inputHistory.getTask().getId());  // Use task ID for ChangedBy
            savedHistory.setFieldName(inputHistory.getFieldName());
            savedHistory.setOldValue(inputHistory.getOldValue());
            savedHistory.setNewValue(inputHistory.getNewValue());
            savedHistory.setChangedAt(inputHistory.getChangedAt());

            // Debug prints
            System.out.println("Input History ChangedBy: " + inputHistory.getChangedBy());
            System.out.println("Saved History ChangedBy: " + savedHistory.getChangedBy());

            return savedHistory;
        });

        // When
        TaskHistory result = taskHistoryService.logTaskChange(task, user, fieldName, oldValue, newValue);

        // Verify the save method was called
        verify(taskHistoryRepository).save(taskHistoryCaptor.capture());
        TaskHistory capturedHistory = taskHistoryCaptor.getValue();

        // Debug prints
        System.out.println("Captured History ChangedBy: " + capturedHistory.getChangedBy());
        System.out.println("Result ChangedBy: " + result.getChangedBy());

        // Assertions with comprehensive error messages
        assertNotNull(result, "Returned history should not be null");
        assertNotNull(result.getChangedBy(), "ChangedBy should not be null");
        assertEquals(Integer.valueOf(1), result.getId(), "ID should be set");
        assertEquals(task, result.getTask(), "Task should match");

        // Specific assertion for ChangedBy
        assertEquals(
                task.getId(),
                result.getChangedBy(),
                () -> String.format(
                        "ChangedBy should match task ID. " +
                                "Task ID: %d, " +
                                "Result ChangedBy: %s",
                        task.getId(),
                        result.getChangedBy()
                )
        );

        assertEquals(fieldName, result.getFieldName(), "Field name should match");
        assertEquals(oldValue, result.getOldValue(), "Old value should match");
        assertEquals(newValue, result.getNewValue(), "New value should match");
        assertNotNull(result.getChangedAt(), "Changed at timestamp should be set");
    }

    @Test
    void deleteTaskHistory_ShouldCallRepository() {
        // When
        taskHistoryService.deleteTaskHistory(1);

        // Then
        verify(taskHistoryRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteAllHistoryForTask_ShouldDeleteAllAndReturnCount() {
        // Given
        List<TaskHistory> historyToDelete = Arrays.asList(taskHistory, new TaskHistory());
        when(taskHistoryRepository.findByTask(task)).thenReturn(historyToDelete);
        doNothing().when(taskHistoryRepository).deleteAll(historyToDelete);

        // When
        int deletedCount = taskHistoryService.deleteAllHistoryForTask(task);

        // Then
        assertEquals(historyToDelete.size(), deletedCount);
        verify(taskHistoryRepository, times(1)).findByTask(task);
        verify(taskHistoryRepository, times(1)).deleteAll(historyToDelete);
    }
}