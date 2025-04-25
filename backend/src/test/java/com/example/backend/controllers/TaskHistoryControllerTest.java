package com.example.backend.controllers;

import com.example.backend.dto.TaskHistoryDTO;
import com.example.backend.dto.UserDTO;
import com.example.backend.models.Task;
import com.example.backend.models.User;
import com.example.backend.services.TaskHistoryService;
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
public class TaskHistoryControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TaskHistoryService taskHistoryService;

    @Mock
    private TaskService taskService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskHistoryController taskHistoryController;

    private TaskHistoryDTO taskHistoryDTO;
    private List<TaskHistoryDTO> taskHistoryDTOList;
    private Task task;
    private UserDTO userDTO;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskHistoryController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDateTime serialization

        // Initialize test data
        task = new Task();
        task.setId(1);
        task.setTitle("Build Foundation");

        userDTO = new UserDTO();
        userDTO.setId(1);
        userDTO.setUsername("worker1");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");

        taskHistoryDTO = new TaskHistoryDTO();
        taskHistoryDTO.setId(1);
        taskHistoryDTO.setTaskId(1);
        taskHistoryDTO.setChangedById(1);
        taskHistoryDTO.setFieldName("status");
        taskHistoryDTO.setOldValue("In Progress");
        taskHistoryDTO.setNewValue("Completed");
        taskHistoryDTO.setChangedAt(LocalDateTime.now());
        taskHistoryDTO.setChangedByUsername("worker1");
        taskHistoryDTO.setChangedByFullName("John Doe");

        TaskHistoryDTO taskHistoryDTO2 = new TaskHistoryDTO();
        taskHistoryDTO2.setId(2);
        taskHistoryDTO2.setTaskId(1);
        taskHistoryDTO2.setChangedById(2);
        taskHistoryDTO2.setFieldName("priority");
        taskHistoryDTO2.setOldValue("Medium");
        taskHistoryDTO2.setNewValue("High");
        taskHistoryDTO2.setChangedAt(LocalDateTime.now());
        taskHistoryDTO2.setChangedByUsername("manager1");
        taskHistoryDTO2.setChangedByFullName("Manager One");

        taskHistoryDTOList = Arrays.asList(taskHistoryDTO, taskHistoryDTO2);
    }

    @Test
    public void getAllTaskHistory_ShouldReturnListOfHistory() throws Exception {
        // Arrange
        when(taskHistoryService.getAllTaskHistory()).thenReturn(taskHistoryDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/task-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fieldName").value("status"))
                .andExpect(jsonPath("$[1].fieldName").value("priority"));
    }

    @Test
    public void getTaskHistoryById_WhenExists_ShouldReturnHistory() throws Exception {
        // Arrange
        when(taskHistoryService.getTaskHistoryById(1)).thenReturn(Optional.of(taskHistoryDTO));

        // Act & Assert
        mockMvc.perform(get("/database/task-history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fieldName").value("status"))
                .andExpect(jsonPath("$.oldValue").value("In Progress"))
                .andExpect(jsonPath("$.newValue").value("Completed"));
    }

    @Test
    public void getTaskHistoryById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskHistoryService.getTaskHistoryById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/task-history/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getHistoryByTask_WhenTaskExists_ShouldReturnHistory() throws Exception {
        // Arrange
        when(taskService.getTaskById(1)).thenReturn(Optional.of(task));
        when(taskHistoryService.getHistoryByTask(any(Task.class))).thenReturn(taskHistoryDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/task-history/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fieldName").value("status"))
                .andExpect(jsonPath("$[1].fieldName").value("priority"));
    }

    @Test
    public void getHistoryByTask_WhenTaskDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskService.getTaskById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/task-history/task/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getHistoryByUser_WhenUserExists_ShouldReturnHistory() throws Exception {
        // Arrange
        when(userService.getUserById(1)).thenReturn(Optional.of(userDTO));
        when(taskHistoryService.getHistoryByUser(any(User.class))).thenReturn(Collections.singletonList(taskHistoryDTO));

        // Act & Assert
        mockMvc.perform(get("/database/task-history/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fieldName").value("status"))
                .andExpect(jsonPath("$[0].changedByUsername").value("worker1"));
    }

    @Test
    public void getHistoryByUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(userService.getUserById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/task-history/user/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createTaskHistory_WithValidData_ShouldReturnCreatedHistory() throws Exception {
        // Arrange
        when(taskService.getTaskEntityById(1)).thenReturn(Optional.of(task));
        when(userService.getUserById(1)).thenReturn(Optional.of(userDTO));
        when(taskHistoryService.saveTaskHistory(any(TaskHistoryDTO.class), any(Task.class))).thenReturn(taskHistoryDTO);

        // Act & Assert
        mockMvc.perform(post("/database/task-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskHistoryDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fieldName").value("status"))
                .andExpect(jsonPath("$.oldValue").value("In Progress"))
                .andExpect(jsonPath("$.newValue").value("Completed"));
    }

    @Test
    public void createTaskHistory_WithInvalidTaskId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(taskService.getTaskEntityById(99)).thenReturn(Optional.empty());

        taskHistoryDTO.setTaskId(99);

        // Act & Assert
        mockMvc.perform(post("/database/task-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskHistoryDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTaskHistory_WithInvalidUserId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(taskService.getTaskEntityById(1)).thenReturn(Optional.of(task));
        when(userService.getUserById(99)).thenReturn(Optional.empty());

        taskHistoryDTO.setChangedById(99);

        // Act & Assert
        mockMvc.perform(post("/database/task-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskHistoryDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void logTaskChange_WithValidData_ShouldReturnCreatedHistory() throws Exception {
        // Arrange
        Map<String, Object> changeParams = new HashMap<>();
        changeParams.put("taskId", 1);
        changeParams.put("changedById", 1);
        changeParams.put("fieldName", "status");
        changeParams.put("oldValue", "In Progress");
        changeParams.put("newValue", "Completed");

        when(taskService.getTaskEntityById(1)).thenReturn(Optional.of(task));
        when(userService.getUserById(1)).thenReturn(Optional.of(userDTO));
        when(taskHistoryService.logTaskChange(any(Task.class), any(User.class), anyString(), anyString(), anyString()))
                .thenReturn(taskHistoryDTO);

        // Act & Assert
        mockMvc.perform(post("/database/task-history/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeParams)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fieldName").value("status"))
                .andExpect(jsonPath("$.oldValue").value("In Progress"))
                .andExpect(jsonPath("$.newValue").value("Completed"));
    }

    @Test
    public void logTaskChange_WithMissingRequiredParams_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Map<String, Object> incompleteParams = new HashMap<>();
        incompleteParams.put("taskId", 1);
        incompleteParams.put("changedById", 1);
        // Missing required fieldName parameter

        // Act & Assert
        mockMvc.perform(post("/database/task-history/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incompleteParams)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void logTaskChange_WithInvalidTaskId_ShouldReturnNotFound() throws Exception {
        // Arrange
        Map<String, Object> changeParams = new HashMap<>();
        changeParams.put("taskId", 99);
        changeParams.put("changedById", 1);
        changeParams.put("fieldName", "status");
        changeParams.put("oldValue", "In Progress");
        changeParams.put("newValue", "Completed");

        when(taskService.getTaskEntityById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/database/task-history/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeParams)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteTaskHistory_WhenHistoryExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(taskHistoryService.getTaskHistoryById(1)).thenReturn(Optional.of(taskHistoryDTO));
        doNothing().when(taskHistoryService).deleteTaskHistory(1);

        // Act & Assert
        mockMvc.perform(delete("/database/task-history/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteTaskHistory_WhenHistoryDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskHistoryService.getTaskHistoryById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/database/task-history/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteAllHistoryForTask_WhenTaskExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(taskService.getTaskById(1)).thenReturn(Optional.of(task));
        doNothing().when(taskHistoryService).deleteAllHistoryForTask(any(Task.class));

        // Act & Assert
        mockMvc.perform(delete("/database/task-history/task/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteAllHistoryForTask_WhenTaskDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskService.getTaskById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/database/task-history/task/99"))
                .andExpect(status().isNotFound());
    }
}