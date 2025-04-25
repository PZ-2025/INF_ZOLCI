package com.example.backend.controllers;

import com.example.backend.dto.TaskDTO;
import com.example.backend.services.TaskService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TaskControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private TaskDTO taskDTO;
    private List<TaskDTO> taskDTOList;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDate and LocalDateTime serialization

        // Initialize test data
        taskDTO = new TaskDTO();
        taskDTO.setId(1);
        taskDTO.setTitle("Build Foundation");
        taskDTO.setDescription("Prepare and build the foundation for the house");
        taskDTO.setTeamId(1);
        taskDTO.setPriorityId(1);
        taskDTO.setStatusId(1);
        taskDTO.setStartDate(LocalDate.now());
        taskDTO.setDeadline(LocalDate.now().plusDays(7));
        taskDTO.setCreatedById(1);
        taskDTO.setCreatedAt(LocalDateTime.now());

        TaskDTO taskDTO2 = new TaskDTO();
        taskDTO2.setId(2);
        taskDTO2.setTitle("Install Plumbing");
        taskDTO2.setDescription("Install plumbing system in the house");
        taskDTO2.setTeamId(2);
        taskDTO2.setPriorityId(2);
        taskDTO2.setStatusId(2);
        taskDTO2.setStartDate(LocalDate.now().plusDays(7));
        taskDTO2.setDeadline(LocalDate.now().plusDays(14));
        taskDTO2.setCreatedById(1);
        taskDTO2.setCreatedAt(LocalDateTime.now());

        taskDTOList = Arrays.asList(taskDTO, taskDTO2);
    }

    @Test
    public void getAllTasks_ShouldReturnListOfTasks() throws Exception {
        // Arrange
        when(taskService.getAllTasks()).thenReturn(taskDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Build Foundation"))
                .andExpect(jsonPath("$[1].title").value("Install Plumbing"));
    }

    @Test
    public void getTaskById_WhenExists_ShouldReturnTask() throws Exception {
        // Arrange
        when(taskService.getTaskById(1)).thenReturn(Optional.of(taskDTO));

        // Act & Assert
        mockMvc.perform(get("/database/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Build Foundation"))
                .andExpect(jsonPath("$.description").value("Prepare and build the foundation for the house"));
    }

    @Test
    public void getTaskById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskService.getTaskById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/tasks/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createTask_WithValidData_ShouldReturnCreatedTask() throws Exception {
        // Arrange
        when(taskService.saveTask(any(TaskDTO.class))).thenReturn(taskDTO);

        // Act & Assert
        mockMvc.perform(post("/database/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Build Foundation"))
                .andExpect(jsonPath("$.teamId").value(1));
    }

    @Test
    public void createTask_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(taskService.saveTask(any(TaskDTO.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/database/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void updateTask_WhenTaskExists_ShouldReturnUpdatedTask() throws Exception {
        // Arrange
        when(taskService.getTaskById(1)).thenReturn(Optional.of(taskDTO));
        when(taskService.updateTask(any(TaskDTO.class))).thenReturn(taskDTO);

        // Update task data
        TaskDTO updatedTask = new TaskDTO();
        updatedTask.setId(1);
        updatedTask.setTitle("Updated Foundation Build");
        updatedTask.setDescription("Updated description");
        updatedTask.setPriorityId(2);
        updatedTask.setStatusId(2);

        // Act & Assert
        mockMvc.perform(put("/database/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Build Foundation")); // Mock returns original

        // Verify service was called with right ID
        verify(taskService).updateTask(argThat(dto -> dto.getId() == 1));
    }

    @Test
    public void updateTask_WhenTaskDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskService.getTaskById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/database/tasks/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateTask_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(taskService.getTaskById(1)).thenReturn(Optional.of(taskDTO));
        when(taskService.updateTask(any(TaskDTO.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(put("/database/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Wystąpił błąd podczas aktualizacji zadania"));
    }

    @Test
    public void deleteTask_WhenTaskExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(taskService.getTaskById(1)).thenReturn(Optional.of(taskDTO));
        doNothing().when(taskService).deleteTask(1);

        // Act & Assert
        mockMvc.perform(delete("/database/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteTask_WhenTaskDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskService.getTaskById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/database/tasks/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getTasksByTeamId_ShouldReturnTasksForTeam() throws Exception {
        // Arrange
        when(taskService.getTasksByTeamId(1)).thenReturn(Collections.singletonList(taskDTO));

        // Act & Assert
        mockMvc.perform(get("/database/tasks/team/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Build Foundation"));
    }

    @Test
    public void getTasksByStatusId_ShouldReturnTasksWithStatus() throws Exception {
        // Arrange
        when(taskService.getTasksByStatusId(1)).thenReturn(Collections.singletonList(taskDTO));

        // Act & Assert
        mockMvc.perform(get("/database/tasks/status/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Build Foundation"));
    }

    @Test
    public void getTasksByPriorityId_ShouldReturnTasksWithPriority() throws Exception {
        // Arrange
        when(taskService.getTasksByPriorityId(1)).thenReturn(Collections.singletonList(taskDTO));

        // Act & Assert
        mockMvc.perform(get("/database/tasks/priority/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Build Foundation"));
    }

    @Test
    public void getTaskByTitle_WhenExists_ShouldReturnTask() throws Exception {
        // Arrange
        when(taskService.getTaskByTitle("Build Foundation")).thenReturn(Optional.of(taskDTO));

        // Act & Assert
        mockMvc.perform(get("/database/tasks/title/Build Foundation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Prepare and build the foundation for the house"));
    }

    @Test
    public void getTaskByTitle_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(taskService.getTaskByTitle("Nonexistent Task")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/tasks/title/Nonexistent Task"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getTasksWithDeadlineBefore_WithValidDate_ShouldReturnTasks() throws Exception {
        // Arrange
        LocalDate deadlineDate = LocalDate.now().plusDays(10);
        String dateString = deadlineDate.toString();

        when(taskService.getTasksWithDeadlineBefore(deadlineDate)).thenReturn(taskDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/tasks/deadline-before/" + dateString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Build Foundation"))
                .andExpect(jsonPath("$[1].title").value("Install Plumbing"));
    }

    @Test
    public void getTasksWithDeadlineBefore_WithInvalidDate_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/database/tasks/deadline-before/invalid-date"))
                .andExpect(status().isBadRequest());
    }
}