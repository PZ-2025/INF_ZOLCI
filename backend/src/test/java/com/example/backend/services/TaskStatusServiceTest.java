package com.example.backend.services;

import com.example.backend.models.Task;
import com.example.backend.models.TaskStatus;
import com.example.backend.repository.TaskStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskStatusServiceTest {

    @Mock
    private TaskStatusRepository taskStatusRepository;

    @InjectMocks
    private TaskStatusService taskStatusService;

    private TaskStatus startedStatus;
    private TaskStatus inProgressStatus;
    private TaskStatus completedStatus;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Inicjalizacja statusów testowych
        startedStatus = new TaskStatus();
        startedStatus.setId(1);
        startedStatus.setName("Rozpoczęte");
        startedStatus.setProgressMin(0);
        startedStatus.setProgressMax(30);
        startedStatus.setDisplayOrder(1);

        inProgressStatus = new TaskStatus();
        inProgressStatus.setId(2);
        inProgressStatus.setName("W toku");
        inProgressStatus.setProgressMin(31);
        inProgressStatus.setProgressMax(99);
        inProgressStatus.setDisplayOrder(2);

        completedStatus = new TaskStatus();
        completedStatus.setId(3);
        completedStatus.setName("Zakończone");
        completedStatus.setProgressMin(100);
        completedStatus.setProgressMax(100);
        completedStatus.setDisplayOrder(3);
    }

    @Test
    void getAllTaskStatuses_ShouldReturnAllStatuses() {
        // Given
        List<TaskStatus> expectedStatuses = Arrays.asList(startedStatus, inProgressStatus, completedStatus);
        when(taskStatusRepository.findAll()).thenReturn(expectedStatuses);

        // When
        List<TaskStatus> actualStatuses = taskStatusService.getAllTaskStatuses();

        // Then
        assertEquals(expectedStatuses.size(), actualStatuses.size());
        assertEquals(expectedStatuses.get(0).getId(), actualStatuses.get(0).getId());
        assertEquals(expectedStatuses.get(1).getId(), actualStatuses.get(1).getId());
        assertEquals(expectedStatuses.get(2).getId(), actualStatuses.get(2).getId());
        verify(taskStatusRepository, times(1)).findAll();
    }

    @Test
    void getAllTaskStatusesSorted_ShouldReturnSortedStatuses() {
        // Given
        List<TaskStatus> sortedStatuses = Arrays.asList(startedStatus, inProgressStatus, completedStatus);
        when(taskStatusRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(sortedStatuses);

        // When
        List<TaskStatus> result = taskStatusService.getAllTaskStatusesSorted();

        // Then
        assertEquals(sortedStatuses.size(), result.size());
        assertEquals(sortedStatuses.get(0).getId(), result.get(0).getId());
        assertEquals(sortedStatuses.get(1).getId(), result.get(1).getId());
        assertEquals(sortedStatuses.get(2).getId(), result.get(2).getId());
        verify(taskStatusRepository, times(1)).findAllByOrderByDisplayOrderAsc();
    }

    @Test
    void getTaskStatusById_WhenStatusExists_ShouldReturnStatus() {
        // Given
        when(taskStatusRepository.findById(1)).thenReturn(Optional.of(startedStatus));

        // When
        Optional<TaskStatus> result = taskStatusService.getTaskStatusById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(startedStatus.getId(), result.get().getId());
        assertEquals(startedStatus.getName(), result.get().getName());
        verify(taskStatusRepository, times(1)).findById(1);
    }

    @Test
    void getTaskStatusById_WhenStatusDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(taskStatusRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<TaskStatus> result = taskStatusService.getTaskStatusById(99);

        // Then
        assertFalse(result.isPresent());
        verify(taskStatusRepository, times(1)).findById(99);
    }

    @Test
    void getTaskStatusByName_WhenStatusExists_ShouldReturnStatus() {
        // Given
        when(taskStatusRepository.findByName("Zakończone")).thenReturn(Optional.of(completedStatus));

        // When
        Optional<TaskStatus> result = taskStatusService.getTaskStatusByName("Zakończone");

        // Then
        assertTrue(result.isPresent());
        assertEquals(completedStatus.getId(), result.get().getId());
        assertEquals("Zakończone", result.get().getName());
        verify(taskStatusRepository, times(1)).findByName("Zakończone");
    }

    @Test
    void saveTaskStatus_ShouldSaveAndReturnStatus() {
        // Given
        TaskStatus newStatus = new TaskStatus();
        newStatus.setName("Anulowane");
        newStatus.setProgressMin(0);
        newStatus.setProgressMax(0);
        newStatus.setDisplayOrder(4);

        when(taskStatusRepository.save(any(TaskStatus.class))).thenReturn(newStatus);

        // When
        TaskStatus savedStatus = taskStatusService.saveTaskStatus(newStatus);

        // Then
        assertEquals(newStatus.getName(), savedStatus.getName());
        assertEquals(newStatus.getProgressMin(), savedStatus.getProgressMin());
        assertEquals(newStatus.getProgressMax(), savedStatus.getProgressMax());
        assertEquals(newStatus.getDisplayOrder(), savedStatus.getDisplayOrder());
        verify(taskStatusRepository, times(1)).save(newStatus);
    }

    @Test
    void createTaskStatus_ShouldCreateAndReturnStatus() {
        // Given
        String name = "Anulowane";
        Integer progressMin = 0;
        Integer progressMax = 0;
        Integer displayOrder = 4;

        when(taskStatusRepository.save(any(TaskStatus.class))).thenAnswer(invocation -> {
            TaskStatus saved = invocation.getArgument(0);
            saved.setId(4); // Symulacja nadania ID przez bazę danych
            return saved;
        });

        // When
        TaskStatus createdStatus = taskStatusService.createTaskStatus(name, progressMin, progressMax, displayOrder);

        // Then
        assertNotNull(createdStatus.getId());
        assertEquals(name, createdStatus.getName());
        assertEquals(progressMin, createdStatus.getProgressMin());
        assertEquals(progressMax, createdStatus.getProgressMax());
        assertEquals(displayOrder, createdStatus.getDisplayOrder());
        verify(taskStatusRepository, times(1)).save(any(TaskStatus.class));
    }

    @Test
    void deleteTaskStatus_WithNoTasks_ShouldDeleteStatus() {
        // Given
        TaskStatus statusWithNoTasks = new TaskStatus();
        statusWithNoTasks.setId(1);
        statusWithNoTasks.setName("Rozpoczęte");
        statusWithNoTasks.setTasks(new HashSet<>());

        when(taskStatusRepository.findById(1)).thenReturn(Optional.of(statusWithNoTasks));
        doNothing().when(taskStatusRepository).deleteById(1);

        // When & Then
        assertDoesNotThrow(() -> taskStatusService.deleteTaskStatus(1));
        verify(taskStatusRepository, times(1)).findById(1);
        verify(taskStatusRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteTaskStatus_WithTasks_ShouldThrowException() {
        // Given
        TaskStatus statusWithTasks = new TaskStatus();
        statusWithTasks.setId(1);
        statusWithTasks.setName("Rozpoczęte");

        Set<Task> tasks = new HashSet<>();
        tasks.add(new Task());
        statusWithTasks.setTasks(tasks);

        when(taskStatusRepository.findById(1)).thenReturn(Optional.of(statusWithTasks));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> taskStatusService.deleteTaskStatus(1));

        assertTrue(exception.getMessage().contains("Nie można usunąć statusu"));
        verify(taskStatusRepository, times(1)).findById(1);
        verify(taskStatusRepository, never()).deleteById(anyInt());
    }

    @Test
    void existsByName_WhenStatusExists_ShouldReturnTrue() {
        // Given
        when(taskStatusRepository.findByName("Zakończone")).thenReturn(Optional.of(completedStatus));

        // When
        boolean result = taskStatusService.existsByName("Zakończone");

        // Then
        assertTrue(result);
        verify(taskStatusRepository, times(1)).findByName("Zakończone");
    }

    @Test
    void existsByName_WhenStatusDoesNotExist_ShouldReturnFalse() {
        // Given
        when(taskStatusRepository.findByName("Nieistniejący")).thenReturn(Optional.empty());

        // When
        boolean result = taskStatusService.existsByName("Nieistniejący");

        // Then
        assertFalse(result);
        verify(taskStatusRepository, times(1)).findByName("Nieistniejący");
    }

    @Test
    void updateDisplayOrder_WhenStatusExists_ShouldUpdateAndReturnStatus() {
        // Given
        when(taskStatusRepository.findById(1)).thenReturn(Optional.of(startedStatus));
        when(taskStatusRepository.save(any(TaskStatus.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Integer newDisplayOrder = 5;

        // When
        Optional<TaskStatus> result = taskStatusService.updateDisplayOrder(1, newDisplayOrder);

        // Then
        assertTrue(result.isPresent());
        assertEquals(newDisplayOrder, result.get().getDisplayOrder());
        verify(taskStatusRepository, times(1)).findById(1);
        verify(taskStatusRepository, times(1)).save(any(TaskStatus.class));
    }

    @Test
    void updateDisplayOrder_WhenStatusDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(taskStatusRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<TaskStatus> result = taskStatusService.updateDisplayOrder(99, 5);

        // Then
        assertFalse(result.isPresent());
        verify(taskStatusRepository, times(1)).findById(99);
        verify(taskStatusRepository, never()).save(any(TaskStatus.class));
    }
}