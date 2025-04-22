package com.example.backend.controllers;

import com.example.backend.dto.TaskStatusDTO;
import com.example.backend.services.TaskStatusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskStatusControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskStatusService taskStatusService;

    @InjectMocks
    private TaskStatusController taskStatusController;

    private ObjectMapper objectMapper;
    private TaskStatusDTO startedStatusDTO;
    private TaskStatusDTO inProgressStatusDTO;
    private TaskStatusDTO completedStatusDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(taskStatusController)
                .build();

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
    void getAllTaskStatuses_ShouldReturnAllStatuses() throws Exception {
        // Given
        List<TaskStatusDTO> statuses = Arrays.asList(startedStatusDTO, inProgressStatusDTO, completedStatusDTO);
        when(taskStatusService.getAllTaskStatuses()).thenReturn(statuses);

        // When & Then
        mockMvc.perform(get("/database/task-statuses")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Rozpoczęte"))
                .andExpect(jsonPath("$[1].name").value("W toku"))
                .andExpect(jsonPath("$[2].name").value("Zakończone"));
    }

    @Test
    void getAllTaskStatusesSorted_ShouldReturnSortedStatuses() throws Exception {
        // Given
        List<TaskStatusDTO> sortedStatuses = Arrays.asList(startedStatusDTO, inProgressStatusDTO, completedStatusDTO);
        when(taskStatusService.getAllTaskStatusesSorted()).thenReturn(sortedStatuses);

        // When & Then
        mockMvc.perform(get("/database/task-statuses/sorted")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].displayOrder").value(1))
                .andExpect(jsonPath("$[1].displayOrder").value(2))
                .andExpect(jsonPath("$[2].displayOrder").value(3));
    }

    @Test
    void getTaskStatusById_WhenExists_ShouldReturnStatus() throws Exception {
        // Given
        when(taskStatusService.getTaskStatusById(1)).thenReturn(Optional.of(startedStatusDTO));

        // When & Then
        mockMvc.perform(get("/database/task-statuses/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Rozpoczęte"));
    }

    @Test
    void createTaskStatus_ShouldCreateAndReturnStatus() throws Exception {
        // Given
        when(taskStatusService.saveTaskStatus(any(TaskStatusDTO.class))).thenReturn(startedStatusDTO);
        when(taskStatusService.existsByName(anyString())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/database/task-statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(startedStatusDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Rozpoczęte"));
    }

    @Test
    void updateTaskStatus_WhenExists_ShouldUpdateAndReturnStatus() throws Exception {
        // Given
        when(taskStatusService.getTaskStatusById(1)).thenReturn(Optional.of(startedStatusDTO));
        when(taskStatusService.saveTaskStatus(any(TaskStatusDTO.class))).thenReturn(startedStatusDTO);

        // When & Then
        mockMvc.perform(put("/database/task-statuses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(startedStatusDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rozpoczęte"));
    }

    @Test
    void updateDisplayOrder_WhenExists_ShouldUpdateOrder() throws Exception {
        // Given
        startedStatusDTO.setDisplayOrder(5);
        when(taskStatusService.updateDisplayOrder(1, 5)).thenReturn(Optional.of(startedStatusDTO));

        // When & Then
        mockMvc.perform(patch("/database/task-statuses/1/display-order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayOrder\": 5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayOrder").value(5));
    }
}