package com.example.backend.controllers;

import com.example.backend.models.ReportType;
import com.example.backend.services.ReportTypeService;
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

class ReportTypeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportTypeService reportTypeService;

    @InjectMocks
    private ReportTypeController reportTypeController;

    private ObjectMapper objectMapper;
    private ReportType teamReportType;
    private ReportType userReportType;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(reportTypeController)
                .build();

        objectMapper = new ObjectMapper();

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
    void getAllReportTypes_ShouldReturnAllTypes() throws Exception {
        // Given
        List<ReportType> reportTypes = Arrays.asList(teamReportType, userReportType);
        when(reportTypeService.getAllReportTypes()).thenReturn(reportTypes);

        // When & Then
        mockMvc.perform(get("/database/report-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Raport zespołu")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Raport użytkownika")));

        verify(reportTypeService, times(1)).getAllReportTypes();
    }

    @Test
    void getReportTypeById_WhenTypeExists_ShouldReturnType() throws Exception {
        // Given
        when(reportTypeService.getReportTypeById(1)).thenReturn(Optional.of(teamReportType));

        // When & Then
        mockMvc.perform(get("/database/report-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Raport zespołu")))
                .andExpect(jsonPath("$.description", is("Podsumowanie zadań zespołu")))
                .andExpect(jsonPath("$.templatePath", is("/templates/reports/team_report.html")));

        verify(reportTypeService, times(1)).getReportTypeById(1);
    }

    @Test
    void getReportTypeById_WhenTypeDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(reportTypeService.getReportTypeById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/database/report-types/99"))
                .andExpect(status().isNotFound());

        verify(reportTypeService, times(1)).getReportTypeById(99);
    }

    @Test
    void getReportTypeByName_WhenTypeExists_ShouldReturnType() throws Exception {
        // Given
        when(reportTypeService.getReportTypeByName("Raport zespołu")).thenReturn(Optional.of(teamReportType));

        // When & Then
        mockMvc.perform(get("/database/report-types/name/Raport zespołu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Raport zespołu")))
                .andExpect(jsonPath("$.description", is("Podsumowanie zadań zespołu")));

        verify(reportTypeService, times(1)).getReportTypeByName("Raport zespołu");
    }

    @Test
    void getReportTypeByName_WhenTypeDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(reportTypeService.getReportTypeByName("Nieistniejący")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/database/report-types/name/Nieistniejący"))
                .andExpect(status().isNotFound());

        verify(reportTypeService, times(1)).getReportTypeByName("Nieistniejący");
    }

    @Test
    void createReportType_WhenNameIsUnique_ShouldReturnCreatedType() throws Exception {
        // Given
        ReportType newType = new ReportType();
        newType.setName("Raport projektu");
        newType.setDescription("Podsumowanie projektu");
        newType.setTemplatePath("/templates/reports/project_report.html");

        when(reportTypeService.existsByName("Raport projektu")).thenReturn(false);
        when(reportTypeService.saveReportType(any(ReportType.class))).thenReturn(newType);

        // When & Then
        mockMvc.perform(post("/database/report-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newType)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Raport projektu")))
                .andExpect(jsonPath("$.description", is("Podsumowanie projektu")))
                .andExpect(jsonPath("$.templatePath", is("/templates/reports/project_report.html")));

        verify(reportTypeService, times(1)).existsByName("Raport projektu");
        verify(reportTypeService, times(1)).saveReportType(any(ReportType.class));
    }

    @Test
    void createReportType_WhenNameExists_ShouldReturnConflict() throws Exception {
        // Given
        ReportType newType = new ReportType();
        newType.setName("Raport zespołu");
        newType.setDescription("Inny opis");
        newType.setTemplatePath("/templates/reports/another_path.html");

        when(reportTypeService.existsByName("Raport zespołu")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/database/report-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newType)))
                .andExpect(status().isConflict());

        verify(reportTypeService, times(1)).existsByName("Raport zespołu");
        verify(reportTypeService, never()).saveReportType(any(ReportType.class));
    }

    @Test
    void createReportTypeFromParams_WithValidData_ShouldReturnCreatedType() throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Raport projektu");
        payload.put("description", "Podsumowanie projektu");
        payload.put("templatePath", "/templates/reports/project_report.html");

        ReportType createdType = new ReportType();
        createdType.setId(3);
        createdType.setName("Raport projektu");
        createdType.setDescription("Podsumowanie projektu");
        createdType.setTemplatePath("/templates/reports/project_report.html");

        when(reportTypeService.existsByName("Raport projektu")).thenReturn(false);
        when(reportTypeService.createReportType(
                eq("Raport projektu"),
                eq("Podsumowanie projektu"),
                eq("/templates/reports/project_report.html")))
                .thenReturn(createdType);

        // When & Then
        mockMvc.perform(post("/database/report-types/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("Raport projektu")))
                .andExpect(jsonPath("$.description", is("Podsumowanie projektu")))
                .andExpect(jsonPath("$.templatePath", is("/templates/reports/project_report.html")));

        verify(reportTypeService, times(1)).existsByName("Raport projektu");
        verify(reportTypeService, times(1)).createReportType(
                eq("Raport projektu"),
                eq("Podsumowanie projektu"),
                eq("/templates/reports/project_report.html"));
    }

    @Test
    void createReportTypeFromParams_WithMissingName_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        // Missing name
        payload.put("description", "Podsumowanie projektu");
        payload.put("templatePath", "/templates/reports/project_report.html");

        // When & Then
        mockMvc.perform(post("/database/report-types/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());

        verify(reportTypeService, never()).existsByName(anyString());
        verify(reportTypeService, never()).createReportType(anyString(), anyString(), anyString());
    }

    @Test
    void updateReportType_WhenTypeExists_ShouldReturnUpdatedType() throws Exception {
        // Given
        ReportType updatedType = new ReportType();
        updatedType.setId(1);
        updatedType.setName("Raport zespołu zmodyfikowany");
        updatedType.setDescription("Nowy opis");
        updatedType.setTemplatePath("/templates/reports/new_team_report.html");

        when(reportTypeService.getReportTypeById(1)).thenReturn(Optional.of(teamReportType));
        when(reportTypeService.saveReportType(any(ReportType.class))).thenReturn(updatedType);

        // When & Then
        mockMvc.perform(put("/database/report-types/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedType)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Raport zespołu zmodyfikowany")))
                .andExpect(jsonPath("$.description", is("Nowy opis")))
                .andExpect(jsonPath("$.templatePath", is("/templates/reports/new_team_report.html")));

        verify(reportTypeService, times(1)).getReportTypeById(1);
        verify(reportTypeService, times(1)).saveReportType(any(ReportType.class));
    }

    @Test
    void updateReportType_WhenTypeDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        ReportType updatedType = new ReportType();
        updatedType.setId(99);
        updatedType.setName("Nieistniejący");
        updatedType.setDescription("Opis");

        when(reportTypeService.getReportTypeById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/database/report-types/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedType)))
                .andExpect(status().isNotFound());

        verify(reportTypeService, times(1)).getReportTypeById(99);
        verify(reportTypeService, never()).saveReportType(any(ReportType.class));
    }

    @Test
    void updateTemplatePath_WhenTypeExists_ShouldReturnUpdatedType() throws Exception {
        // Given
        Map<String, String> payload = new HashMap<>();
        payload.put("templatePath", "/templates/reports/new_template.html");

        ReportType updatedType = new ReportType();
        updatedType.setId(1);
        updatedType.setName("Raport zespołu");
        updatedType.setDescription("Podsumowanie zadań zespołu");
        updatedType.setTemplatePath("/templates/reports/new_template.html");

        when(reportTypeService.updateTemplatePath(eq(1), eq("/templates/reports/new_template.html")))
                .thenReturn(Optional.of(updatedType));

        // When & Then
        mockMvc.perform(patch("/database/report-types/1/template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Raport zespołu")))
                .andExpect(jsonPath("$.templatePath", is("/templates/reports/new_template.html")));

        verify(reportTypeService, times(1)).updateTemplatePath(eq(1), eq("/templates/reports/new_template.html"));
    }

    @Test
    void updateTemplatePath_WhenTypeDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        Map<String, String> payload = new HashMap<>();
        payload.put("templatePath", "/templates/reports/new_template.html");

        when(reportTypeService.updateTemplatePath(eq(99), eq("/templates/reports/new_template.html")))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(patch("/database/report-types/99/template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound());

        verify(reportTypeService, times(1)).updateTemplatePath(eq(99), eq("/templates/reports/new_template.html"));
    }

    @Test
    void updateTemplatePath_WithMissingTemplatePath_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, String> payload = new HashMap<>();
        // Missing templatePath

        // When & Then
        mockMvc.perform(patch("/database/report-types/1/template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());

        verify(reportTypeService, never()).updateTemplatePath(anyInt(), anyString());
    }

    @Test
    void deleteReportType_WhenTypeExistsWithNoReports_ShouldReturnNoContent() throws Exception {
        // Given
        when(reportTypeService.getReportTypeById(1)).thenReturn(Optional.of(teamReportType));
        doNothing().when(reportTypeService).deleteReportType(1);

        // When & Then
        mockMvc.perform(delete("/database/report-types/1"))
                .andExpect(status().isNoContent());

        verify(reportTypeService, times(1)).getReportTypeById(1);
        verify(reportTypeService, times(1)).deleteReportType(1);
    }

    @Test
    void deleteReportType_WhenTypeDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(reportTypeService.getReportTypeById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/database/report-types/99"))
                .andExpect(status().isNotFound());

        verify(reportTypeService, times(1)).getReportTypeById(99);
        verify(reportTypeService, never()).deleteReportType(anyInt());
    }

    @Test
    void deleteReportType_WhenTypeHasReports_ShouldReturnConflict() throws Exception {
        // Given
        when(reportTypeService.getReportTypeById(1)).thenReturn(Optional.of(teamReportType));
        doThrow(new IllegalStateException("Nie można usunąć typu raportu, do którego przypisane są raporty"))
                .when(reportTypeService).deleteReportType(1);

        // When & Then
        mockMvc.perform(delete("/database/report-types/1"))
                .andExpect(status().isConflict());

        verify(reportTypeService, times(1)).getReportTypeById(1);
        verify(reportTypeService, times(1)).deleteReportType(1);
    }
}