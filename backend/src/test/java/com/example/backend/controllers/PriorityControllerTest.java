package com.example.backend.controllers;

import com.example.backend.models.Priority;
import com.example.backend.services.PriorityService;
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

class PriorityControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PriorityService priorityService;

    @InjectMocks
    private PriorityController priorityController;

    private ObjectMapper objectMapper;
    private Priority lowPriority;
    private Priority mediumPriority;
    private Priority highPriority;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(priorityController)
                .build();

        objectMapper = new ObjectMapper();

        // Inicjalizacja priorytetów testowych
        lowPriority = new Priority();
        lowPriority.setId(1);
        lowPriority.setName("Niski");
        lowPriority.setValue(1);
        lowPriority.setColorCode("#28a745");

        mediumPriority = new Priority();
        mediumPriority.setId(2);
        mediumPriority.setName("Średni");
        mediumPriority.setValue(2);
        mediumPriority.setColorCode("#ffc107");

        highPriority = new Priority();
        highPriority.setId(3);
        highPriority.setName("Wysoki");
        highPriority.setValue(3);
        highPriority.setColorCode("#dc3545");
    }

    @Test
    void getAllPriorities_ShouldReturnAllPriorities() throws Exception {
        // Given
        List<Priority> priorities = Arrays.asList(lowPriority, mediumPriority, highPriority);
        when(priorityService.getAllPriorities()).thenReturn(priorities);

        // When & Then
        mockMvc.perform(get("/database/priorities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Niski")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Średni")))
                .andExpect(jsonPath("$[2].id", is(3)))
                .andExpect(jsonPath("$[2].name", is("Wysoki")));

        verify(priorityService, times(1)).getAllPriorities();
    }

    @Test
    void getAllPrioritiesSorted_ShouldReturnSortedPriorities() throws Exception {
        // Given
        List<Priority> sortedPriorities = Arrays.asList(highPriority, mediumPriority, lowPriority);
        when(priorityService.getAllPrioritiesSortedByValue()).thenReturn(sortedPriorities);

        // When & Then
        mockMvc.perform(get("/database/priorities/sorted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(3)))
                .andExpect(jsonPath("$[0].name", is("Wysoki")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Średni")))
                .andExpect(jsonPath("$[2].id", is(1)))
                .andExpect(jsonPath("$[2].name", is("Niski")));

        verify(priorityService, times(1)).getAllPrioritiesSortedByValue();
    }

    @Test
    void getPriorityById_WhenPriorityExists_ShouldReturnPriority() throws Exception {
        // Given
        when(priorityService.getPriorityById(1)).thenReturn(Optional.of(lowPriority));

        // When & Then
        mockMvc.perform(get("/database/priorities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Niski")))
                .andExpect(jsonPath("$.value", is(1)))
                .andExpect(jsonPath("$.colorCode", is("#28a745")));

        verify(priorityService, times(1)).getPriorityById(1);
    }

    @Test
    void getPriorityById_WhenPriorityDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(priorityService.getPriorityById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/database/priorities/99"))
                .andExpect(status().isNotFound());

        verify(priorityService, times(1)).getPriorityById(99);
    }

    @Test
    void getPriorityByName_WhenPriorityExists_ShouldReturnPriority() throws Exception {
        // Given
        when(priorityService.getPriorityByName("Wysoki")).thenReturn(Optional.of(highPriority));

        // When & Then
        mockMvc.perform(get("/database/priorities/name/Wysoki"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("Wysoki")))
                .andExpect(jsonPath("$.value", is(3)))
                .andExpect(jsonPath("$.colorCode", is("#dc3545")));

        verify(priorityService, times(1)).getPriorityByName("Wysoki");
    }

    @Test
    void getPriorityByName_WhenPriorityDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(priorityService.getPriorityByName("Nieistniejący")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/database/priorities/name/Nieistniejący"))
                .andExpect(status().isNotFound());

        verify(priorityService, times(1)).getPriorityByName("Nieistniejący");
    }

    @Test
    void createPriority_WhenNameIsUnique_ShouldReturnCreatedPriority() throws Exception {
        // Given
        Priority newPriority = new Priority();
        newPriority.setName("Krytyczny");
        newPriority.setValue(4);
        newPriority.setColorCode("#ff0000");

        when(priorityService.existsByName("Krytyczny")).thenReturn(false);
        when(priorityService.savePriority(any(Priority.class))).thenReturn(newPriority);

        // When & Then
        mockMvc.perform(post("/database/priorities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPriority)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Krytyczny")))
                .andExpect(jsonPath("$.value", is(4)))
                .andExpect(jsonPath("$.colorCode", is("#ff0000")));

        verify(priorityService, times(1)).existsByName("Krytyczny");
        verify(priorityService, times(1)).savePriority(any(Priority.class));
    }

    @Test
    void createPriority_WhenNameExists_ShouldReturnConflict() throws Exception {
        // Given
        Priority newPriority = new Priority();
        newPriority.setName("Wysoki");
        newPriority.setValue(4);
        newPriority.setColorCode("#ff0000");

        when(priorityService.existsByName("Wysoki")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/database/priorities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPriority)))
                .andExpect(status().isConflict());

        verify(priorityService, times(1)).existsByName("Wysoki");
        verify(priorityService, never()).savePriority(any(Priority.class));
    }

    @Test
    void createPriorityFromParams_WithValidData_ShouldReturnCreatedPriority() throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Krytyczny");
        payload.put("value", 4);
        payload.put("colorCode", "#ff0000");

        Priority createdPriority = new Priority();
        createdPriority.setId(4);
        createdPriority.setName("Krytyczny");
        createdPriority.setValue(4);
        createdPriority.setColorCode("#ff0000");

        when(priorityService.existsByName("Krytyczny")).thenReturn(false);
        when(priorityService.createPriority(eq("Krytyczny"), eq(4), eq("#ff0000"))).thenReturn(createdPriority);

        // When & Then
        mockMvc.perform(post("/database/priorities/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(4)))
                .andExpect(jsonPath("$.name", is("Krytyczny")))
                .andExpect(jsonPath("$.value", is(4)))
                .andExpect(jsonPath("$.colorCode", is("#ff0000")));

        verify(priorityService, times(1)).existsByName("Krytyczny");
        verify(priorityService, times(1)).createPriority(eq("Krytyczny"), eq(4), eq("#ff0000"));
    }

    @Test
    void createPriorityFromParams_WithMissingData_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Krytyczny");
        // Missing value

        // When & Then
        mockMvc.perform(post("/database/priorities/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());

        verify(priorityService, never()).existsByName(anyString());
        verify(priorityService, never()).createPriority(anyString(), anyInt(), anyString());
    }

    @Test
    void updatePriority_WhenPriorityExists_ShouldReturnUpdatedPriority() throws Exception {
        // Given
        Priority updatedPriority = new Priority();
        updatedPriority.setId(1);
        updatedPriority.setName("Niski zmodyfikowany");
        updatedPriority.setValue(1);
        updatedPriority.setColorCode("#00ff00");

        when(priorityService.getPriorityById(1)).thenReturn(Optional.of(lowPriority));
        when(priorityService.savePriority(any(Priority.class))).thenReturn(updatedPriority);

        // When & Then
        mockMvc.perform(put("/database/priorities/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedPriority)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Niski zmodyfikowany")))
                .andExpect(jsonPath("$.colorCode", is("#00ff00")));

        verify(priorityService, times(1)).getPriorityById(1);
        verify(priorityService, times(1)).savePriority(any(Priority.class));
    }

    @Test
    void updatePriority_WhenPriorityDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        Priority updatedPriority = new Priority();
        updatedPriority.setId(99);
        updatedPriority.setName("Nieistniejący");
        updatedPriority.setValue(5);

        when(priorityService.getPriorityById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/database/priorities/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedPriority)))
                .andExpect(status().isNotFound());

        verify(priorityService, times(1)).getPriorityById(99);
        verify(priorityService, never()).savePriority(any(Priority.class));
    }

    @Test
    void updatePriorityColor_WhenPriorityExists_ShouldReturnUpdatedPriority() throws Exception {
        // Given
        Map<String, String> payload = new HashMap<>();
        payload.put("colorCode", "#00ff00");

        Priority updatedPriority = new Priority();
        updatedPriority.setId(1);
        updatedPriority.setName("Niski");
        updatedPriority.setValue(1);
        updatedPriority.setColorCode("#00ff00");

        when(priorityService.updateColor(eq(1), eq("#00ff00"))).thenReturn(Optional.of(updatedPriority));

        // When & Then
        mockMvc.perform(patch("/database/priorities/1/color")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Niski")))
                .andExpect(jsonPath("$.colorCode", is("#00ff00")));

        verify(priorityService, times(1)).updateColor(eq(1), eq("#00ff00"));
    }

    @Test
    void updatePriorityColor_WhenPriorityDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        Map<String, String> payload = new HashMap<>();
        payload.put("colorCode", "#00ff00");

        when(priorityService.updateColor(eq(99), eq("#00ff00"))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(patch("/database/priorities/99/color")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound());

        verify(priorityService, times(1)).updateColor(eq(99), eq("#00ff00"));
    }

    @Test
    void updatePriorityColor_WithMissingColorCode_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, String> payload = new HashMap<>();
        // Missing colorCode

        // When & Then
        mockMvc.perform(patch("/database/priorities/1/color")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());

        verify(priorityService, never()).updateColor(anyInt(), anyString());
    }

    @Test
    void deletePriority_WhenPriorityExistsWithNoTasks_ShouldReturnNoContent() throws Exception {
        // Given
        when(priorityService.getPriorityById(1)).thenReturn(Optional.of(lowPriority));
        doNothing().when(priorityService).deletePriority(1);

        // When & Then
        mockMvc.perform(delete("/database/priorities/1"))
                .andExpect(status().isNoContent());

        verify(priorityService, times(1)).getPriorityById(1);
        verify(priorityService, times(1)).deletePriority(1);
    }

    @Test
    void deletePriority_WhenPriorityDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(priorityService.getPriorityById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/database/priorities/99"))
                .andExpect(status().isNotFound());

        verify(priorityService, times(1)).getPriorityById(99);
        verify(priorityService, never()).deletePriority(anyInt());
    }

    @Test
    void deletePriority_WhenPriorityHasTasks_ShouldReturnConflict() throws Exception {
        // Given
        when(priorityService.getPriorityById(1)).thenReturn(Optional.of(lowPriority));
        doThrow(new IllegalStateException("Nie można usunąć priorytetu, do którego przypisane są zadania"))
                .when(priorityService).deletePriority(1);

        // When & Then
        mockMvc.perform(delete("/database/priorities/1"))
                .andExpect(status().isConflict());

        verify(priorityService, times(1)).getPriorityById(1);
        verify(priorityService, times(1)).deletePriority(1);
    }
}