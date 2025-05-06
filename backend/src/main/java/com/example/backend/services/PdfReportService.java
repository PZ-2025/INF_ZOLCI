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

@Service
@Transactional
public class PdfReportService {

    private final ReportRepository reportRepository;
    private final ReportTypeRepository reportTypeRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.reports.storage-path:./reports}")
    private String reportStoragePath;

    @Autowired
    public PdfReportService(ReportRepository reportRepository,
                            ReportTypeRepository reportTypeRepository,
                            ObjectMapper objectMapper) {
        this.reportRepository = reportRepository;
        this.reportTypeRepository = reportTypeRepository;
        this.objectMapper = objectMapper;
    }

    public Report generateConstructionProgressReport(ConstructionProgressReportDTO reportDTO, User createdBy) throws Exception {
        // 1. Convert DTO to library model
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

        // 2. Prepare parameters for the report generator
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("dateFrom", reportDTO.getDateFrom());
        parameters.put("dateTo", reportDTO.getDateTo());
        parameters.put("completedPercentage", reportDTO.getCompletedPercentage());
        parameters.put("delayedCount", reportDTO.getDelayedCount());

        // 3. Create report generator
        ConstructionProgressReportGenerator generator = new ConstructionProgressReportGenerator();

        // 4. Generate the PDF and save it
        String reportType = "construction-progress";
        String fileName = FileStorageUtils.createUniqueFileName(reportType, "pdf");
        Path filePath = FileStorageUtils.resolveReportPath(reportStoragePath, reportType, fileName);

        generator.saveReport(dataItems, parameters, filePath);

        // 5. Create and save report entity in the database
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

    public Report generateEmployeeLoadReport(EmployeeLoadReportDTO reportDTO, User createdBy) throws Exception {
        // 1. Convert DTO to library model
        List<EmployeeLoad> dataItems = reportDTO.getItems().stream()
                .map(item -> {
                    EmployeeLoad load = new EmployeeLoad();
                    load.setEmployeeId(item.getEmployeeId());
                    load.setEmployeeName(item.getEmployeeName());
                    load.setTaskCount(item.getTaskCount());
                    load.setTotalHours(item.getTotalHours());
                    return load;
                })
                .collect(Collectors.toList());

        // 2. Prepare parameters for the report generator
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("dateFrom", reportDTO.getDateFrom());
        parameters.put("dateTo", reportDTO.getDateTo());

        // 3. Create report generator
        EmployeeLoadReportGenerator generator = new EmployeeLoadReportGenerator();

        // 4. Generate the PDF and save it
        String reportType = "employee-load";
        String fileName = FileStorageUtils.createUniqueFileName(reportType, "pdf");
        Path filePath = FileStorageUtils.resolveReportPath(reportStoragePath, reportType, fileName);

        generator.saveReport(dataItems, parameters, filePath);

        // 5. Create and save report entity in the database
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

    public Report generateTeamEfficiencyReport(TeamEfficiencyReportDTO reportDTO, User createdBy) throws Exception {
        // 1. Convert DTO to library model
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

        // 2. Prepare parameters for the report generator
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("dateFrom", reportDTO.getDateFrom());
        parameters.put("dateTo", reportDTO.getDateTo());

        // 3. Create report generator
        TeamEfficiencyReportGenerator generator = new TeamEfficiencyReportGenerator();

        // 4. Generate the PDF and save it
        String reportType = "team-efficiency";
        String fileName = FileStorageUtils.createUniqueFileName(reportType, "pdf");
        Path filePath = FileStorageUtils.resolveReportPath(reportStoragePath, reportType, fileName);

        generator.saveReport(dataItems, parameters, filePath);

        // 5. Create and save report entity in the database
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