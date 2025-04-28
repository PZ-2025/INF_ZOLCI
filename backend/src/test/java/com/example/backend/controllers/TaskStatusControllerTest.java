package com.example.backend.controllers;

import com.example.backend.dto.TaskStatusDTO;
import com.example.backend.services.TaskStatusService;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TaskStatusControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TaskStatusService taskStatusService;

    @InjectMocks
    private TaskStatusController taskStatusController;

    private TaskStatusDTO taskStatusDTO;
    private List<TaskStatusDTO> taskStatusDTOList;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskStatusController).build();
        objectMapper = new ObjectMapper();

        // Initialize test data
        taskStatusDTO = new TaskStatusDTO();
        taskStatusDTO.setId(1);
        taskStatusDTO.setName("In Progress");
        taskStatusDTO.setProgressMin(10);
        taskStatusDTO.setProgressMax(90);
        taskStatusDTO.setDisplayOrder(2);

        TaskStatusDTO taskStatusDTO2 = new TaskStatusDTO();
        taskStatusDTO2.setId(2);
        taskStatusDTO2.setName("Completed");
        taskStatusDTO2.setProgressMin(100);
        taskStatusDTO2.setProgressMax(100);
        taskStatusDTO2.setDisplayOrder(3);

        taskStatusDTOList = Arrays.asList(taskStatusDTO, taskStatusDTO2);
    }

    @Test
    public void getAllTaskStatuses_ShouldReturnListOfStatuses() throws Exception {
        // Arrange
        when(taskStatusService.getAllTaskStatuses()).thenReturn(taskStatusDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/task-statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("In Progress"))
                .andExpect(jsonPath("$[1].name").value("Completed"));
    }

    @Test
    public void getAllTaskStatusesSorted_ShouldReturnSortedList() throws Exception {
        // Arrange
        when(taskStatusService.getAllTaskStatusesSorted()).thenReturn(taskStatusDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/task-statuses/sorted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("In Progress"))
                .andExpect(jsonPath("$[1].name").value("Completed"));
    }

    @Test
    public void getTaskStatusById_WhenExists_ShouldReturnStatus() throws Exception {
        // Arrange
        Optional<TaskStatusDTO> optionalStatus = Optional.of(taskStatusDTO);
        when(taskStatusService.getTaskStatusById(1)).thenReturn(optionalStatus);

        // Act & Assert
        mockMvc.perform(get("/database/task-statuses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("In Progress"))
                .andExpect(jsonPath("$.progressMin").value(10))
                .andExpect(jsonPath("$.progressMax").value(90));
    }

    @Test
    public void getTaskStatusById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskStatusService.getTaskStatusById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/task-statuses/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getTaskStatusByName_WhenExists_ShouldReturnStatus() throws Exception {
        // Arrange
        Optional<TaskStatusDTO> optionalStatus = Optional.of(taskStatusDTO);
        when(taskStatusService.getTaskStatusByName("In Progress")).thenReturn(optionalStatus);

        // Act & Assert
        mockMvc.perform(get("/database/task-statuses/name/In Progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.progressMin").value(10))
                .andExpect(jsonPath("$.progressMax").value(90));
    }

    @Test
    public void getTaskStatusByName_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskStatusService.getTaskStatusByName("Non-existent")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/task-statuses/name/Non-existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createTaskStatus_WithValidData_ShouldReturnCreatedStatus() throws Exception {
        // Arrange
        when(taskStatusService.existsByName("New Status")).thenReturn(false);
        when(taskStatusService.saveTaskStatus(any(TaskStatusDTO.class))).thenReturn(taskStatusDTO);

        TaskStatusDTO newStatus = new TaskStatusDTO();
        newStatus.setName("New Status");
        newStatus.setProgressMin(0);
        newStatus.setProgressMax(50);
        newStatus.setDisplayOrder(1);

        // Act & Assert
        mockMvc.perform(post("/database/task-statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStatus)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("In Progress")); // Mock returns our pre-defined taskStatusDTO
    }

    @Test
    public void createTaskStatus_WithExistingName_ShouldReturnConflict() throws Exception {
        // Arrange
        when(taskStatusService.existsByName("In Progress")).thenReturn(true);

        TaskStatusDTO newStatus = new TaskStatusDTO();
        newStatus.setName("In Progress");
        newStatus.setProgressMin(0);
        newStatus.setProgressMax(50);
        newStatus.setDisplayOrder(1);

        // Act & Assert
        mockMvc.perform(post("/database/task-statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStatus)))
                .andExpect(status().isConflict());
    }

    @Test
    public void createTaskStatusFromParams_WithValidParams_ShouldReturnCreatedStatus() throws Exception {
        // Arrange
        when(taskStatusService.existsByName("New Status")).thenReturn(false);
        when(taskStatusService.createTaskStatus(eq("New Status"), eq(0), eq(50), eq(1))).thenReturn(taskStatusDTO);

        // Act & Assert
        mockMvc.perform(post("/database/task-statuses/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Status\",\"progressMin\":0,\"progressMax\":50,\"displayOrder\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("In Progress")); // Mock returns our pre-defined taskStatusDTO
    }

    @Test
    public void updateTaskStatus_WhenExists_ShouldReturnUpdatedStatus() throws Exception {
        // Arrange
        Optional<TaskStatusDTO> optionalStatus = Optional.of(taskStatusDTO);
        when(taskStatusService.getTaskStatusById(1)).thenReturn(optionalStatus);
        when(taskStatusService.saveTaskStatus(any(TaskStatusDTO.class))).thenReturn(taskStatusDTO);

        TaskStatusDTO updatedStatus = new TaskStatusDTO();
        updatedStatus.setId(1);
        updatedStatus.setName("Updated Status");
        updatedStatus.setProgressMin(20);
        updatedStatus.setProgressMax(80);
        updatedStatus.setDisplayOrder(2);

        // Act & Assert
        mockMvc.perform(put("/database/task-statuses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedStatus)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("In Progress")); // Mock returns our pre-defined taskStatusDTO
    }

    @Test
    public void updateTaskStatus_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskStatusService.getTaskStatusById(99)).thenReturn(Optional.empty());

        TaskStatusDTO updatedStatus = new TaskStatusDTO();
        updatedStatus.setId(99);
        updatedStatus.setName("Updated Status");
        updatedStatus.setProgressMin(20);
        updatedStatus.setProgressMax(80);
        updatedStatus.setDisplayOrder(2);

        // Act & Assert
        mockMvc.perform(put("/database/task-statuses/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedStatus)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateDisplayOrder_WhenExists_ShouldReturnUpdatedStatus() throws Exception {
        // Arrange
        Optional<TaskStatusDTO> optionalStatus = Optional.of(taskStatusDTO);
        when(taskStatusService.updateDisplayOrder(eq(1), eq(5))).thenReturn(optionalStatus);

        // Act & Assert
        mockMvc.perform(patch("/database/task-statuses/1/display-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayOrder\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("In Progress")); // Mock returns our pre-defined taskStatusDTO
    }

    @Test
    public void updateDisplayOrder_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskStatusService.updateDisplayOrder(eq(99), anyInt())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(patch("/database/task-statuses/99/display-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayOrder\":5}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteTaskStatus_WhenExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        Optional<TaskStatusDTO> optionalStatus = Optional.of(taskStatusDTO);
        when(taskStatusService.getTaskStatusById(1)).thenReturn(optionalStatus);
        doNothing().when(taskStatusService).deleteTaskStatus(1);

        // Act & Assert
        mockMvc.perform(delete("/database/task-statuses/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteTaskStatus_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskStatusService.getTaskStatusById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/database/task-statuses/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteTaskStatus_WithAssignedTasks_ShouldReturnConflict() throws Exception {
        // Arrange
        Optional<TaskStatusDTO> optionalStatus = Optional.of(taskStatusDTO);
        when(taskStatusService.getTaskStatusById(1)).thenReturn(optionalStatus);
        doThrow(new IllegalStateException("Cannot delete status with assigned tasks"))
                .when(taskStatusService).deleteTaskStatus(1);

        // Act & Assert
        mockMvc.perform(delete("/database/task-statuses/1"))
                .andExpect(status().isConflict());
    }
}