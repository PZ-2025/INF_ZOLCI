package com.example.backend.services;

import com.example.backend.models.Report;
import com.example.backend.models.ReportType;
import com.example.backend.repository.ReportTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportTypeServiceTest {

    @Mock
    private ReportTypeRepository reportTypeRepository;

    @InjectMocks
    private ReportTypeService reportTypeService;

    private ReportType teamReportType;
    private ReportType userReportType;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

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
    }

    @Test
    void getAllReportTypes_ShouldReturnAllTypes() {
        // Given
        List<ReportType> expectedTypes = Arrays.asList(teamReportType, userReportType);
        when(reportTypeRepository.findAll()).thenReturn(expectedTypes);

        // When
        List<ReportType> actualTypes = reportTypeService.getAllReportTypes();

        // Then
        assertEquals(expectedTypes.size(), actualTypes.size());
        assertEquals(expectedTypes.get(0).getId(), actualTypes.get(0).getId());
        assertEquals(expectedTypes.get(1).getId(), actualTypes.get(1).getId());
        verify(reportTypeRepository, times(1)).findAll();
    }

    @Test
    void getReportTypeById_WhenTypeExists_ShouldReturnType() {
        // Given
        when(reportTypeRepository.findById(1)).thenReturn(Optional.of(teamReportType));

        // When
        Optional<ReportType> result = reportTypeService.getReportTypeById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(teamReportType.getId(), result.get().getId());
        assertEquals(teamReportType.getName(), result.get().getName());
        verify(reportTypeRepository, times(1)).findById(1);
    }

    @Test
    void getReportTypeById_WhenTypeDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(reportTypeRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<ReportType> result = reportTypeService.getReportTypeById(99);

        // Then
        assertFalse(result.isPresent());
        verify(reportTypeRepository, times(1)).findById(99);
    }

    @Test
    void getReportTypeByName_WhenTypeExists_ShouldReturnType() {
        // Given
        when(reportTypeRepository.findByName("Raport zespołu")).thenReturn(Optional.of(teamReportType));

        // When
        Optional<ReportType> result = reportTypeService.getReportTypeByName("Raport zespołu");

        // Then
        assertTrue(result.isPresent());
        assertEquals(teamReportType.getId(), result.get().getId());
        assertEquals("Raport zespołu", result.get().getName());
        verify(reportTypeRepository, times(1)).findByName("Raport zespołu");
    }

    @Test
    void saveReportType_ShouldSaveAndReturnType() {
        // Given
        ReportType newType = new ReportType();
        newType.setName("Raport projektu");
        newType.setDescription("Podsumowanie projektu");
        newType.setTemplatePath("/templates/reports/project_report.html");

        when(reportTypeRepository.save(any(ReportType.class))).thenReturn(newType);

        // When
        ReportType savedType = reportTypeService.saveReportType(newType);

        // Then
        assertEquals(newType.getName(), savedType.getName());
        assertEquals(newType.getDescription(), savedType.getDescription());
        assertEquals(newType.getTemplatePath(), savedType.getTemplatePath());
        verify(reportTypeRepository, times(1)).save(newType);
    }

    @Test
    void createReportType_ShouldCreateAndReturnType() {
        // Given
        String name = "Raport projektu";
        String description = "Podsumowanie projektu";
        String templatePath = "/templates/reports/project_report.html";

        when(reportTypeRepository.save(any(ReportType.class))).thenAnswer(invocation -> {
            ReportType saved = invocation.getArgument(0);
            saved.setId(3); // Symulacja nadania ID przez bazę danych
            return saved;
        });

        // When
        ReportType createdType = reportTypeService.createReportType(name, description, templatePath);

        // Then
        assertNotNull(createdType.getId());
        assertEquals(name, createdType.getName());
        assertEquals(description, createdType.getDescription());
        assertEquals(templatePath, createdType.getTemplatePath());
        verify(reportTypeRepository, times(1)).save(any(ReportType.class));
    }

    @Test
    void deleteReportType_WithNoReports_ShouldDeleteType() {
        // Given
        ReportType typeWithNoReports = new ReportType();
        typeWithNoReports.setId(1);
        typeWithNoReports.setName("Raport zespołu");
        typeWithNoReports.setReports(new HashSet<>());

        when(reportTypeRepository.findById(1)).thenReturn(Optional.of(typeWithNoReports));
        doNothing().when(reportTypeRepository).deleteById(1);

        // When & Then
        assertDoesNotThrow(() -> reportTypeService.deleteReportType(1));
        verify(reportTypeRepository, times(1)).findById(1);
        verify(reportTypeRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteReportType_WithReports_ShouldThrowException() {
        // Given
        ReportType typeWithReports = new ReportType();
        typeWithReports.setId(1);
        typeWithReports.setName("Raport zespołu");

        Set<Report> reports = new HashSet<>();
        reports.add(new Report());
        typeWithReports.setReports(reports);

        when(reportTypeRepository.findById(1)).thenReturn(Optional.of(typeWithReports));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> reportTypeService.deleteReportType(1));

        assertTrue(exception.getMessage().contains("Nie można usunąć typu raportu"));
        verify(reportTypeRepository, times(1)).findById(1);
        verify(reportTypeRepository, never()).deleteById(anyInt());
    }

    @Test
    void existsByName_WhenTypeExists_ShouldReturnTrue() {
        // Given
        when(reportTypeRepository.findByName("Raport zespołu")).thenReturn(Optional.of(teamReportType));

        // When
        boolean result = reportTypeService.existsByName("Raport zespołu");

        // Then
        assertTrue(result);
        verify(reportTypeRepository, times(1)).findByName("Raport zespołu");
    }

    @Test
    void existsByName_WhenTypeDoesNotExist_ShouldReturnFalse() {
        // Given
        when(reportTypeRepository.findByName("Nieistniejący")).thenReturn(Optional.empty());

        // When
        boolean result = reportTypeService.existsByName("Nieistniejący");

        // Then
        assertFalse(result);
        verify(reportTypeRepository, times(1)).findByName("Nieistniejący");
    }

    @Test
    void updateTemplatePath_WhenTypeExists_ShouldUpdateAndReturnType() {
        // Given
        when(reportTypeRepository.findById(1)).thenReturn(Optional.of(teamReportType));
        when(reportTypeRepository.save(any(ReportType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String newTemplatePath = "/templates/reports/new_team_report.html";

        // When
        Optional<ReportType> result = reportTypeService.updateTemplatePath(1, newTemplatePath);

        // Then
        assertTrue(result.isPresent());
        assertEquals(newTemplatePath, result.get().getTemplatePath());
        verify(reportTypeRepository, times(1)).findById(1);
        verify(reportTypeRepository, times(1)).save(any(ReportType.class));
    }

    @Test
    void updateTemplatePath_WhenTypeDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(reportTypeRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<ReportType> result = reportTypeService.updateTemplatePath(99, "/templates/reports/new_template.html");

        // Then
        assertFalse(result.isPresent());
        verify(reportTypeRepository, times(1)).findById(99);
        verify(reportTypeRepository, never()).save(any(ReportType.class));
    }
}