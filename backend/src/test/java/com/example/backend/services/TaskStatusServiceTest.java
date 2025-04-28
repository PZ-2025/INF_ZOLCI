package com.example.backend.services;

import com.example.backend.dto.TaskStatusDTO;
import com.example.backend.models.Task;
import com.example.backend.models.TaskStatus;
import com.example.backend.repository.TaskStatusRepository;
import com.example.backend.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskStatusServiceTest {

    @Mock
    private TaskStatusRepository taskStatusRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskStatusService taskStatusService;

    private TaskStatus startedStatus;
    private TaskStatus inProgressStatus;
    private TaskStatus completedStatus;
    private TaskStatusDTO startedStatusDTO;
    private TaskStatusDTO inProgressStatusDTO;
    private TaskStatusDTO completedStatusDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Ustawiamy defaultStatusId przez ReflectionTestUtils
        ReflectionTestUtils.setField(taskStatusService, "defaultStatusId", 1);

        // Inicjalizacja statusów testowych - encje
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

        // Inicjalizacja DTO
        startedStatusDTO = new TaskStatusDTO();
        startedStatusDTO.setId(1);
        startedStatusDTO.setName("Rozpoczęte");
        startedStatusDTO.setProgressMin(0);
        startedStatusDTO.setProgressMax(30);
        startedStatusDTO.setDisplayOrder(1);

        inProgressStatusDTO = new TaskStatusDTO();
        inProgressStatusDTO.setId(2);
        inProgressStatusDTO.setName("W toku");
        inProgressStatusDTO.setProgressMin(31);
        inProgressStatusDTO.setProgressMax(99);
        inProgressStatusDTO.setDisplayOrder(2);

        completedStatusDTO = new TaskStatusDTO();
        completedStatusDTO.setId(3);
        completedStatusDTO.setName("Zakończone");
        completedStatusDTO.setProgressMin(100);
        completedStatusDTO.setProgressMax(100);
        completedStatusDTO.setDisplayOrder(3);
    }

    @Test
    void getAllTaskStatuses_ShouldReturnAllStatusesAsDTO() {
        // Given
        when(taskStatusRepository.findAll())
            .thenReturn(Arrays.asList(startedStatus, inProgressStatus, completedStatus));

        // When
        List<TaskStatusDTO> result = taskStatusService.getAllTaskStatuses();

        // Then
        assertEquals(3, result.size());
        assertEquals("Rozpoczęte", result.get(0).getName());
        assertEquals("W toku", result.get(1).getName());
        assertEquals("Zakończone", result.get(2).getName());
        verify(taskStatusRepository).findAll();
    }

    @Test
    void deleteTaskStatus_WithTasks_ShouldMoveTasksToDefaultStatus() {
        // Given
        Set<Task> tasks = new HashSet<>();
        Task task = new Task();
        task.setId(1);
        task.setStatus(inProgressStatus); // Dodajemy powiązanie z pierwotnym statusem
        tasks.add(task);
        inProgressStatus.setTasks(tasks);

        when(taskStatusRepository.findById(2)).thenReturn(Optional.of(inProgressStatus));
        when(taskStatusRepository.findById(1)).thenReturn(Optional.of(startedStatus));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // When
        taskStatusService.deleteTaskStatus(2);

        // Then
        verify(taskRepository).save(task);
        verify(taskStatusRepository).delete(inProgressStatus);
        
        // Dodatkowa weryfikacja
        assertEquals(startedStatus, task.getStatus());
    }

    @Test
    void deleteTaskStatus_DefaultStatus_ShouldThrowException() {
        // Given
        when(taskStatusRepository.findById(1)).thenReturn(Optional.of(startedStatus));

        // When & Then
        assertThrows(IllegalStateException.class, () -> 
            taskStatusService.deleteTaskStatus(1));
    }
}