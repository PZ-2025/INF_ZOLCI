package com.example.backend.services;

import com.example.backend.dto.ReportTypeDTO;
import com.example.backend.models.Report;
import com.example.backend.models.ReportType;
import com.example.backend.repository.ReportTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportTypeServiceTest {

    @Mock
    private ReportTypeRepository reportTypeRepository;

    @InjectMocks
    private ReportTypeService reportTypeService;

    private ReportType reportType;
    private ReportTypeDTO reportTypeDTO;

    @BeforeEach
    void setUp() {
        // Initialize test data
        reportType = new ReportType();
        reportType.setId(1);
        reportType.setName("Monthly Report");
        reportType.setDescription("Monthly construction progress report");
        reportType.setTemplatePath("/templates/monthly_report.html");
        reportType.setReports(new HashSet<>());

        reportTypeDTO = new ReportTypeDTO();
        reportTypeDTO.setId(1);
        reportTypeDTO.setName("Monthly Report");
        reportTypeDTO.setDescription("Monthly construction progress report");
        reportTypeDTO.setTemplatePath("/templates/monthly_report.html");
    }

    @Test
    void mapToDTO_ShouldCorrectlyMapEntityToDTO() {
        // Act
        ReportTypeDTO result = reportTypeService.mapToDTO(reportType);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Monthly Report", result.getName());
        assertEquals("Monthly construction progress report", result.getDescription());
        assertEquals("/templates/monthly_report.html", result.getTemplatePath());
    }

    @Test
    void mapToDTO_WithNullEntity_ShouldReturnNull() {
        // Act
        ReportTypeDTO result = reportTypeService.mapToDTO(null);

        // Assert
        assertNull(result);
    }

    @Test
    void mapToEntity_ShouldCorrectlyMapDTOToEntity() {
        // Act
        ReportType result = reportTypeService.mapToEntity(reportTypeDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Monthly Report", result.getName());
        assertEquals("Monthly construction progress report", result.getDescription());
        assertEquals("/templates/monthly_report.html", result.getTemplatePath());
    }

    @Test
    void mapToEntity_WithNullDTO_ShouldReturnNull() {
        // Act
        ReportType result = reportTypeService.mapToEntity(null);

        // Assert
        assertNull(result);
    }

    @Test
    void getAllReportTypes_ShouldReturnAllTypes() {
        // Arrange
        when(reportTypeRepository.findAll()).thenReturn(Arrays.asList(reportType));

        // Act
        List<ReportTypeDTO> result = reportTypeService.getAllReportTypes();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Monthly Report", result.get(0).getName());
    }

    @Test
    void getReportTypeById_WhenExists_ShouldReturnType() {
        // Arrange
        when(reportTypeRepository.findById(1)).thenReturn(Optional.of(reportType));

        // Act
        Optional<ReportTypeDTO> result = reportTypeService.getReportTypeById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Monthly Report", result.get().getName());
        assertEquals("Monthly construction progress report", result.get().getDescription());
    }

    @Test
    void getReportTypeById_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(reportTypeRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<ReportTypeDTO> result = reportTypeService.getReportTypeById(999);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getReportTypeByName_WhenExists_ShouldReturnType() {
        // Arrange
        when(reportTypeRepository.findByName("Monthly Report")).thenReturn(Optional.of(reportType));

        // Act
        Optional<ReportTypeDTO> result = reportTypeService.getReportTypeByName("Monthly Report");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("/templates/monthly_report.html", result.get().getTemplatePath());
    }

    @Test
    void getReportTypeByName_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(reportTypeRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<ReportTypeDTO> result = reportTypeService.getReportTypeByName("Nonexistent");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void saveReportType_ShouldMapAndSaveType() {
        // Arrange
        when(reportTypeRepository.save(any(ReportType.class))).thenReturn(reportType);

        // Act
        ReportType result = reportTypeService.saveReportType(reportTypeDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Monthly Report", result.getName());
        assertEquals("Monthly construction progress report", result.getDescription());
        verify(reportTypeRepository).save(any(ReportType.class));
    }

    @Test
    void createReportType_ShouldCreateAndSaveType() {
        // Arrange
        when(reportTypeRepository.save(any(ReportType.class))).thenReturn(reportType);

        // Act
        ReportType result = reportTypeService.createReportType(
                "Monthly Report",
                "Monthly construction progress report",
                "/templates/monthly_report.html"
        );

        // Assert
        assertNotNull(result);
        assertEquals("Monthly Report", result.getName());
        assertEquals("Monthly construction progress report", result.getDescription());
        assertEquals("/templates/monthly_report.html", result.getTemplatePath());
        verify(reportTypeRepository).save(any(ReportType.class));
    }

    @Test
    void deleteReportType_WhenTypeHasNoReports_ShouldDelete() {
        // Arrange
        when(reportTypeRepository.findById(1)).thenReturn(Optional.of(reportType));

        // Act
        reportTypeService.deleteReportType(1);

        // Assert
        verify(reportTypeRepository).deleteById(1);
    }

    @Test
    void deleteReportType_WhenTypeHasReports_ShouldThrowException() {
        // Arrange
        ReportType typeWithReports = new ReportType();
        typeWithReports.setId(1);
        typeWithReports.setName("Monthly Report");

        Set<Report> reports = new HashSet<>();
        reports.add(new Report());
        typeWithReports.setReports(reports);

        when(reportTypeRepository.findById(1)).thenReturn(Optional.of(typeWithReports));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> reportTypeService.deleteReportType(1));

        assertEquals("Nie można usunąć typu raportu, do którego przypisane są raporty", exception.getMessage());
        verify(reportTypeRepository, never()).deleteById(anyInt());
    }

    @Test
    void existsByName_WhenNameExists_ShouldReturnTrue() {
        // Arrange
        when(reportTypeRepository.findByName("Monthly Report")).thenReturn(Optional.of(reportType));

        // Act
        boolean result = reportTypeService.existsByName("Monthly Report");

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByName_WhenNameDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(reportTypeRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

        // Act
        boolean result = reportTypeService.existsByName("Nonexistent");

        // Assert
        assertFalse(result);
    }
}