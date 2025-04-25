package com.example.backend.controllers;

import com.example.backend.dto.TaskCommentDTO;
import com.example.backend.dto.TaskDTO;
import com.example.backend.dto.UserDTO;
import com.example.backend.models.Task;
import com.example.backend.models.User;
import com.example.backend.services.TaskCommentService;
import com.example.backend.services.TaskService;
import com.example.backend.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TaskCommentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TaskCommentService taskCommentService;

    @Mock
    private TaskService taskService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskCommentController taskCommentController;

    private TaskCommentDTO taskCommentDTO;
    private List<TaskCommentDTO> taskCommentDTOList;
    private Task task;
    private TaskDTO taskDTO;
    private User user;
    private UserDTO userDTO;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskCommentController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDateTime serialization

        // Initialize test data
        task = new Task();
        task.setId(1);
        task.setTitle("Build Foundation");

        // Create TaskDTO
        taskDTO = new TaskDTO();
        taskDTO.setId(1);
        taskDTO.setTitle("Build Foundation");

        user = new User();
        user.setId(1);
        user.setUsername("worker1");
        user.setFirstName("John");
        user.setLastName("Doe");

        // Create UserDTO
        userDTO = new UserDTO();
        userDTO.setId(1);
        userDTO.setUsername("worker1");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");

        taskCommentDTO = new TaskCommentDTO();
        taskCommentDTO.setId(1);
        taskCommentDTO.setTaskId(1);
        taskCommentDTO.setUserId(1);
        taskCommentDTO.setComment("Need more concrete for this job");
        taskCommentDTO.setCreatedAt(LocalDateTime.now());
        taskCommentDTO.setUsername("worker1");
        taskCommentDTO.setUserFullName("John Doe");

        TaskCommentDTO taskCommentDTO2 = new TaskCommentDTO();
        taskCommentDTO2.setId(2);
        taskCommentDTO2.setTaskId(1);
        taskCommentDTO2.setUserId(2);
        taskCommentDTO2.setComment("I'll order additional supplies");
        taskCommentDTO2.setCreatedAt(LocalDateTime.now());
        taskCommentDTO2.setUsername("manager1");
        taskCommentDTO2.setUserFullName("Manager One");

        taskCommentDTOList = Arrays.asList(taskCommentDTO, taskCommentDTO2);
    }

    @Test
    public void getAllTaskComments_ShouldReturnListOfComments() throws Exception {
        // Arrange
        when(taskCommentService.getAllTaskComments()).thenReturn(taskCommentDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/task-comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comment").value("Need more concrete for this job"))
                .andExpect(jsonPath("$[1].comment").value("I'll order additional supplies"));
    }

    @Test
    public void getTaskCommentById_WhenExists_ShouldReturnComment() throws Exception {
        // Arrange
        Optional<TaskCommentDTO> optionalComment = Optional.of(taskCommentDTO);
        when(taskCommentService.getTaskCommentById(1)).thenReturn(optionalComment);

        // Act & Assert
        mockMvc.perform(get("/database/task-comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("Need more concrete for this job"))
                .andExpect(jsonPath("$.username").value("worker1"));
    }

    @Test
    public void getTaskCommentById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskCommentService.getTaskCommentById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/task-comments/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getCommentsByTask_WhenTaskExists_ShouldReturnComments() throws Exception {
        // Arrange
        Optional<TaskDTO> optionalTaskDTO = Optional.of(taskDTO);
        when(taskService.getTaskById(1)).thenReturn(optionalTaskDTO);
        when(taskCommentService.getCommentsByTask(any(Task.class))).thenReturn(taskCommentDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/task-comments/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comment").value("Need more concrete for this job"))
                .andExpect(jsonPath("$[1].comment").value("I'll order additional supplies"));
    }

    @Test
    public void getCommentsByTask_WhenTaskDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskService.getTaskById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/task-comments/task/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getCommentsByUser_WhenUserExists_ShouldReturnComments() throws Exception {
        // Arrange
        Optional<UserDTO> optionalUserDTO = Optional.of(userDTO);
        when(userService.getUserById(1)).thenReturn(optionalUserDTO);
        when(taskCommentService.getCommentsByUser(any(User.class))).thenReturn(Collections.singletonList(taskCommentDTO));

        // Act & Assert
        mockMvc.perform(get("/database/task-comments/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comment").value("Need more concrete for this job"))
                .andExpect(jsonPath("$[0].username").value("worker1"));
    }

    @Test
    public void getCommentsByUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(userService.getUserById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/task-comments/user/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createTaskComment_WithValidData_ShouldReturnCreatedComment() throws Exception {
        // Arrange
        when(taskCommentService.saveTaskComment(any(TaskCommentDTO.class))).thenReturn(taskCommentDTO);

        // Act & Assert
        mockMvc.perform(post("/database/task-comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskCommentDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comment").value("Need more concrete for this job"))
                .andExpect(jsonPath("$.username").value("worker1"));
    }

    @Test
    public void addCommentToTask_WithValidParams_ShouldReturnCreatedComment() throws Exception {
        // Arrange
        Map<String, Object> commentParams = new HashMap<>();
        commentParams.put("taskId", 1);
        commentParams.put("userId", 1);
        commentParams.put("comment", "Need more concrete for this job");

        Optional<TaskDTO> optionalTaskDTO = Optional.of(taskDTO);
        Optional<UserDTO> optionalUserDTO = Optional.of(userDTO);
        when(taskService.getTaskById(1)).thenReturn(optionalTaskDTO);
        when(userService.getUserById(1)).thenReturn(optionalUserDTO);
        when(taskCommentService.addCommentToTask(any(Task.class), any(User.class), anyString())).thenReturn(taskCommentDTO);

        // Act & Assert
        mockMvc.perform(post("/database/task-comments/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentParams)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comment").value("Need more concrete for this job"))
                .andExpect(jsonPath("$.username").value("worker1"));
    }

    @Test
    public void addCommentToTask_WithMissingParams_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Map<String, Object> incompleteParams = new HashMap<>();
        incompleteParams.put("taskId", 1);
        incompleteParams.put("userId", 1);
        // Missing required comment parameter

        // Act & Assert
        mockMvc.perform(post("/database/task-comments/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incompleteParams)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addCommentToTask_WhenTaskDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        Map<String, Object> commentParams = new HashMap<>();
        commentParams.put("taskId", 99);
        commentParams.put("userId", 1);
        commentParams.put("comment", "Need more concrete for this job");

        when(taskService.getTaskById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/database/task-comments/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentParams)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateTaskComment_WhenCommentExists_ShouldReturnUpdatedComment() throws Exception {
        // Arrange
        Optional<TaskCommentDTO> optionalComment = Optional.of(taskCommentDTO);
        when(taskCommentService.getTaskCommentById(1)).thenReturn(optionalComment);
        when(taskCommentService.saveTaskComment(any(TaskCommentDTO.class))).thenReturn(taskCommentDTO);

        // Update the comment
        TaskCommentDTO updatedComment = new TaskCommentDTO();
        updatedComment.setId(1);
        updatedComment.setTaskId(1);
        updatedComment.setUserId(1);
        updatedComment.setComment("Updated: Need more concrete and sand for this job");

        // Act & Assert
        mockMvc.perform(put("/database/task-comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedComment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("Need more concrete for this job")); // Mock returns original

        // Verify that the ID was set in the DTO before saving
        verify(taskCommentService).saveTaskComment(argThat(dto -> dto.getId() == 1));
    }

    @Test
    public void updateTaskComment_WhenCommentDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskCommentService.getTaskCommentById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/database/task-comments/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskCommentDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteTaskComment_WhenCommentExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        Optional<TaskCommentDTO> optionalComment = Optional.of(taskCommentDTO);
        when(taskCommentService.getTaskCommentById(1)).thenReturn(optionalComment);
        doNothing().when(taskCommentService).deleteTaskComment(1);

        // Act & Assert
        mockMvc.perform(delete("/database/task-comments/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteTaskComment_WhenCommentDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskCommentService.getTaskCommentById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/database/task-comments/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteAllCommentsForTask_WhenTaskExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        Optional<TaskDTO> optionalTaskDTO = Optional.of(taskDTO);
        when(taskService.getTaskById(1)).thenReturn(optionalTaskDTO);
        doNothing().when(taskCommentService).deleteAllCommentsForTask(any(Task.class));

        // Act & Assert
        mockMvc.perform(delete("/database/task-comments/task/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteAllCommentsForTask_WhenTaskDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskService.getTaskById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/database/task-comments/task/99"))
                .andExpect(status().isNotFound());
    }
}