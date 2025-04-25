package com.example.backend.controllers;

import com.example.backend.dto.PriorityDTO;
import com.example.backend.services.PriorityService;
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
public class PriorityControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PriorityService priorityService;

    @InjectMocks
    private PriorityController priorityController;

    private PriorityDTO priorityDTO;
    private List<PriorityDTO> priorityDTOList;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(priorityController).build();
        objectMapper = new ObjectMapper();

        // Initialize test data
        priorityDTO = new PriorityDTO();
        priorityDTO.setId(1);
        priorityDTO.setName("High");
        priorityDTO.setValue(3);
        priorityDTO.setColorCode("#FF0000");

        PriorityDTO priorityDTO2 = new PriorityDTO();
        priorityDTO2.setId(2);
        priorityDTO2.setName("Medium");
        priorityDTO2.setValue(2);
        priorityDTO2.setColorCode("#FFFF00");

        priorityDTOList = Arrays.asList(priorityDTO, priorityDTO2);
    }

    @Test
    public void getAllPriorities_ShouldReturnListOfPriorities() throws Exception {
        // Arrange
        when(priorityService.getAllPriorities()).thenReturn(priorityDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/priorities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("High"))
                .andExpect(jsonPath("$[1].name").value("Medium"))
                .andExpect(jsonPath("$[0].value").value(3))
                .andExpect(jsonPath("$[1].value").value(2));
    }

    @Test
    public void getAllPrioritiesSorted_ShouldReturnSortedPriorities() throws Exception {
        // Arrange
        when(priorityService.getAllPrioritiesSortedByValue()).thenReturn(priorityDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/priorities/sorted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("High"))
                .andExpect(jsonPath("$[1].name").value("Medium"));
    }

    @Test
    public void getPriorityById_WhenExists_ShouldReturnPriority() throws Exception {
        // Arrange
        when(priorityService.getPriorityById(1)).thenReturn(Optional.of(priorityDTO));

        // Act & Assert
        mockMvc.perform(get("/database/priorities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("High"))
                .andExpect(jsonPath("$.value").value(3))
                .andExpect(jsonPath("$.colorCode").value("#FF0000"));
    }

    @Test
    public void getPriorityById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(priorityService.getPriorityById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/priorities/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getPriorityByName_WhenExists_ShouldReturnPriority() throws Exception {
        // Arrange
        when(priorityService.getPriorityByName("High")).thenReturn(Optional.of(priorityDTO));

        // Act & Assert
        mockMvc.perform(get("/database/priorities/name/High"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.value").value(3));
    }

    @Test
    public void getPriorityByName_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(priorityService.getPriorityByName("Unknown")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/priorities/name/Unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createPriority_WithValidData_ShouldReturnCreatedPriority() throws Exception {
        // Arrange
        when(priorityService.existsByName(anyString())).thenReturn(false);
        when(priorityService.savePriority(any(PriorityDTO.class))).thenReturn(priorityDTO);

        // Act & Assert
        mockMvc.perform(post("/database/priorities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(priorityDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("High"))
                .andExpect(jsonPath("$.value").value(3));
    }

    @Test
    public void createPriority_WithExistingName_ShouldReturnConflict() throws Exception {
        // Arrange
        when(priorityService.existsByName(anyString())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/database/priorities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(priorityDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    public void createPriorityFromParams_WithValidParams_ShouldReturnCreatedPriority() throws Exception {
        // Arrange
        Map<String, Object> params = new HashMap<>();
        params.put("name", "High");
        params.put("value", 3);
        params.put("colorCode", "#FF0000");

        when(priorityService.existsByName(anyString())).thenReturn(false);
        when(priorityService.createPriority(anyString(), anyInt(), anyString())).thenReturn(priorityDTO);

        // Act & Assert
        mockMvc.perform(post("/database/priorities/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("High"))
                .andExpect(jsonPath("$.value").value(3));
    }

    @Test
    public void createPriorityFromParams_WithMissingParams_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Map<String, Object> params = new HashMap<>();
        params.put("name", "High");
        // Missing required value parameter

        // Act & Assert
        mockMvc.perform(post("/database/priorities/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updatePriority_WhenPriorityExists_ShouldReturnUpdatedPriority() throws Exception {
        // Arrange
        when(priorityService.getPriorityById(1)).thenReturn(Optional.of(priorityDTO));
        when(priorityService.savePriority(any(PriorityDTO.class))).thenReturn(priorityDTO);

        // Update the priority DTO
        priorityDTO.setName("Very High");

        // Act & Assert
        mockMvc.perform(put("/database/priorities/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(priorityDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("High")); // Mock returns original

        // Verify that the ID was set in the DTO before saving
        verify(priorityService).savePriority(argThat(dto -> dto.getId() == 1));
    }

    @Test
    public void updatePriority_WhenPriorityDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(priorityService.getPriorityById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/database/priorities/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(priorityDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updatePriorityColor_WhenPriorityExists_ShouldReturnUpdatedPriority() throws Exception {
        // Arrange
        Map<String, String> colorUpdate = new HashMap<>();
        colorUpdate.put("colorCode", "#00FF00");

        when(priorityService.updateColor(1, "#00FF00")).thenReturn(Optional.of(priorityDTO));

        // Act & Assert
        mockMvc.perform(patch("/database/priorities/1/color")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(colorUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("High"));
    }

    @Test
    public void updatePriorityColor_WithMissingColorCode_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Map<String, String> emptyUpdate = new HashMap<>();

        // Act & Assert
        mockMvc.perform(patch("/database/priorities/1/color")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyUpdate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updatePriorityColor_WhenPriorityDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        Map<String, String> colorUpdate = new HashMap<>();
        colorUpdate.put("colorCode", "#00FF00");

        when(priorityService.updateColor(99, "#00FF00")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(patch("/database/priorities/99/color")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(colorUpdate)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deletePriority_WhenPriorityExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(priorityService.getPriorityById(1)).thenReturn(Optional.of(priorityDTO));
        doNothing().when(priorityService).deletePriority(1);

        // Act & Assert
        mockMvc.perform(delete("/database/priorities/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deletePriority_WhenPriorityDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(priorityService.getPriorityById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/database/priorities/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deletePriority_WhenPriorityHasAssignedTasks_ShouldReturnConflict() throws Exception {
        // Arrange
        when(priorityService.getPriorityById(1)).thenReturn(Optional.of(priorityDTO));
        doThrow(new IllegalStateException("Cannot delete priority with assigned tasks"))
                .when(priorityService).deletePriority(1);

        // Act & Assert
        mockMvc.perform(delete("/database/priorities/1"))
                .andExpect(status().isConflict());
    }
}