package com.example.backend.controllers;

import com.example.backend.dto.ReportTypeDTO;
import com.example.backend.models.ReportType;
import com.example.backend.services.ReportTypeService;
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
public class ReportTypeControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ReportTypeService reportTypeService;

    @InjectMocks
    private ReportTypeController reportTypeController;

    private ReportTypeDTO reportTypeDTO;
    private List<ReportTypeDTO> reportTypeDTOList;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportTypeController).build();
        objectMapper = new ObjectMapper();

        // Initialize test data
        reportTypeDTO = new ReportTypeDTO();
        reportTypeDTO.setId(1);
        reportTypeDTO.setName("Monthly Report");
        reportTypeDTO.setDescription("Monthly construction progress report");
        reportTypeDTO.setTemplatePath("/templates/monthly_report.html");

        ReportTypeDTO reportTypeDTO2 = new ReportTypeDTO();
        reportTypeDTO2.setId(2);
        reportTypeDTO2.setName("Project Report");
        reportTypeDTO2.setDescription("Project overview report");
        reportTypeDTO2.setTemplatePath("/templates/project_report.html");

        reportTypeDTOList = Arrays.asList(reportTypeDTO, reportTypeDTO2);
    }

    @Test
    public void getAllReportTypes_ShouldReturnListOfReportTypes() throws Exception {
        // Arrange
        when(reportTypeService.getAllReportTypes()).thenReturn(reportTypeDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/report-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Monthly Report"))
                .andExpect(jsonPath("$[1].name").value("Project Report"));
    }

    @Test
    public void getReportTypeById_WhenExists_ShouldReturnReportType() throws Exception {
        // Arrange
        when(reportTypeService.getReportTypeById(1)).thenReturn(Optional.of(reportTypeDTO));

        // Act & Assert
        mockMvc.perform(get("/database/report-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Monthly Report"))
                .andExpect(jsonPath("$.description").value("Monthly construction progress report"));
    }

    @Test
    public void getReportTypeById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(reportTypeService.getReportTypeById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/report-types/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createReportType_WithValidData_ShouldReturnCreatedReportType() throws Exception {
        // Arrange
        ReportType savedEntity = new ReportType();
        savedEntity.setId(1);
        savedEntity.setName("Monthly Report");
        savedEntity.setDescription("Monthly construction progress report");
        savedEntity.setTemplatePath("/templates/monthly_report.html");

        when(reportTypeService.existsByName(anyString())).thenReturn(false);
        when(reportTypeService.saveReportType(any(ReportTypeDTO.class))).thenReturn(savedEntity);
        when(reportTypeService.mapToDTO(any(ReportType.class))).thenReturn(reportTypeDTO);

        // Act & Assert
        mockMvc.perform(post("/database/report-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reportTypeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Monthly Report"))
                .andExpect(jsonPath("$.description").value("Monthly construction progress report"));
    }

    @Test
    public void createReportType_WithExistingName_ShouldReturnConflict() throws Exception {
        // Arrange
        when(reportTypeService.existsByName(anyString())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/database/report-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reportTypeDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    public void updateReportType_WhenReportTypeExists_ShouldReturnUpdatedReportType() throws Exception {
        // Arrange
        ReportType updatedEntity = new ReportType();
        updatedEntity.setId(1);
        updatedEntity.setName("Updated Monthly Report");
        updatedEntity.setDescription("Updated description");
        updatedEntity.setTemplatePath("/templates/updated_monthly_report.html");

        ReportTypeDTO updatedDTO = new ReportTypeDTO();
        updatedDTO.setId(1);
        updatedDTO.setName("Updated Monthly Report");
        updatedDTO.setDescription("Updated description");
        updatedDTO.setTemplatePath("/templates/updated_monthly_report.html");

        when(reportTypeService.getReportTypeById(1)).thenReturn(Optional.of(reportTypeDTO));
        when(reportTypeService.saveReportType(any(ReportTypeDTO.class))).thenReturn(updatedEntity);
        when(reportTypeService.mapToDTO(any(ReportType.class))).thenReturn(updatedDTO);

        // Act & Assert
        mockMvc.perform(put("/database/report-types/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Monthly Report"))
                .andExpect(jsonPath("$.description").value("Updated description"));

        // Verify that the ID was set in the DTO before saving
        verify(reportTypeService).saveReportType(argThat(dto -> dto.getId() == 1));
    }

    @Test
    public void updateReportType_WhenReportTypeDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(reportTypeService.getReportTypeById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/database/report-types/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reportTypeDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteReportType_WhenReportTypeExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(reportTypeService.getReportTypeById(1)).thenReturn(Optional.of(reportTypeDTO));
        doNothing().when(reportTypeService).deleteReportType(1);

        // Act & Assert
        mockMvc.perform(delete("/database/report-types/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteReportType_WhenReportTypeDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(reportTypeService.getReportTypeById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/database/report-types/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteReportType_WhenReportTypeHasReports_ShouldReturnConflict() throws Exception {
        // Arrange
        when(reportTypeService.getReportTypeById(1)).thenReturn(Optional.of(reportTypeDTO));
        doThrow(new IllegalStateException("Cannot delete report type with reports"))
                .when(reportTypeService).deleteReportType(1);

        // Act & Assert
        mockMvc.perform(delete("/database/report-types/1"))
                .andExpect(status().isConflict());
    }
}