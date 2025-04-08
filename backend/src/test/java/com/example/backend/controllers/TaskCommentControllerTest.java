package com.example.backend.controllers;

import com.example.backend.models.Task;
import com.example.backend.models.TaskComment;
import com.example.backend.models.User;
import com.example.backend.services.TaskCommentService;
import com.example.backend.services.TaskService;
import com.example.backend.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

class TaskCommentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskCommentService taskCommentService;

    @Mock
    private TaskService taskService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskCommentController taskCommentController;

    private ObjectMapper objectMapper;
    private Task task;
    private User user;
    private TaskComment comment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(taskCommentController)
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

        comment = new TaskComment();
        comment.setId(1);
        comment.setTask(task);
        comment.setUser(user);
        comment.setComment("Test comment");
        comment.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getAllTaskComments_ShouldReturnAllComments() throws Exception {
        // Given
        List<TaskComment> comments = Arrays.asList(comment);
        when(taskCommentService.getAllTaskComments()).thenReturn(comments);

        // When & Then
        mockMvc.perform(get("/database/task-comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].comment", is("Test comment")));

        verify(taskCommentService, times(1)).getAllTaskComments();
    }

    @Test
    void getTaskCommentById_WhenCommentExists_ShouldReturnComment() throws Exception {
        // Given
        when(taskCommentService.getTaskCommentById(1)).thenReturn(Optional.of(comment));

        // When & Then
        mockMvc.perform(get("/database/task-comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.comment", is("Test comment")));

        verify(taskCommentService, times(1)).getTaskCommentById(1);
    }

    @Test
    void getTaskCommentById_WhenCommentDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(taskCommentService.getTaskCommentById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/database/task-comments/99"))
                .andExpect(status().isNotFound());

        verify(taskCommentService, times(1)).getTaskCommentById(99);
    }

    @Test
    void getCommentsByTask_WhenTaskExists_ShouldReturnComments() throws Exception {
        // Given
        List<TaskComment> comments = Arrays.asList(comment);
        when(taskService.getTaskById(1)).thenReturn(Optional.of(task));
        when(taskCommentService.getCommentsByTask(task)).thenReturn(comments);

        // When & Then
        mockMvc.perform(get("/database/task-comments/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].comment", is("Test comment")));

        verify(taskService, times(1)).getTaskById(1);
        verify(taskCommentService, times(1)).getCommentsByTask(task);
    }

    @Test
    void getCommentsByTask_WhenTaskDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(taskService.getTaskById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/database/task-comments/task/99"))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).getTaskById(99);
        verify(taskCommentService, never()).getCommentsByTask(any(Task.class));
    }

    @Test
    void getCommentsByUser_WhenUserExists_ShouldReturnComments() throws Exception {
        // Given
        List<TaskComment> comments = Arrays.asList(comment);
        when(userService.getUserById(1)).thenReturn(Optional.of(user));
        when(taskCommentService.getCommentsByUser(user)).thenReturn(comments);

        // When & Then
        mockMvc.perform(get("/database/task-comments/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].comment", is("Test comment")));

        verify(userService, times(1)).getUserById(1);
        verify(taskCommentService, times(1)).getCommentsByUser(user);
    }

    @Test
    void getCommentsByUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(userService.getUserById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/database/task-comments/user/99"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(99);
        verify(taskCommentService, never()).getCommentsByUser(any(User.class));
    }

    @Test
    void createTaskComment_ShouldReturnCreatedComment() throws Exception {
        // Given
        when(taskCommentService.saveTaskComment(any(TaskComment.class))).thenReturn(comment);

        // When & Then
        mockMvc.perform(post("/database/task-comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.comment", is("Test comment")));

        verify(taskCommentService, times(1)).saveTaskComment(any(TaskComment.class));
    }

    @Test
    void addCommentToTask_WithValidData_ShouldReturnCreatedComment() throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", 1);
        payload.put("userId", 1);
        payload.put("comment", "New test comment");

        when(taskService.getTaskById(1)).thenReturn(Optional.of(task));
        when(userService.getUserById(1)).thenReturn(Optional.of(user));
        when(taskCommentService.addCommentToTask(task, user, "New test comment")).thenReturn(comment);

        // When & Then
        mockMvc.perform(post("/database/task-comments/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)));

        verify(taskService, times(1)).getTaskById(1);
        verify(userService, times(1)).getUserById(1);
        verify(taskCommentService, times(1)).addCommentToTask(task, user, "New test comment");
    }

    @Test
    void addCommentToTask_WithMissingData_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", 1);
        // Missing userId and comment

        // When & Then
        mockMvc.perform(post("/database/task-comments/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).getTaskById(anyInt());
        verify(userService, never()).getUserById(anyInt());
        verify(taskCommentService, never()).addCommentToTask(any(), any(), anyString());
    }

    @Test
    void updateTaskComment_WhenCommentExists_ShouldReturnUpdatedComment() throws Exception {
        // Given
        TaskComment updatedComment = new TaskComment();
        updatedComment.setId(1);
        updatedComment.setTask(task);
        updatedComment.setUser(user);
        updatedComment.setComment("Updated comment");
        updatedComment.setCreatedAt(comment.getCreatedAt());

        when(taskCommentService.getTaskCommentById(1)).thenReturn(Optional.of(comment));
        when(taskCommentService.saveTaskComment(any(TaskComment.class))).thenReturn(updatedComment);

        // When & Then
        mockMvc.perform(put("/database/task-comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedComment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.comment", is("Updated comment")));

        verify(taskCommentService, times(1)).getTaskCommentById(1);
        verify(taskCommentService, times(1)).saveTaskComment(any(TaskComment.class));
    }

    @Test
    void updateTaskComment_WhenCommentDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        TaskComment updatedComment = new TaskComment();
        updatedComment.setComment("Updated comment");

        when(taskCommentService.getTaskCommentById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/database/task-comments/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedComment)))
                .andExpect(status().isNotFound());

        verify(taskCommentService, times(1)).getTaskCommentById(99);
        verify(taskCommentService, never()).saveTaskComment(any(TaskComment.class));
    }

    @Test
    void deleteTaskComment_WhenCommentExists_ShouldReturnNoContent() throws Exception {
        // Given
        when(taskCommentService.getTaskCommentById(1)).thenReturn(Optional.of(comment));
        doNothing().when(taskCommentService).deleteTaskComment(1);

        // When & Then
        mockMvc.perform(delete("/database/task-comments/1"))
                .andExpect(status().isNoContent());

        verify(taskCommentService, times(1)).getTaskCommentById(1);
        verify(taskCommentService, times(1)).deleteTaskComment(1);
    }

    @Test
    void deleteTaskComment_WhenCommentDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(taskCommentService.getTaskCommentById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/database/task-comments/99"))
                .andExpect(status().isNotFound());

        verify(taskCommentService, times(1)).getTaskCommentById(99);
        verify(taskCommentService, never()).deleteTaskComment(anyInt());
    }

    @Test
    void deleteAllCommentsForTask_WhenTaskExists_ShouldReturnNoContent() throws Exception {
        // Given
        when(taskService.getTaskById(1)).thenReturn(Optional.of(task));
        when(taskCommentService.deleteAllCommentsForTask(task)).thenReturn(2);

        // When & Then
        mockMvc.perform(delete("/database/task-comments/task/1"))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).getTaskById(1);
        verify(taskCommentService, times(1)).deleteAllCommentsForTask(task);
    }

    @Test
    void deleteAllCommentsForTask_WhenTaskDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(taskService.getTaskById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/database/task-comments/task/99"))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).getTaskById(99);
        verify(taskCommentService, never()).deleteAllCommentsForTask(any(Task.class));
    }
}