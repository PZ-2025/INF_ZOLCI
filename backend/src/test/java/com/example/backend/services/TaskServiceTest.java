package com.example.backend.services;

import com.example.backend.models.Priority;
import com.example.backend.models.Task;
import com.example.backend.models.TaskStatus;
import com.example.backend.models.Team;
import com.example.backend.models.User;
import com.example.backend.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Team team;
    private Priority priority;
    private TaskStatus status;
    private Task task1;
    private Task task2;
    private LocalDateTime now;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        now = LocalDateTime.now();
        today = LocalDate.now();

        // Inicjalizacja użytkownika testowego
        user = new User();
        user.setId(1);
        user.setUsername("testuser");

        // Inicjalizacja zespołu testowego
        team = new Team();
        team.setId(1);
        team.setName("Test Team");

        // Inicjalizacja priorytetu testowego
        priority = new Priority();
        priority.setId(1);
        priority.setName("High");
        priority.setValue(3);

        // Inicjalizacja statusu zadania testowego
        status = new TaskStatus();
        status.setId(1);
        status.setName("In Progress");
        status.setProgressMin(30);
        status.setProgressMax(70);

        // Inicjalizacja zadań testowych
        task1 = new Task();
        task1.setId(1);
        task1.setTitle("Task 1");
        task1.setDescription("Description for task 1");
        task1.setTeam(team);
        task1.setPriority(priority);
        task1.setStatus(status);
        task1.setCreatedBy(user);
        task1.setStartDate(today);
        task1.setDeadline(today.plusDays(7));
        task1.setCreatedAt(now);

        task2 = new Task();
        task2.setId(2);
        task2.setTitle("Task 2");
        task2.setDescription("Description for task 2");
        task2.setTeam(team);
        task2.setPriority(priority);
        task2.setStatus(status);
        task2.setCreatedBy(user);
        task2.setStartDate(today.minusDays(1));
        task2.setDeadline(today.plusDays(5));
        task2.setCreatedAt(now);
    }

    @Test
    void getAllTasks_ShouldReturnAllTasks() {
        // Given
        List<Task> expectedTasks = Arrays.asList(task1, task2);
        when(taskRepository.findAll()).thenReturn(expectedTasks);

        // When
        List<Task> actualTasks = taskService.getAllTasks();

        // Then
        assertEquals(expectedTasks.size(), actualTasks.size());
        assertEquals(expectedTasks.get(0).getId(), actualTasks.get(0).getId());
        assertEquals(expectedTasks.get(1).getId(), actualTasks.get(1).getId());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void getTaskById_WhenTaskExists_ShouldReturnTask() {
        // Given
        when(taskRepository.findById(1)).thenReturn(Optional.of(task1));

        // When
        Optional<Task> result = taskService.getTaskById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(task1.getId(), result.get().getId());
        assertEquals(task1.getTitle(), result.get().getTitle());
        verify(taskRepository, times(1)).findById(1);
    }

    @Test
    void getTaskById_WhenTaskDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(taskRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<Task> result = taskService.getTaskById(99);

        // Then
        assertFalse(result.isPresent());
        verify(taskRepository, times(1)).findById(99);
    }

    @Test
    void saveTask_ShouldSaveAndReturnTask() {
        // Given
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setDescription("Description for new task");
        newTask.setTeam(team);
        newTask.setPriority(priority);
        newTask.setStatus(status);
        newTask.setCreatedBy(user);
        newTask.setStartDate(today);
        newTask.setDeadline(today.plusDays(10));

        when(taskRepository.save(any(Task.class))).thenReturn(newTask);

        // When
        Task savedTask = taskService.saveTask(newTask);

        // Then
        assertEquals(newTask.getTitle(), savedTask.getTitle());
        assertEquals(newTask.getDescription(), savedTask.getDescription());
        verify(taskRepository, times(1)).save(newTask);
    }

    @Test
    void updateTask_ShouldUpdateAndReturnTask() {
        // Given
        Task updatedTask = new Task();
        updatedTask.setId(1);
        updatedTask.setTitle("Updated Task 1");
        updatedTask.setDescription("Updated description for task 1");
        updatedTask.setTeam(team);
        updatedTask.setPriority(priority);
        updatedTask.setStatus(status);
        updatedTask.setCreatedBy(user);
        updatedTask.setStartDate(today);
        updatedTask.setDeadline(today.plusDays(14));

        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        // When
        Task result = taskService.updateTask(updatedTask);

        // Then
        assertEquals(updatedTask.getId(), result.getId());
        assertEquals(updatedTask.getTitle(), result.getTitle());
        assertEquals(updatedTask.getDescription(), result.getDescription());
        assertEquals(updatedTask.getDeadline(), result.getDeadline());
        verify(taskRepository, times(1)).save(updatedTask);
    }

    @Test
    void deleteTask_ShouldCallRepository() {
        // When
        taskService.deleteTask(1);

        // Then
        verify(taskRepository, times(1)).deleteById(1);
    }

    @Test
    void getTasksByTeam_ShouldReturnTasksForTeam() {
        // Given
        List<Task> expectedTasks = Arrays.asList(task1, task2);
        when(taskRepository.findByTeam(team)).thenReturn(expectedTasks);

        // When
        List<Task> actualTasks = taskService.getTasksByTeam(team);

        // Then
        assertEquals(expectedTasks.size(), actualTasks.size());
        assertEquals(expectedTasks.get(0).getId(), actualTasks.get(0).getId());
        assertEquals(expectedTasks.get(1).getId(), actualTasks.get(1).getId());
        verify(taskRepository, times(1)).findByTeam(team);
    }

    @Test
    void getTasksByStatus_ShouldReturnTasksForStatus() {
        // Given
        List<Task> expectedTasks = Arrays.asList(task1, task2);
        when(taskRepository.findByStatus(status)).thenReturn(expectedTasks);

        // When
        List<Task> actualTasks = taskService.getTasksByStatus(status);

        // Then
        assertEquals(expectedTasks.size(), actualTasks.size());
        assertEquals(expectedTasks.get(0).getId(), actualTasks.get(0).getId());
        assertEquals(expectedTasks.get(1).getId(), actualTasks.get(1).getId());
        verify(taskRepository, times(1)).findByStatus(status);
    }

    @Test
    void getTasksCreatedBy_ShouldReturnTasksCreatedByUser() {
        // Given
        List<Task> expectedTasks = Arrays.asList(task1, task2);
        when(taskRepository.findByCreatedBy(user)).thenReturn(expectedTasks);

        // When
        List<Task> actualTasks = taskService.getTasksCreatedBy(user);

        // Then
        assertEquals(expectedTasks.size(), actualTasks.size());
        assertEquals(expectedTasks.get(0).getId(), actualTasks.get(0).getId());
        assertEquals(expectedTasks.get(1).getId(), actualTasks.get(1).getId());
        verify(taskRepository, times(1)).findByCreatedBy(user);
    }

    @Test
    void getTasksWithDeadlineBefore_ShouldReturnTasksBeforeDeadline() {
        // Given
        LocalDate deadline = today.plusDays(10);
        List<Task> expectedTasks = Arrays.asList(task1, task2);
        when(taskRepository.findByDeadlineBefore(deadline)).thenReturn(expectedTasks);

        // When
        List<Task> actualTasks = taskService.getTasksWithDeadlineBefore(deadline);

        // Then
        assertEquals(expectedTasks.size(), actualTasks.size());
        assertEquals(expectedTasks.get(0).getId(), actualTasks.get(0).getId());
        assertEquals(expectedTasks.get(1).getId(), actualTasks.get(1).getId());
        verify(taskRepository, times(1)).findByDeadlineBefore(deadline);
    }

    @Test
    void getTaskByTitle_WhenTaskExists_ShouldReturnTask() {
        // Given
        when(taskRepository.findByTitle("Task 1")).thenReturn(Optional.of(task1));

        // When
        Optional<Task> result = taskService.getTaskByTitle("Task 1");

        // Then
        assertTrue(result.isPresent());
        assertEquals(task1.getId(), result.get().getId());
        assertEquals("Task 1", result.get().getTitle());
        verify(taskRepository, times(1)).findByTitle("Task 1");
    }

    @Test
    void getTaskByTitle_WhenTaskDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(taskRepository.findByTitle("Nonexistent Task")).thenReturn(Optional.empty());

        // When
        Optional<Task> result = taskService.getTaskByTitle("Nonexistent Task");

        // Then
        assertFalse(result.isPresent());
        verify(taskRepository, times(1)).findByTitle("Nonexistent Task");
    }

    @Test
    void getTasksByTeamId_ShouldReturnTasksForTeamId() {
        // Given
        List<Task> expectedTasks = Arrays.asList(task1, task2);
        when(taskRepository.findByTeamId(1)).thenReturn(expectedTasks);

        // When
        List<Task> actualTasks = taskService.getTasksByTeamId(1);

        // Then
        assertEquals(expectedTasks.size(), actualTasks.size());
        assertEquals(expectedTasks.get(0).getId(), actualTasks.get(0).getId());
        assertEquals(expectedTasks.get(1).getId(), actualTasks.get(1).getId());
        verify(taskRepository, times(1)).findByTeamId(1);
    }

    @Test
    void getTasksByStatusId_ShouldReturnTasksForStatusId() {
        // Given
        List<Task> expectedTasks = Arrays.asList(task1, task2);
        when(taskRepository.findByStatusId(1)).thenReturn(expectedTasks);

        // When
        List<Task> actualTasks = taskService.getTasksByStatusId(1);

        // Then
        assertEquals(expectedTasks.size(), actualTasks.size());
        assertEquals(expectedTasks.get(0).getId(), actualTasks.get(0).getId());
        assertEquals(expectedTasks.get(1).getId(), actualTasks.get(1).getId());
        verify(taskRepository, times(1)).findByStatusId(1);
    }

    @Test
    void getTasksByPriorityId_ShouldReturnTasksForPriorityId() {
        // Given
        List<Task> expectedTasks = Arrays.asList(task1, task2);
        when(taskRepository.findByPriorityId(1)).thenReturn(expectedTasks);

        // When
        List<Task> actualTasks = taskService.getTasksByPriorityId(1);

        // Then
        assertEquals(expectedTasks.size(), actualTasks.size());
        assertEquals(expectedTasks.get(0).getId(), actualTasks.get(0).getId());
        assertEquals(expectedTasks.get(1).getId(), actualTasks.get(1).getId());
        verify(taskRepository, times(1)).findByPriorityId(1);
    }
}