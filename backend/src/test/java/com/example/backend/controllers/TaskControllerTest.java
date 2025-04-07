package  com.example.backend.controllers;

import com.example.backend.models.Task;
import com.example.backend.models.TaskStatus;
import com.example.backend.models.Team;
import com.example.backend.models.User;
import com.example.backend.services.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TaskControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private ObjectMapper objectMapper;
    private Task task;
    private Team team;
    private User user;
    private TaskStatus status;

    @BeforeEach
    void setUp() {
        // Inicjalizacja MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();

        // Konfiguracja ObjectMapper do obsługi LocalDate
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Inicjalizacja danych testowych
        user = new User();
        user.setId(1);
        user.setUsername("testUser");

        team = new Team();
        team.setId(1);
        team.setName("TestTeam");

        status = new TaskStatus();
        status.setId(1);
        status.setName("IN_PROGRESS");

        task = new Task();
        task.setId(1);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setCreatedBy(user);
        task.setTeam(team);
        task.setStatus(status);
        task.setDeadline(LocalDate.now().plusDays(7));
    }

    @Test
    void getAllTasks_ShouldReturnAllTasks() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(task);
        when(taskService.getAllTasks()).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/database/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Task")));

        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    void getTaskById_WhenTaskExists_ShouldReturnTask() throws Exception {
        // Given
        when(taskService.getTaskById(1)).thenReturn(Optional.of(task));

        // When & Then
        mockMvc.perform(get("/database/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Task")));

        verify(taskService, times(1)).getTaskById(1);
    }

    @Test
    void getTaskById_WhenTaskDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(taskService.getTaskById(anyInt())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/database/tasks/999"))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).getTaskById(999);
    }

    @Test
    void createTask_ShouldReturnCreatedTask() throws Exception {
        // Given
        when(taskService.saveTask(any(Task.class))).thenReturn(task);

        // When & Then
        mockMvc.perform(post("/database/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Task")));

        verify(taskService, times(1)).saveTask(any(Task.class));
    }

    @Test
    void updateTask_WhenTaskExists_ShouldReturnUpdatedTask() throws Exception {
        // Given
        when(taskService.getTaskById(1)).thenReturn(Optional.of(task));
        when(taskService.updateTask(any(Task.class))).thenReturn(task);

        // When & Then
        mockMvc.perform(put("/database/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Task")));

        verify(taskService, times(1)).getTaskById(1);
        verify(taskService, times(1)).updateTask(any(Task.class));
    }

    @Test
    void updateTask_WhenTaskDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(taskService.getTaskById(anyInt())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/database/tasks/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).getTaskById(999);
        verify(taskService, never()).updateTask(any(Task.class));
    }

    @Test
    void deleteTask_WhenTaskExists_ShouldReturnNoContent() throws Exception {
        // Given
        when(taskService.getTaskById(1)).thenReturn(Optional.of(task));
        doNothing().when(taskService).deleteTask(1);

        // When & Then
        mockMvc.perform(delete("/database/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).getTaskById(1);
        verify(taskService, times(1)).deleteTask(1);
    }

    @Test
    void deleteTask_WhenTaskDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(taskService.getTaskById(anyInt())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/database/tasks/999"))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).getTaskById(999);
        verify(taskService, never()).deleteTask(anyInt());
    }

    @Test
    void getTasksByTeamId_ShouldReturnTasksForTeam() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(task);
        when(taskService.getTasksByTeamId(1)).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/database/tasks/team/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Task")));

        verify(taskService, times(1)).getTasksByTeamId(1);
    }

    @Test
    void getTasksByStatusId_ShouldReturnTasksForStatus() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(task);
        when(taskService.getTasksByStatusId(1)).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/database/tasks/status/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Task")));

        verify(taskService, times(1)).getTasksByStatusId(1);
    }

    @Test
    void getTaskByTitle_WhenTaskExists_ShouldReturnTask() throws Exception {
        // Given
        when(taskService.getTaskByTitle("Test Task")).thenReturn(Optional.of(task));

        // When & Then
        mockMvc.perform(get("/database/tasks/title/Test Task"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Task")));

        verify(taskService, times(1)).getTaskByTitle("Test Task");
    }

    @Test
    void getTasksWithDeadlineBefore_ShouldReturnTasksBeforeDeadline() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(task);
        LocalDate testDate = LocalDate.now().plusDays(10);
        when(taskService.getTasksWithDeadlineBefore(any(LocalDate.class))).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/database/tasks/deadline-before/" + testDate))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Task")));

        verify(taskService, times(1)).getTasksWithDeadlineBefore(any(LocalDate.class));
    }
}