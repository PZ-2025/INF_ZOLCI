package com.example.backend.services;

import com.example.backend.dto.reports.*;
import com.example.backend.models.Report;
import com.example.backend.models.ReportType;
import com.example.backend.models.User;
import com.example.backend.repository.ReportRepository;
import com.example.backend.repository.ReportTypeRepository;
import org.example.reporting.generator.*;
import org.example.reporting.model.*;
import org.example.reporting.storage.FileStorageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serwis odpowiedzialny za generowanie raportów PDF.
 * Obsługuje konwersję danych, generowanie plików PDF oraz zapisywanie informacji o raportach w bazie danych.
 */
@Service
@Transactional
public class PdfReportService {

    private final ReportRepository reportRepository;
    private final ReportTypeRepository reportTypeRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.reports.storage-path}")
    private String reportStoragePath;

    @Autowired
    public PdfReportService(ReportRepository reportRepository,
                            ReportTypeRepository reportTypeRepository,
                            ObjectMapper objectMapper) {
        this.reportRepository = reportRepository;
        this.reportTypeRepository = reportTypeRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Generuje raport postępu budowy w formacie PDF.
     *
     * @param reportDTO Obiekt zawierający dane do raportu
     * @param createdBy Użytkownik generujący raport
     * @return Obiekt Report zawierający informacje o wygenerowanym raporcie
     * @throws Exception w przypadku błędu podczas generowania raportu
     */
    public Report generateConstructionProgressReport(ConstructionProgressReportDTO reportDTO, User createdBy) throws Exception {
        // 1. Konwersja DTO na model biblioteczny
        List<ConstructionProgress> dataItems = reportDTO.getItems().stream()
                .map(item -> {
                    ConstructionProgress progress = new ConstructionProgress();
                    progress.setTaskName(item.getTaskName());
                    progress.setStatus(item.getStatus());
                    progress.setPlannedEnd(item.getPlannedEnd());
                    progress.setActualEnd(item.getActualEnd());
                    return progress;
                })
                .collect(Collectors.toList());

        // 2. Przygotowanie parametrów dla generatora raportów
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("dateFrom", reportDTO.getDateFrom());
        parameters.put("dateTo", reportDTO.getDateTo());
        parameters.put("completedPercentage", reportDTO.getCompletedPercentage());
        parameters.put("delayedCount", reportDTO.getDelayedCount());

        // 3. Utworzenie generatora raportów
        ConstructionProgressReportGenerator generator = new ConstructionProgressReportGenerator();

        // 4. Generowanie PDF i zapisanie go
        String reportType = "construction-progress";
        String fileName = FileStorageUtils.createUniqueFileName(reportType, "pdf");
        Path filePath = FileStorageUtils.resolveReportPath(reportStoragePath, reportType, fileName);

        generator.saveReport(dataItems, parameters, filePath);

        // 5. Utworzenie i zapisanie encji raportu w bazie danych
        Report report = new Report();
        report.setName("Raport postępu budowy");

        ReportType type = reportTypeRepository.findByName("Raport postępu budowy")
                .orElseThrow(() -> new RuntimeException("Report type not found"));
        report.setType(type);

        report.setCreatedBy(createdBy);
        report.setParameters(objectMapper.writeValueAsString(parameters));
        report.setFileName(fileName);
        report.setFilePath(filePath.toString());
        report.setCreatedAt(LocalDateTime.now());

        return reportRepository.save(report);
    }

    /**
     * Generuje raport obciążenia pracownika w formacie PDF.
     *
     * @param reportDTO Obiekt zawierający dane do raportu
     * @param createdBy Użytkownik generujący raport
     * @return Obiekt Report zawierający informacje o wygenerowanym raporcie
     * @throws Exception w przypadku błędu podczas generowania raportu
     */
    public Report generateEmployeeLoadReport(EmployeeLoadReportDTO reportDTO, User createdBy) throws Exception {
        List<EmployeeLoad> dataItems = reportDTO.getItems().stream()
                .map(item -> {
                    EmployeeLoad load = new EmployeeLoad();
                    load.setEmployeeId(item.getEmployeeId());
                    load.setEmployeeName(item.getEmployeeName());
                    load.setTaskCount(item.getTaskCount());
                    load.setTotalHours(item.getTotalHours());

                    // Dodanie brakujących transferów danych
                    load.setFteEquivalent(item.getFteEquivalent());

                    // Konwersja informacji o zadaniach
                    if (item.getTasks() != null && !item.getTasks().isEmpty()) {
                        List<TaskDetail> taskDetails = item.getTasks().stream()
                                .map(taskDTO -> {
                                    TaskDetail detail = new TaskDetail();
                                    detail.setTaskId(taskDTO.getTaskId());
                                    detail.setTaskName(taskDTO.getTaskName());
                                    detail.setStatus(taskDTO.getStatus());
                                    detail.setPriority(taskDTO.getPriority());
                                    detail.setStartDate(taskDTO.getStartDate());
                                    detail.setDeadlineDate(taskDTO.getDeadlineDate());
                                    detail.setCompletedDate(taskDTO.getCompletedDate());
                                    detail.setEstimatedHours(taskDTO.getEstimatedHours());
                                    detail.setDelayed(taskDTO.isDelayed());
                                    return detail;
                                }).collect(Collectors.toList());
                        load.setTasks(taskDetails);
                    }

                    // Konwersja informacji o statusach
                    if (item.getTasksByStatus() != null) {
                        load.setTasksByStatus(new HashMap<>(item.getTasksByStatus()));
                    } else {
                        // Domyślna wartość jeśli brak danych
                        Map<String, Integer> defaultStatuses = new HashMap<>();
                        defaultStatuses.put("Nieznany", item.getTaskCount());
                        load.setTasksByStatus(defaultStatuses);
                    }

                    return load;
                })
                .collect(Collectors.toList());

        // 2. Przygotowanie parametrów dla generatora raportów
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("dateFrom", reportDTO.getDateFrom());
        parameters.put("dateTo", reportDTO.getDateTo());
        parameters.put("workingDays", reportDTO.getWorkingDays());

        // 3. Utworzenie generatora raportów
        EmployeeLoadReportGenerator generator = new EmployeeLoadReportGenerator();

        // 4. Generowanie PDF i zapisanie go
        String reportType = "employee-load";
        String fileName = FileStorageUtils.createUniqueFileName(reportType, "pdf");
        Path filePath = FileStorageUtils.resolveReportPath(reportStoragePath, reportType, fileName);

        generator.saveReport(dataItems, parameters, filePath);

        // 5. Utworzenie i zapisanie encji raportu w bazie danych
        Report report = new Report();
        report.setName("Raport obciążenia pracownika");

        ReportType type = reportTypeRepository.findByName("Raport obciążenia pracownika")
                .orElseThrow(() -> new RuntimeException("Report type not found"));
        report.setType(type);

        report.setCreatedBy(createdBy);
        report.setParameters(objectMapper.writeValueAsString(parameters));
        report.setFileName(fileName);
        report.setFilePath(filePath.toString());
        report.setCreatedAt(LocalDateTime.now());

        return reportRepository.save(report);
    }

    /**
     * Generuje raport efektywności zespołu w formacie PDF.
     *
     * @param reportDTO Obiekt zawierający dane do raportu
     * @param createdBy Użytkownik generujący raport
     * @return Obiekt Report zawierający informacje o wygenerowanym raporcie
     * @throws Exception w przypadku błędu podczas generowania raportu
     */
    public Report generateTeamEfficiencyReport(TeamEfficiencyReportDTO reportDTO, User createdBy) throws Exception {
        // 1. Konwersja DTO na model biblioteczny
        List<TeamEfficiency> dataItems = reportDTO.getItems().stream()
                .map(item -> {
                    TeamEfficiency efficiency = new TeamEfficiency();
                    efficiency.setTeamName(item.getTeamName());
                    efficiency.setAvgCompletionHours(item.getAvgCompletionHours());
                    efficiency.setOpenIssues(item.getOpenIssues());
                    efficiency.setClosedIssues(item.getClosedIssues());
                    return efficiency;
                })
                .collect(Collectors.toList());

        // 2. Przygotowanie parametrów dla generatora raportów
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("dateFrom", reportDTO.getDateFrom());
        parameters.put("dateTo", reportDTO.getDateTo());

        // 3. Utworzenie generatora raportów
        TeamEfficiencyReportGenerator generator = new TeamEfficiencyReportGenerator();

        // 4. Generowanie PDF i zapisanie go
        String reportType = "team-efficiency";
        String fileName = FileStorageUtils.createUniqueFileName(reportType, "pdf");
        Path filePath = FileStorageUtils.resolveReportPath(reportStoragePath, reportType, fileName);

        generator.saveReport(dataItems, parameters, filePath);

        // 5. Utworzenie i zapisanie encji raportu w bazie danych
        Report report = new Report();
        report.setName("Raport efektywności zespołu");

        ReportType type = reportTypeRepository.findByName("Raport efektywności zespołu")
                .orElseThrow(() -> new RuntimeException("Report type not found"));
        report.setType(type);

        report.setCreatedBy(createdBy);
        report.setParameters(objectMapper.writeValueAsString(parameters));
        report.setFileName(fileName);
        report.setFilePath(filePath.toString());
        report.setCreatedAt(LocalDateTime.now());

        return reportRepository.save(report);
    }
}