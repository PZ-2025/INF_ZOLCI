package com.example.backend.controllers;

import com.example.backend.models.TaskStatus;
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

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
    private TaskStatus startedStatus;
    private TaskStatus inProgressStatus;
    private TaskStatus completedStatus;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(taskStatusController)
                .build();

        objectMapper = new ObjectMapper();

        // Inicjalizacja statusów testowych
        startedStatus = new TaskStatus();
        startedStatus.setId(1);
        startedStatus.setName("Rozpoczęte");
        startedStatus.setProgressMin(0);
        startedStatus.setProgressMax(30);
        startedStatus.setDisplayOrder(1);

        inProgressStatus = new TaskStatus();
        inProgressStatus.setId(2);
        inProgressStatus.setName("W toku");
        inProgressStatus.setProgressMin(31);
        inProgressStatus.setProgressMax(99);
        inProgressStatus.setDisplayOrder(2);

        completedStatus = new TaskStatus();
        completedStatus.setId(3);
        completedStatus.setName("Zakończone");
        completedStatus.setProgressMin(100);
        completedStatus.setProgressMax(100);
        completedStatus.setDisplayOrder(3);
    }

    @Test
    void getAllTaskStatuses_ShouldReturnAllStatuses() throws Exception {
        // Given
        List<TaskStatus> statuses = Arrays.asList(startedStatus, inProgressStatus, completedStatus);
        when(taskStatusService.getAllTaskStatuses()).thenReturn(statuses);

        // When & Then
        mockMvc.perform(get("/database/task-statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Rozpoczęte")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("W toku")))
                .andExpect(jsonPath("$[2].id", is(3)))
                .andExpect(jsonPath("$[2].name", is("Zakończone")));

        verify(taskStatusService, times(1)).getAllTaskStatuses();
    }

    @Test
    void getAllTaskStatusesSorted_ShouldReturnSortedStatuses() throws Exception {
        // Given
        List<TaskStatus> sortedStatuses = Arrays.asList(startedStatus, inProgressStatus, completedStatus);
        when(taskStatusService.getAllTaskStatusesSorted()).thenReturn(sortedStatuses);

        // When & Then
        mockMvc.perform(get("/database/task-statuses/sorted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].displayOrder", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].displayOrder", is(2)))
                .andExpect(jsonPath("$[2].id", is(3)))
                .andExpect(jsonPath("$[2].displayOrder", is(3)));

        verify(taskStatusService, times(1)).getAllTaskStatusesSorted();
    }

    @Test
    void getTaskStatusById_WhenStatusExists_ShouldReturnStatus() throws Exception {
        // Given
        when(taskStatusService.getTaskStatusById(1)).thenReturn(Optional.of(startedStatus));

        // When & Then
        mockMvc.perform(get("/database/task-statuses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Rozpoczęte")))
                .andExpect(jsonPath("$.progressMin", is(0)))
                .andExpect(jsonPath("$.progressMax", is(30)))
                .andExpect(jsonPath("$.displayOrder", is(1)));

        verify(taskStatusService, times(1)).getTaskStatusById(1);
    }

    @Test
    void getTaskStatusById_WhenStatusDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(taskStatusService.getTaskStatusById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/database/task-statuses/99"))
                .andExpect(status().isNotFound());

        verify(taskStatusService, times(1)).getTaskStatusById(99);
    }

    @Test
    void getTaskStatusByName_WhenStatusExists_ShouldReturnStatus() throws Exception {
        // Given
        when(taskStatusService.getTaskStatusByName("Zakończone")).thenReturn(Optional.of(completedStatus));

        // When & Then
        mockMvc.perform(get("/database/task-statuses/name/Zakończone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("Zakończone")))
                .andExpect(jsonPath("$.progressMin", is(100)))
                .andExpect(jsonPath("$.progressMax", is(100)))
                .andExpect(jsonPath("$.displayOrder", is(3)));

        verify(taskStatusService, times(1)).getTaskStatusByName("Zakończone");
    }

    @Test
    void getTaskStatusByName_WhenStatusDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(taskStatusService.getTaskStatusByName("Nieistniejący")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/database/task-statuses/name/Nieistniejący"))
                .andExpect(status().isNotFound());

        verify(taskStatusService, times(1)).getTaskStatusByName("Nieistniejący");
    }

    @Test
    void createTaskStatus_WhenNameIsUnique_ShouldReturnCreatedStatus() throws Exception {
        // Given
        TaskStatus newStatus = new TaskStatus();
        newStatus.setName("Anulowane");
        newStatus.setProgressMin(0);
        newStatus.setProgressMax(0);
        newStatus.setDisplayOrder(4);

        when(taskStatusService.existsByName("Anulowane")).thenReturn(false);
        when(taskStatusService.saveTaskStatus(any(TaskStatus.class))).thenReturn(newStatus);

        // When & Then
        mockMvc.perform(post("/database/task-statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStatus)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Anulowane")))
                .andExpect(jsonPath("$.progressMin", is(0)))
                .andExpect(jsonPath("$.progressMax", is(0)))
                .andExpect(jsonPath("$.displayOrder", is(4)));

        verify(taskStatusService, times(1)).existsByName("Anulowane");
        verify(taskStatusService, times(1)).saveTaskStatus(any(TaskStatus.class));
    }

    @Test
    void createTaskStatus_WhenNameExists_ShouldReturnConflict() throws Exception {
        // Given
        TaskStatus newStatus = new TaskStatus();
        newStatus.setName("Zakończone");
        newStatus.setProgressMin(0);
        newStatus.setProgressMax(0);
        newStatus.setDisplayOrder(4);

        when(taskStatusService.existsByName("Zakończone")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/database/task-statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStatus)))
                .andExpect(status().isConflict());

        verify(taskStatusService, times(1)).existsByName("Zakończone");
        verify(taskStatusService, never()).saveTaskStatus(any(TaskStatus.class));
    }

    @Test
    void createTaskStatusFromParams_WithValidData_ShouldReturnCreatedStatus() throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Anulowane");
        payload.put("progressMin", 0);
        payload.put("progressMax", 0);
        payload.put("displayOrder", 4);

        TaskStatus createdStatus = new TaskStatus();
        createdStatus.setId(4);
        createdStatus.setName("Anulowane");
        createdStatus.setProgressMin(0);
        createdStatus.setProgressMax(0);
        createdStatus.setDisplayOrder(4);

        when(taskStatusService.existsByName("Anulowane")).thenReturn(false);
        when(taskStatusService.createTaskStatus(eq("Anulowane"), eq(0), eq(0), eq(4))).thenReturn(createdStatus);

        // When & Then
        mockMvc.perform(post("/database/task-statuses/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(4)))
                .andExpect(jsonPath("$.name", is("Anulowane")))
                .andExpect(jsonPath("$.progressMin", is(0)))
                .andExpect(jsonPath("$.progressMax", is(0)))
                .andExpect(jsonPath("$.displayOrder", is(4)));

        verify(taskStatusService, times(1)).existsByName("Anulowane");
        verify(taskStatusService, times(1)).createTaskStatus(eq("Anulowane"), eq(0), eq(0), eq(4));
    }

    @Test
    void createTaskStatusFromParams_WithMissingData_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Anulowane");
        payload.put("progressMin", 0);
        // Missing progressMax and displayOrder

        // When & Then
        mockMvc.perform(post("/database/task-statuses/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());

        verify(taskStatusService, never()).existsByName(anyString());
        verify(taskStatusService, never()).createTaskStatus(anyString(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void updateTaskStatus_WhenStatusExists_ShouldReturnUpdatedStatus() throws Exception {
        // Given
        TaskStatus updatedStatus = new TaskStatus();
        updatedStatus.setId(1);
        updatedStatus.setName("Rozpoczęte zmodyfikowane");
        updatedStatus.setProgressMin(5);
        updatedStatus.setProgressMax(35);
        updatedStatus.setDisplayOrder(1);

        when(taskStatusService.getTaskStatusById(1)).thenReturn(Optional.of(startedStatus));
        when(taskStatusService.saveTaskStatus(any(TaskStatus.class))).thenReturn(updatedStatus);

        // When & Then
        mockMvc.perform(put("/database/task-statuses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedStatus)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Rozpoczęte zmodyfikowane")))
                .andExpect(jsonPath("$.progressMin", is(5)))
                .andExpect(jsonPath("$.progressMax", is(35)));

        verify(taskStatusService, times(1)).getTaskStatusById(1);
        verify(taskStatusService, times(1)).saveTaskStatus(any(TaskStatus.class));
    }

    @Test
    void updateTaskStatus_WhenStatusDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        TaskStatus updatedStatus = new TaskStatus();
        updatedStatus.setId(99);
        updatedStatus.setName("Nieistniejący");
        updatedStatus.setProgressMin(0);
        updatedStatus.setProgressMax(0);

        when(taskStatusService.getTaskStatusById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/database/task-statuses/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedStatus)))
                .andExpect(status().isNotFound());

        verify(taskStatusService, times(1)).getTaskStatusById(99);
        verify(taskStatusService, never()).saveTaskStatus(any(TaskStatus.class));
    }

    @Test
    void updateDisplayOrder_WhenStatusExists_ShouldReturnUpdatedStatus() throws Exception {
        // Given
        Map<String, Integer> payload = new HashMap<>();
        payload.put("displayOrder", 5);

        TaskStatus updatedStatus = new TaskStatus();
        updatedStatus.setId(1);
        updatedStatus.setName("Rozpoczęte");
        updatedStatus.setProgressMin(0);
        updatedStatus.setProgressMax(30);
        updatedStatus.setDisplayOrder(5);

        when(taskStatusService.updateDisplayOrder(eq(1), eq(5))).thenReturn(Optional.of(updatedStatus));

        // When & Then
        mockMvc.perform(patch("/database/task-statuses/1/display-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Rozpoczęte")))
                .andExpect(jsonPath("$.displayOrder", is(5)));

        verify(taskStatusService, times(1)).updateDisplayOrder(eq(1), eq(5));
    }

    @Test
    void updateDisplayOrder_WhenStatusDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        Map<String, Integer> payload = new HashMap<>();
        payload.put("displayOrder", 5);

        when(taskStatusService.updateDisplayOrder(eq(99), eq(5))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(patch("/database/task-statuses/99/display-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound());

        verify(taskStatusService, times(1)).updateDisplayOrder(eq(99), eq(5));
    }

    @Test
    void updateDisplayOrder_WithMissingData_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        // Missing displayOrder

        // When & Then
        mockMvc.perform(patch("/database/task-statuses/1/display-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());

        verify(taskStatusService, never()).updateDisplayOrder(anyInt(), anyInt());
    }

    @Test
    void deleteTaskStatus_WhenStatusExistsWithNoTasks_ShouldReturnNoContent() throws Exception {
        // Given
        when(taskStatusService.getTaskStatusById(1)).thenReturn(Optional.of(startedStatus));
        doNothing().when(taskStatusService).deleteTaskStatus(1);

        // When & Then
        mockMvc.perform(delete("/database/task-statuses/1"))
                .andExpect(status().isNoContent());

        verify(taskStatusService, times(1)).getTaskStatusById(1);
        verify(taskStatusService, times(1)).deleteTaskStatus(1);
    }

    @Test
    void deleteTaskStatus_WhenStatusDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(taskStatusService.getTaskStatusById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/database/task-statuses/99"))
                .andExpect(status().isNotFound());

        verify(taskStatusService, times(1)).getTaskStatusById(99);
        verify(taskStatusService, never()).deleteTaskStatus(anyInt());
    }

    @Test
    void deleteTaskStatus_WhenStatusHasTasks_ShouldReturnConflict() throws Exception {
        // Given
        when(taskStatusService.getTaskStatusById(1)).thenReturn(Optional.of(startedStatus));
        doThrow(new IllegalStateException("Nie można usunąć statusu, do którego przypisane są zadania"))
                .when(taskStatusService).deleteTaskStatus(1);

        // When & Then
        mockMvc.perform(delete("/database/task-statuses/1"))
                .andExpect(status().isConflict());

        verify(taskStatusService, times(1)).getTaskStatusById(1);
        verify(taskStatusService, times(1)).deleteTaskStatus(1);
    }
}