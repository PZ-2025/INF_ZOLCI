package com.example.backend.services;

import com.example.backend.dto.ReportDTO;
import com.example.backend.models.Report;
import com.example.backend.models.ReportType;
import com.example.backend.models.User;
import com.example.backend.repository.ReportRepository;
import com.example.backend.repository.ReportTypeRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportTypeRepository reportTypeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportService reportService;

    private Report report;
    private ReportDTO reportDTO;
    private ReportType reportType;
    private User user;

    @BeforeEach
    void setUp() {
        // Initialize test data
        reportType = new ReportType();
        reportType.setId(1);
        reportType.setName("Monthly Report");
        reportType.setDescription("Monthly construction progress report");

        user = new User();
        user.setId(1);
        user.setUsername("manager1");
        user.setFirstName("John");
        user.setLastName("Manager");

        report = new Report();
        report.setId(1);
        report.setName("February 2025 Progress Report");
        report.setType(reportType);
        report.setCreatedBy(user);
        report.setParameters("{\"month\":\"February\",\"year\":2025}");
        report.setFileName("feb_2025_report.pdf");
        report.setFilePath("/reports/2025/feb_2025_report.pdf");
        report.setCreatedAt(LocalDateTime.now());

        reportDTO = new ReportDTO();
        reportDTO.setId(1);
        reportDTO.setName("February 2025 Progress Report");
        reportDTO.setTypeId(1);
        reportDTO.setCreatedById(1);
        reportDTO.setParameters("{\"month\":\"February\",\"year\":2025}");
        reportDTO.setFileName("feb_2025_report.pdf");
        reportDTO.setFilePath("/reports/2025/feb_2025_report.pdf");
        reportDTO.setCreatedAt(LocalDateTime.now());
        reportDTO.setTypeName("Monthly Report");
        reportDTO.setCreatedByUsername("manager1");
        reportDTO.setCreatedByFullName("John Manager");
    }

    @Test
    void mapToDTO_ShouldCorrectlyMapEntityToDTO() {
        // Act
        ReportDTO result = reportService.mapToDTO(report);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("February 2025 Progress Report", result.getName());
        assertEquals(1, result.getTypeId());
        assertEquals("Monthly Report", result.getTypeName());
        assertEquals(1, result.getCreatedById());
        assertEquals("manager1", result.getCreatedByUsername());
        assertEquals("John Manager", result.getCreatedByFullName());
        assertEquals("{\"month\":\"February\",\"year\":2025}", result.getParameters());
        assertEquals("feb_2025_report.pdf", result.getFileName());
        assertEquals("/reports/2025/feb_2025_report.pdf", result.getFilePath());
    }

    @Test
    void mapToDTO_WithNullReport_ShouldReturnNull() {
        // Act
        ReportDTO result = reportService.mapToDTO(null);

        // Assert
        assertNull(result);
    }

    @Test
    void mapToEntity_ShouldCorrectlyMapDTOToEntity() {
        // Arrange
        when(reportTypeRepository.findById(1)).thenReturn(Optional.of(reportType));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        // Act
        Report result = reportService.mapToEntity(reportDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("February 2025 Progress Report", result.getName());
        assertEquals(reportType, result.getType());
        assertEquals(user, result.getCreatedBy());
        assertEquals("{\"month\":\"February\",\"year\":2025}", result.getParameters());
        assertEquals("feb_2025_report.pdf", result.getFileName());
        assertEquals("/reports/2025/feb_2025_report.pdf", result.getFilePath());
    }

    @Test
    void mapToEntity_WithNullDTO_ShouldReturnNull() {
        // Act
        Report result = reportService.mapToEntity(null);

        // Assert
        assertNull(result);
    }

    @Test
    void getReportsByType_ShouldReturnReportsForType() {
        // Arrange
        when(reportRepository.findByType(reportType)).thenReturn(Arrays.asList(report));

        // Act
        List<ReportDTO> result = reportService.getReportsByType(reportType);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("February 2025 Progress Report", result.get(0).getName());
        assertEquals(1, result.get(0).getTypeId());
    }

    @Test
    void getReportsByCreatedBy_ShouldReturnReportsCreatedByUser() {
        // Arrange
        when(reportRepository.findByCreatedBy(user)).thenReturn(Arrays.asList(report));

        // Act
        List<ReportDTO> result = reportService.getReportsByCreatedBy(user);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("February 2025 Progress Report", result.get(0).getName());
        assertEquals(1, result.get(0).getCreatedById());
    }

    @Test
    void saveReport_ShouldMapAndSaveReport() {
        // Arrange
        when(reportTypeRepository.findById(1)).thenReturn(Optional.of(reportType));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(reportRepository.save(any(Report.class))).thenReturn(report);

        // Act
        ReportDTO result = reportService.saveReport(reportDTO);

        // Assert
        assertNotNull(result);
        assertEquals("February 2025 Progress Report", result.getName());
        assertEquals(1, result.getTypeId());
        assertEquals(1, result.getCreatedById());
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void deleteReport_WhenReportExists_ShouldReturnTrue() {
        // Arrange
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        // Act
        boolean result = reportService.deleteReport(1L);

        // Assert
        assertTrue(result);
        verify(reportRepository).delete(report);
    }

    @Test
    void deleteReport_WhenReportDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        boolean result = reportService.deleteReport(999L);

        // Assert
        assertFalse(result);
        verify(reportRepository, never()).delete(any(Report.class));
    }

    @Test
    void getReportById_WhenExists_ShouldReturnReport() {
        // Arrange
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        // Act
        Optional<ReportDTO> result = reportService.getReportById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("February 2025 Progress Report", result.get().getName());
        assertEquals(1, result.get().getTypeId());
        assertEquals(1, result.get().getCreatedById());
    }

    @Test
    void getReportById_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ReportDTO> result = reportService.getReportById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getAllReports_ShouldReturnAllReports() {
        // Arrange
        when(reportRepository.findAll()).thenReturn(Arrays.asList(report));

        // Act
        List<ReportDTO> result = reportService.getAllReports();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("February 2025 Progress Report", result.get(0).getName());
    }
}