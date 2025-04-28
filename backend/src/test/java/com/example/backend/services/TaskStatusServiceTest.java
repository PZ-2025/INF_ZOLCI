package com.example.backend.services;

import com.example.backend.dto.TaskStatusDTO;
import com.example.backend.models.Task;
import com.example.backend.models.TaskStatus;
import com.example.backend.repository.TaskStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskStatusServiceTest {

    @Mock
    private TaskStatusRepository taskStatusRepository;

    @InjectMocks
    private TaskStatusService taskStatusService;

    private TaskStatus taskStatus;
    private TaskStatusDTO taskStatusDTO;

    @BeforeEach
    void setUp() {
        // Initialize test data
        taskStatus = new TaskStatus();
        taskStatus.setId(1);
        taskStatus.setName("In Progress");
        taskStatus.setProgressMin(30);
        taskStatus.setProgressMax(70);
        taskStatus.setDisplayOrder(2);
        taskStatus.setTasks(new HashSet<>());

        taskStatusDTO = new TaskStatusDTO();
        taskStatusDTO.setId(1);
        taskStatusDTO.setName("In Progress");
        taskStatusDTO.setProgressMin(30);
        taskStatusDTO.setProgressMax(70);
        taskStatusDTO.setDisplayOrder(2);
    }

    @Test
    void getAllTaskStatuses_ShouldReturnAllStatuses() {
        // Arrange
        when(taskStatusRepository.findAll()).thenReturn(Arrays.asList(taskStatus));

        // Act
        List<TaskStatusDTO> result = taskStatusService.getAllTaskStatuses();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("In Progress", result.get(0).getName());
        assertEquals(30, result.get(0).getProgressMin());
        assertEquals(70, result.get(0).getProgressMax());
        assertEquals(2, result.get(0).getDisplayOrder());
    }

    @Test
    void getAllTaskStatusesSorted_ShouldReturnSortedStatuses() {
        // Arrange
        when(taskStatusRepository.findAllByOrderByDisplayOrderAsc())
                .thenReturn(Arrays.asList(taskStatus));

        // Act
        List<TaskStatusDTO> result = taskStatusService.getAllTaskStatusesSorted();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("In Progress", result.get(0).getName());
    }

    @Test
    void getTaskStatusById_WhenExists_ShouldReturnStatus() {
        // Arrange
        when(taskStatusRepository.findById(1)).thenReturn(Optional.of(taskStatus));

        // Act
        Optional<TaskStatusDTO> result = taskStatusService.getTaskStatusById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("In Progress", result.get().getName());
    }

    @Test
    void getTaskStatusById_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(taskStatusRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<TaskStatusDTO> result = taskStatusService.getTaskStatusById(999);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getTaskStatusByName_WhenExists_ShouldReturnStatus() {
        // Arrange
        when(taskStatusRepository.findByName("In Progress")).thenReturn(Optional.of(taskStatus));

        // Act
        Optional<TaskStatusDTO> result = taskStatusService.getTaskStatusByName("In Progress");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
    }

    @Test
    void getTaskStatusByName_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(taskStatusRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<TaskStatusDTO> result = taskStatusService.getTaskStatusByName("Nonexistent");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void saveTaskStatus_ShouldMapAndSaveStatus() {
        // Arrange
        when(taskStatusRepository.save(any(TaskStatus.class))).thenReturn(taskStatus);

        // Act
        TaskStatusDTO result = taskStatusService.saveTaskStatus(taskStatusDTO);

        // Assert
        assertNotNull(result);
        assertEquals("In Progress", result.getName());
        assertEquals(30, result.getProgressMin());
        assertEquals(70, result.getProgressMax());
        assertEquals(2, result.getDisplayOrder());
        verify(taskStatusRepository).save(any(TaskStatus.class));
    }

    @Test
    void createTaskStatus_ShouldCreateAndSaveNewStatus() {
        // Arrange
        when(taskStatusRepository.save(any(TaskStatus.class))).thenReturn(taskStatus);

        // Act
        TaskStatusDTO result = taskStatusService.createTaskStatus(
                "In Progress", 30, 70, 2);

        // Assert
        assertNotNull(result);
        assertEquals("In Progress", result.getName());
        assertEquals(30, result.getProgressMin());
        assertEquals(70, result.getProgressMax());
        assertEquals(2, result.getDisplayOrder());
        verify(taskStatusRepository).save(any(TaskStatus.class));
    }

    @Test
    void deleteTaskStatus_WhenStatusHasNoTasks_ShouldDelete() {
        // Arrange
        when(taskStatusRepository.findById(1)).thenReturn(Optional.of(taskStatus));

        // Act
        taskStatusService.deleteTaskStatus(1);

        // Assert
        verify(taskStatusRepository).deleteById(1);
    }

    @Test
    void deleteTaskStatus_WhenStatusHasTasks_ShouldThrowException() {
        // Arrange
        TaskStatus statusWithTasks = new TaskStatus();
        statusWithTasks.setId(1);
        statusWithTasks.setName("In Progress");

        Set<Task> tasks = new HashSet<>();
        tasks.add(new Task());
        statusWithTasks.setTasks(tasks);

        when(taskStatusRepository.findById(1)).thenReturn(Optional.of(statusWithTasks));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> taskStatusService.deleteTaskStatus(1));

        assertEquals("Nie można usunąć statusu, do którego przypisane są zadania", exception.getMessage());
        verify(taskStatusRepository, never()).deleteById(anyInt());
    }

    @Test
    void existsByName_WhenNameExists_ShouldReturnTrue() {
        // Arrange
        when(taskStatusRepository.findByName("In Progress")).thenReturn(Optional.of(taskStatus));

        // Act
        boolean result = taskStatusService.existsByName("In Progress");

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByName_WhenNameDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(taskStatusRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

        // Act
        boolean result = taskStatusService.existsByName("Nonexistent");

        // Assert
        assertFalse(result);
    }

    @Test
    void updateDisplayOrder_WhenStatusExists_ShouldUpdateAndReturnStatus() {
        // Arrange
        when(taskStatusRepository.findById(1)).thenReturn(Optional.of(taskStatus));

        // Configure save to update the display order
        when(taskStatusRepository.save(any(TaskStatus.class))).thenAnswer(invocation -> {
            TaskStatus savedStatus = invocation.getArgument(0);
            savedStatus.setDisplayOrder(3);
            return savedStatus;
        });

        // Act
        Optional<TaskStatusDTO> result = taskStatusService.updateDisplayOrder(1, 3);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(3, result.get().getDisplayOrder());
        verify(taskStatusRepository).save(any(TaskStatus.class));
    }

    @Test
    void updateDisplayOrder_WhenStatusDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(taskStatusRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<TaskStatusDTO> result = taskStatusService.updateDisplayOrder(999, 3);

        // Assert
        assertFalse(result.isPresent());
        verify(taskStatusRepository, never()).save(any(TaskStatus.class));
    }
}