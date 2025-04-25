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

import java.util.*;

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
        taskStatusDTO.setProgressMin(30);
        taskStatusDTO.setProgressMax(70);
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
    public void getAllTaskStatusesSorted_ShouldReturnSortedStatuses() throws Exception {
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
        when(taskStatusService.getTaskStatusById(1)).thenReturn(Optional.of(taskStatusDTO));

        // Act & Assert
        mockMvc.perform(get("/database/task-statuses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("In Progress"))
                .andExpect(jsonPath("$.progressMin").value(30))
                .andExpect(jsonPath("$.progressMax").value(70));
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
        when(taskStatusService.getTaskStatusByName("In Progress")).thenReturn(Optional.of(taskStatusDTO));

        // Act & Assert
        mockMvc.perform(get("/database/task-statuses/name/In Progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.progressMin").value(30))
                .andExpect(jsonPath("$.progressMax").value(70));
    }

    @Test
    public void getTaskStatusByName_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskStatusService.getTaskStatusByName("Unknown")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/task-statuses/name/Unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createTaskStatus_WithValidData_ShouldReturnCreatedStatus() throws Exception {
        // Arrange
        when(taskStatusService.existsByName(anyString())).thenReturn(false);
        when(taskStatusService.saveTaskStatus(any(TaskStatusDTO.class))).thenReturn(taskStatusDTO);

        // Act & Assert
        mockMvc.perform(post("/database/task-statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskStatusDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("In Progress"))
                .andExpect(jsonPath("$.progressMin").value(30));
    }

    @Test
    public void createTaskStatus_WithExistingName_ShouldReturnConflict() throws Exception {
        // Arrange
        when(taskStatusService.existsByName(anyString())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/database/task-statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskStatusDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    public void createTaskStatusFromParams_WithValidParams_ShouldReturnCreatedStatus() throws Exception {
        // Arrange
        Map<String, Object> params = new HashMap<>();
        params.put("name", "In Progress");
        params.put("progressMin", 30);
        params.put("progressMax", 70);
        params.put("displayOrder", 2);

        when(taskStatusService.existsByName(anyString())).thenReturn(false);
        when(taskStatusService.createTaskStatus(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(taskStatusDTO);

        // Act & Assert
        mockMvc.perform(post("/database/task-statuses/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("In Progress"))
                .andExpect(jsonPath("$.progressMin").value(30));
    }

    @Test
    public void createTaskStatusFromParams_WithMissingParams_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Map<String, Object> params = new HashMap<>();
        params.put("name", "In Progress");
        params.put("progressMin", 30);
        // Missing required parameters

        // Act & Assert
        mockMvc.perform(post("/database/task-statuses/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateTaskStatus_WhenStatusExists_ShouldReturnUpdatedStatus() throws Exception {
        // Arrange
        when(taskStatusService.getTaskStatusById(1)).thenReturn(Optional.of(taskStatusDTO));
        when(taskStatusService.saveTaskStatus(any(TaskStatusDTO.class))).thenReturn(taskStatusDTO);

        // Update the status
        TaskStatusDTO updatedStatus = new TaskStatusDTO();
        updatedStatus.setId(1);
        updatedStatus.setName("Updated In Progress");
        updatedStatus.setProgressMin(25);
        updatedStatus.setProgressMax(75);
        updatedStatus.setDisplayOrder(2);

        // Act & Assert
        mockMvc.perform(put("/database/task-statuses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedStatus)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("In Progress")); // Mock returns original

        // Verify that the ID was set in the DTO before saving
        verify(taskStatusService).saveTaskStatus(argThat(dto -> dto.getId() == 1));
    }

    @Test
    public void updateTaskStatus_WhenStatusDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskStatusService.getTaskStatusById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/database/task-statuses/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskStatusDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateDisplayOrder_WhenStatusExists_ShouldReturnUpdatedStatus() throws Exception {
        // Arrange
        Map<String, Integer> displayOrderUpdate = new HashMap<>();
        displayOrderUpdate.put("displayOrder", 3);

        when(taskStatusService.updateDisplayOrder(1, 3)).thenReturn(Optional.of(taskStatusDTO));

        // Act & Assert
        mockMvc.perform(patch("/database/task-statuses/1/display-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(displayOrderUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("In Progress"));
    }

    @Test
    public void updateDisplayOrder_WithMissingDisplayOrder_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Map<String, Integer> emptyUpdate = new HashMap<>();

        // Act & Assert
        mockMvc.perform(patch("/database/task-statuses/1/display-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyUpdate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateDisplayOrder_WhenStatusDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        Map<String, Integer> displayOrderUpdate = new HashMap<>();
        displayOrderUpdate.put("displayOrder", 3);

        when(taskStatusService.updateDisplayOrder(99, 3)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(patch("/database/task-statuses/99/display-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(displayOrderUpdate)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteTaskStatus_WhenStatusExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(taskStatusService.getTaskStatusById(1)).thenReturn(Optional.of(taskStatusDTO));
        doNothing().when(taskStatusService).deleteTaskStatus(1);

        // Act & Assert
        mockMvc.perform(delete("/database/task-statuses/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteTaskStatus_WhenStatusDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskStatusService.getTaskStatusById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/database/task-statuses/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteTaskStatus_WhenStatusHasAssignedTasks_ShouldReturnConflict() throws Exception {
        // Arrange
        when(taskStatusService.getTaskStatusById(1)).thenReturn(Optional.of(taskStatusDTO));
        doThrow(new IllegalStateException("Cannot delete status with assigned tasks"))
                .when(taskStatusService).deleteTaskStatus(1);

        // Act & Assert
        mockMvc.perform(delete("/database/task-statuses/1"))
                .andExpect(status().isConflict());
    }
}