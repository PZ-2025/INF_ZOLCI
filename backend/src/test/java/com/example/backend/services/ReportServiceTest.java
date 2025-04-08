package com.example.backend.services;

import com.example.backend.models.Report;
import com.example.backend.models.ReportType;
import com.example.backend.models.User;
import com.example.backend.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    private User user;
    private ReportType teamReportType;
    private ReportType userReportType;
    private Report teamReport;
    private Report userReport;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        now = LocalDateTime.now();

        // Inicjalizacja użytkownika testowego
        user = new User();
        user.setId(1);
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setIsActive(true);

        // Inicjalizacja typów raportów testowych
        teamReportType = new ReportType();
        teamReportType.setId(1);
        teamReportType.setName("Raport zespołu");
        teamReportType.setDescription("Podsumowanie zadań zespołu");
        teamReportType.setTemplatePath("/templates/reports/team_report.html");

        userReportType = new ReportType();
        userReportType.setId(2);
        userReportType.setName("Raport użytkownika");
        userReportType.setDescription("Podsumowanie zadań użytkownika");
        userReportType.setTemplatePath("/templates/reports/user_report.html");

        // Inicjalizacja raportów testowych
        teamReport = new Report();
        teamReport.setId(1);
        teamReport.setName("Raport zespołu Alpha za kwiecień 2023");
        teamReport.setType(teamReportType);
        teamReport.setCreatedBy(user);
        teamReport.setParameters("{\"teamId\": 1, \"month\": 4, \"year\": 2023}");
        teamReport.setFileName("team_alpha_report_2023_04.pdf");
        teamReport.setFilePath("/reports/team_alpha_report_2023_04.pdf");
        teamReport.setCreatedAt(now);

        userReport = new Report();
        userReport.setId(2);
        userReport.setName("Raport użytkownika Jan Kowalski za maj 2023");
        userReport.setType(userReportType);
        userReport.setCreatedBy(user);
        userReport.setParameters("{\"userId\": 3, \"month\": 5, \"year\": 2023}");
        userReport.setFileName("user_jan_kowalski_report_2023_05.pdf");
        userReport.setFilePath("/reports/user_jan_kowalski_report_2023_05.pdf");
        userReport.setCreatedAt(now);
    }

    @Test
    void getReportsByType_ShouldReturnReportsForType() {
        // Given
        List<Report> expectedReports = Arrays.asList(teamReport);
        when(reportRepository.findByType(teamReportType)).thenReturn(expectedReports);

        // When
        List<Report> actualReports = reportService.getReportsByType(teamReportType);

        // Then
        assertEquals(expectedReports.size(), actualReports.size());
        assertEquals(expectedReports.get(0).getId(), actualReports.get(0).getId());
        assertEquals(expectedReports.get(0).getName(), actualReports.get(0).getName());
        verify(reportRepository, times(1)).findByType(teamReportType);
    }

    @Test
    void getReportsByCreatedBy_ShouldReturnReportsCreatedByUser() {
        // Given
        List<Report> expectedReports = Arrays.asList(teamReport, userReport);
        when(reportRepository.findByCreatedBy(user)).thenReturn(expectedReports);

        // When
        List<Report> actualReports = reportService.getReportsByCreatedBy(user);

        // Then
        assertEquals(expectedReports.size(), actualReports.size());
        assertEquals(expectedReports.get(0).getId(), actualReports.get(0).getId());
        assertEquals(expectedReports.get(1).getId(), actualReports.get(1).getId());
        verify(reportRepository, times(1)).findByCreatedBy(user);
    }

    @Test
    void saveReport_ShouldSaveAndReturnReport() {
        // Given
        Report newReport = new Report();
        newReport.setName("Nowy raport");
        newReport.setType(teamReportType);
        newReport.setCreatedBy(user);
        newReport.setParameters("{\"teamId\": 2, \"month\": 6, \"year\": 2023}");
        newReport.setFileName("new_report.pdf");
        newReport.setFilePath("/reports/new_report.pdf");

        when(reportRepository.save(any(Report.class))).thenReturn(newReport);

        // When
        Report savedReport = reportService.saveReport(newReport);

        // Then
        assertEquals(newReport.getName(), savedReport.getName());
        assertEquals(newReport.getType(), savedReport.getType());
        assertEquals(newReport.getCreatedBy(), savedReport.getCreatedBy());
        verify(reportRepository, times(1)).save(newReport);
    }

    @Test
    void deleteReport_ShouldCallRepository() {
        // When
        reportService.deleteReport(teamReport);

        // Then
        verify(reportRepository, times(1)).delete(teamReport);
    }

    @Test
    void getReportById_WhenReportExists_ShouldReturnReport() {
        // Given
        when(reportRepository.findById(1L)).thenReturn(Optional.of(teamReport));

        // When
        Optional<Report> result = reportService.getReportById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(teamReport.getId(), result.get().getId());
        assertEquals(teamReport.getName(), result.get().getName());
        verify(reportRepository, times(1)).findById(1L);
    }

    @Test
    void getReportById_WhenReportDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Report> result = reportService.getReportById(99L);

        // Then
        assertFalse(result.isPresent());
        verify(reportRepository, times(1)).findById(99L);
    }

    @Test
    void getAllReports_ShouldReturnAllReports() {
        // Given
        List<Report> expectedReports = Arrays.asList(teamReport, userReport);
        when(reportRepository.findAll()).thenReturn(expectedReports);

        // When
        List<Report> actualReports = reportService.getAllReports();

        // Then
        assertEquals(expectedReports.size(), actualReports.size());
        assertEquals(expectedReports.get(0).getId(), actualReports.get(0).getId());
        assertEquals(expectedReports.get(1).getId(), actualReports.get(1).getId());
        verify(reportRepository, times(1)).findAll();
    }
}