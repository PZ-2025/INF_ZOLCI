package com.example.backend.controllers;

import com.example.backend.models.Task;
import com.example.backend.models.TaskHistory;
import com.example.backend.models.User;
import com.example.backend.services.TaskHistoryService;
import com.example.backend.services.TaskService;
import com.example.backend.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskHistoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskHistoryService taskHistoryService;

    @Mock
    private TaskService taskService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskHistoryController taskHistoryController;

    private ObjectMapper objectMapper;
    private Task task;
    private User user;
    private TaskHistory history;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(taskHistoryController)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Inicjalizacja danych testowych
        task = new Task();
        task.setId(1);
        task.setTitle("Test Task");

        user = new User();
        user.setId(1);
        user.setUsername("testuser");

        history = new TaskHistory();
        history.setId(1);
        history.setTask(task);
        history.setChangedBy(user.getId());
        history.setFieldName("status");
        history.setOldValue("W toku");
        history.setNewValue("Zakończone");
        history.setChangedAt(LocalDateTime.now());
    }

    @Test
    void getAllTaskHistory_ShouldReturnAllHistory() throws Exception {
        // Given
        List<TaskHistory> historyList = Arrays.asList(history);
        when(taskHistoryService.getAllTaskHistory()).thenReturn(historyList);

        // When & Then
        mockMvc.perform(get("/database/task-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].fieldName", is("status")))
                .andExpect(jsonPath("$[0].oldValue", is("W toku")))
                .andExpect(jsonPath("$[0].newValue", is("Zakończone")));

        verify(taskHistoryService, times(1)).getAllTaskHistory();
    }

    @Test
    void getTaskHistoryById_WhenHistoryExists_ShouldReturnHistory() throws Exception {
        // Given
        when(taskHistoryService.getTaskHistoryById(1)).thenReturn(Optional.of(history));

        // When & Then
        mockMvc.perform(get("/database/task-history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.fieldName", is("status")))
                .andExpect(jsonPath("$.oldValue", is("W toku")))
                .andExpect(jsonPath("$.newValue", is("Zakończone")));

        verify(taskHistoryService, times(1)).getTaskHistoryById(1);
    }

    @Test
    void getTaskHistoryById_WhenHistoryDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(taskHistoryService.getTaskHistoryById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/database/task-history/99"))
                .andExpect(status().isNotFound());

        verify(taskHistoryService, times(1)).getTaskHistoryById(99);
    }

    @Test
    void getHistoryByTask_WhenTaskExists_ShouldReturnHistory() throws Exception {
        // Given
        List<TaskHistory> historyList = Arrays.asList(history);
        when(taskService.getTaskById(1)).thenReturn(Optional.of(task));
        when(taskHistoryService.getHistoryByTask(task)).thenReturn(historyList);

        // When & Then
        mockMvc.perform(get("/database/task-history/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].fieldName", is("status")));

        verify(taskService, times(1)).getTaskById(1);
        verify(taskHistoryService, times(1)).getHistoryByTask(task);
    }

    @Test
    void getHistoryByTask_WhenTaskDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(taskService.getTaskById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/database/task-history/task/99"))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).getTaskById(99);
        verify(taskHistoryService, never()).getHistoryByTask(any(Task.class));
    }

    @Test
    void getHistoryByUser_WhenUserExists_ShouldReturnHistory() throws Exception {
        // Given
        List<TaskHistory> historyList = Arrays.asList(history);
        when(userService.getUserById(1)).thenReturn(Optional.of(user));
        when(taskHistoryService.getHistoryByUser(user)).thenReturn(historyList);

        // When & Then
        mockMvc.perform(get("/database/task-history/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].fieldName", is("status")));

        verify(userService, times(1)).getUserById(1);
        verify(taskHistoryService, times(1)).getHistoryByUser(user);
    }

    @Test
    void getHistoryByUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(userService.getUserById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/database/task-history/user/99"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(99);
        verify(taskHistoryService, never()).getHistoryByUser(any(User.class));
    }

    @Test
    void createTaskHistory_ShouldReturnCreatedHistory() throws Exception {
        // Given
        when(taskHistoryService.saveTaskHistory(any(TaskHistory.class))).thenReturn(history);

        // When & Then
        mockMvc.perform(post("/database/task-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(history)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.fieldName", is("status")));

        verify(taskHistoryService, times(1)).saveTaskHistory(any(TaskHistory.class));
    }

    @Test
    void logTaskChange_WithValidData_ShouldReturnCreatedHistory() throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", 1);
        payload.put("userId", 1);
        payload.put("fieldName", "priority");
        payload.put("oldValue", "Niski");
        payload.put("newValue", "Wysoki");

        when(taskService.getTaskById(1)).thenReturn(Optional.of(task));
        when(userService.getUserById(1)).thenReturn(Optional.of(user));
        when(taskHistoryService.logTaskChange(eq(task), eq(user), eq("priority"), eq("Niski"), eq("Wysoki")))
                .thenReturn(history);

        // When & Then
        mockMvc.perform(post("/database/task-history/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)));

        verify(taskService, times(1)).getTaskById(1);
        verify(userService, times(1)).getUserById(1);
        verify(taskHistoryService, times(1)).logTaskChange(eq(task), eq(user), eq("priority"), eq("Niski"), eq("Wysoki"));
    }

    @Test
    void logTaskChange_WithMissingData_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", 1);
        // Missing userId and fieldName

        // When & Then
        mockMvc.perform(post("/database/task-history/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).getTaskById(anyInt());
        verify(userService, never()).getUserById(anyInt());
        verify(taskHistoryService, never()).logTaskChange(any(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void deleteTaskHistory_WhenHistoryExists_ShouldReturnNoContent() throws Exception {
        // Given
        when(taskHistoryService.getTaskHistoryById(1)).thenReturn(Optional.of(history));
        doNothing().when(taskHistoryService).deleteTaskHistory(1);

        // When & Then
        mockMvc.perform(delete("/database/task-history/1"))
                .andExpect(status().isNoContent());

        verify(taskHistoryService, times(1)).getTaskHistoryById(1);
        verify(taskHistoryService, times(1)).deleteTaskHistory(1);
    }

    @Test
    void deleteTaskHistory_WhenHistoryDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(taskHistoryService.getTaskHistoryById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/database/task-history/99"))
                .andExpect(status().isNotFound());

        verify(taskHistoryService, times(1)).getTaskHistoryById(99);
        verify(taskHistoryService, never()).deleteTaskHistory(anyInt());
    }

    @Test
    void deleteAllHistoryForTask_WhenTaskExists_ShouldReturnNoContent() throws Exception {
        // Given
        when(taskService.getTaskById(1)).thenReturn(Optional.of(task));
        when(taskHistoryService.deleteAllHistoryForTask(task)).thenReturn(2);

        // When & Then
        mockMvc.perform(delete("/database/task-history/task/1"))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).getTaskById(1);
        verify(taskHistoryService, times(1)).deleteAllHistoryForTask(task);
    }

    @Test
    void deleteAllHistoryForTask_WhenTaskDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(taskService.getTaskById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/database/task-history/task/99"))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).getTaskById(99);
        verify(taskHistoryService, never()).deleteAllHistoryForTask(any(Task.class));
    }
}