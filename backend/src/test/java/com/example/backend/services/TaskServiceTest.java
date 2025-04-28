package com.example.backend.services;

import com.example.backend.dto.TaskDTO;
import com.example.backend.models.*;
import com.example.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PriorityRepository priorityRepository;

    @Mock
    private TaskStatusRepository taskStatusRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private TaskDTO taskDTO;
    private Team team;
    private Priority priority;
    private TaskStatus status;
    private User user;

    @BeforeEach
    void setUp() {
        // Initialize test data
        team = new Team();
        team.setId(1);
        team.setName("Test Team");

        priority = new Priority();
        priority.setId(1);
        priority.setName("High");
        priority.setValue(3);

        status = new TaskStatus();
        status.setId(1);
        status.setName("In Progress");

        user = new User();
        user.setId(1);
        user.setUsername("testuser");

        task = new Task();
        task.setId(1);
        task.setTitle("Build Foundation");
        task.setDescription("Prepare and build the foundation for the house");
        task.setTeam(team);
        task.setPriority(priority);
        task.setStatus(status);
        task.setCreatedBy(user);
        task.setStartDate(LocalDate.now());
        task.setDeadline(LocalDate.now().plusDays(7));
        task.setCreatedAt(LocalDateTime.now());

        taskDTO = new TaskDTO();
        taskDTO.setId(1);
        taskDTO.setTitle("Build Foundation");
        taskDTO.setDescription("Prepare and build the foundation for the house");
        taskDTO.setTeamId(1);
        taskDTO.setPriorityId(1);
        taskDTO.setStatusId(1);
        taskDTO.setCreatedById(1);
        taskDTO.setStartDate(LocalDate.now());
        taskDTO.setDeadline(LocalDate.now().plusDays(7));
        taskDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getAllTasks_ShouldReturnListOfTasks() {
        // Arrange
        when(taskRepository.findAll()).thenReturn(Arrays.asList(task));

        // Act
        List<TaskDTO> result = taskService.getAllTasks();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Build Foundation", result.get(0).getTitle());
    }

    @Test
    void getTaskById_WhenTaskExists_ShouldReturnTask() {
        // Arrange
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));

        // Act
        Optional<TaskDTO> result = taskService.getTaskById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Build Foundation", result.get().getTitle());
    }

    @Test
    void getTaskById_WhenTaskDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(taskRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<TaskDTO> result = taskService.getTaskById(999);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getTaskEntityById_WhenTaskExists_ShouldReturnTaskEntity() {
        // Arrange
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));

        // Act
        Optional<Task> result = taskService.getTaskEntityById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Build Foundation", result.get().getTitle());
    }

    @Test
    void saveTask_ShouldCorrectlyMapAndPersistTask() {
        // Arrange
        when(teamRepository.findById(1)).thenReturn(Optional.of(team));
        when(priorityRepository.findById(1)).thenReturn(Optional.of(priority));
        when(taskStatusRepository.findById(1)).thenReturn(Optional.of(status));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        TaskDTO result = taskService.saveTask(taskDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Build Foundation", result.getTitle());
        assertEquals(1, result.getTeamId());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTask_WhenTaskExists_ShouldUpdateFields() {
        // Arrange
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));
        when(teamRepository.findById(1)).thenReturn(Optional.of(team));
        when(priorityRepository.findById(1)).thenReturn(Optional.of(priority));
        when(taskStatusRepository.findById(1)).thenReturn(Optional.of(status));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Update title in DTO
        taskDTO.setTitle("Updated Foundation Work");

        // Act
        TaskDTO result = taskService.updateTask(taskDTO);

        // Assert
        assertNotNull(result);
        // The mock is set to return the original task, but we're checking that the service
        // attempts to save a task with updated fields
        verify(taskRepository).save(argThat(updatedTask ->
                updatedTask.getTitle().equals("Updated Foundation Work")));
    }

    @Test
    void deleteTask_ShouldCallRepositoryDeleteMethod() {
        // Act
        taskService.deleteTask(1);

        // Assert
        verify(taskRepository).deleteById(1);
    }

    @Test
    void getTasksByTeam_ShouldReturnTasksForGivenTeam() {
        // Arrange
        when(taskRepository.findByTeam(team)).thenReturn(Arrays.asList(task));

        // Act
        List<TaskDTO> result = taskService.getTasksByTeam(team);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Build Foundation", result.get(0).getTitle());
    }

    @Test
    void getTasksByStatus_ShouldReturnTasksWithGivenStatus() {
        // Arrange
        when(taskRepository.findByStatus(status)).thenReturn(Arrays.asList(task));

        // Act
        List<TaskDTO> result = taskService.getTasksByStatus(status);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Build Foundation", result.get(0).getTitle());
    }

    @Test
    void getTasksCreatedBy_ShouldReturnTasksCreatedByUser() {
        // Arrange
        when(taskRepository.findByCreatedBy(user)).thenReturn(Arrays.asList(task));

        // Act
        List<TaskDTO> result = taskService.getTasksCreatedBy(user);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Build Foundation", result.get(0).getTitle());
    }

    @Test
    void getTasksWithDeadlineBefore_ShouldReturnTasksWithEarlierDeadline() {
        // Arrange
        LocalDate deadlineDate = LocalDate.now().plusDays(10);
        when(taskRepository.findByDeadlineBefore(deadlineDate)).thenReturn(Arrays.asList(task));

        // Act
        List<TaskDTO> result = taskService.getTasksWithDeadlineBefore(deadlineDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Build Foundation", result.get(0).getTitle());
    }

    @Test
    void getTaskByTitle_WhenTaskExists_ShouldReturnTask() {
        // Arrange
        when(taskRepository.findByTitle("Build Foundation")).thenReturn(Optional.of(task));

        // Act
        Optional<TaskDTO> result = taskService.getTaskByTitle("Build Foundation");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
    }

    @Test
    void getTaskByTitle_WhenTaskDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(taskRepository.findByTitle("Nonexistent Task")).thenReturn(Optional.empty());

        // Act
        Optional<TaskDTO> result = taskService.getTaskByTitle("Nonexistent Task");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getTasksByTeamId_ShouldReturnTasksForTeamId() {
        // Arrange
        when(taskRepository.findByTeamId(1)).thenReturn(Arrays.asList(task));

        // Act
        List<TaskDTO> result = taskService.getTasksByTeamId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Build Foundation", result.get(0).getTitle());
    }

    @Test
    void getTasksByStatusId_ShouldReturnTasksWithStatusId() {
        // Arrange
        when(taskRepository.findByStatusId(1)).thenReturn(Arrays.asList(task));

        // Act
        List<TaskDTO> result = taskService.getTasksByStatusId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Build Foundation", result.get(0).getTitle());
    }

    @Test
    void getTasksByPriorityId_ShouldReturnTasksWithPriorityId() {
        // Arrange
        when(taskRepository.findByPriorityId(1)).thenReturn(Arrays.asList(task));

        // Act
        List<TaskDTO> result = taskService.getTasksByPriorityId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Build Foundation", result.get(0).getTitle());
    }
}