package com.example.backend.controllers;

import com.example.backend.dto.ReportDTO;
import com.example.backend.dto.ReportTypeDTO;
import com.example.backend.dto.UserDTO;
import com.example.backend.models.ReportType;
import com.example.backend.models.User;
import com.example.backend.services.ReportService;
import com.example.backend.services.ReportTypeService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ReportControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ReportService reportService;

    @Mock
    private ReportTypeService reportTypeService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReportController reportController;

    private ReportDTO reportDTO;
    private List<ReportDTO> reportDTOList;
    private ReportTypeDTO reportTypeDTO;
    private UserDTO userDTO;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDateTime serialization

        // Initialize test data
        reportDTO = new ReportDTO();
        reportDTO.setId(1);
        reportDTO.setName("Monthly Progress Report");
        reportDTO.setTypeId(1);
        reportDTO.setCreatedById(1);
        reportDTO.setParameters("{\"month\":\"January\",\"year\":2025}");
        reportDTO.setFileName("jan_2025_report.pdf");
        reportDTO.setFilePath("/reports/2025/jan_2025_report.pdf");
        reportDTO.setCreatedAt(LocalDateTime.now());
        reportDTO.setTypeName("Monthly Report");
        reportDTO.setCreatedByUsername("manager1");
        reportDTO.setCreatedByFullName("John Manager");

        ReportDTO reportDTO2 = new ReportDTO();
        reportDTO2.setId(2);
        reportDTO2.setName("Project Overview Report");
        reportDTO2.setTypeId(2);
        reportDTO2.setCreatedById(1);
        reportDTO2.setFileName("project_overview.pdf");
        reportDTO2.setCreatedAt(LocalDateTime.now());

        reportDTOList = Arrays.asList(reportDTO, reportDTO2);

        reportTypeDTO = new ReportTypeDTO();
        reportTypeDTO.setId(1);
        reportTypeDTO.setName("Monthly Report");
        reportTypeDTO.setDescription("Monthly progress reports");

        userDTO = new UserDTO();
        userDTO.setId(1);
        userDTO.setUsername("manager1");
        userDTO.setFirstName("John");
        userDTO.setLastName("Manager");
    }

    @Test
    public void getAllReports_ShouldReturnListOfReports() throws Exception {
        // Arrange
        when(reportService.getAllReports()).thenReturn(reportDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Monthly Progress Report"))
                .andExpect(jsonPath("$[1].name").value("Project Overview Report"));
    }

    @Test
    public void getReportById_WhenExists_ShouldReturnReport() throws Exception {
        // Arrange
        when(reportService.getReportById(1L)).thenReturn(Optional.of(reportDTO));

        // Act & Assert
        mockMvc.perform(get("/database/reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Monthly Progress Report"))
                .andExpect(jsonPath("$.typeName").value("Monthly Report"));
    }

    @Test
    public void getReportById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(reportService.getReportById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/reports/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createReport_WithValidData_ShouldReturnCreatedReport() throws Exception {
        // Arrange
        when(reportService.saveReport(any(ReportDTO.class))).thenReturn(reportDTO);

        // Act & Assert
        mockMvc.perform(post("/database/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reportDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Monthly Progress Report"));
    }

    @Test
    public void deleteReport_WhenReportExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(reportService.deleteReport(1L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/database/reports/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteReport_WhenReportDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(reportService.deleteReport(99L)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/database/reports/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getReportsByTypeId_WhenTypeExists_ShouldReturnReportsList() throws Exception {
        // Arrange
        ReportType reportType = new ReportType();
        reportType.setId(1);
        reportType.setName("Monthly Report");

        when(reportTypeService.getReportTypeById(1)).thenReturn(Optional.of(reportTypeDTO));
        when(reportService.getReportsByType(any(ReportType.class))).thenReturn(reportDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/reports/type/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Monthly Progress Report"))
                .andExpect(jsonPath("$[1].name").value("Project Overview Report"));
    }

    @Test
    public void getReportsByTypeId_WhenTypeDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(reportTypeService.getReportTypeById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/reports/type/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getReportsByUserId_WhenUserExists_ShouldReturnReportsList() throws Exception {
        // Arrange
        User user = new User();
        user.setId(1);

        when(userService.getUserById(1)).thenReturn(Optional.of(userDTO));
        when(reportService.getReportsByCreatedBy(any(User.class))).thenReturn(reportDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/reports/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Monthly Progress Report"))
                .andExpect(jsonPath("$[1].name").value("Project Overview Report"));
    }

    @Test
    public void getReportsByUserId_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(userService.getUserById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/reports/user/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateReport_WhenReportExists_ShouldReturnUpdatedReport() throws Exception {
        // Arrange
        when(reportService.getReportById(1L)).thenReturn(Optional.of(reportDTO));
        when(reportService.saveReport(any(ReportDTO.class))).thenReturn(reportDTO);

        // Update the report
        ReportDTO updatedReport = new ReportDTO();
        updatedReport.setId(1);
        updatedReport.setName("Updated Monthly Progress Report");
        updatedReport.setTypeId(1);
        updatedReport.setCreatedById(1);

        // Act & Assert
        mockMvc.perform(put("/database/reports/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedReport)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Monthly Progress Report")); // Mock returns original

        // Verify that the ID was set in the DTO before saving
        verify(reportService).saveReport(argThat(dto -> dto.getId() == 1));
    }

    @Test
    public void updateReport_WhenReportDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(reportService.getReportById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/database/reports/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reportDTO)))
                .andExpect(status().isNotFound());
    }
}