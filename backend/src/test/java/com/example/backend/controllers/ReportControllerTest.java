package  com.example.backend.controllers;

import com.example.backend.models.Report;
import com.example.backend.models.User;
import com.example.backend.services.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    private Report report;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1);
        user.setUsername("john_doe");

        report = new Report();
        report.setId(1);
        report.setName("Test Report");
        report.setCreatedBy(user);
    }

    @Test
    void getAllReports() {
        when(reportService.getAllReports()).thenReturn(Arrays.asList(report));

        ResponseEntity<List<Report>> response = reportController.getAllReports();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        verify(reportService, times(1)).getAllReports();
    }

    @Test
    void getReportById_found() {
        when(reportService.getReportById(1L)).thenReturn(Optional.of(report));

        ResponseEntity<Report> response = reportController.getReportById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(report, response.getBody());
    }

    @Test
    void getReportById_notFound() {
        when(reportService.getReportById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Report> response = reportController.getReportById(1L);

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void createReport() {
        when(reportService.saveReport(report)).thenReturn(report);

        ResponseEntity<Report> response = reportController.createReport(report);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(report, response.getBody());
        verify(reportService, times(1)).saveReport(report);
    }

    @Test
    void deleteReport_found() {
        when(reportService.getReportById(1L)).thenReturn(Optional.of(report));
        doNothing().when(reportService).deleteReport(report);

        ResponseEntity<Void> response = reportController.deleteReport(1L);

        assertEquals(204, response.getStatusCodeValue());
        verify(reportService).deleteReport(report);
    }

    @Test
    void deleteReport_notFound() {
        when(reportService.getReportById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = reportController.deleteReport(1L);

        assertEquals(404, response.getStatusCodeValue());
        verify(reportService, never()).deleteReport(any());
    }



    @Test
    void getReportsByUser() {
        when(reportService.getReportsByCreatedBy(any(User.class))).thenReturn(Arrays.asList(report));

        ResponseEntity<List<Report>> response = reportController.getReportsByUser(1);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals(user.getId(), response.getBody().get(0).getCreatedBy().getId());
    }
}
