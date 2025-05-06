package com.example.backend.controllers;

import com.example.backend.dto.reports.*;
import com.example.backend.models.Report;
import com.example.backend.models.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.services.PdfReportService;
import com.example.backend.services.ReportDataService;
import com.example.backend.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Kontroler REST odpowiedzialny za generowanie i pobieranie raportów PDF.
 * Obsługuje żądania dotyczące generowania raportów o postępie budowy,
 * obciążeniu pracowników oraz efektywności zespołów.
 */
@RestController
@RequestMapping("/api/generate-report")
public class PdfReportController {

    private final PdfReportService pdfReportService;
    private final ReportDataService reportDataService;
    private final UserRepository userRepository;
    private final ReportService reportService;

    @Autowired
    public PdfReportController(
            PdfReportService pdfReportService,
            ReportDataService reportDataService,
            UserRepository userRepository,
            ReportService reportService) {
        this.pdfReportService = pdfReportService;
        this.reportDataService = reportDataService;
        this.userRepository = userRepository;
        this.reportService = reportService;
    }

    /**
     * Generuje raport postępu budowy na podstawie podanych parametrów.
     *
     * @param teamId   Identyfikator zespołu
     * @param dateFrom Data początkowa zakresu raportu
     * @param dateTo   Data końcowa zakresu raportu
     * @param userId   Identyfikator użytkownika generującego raport
     * @return ResponseEntity zawierający informacje o wygenerowanym raporcie lub błędzie
     */
    @PostMapping("/construction-progress")
    public ResponseEntity<?> generateConstructionProgressReport(
            @RequestParam Integer teamId,
            @RequestParam String dateFrom,
            @RequestParam String dateTo,
            @RequestParam Integer userId) {

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Collect data
            ConstructionProgressReportDTO reportDTO =
                    reportDataService.collectConstructionProgressData(teamId, dateFrom, dateTo);

            // Generate report
            Report report = pdfReportService.generateConstructionProgressReport(reportDTO, user);

            Map<String, Object> response = new HashMap<>();
            response.put("reportId", report.getId());
            response.put("fileName", report.getFileName());
            response.put("message", "Construction Progress Report generated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating report: " + e.getMessage());
        }
    }

    /**
     * Generuje raport obciążenia pracownika na podstawie podanych parametrów.
     *
     * @param targetUserId Identyfikator pracownika (opcjonalny)
     * @param dateFrom     Data początkowa zakresu raportu
     * @param dateTo       Data końcowa zakresu raportu
     * @param userId       Identyfikator użytkownika generującego raport
     * @return ResponseEntity zawierający informacje o wygenerowanym raporcie lub błędzie
     */
    @PostMapping("/employee-load")
    public ResponseEntity<?> generateEmployeeLoadReport(
            @RequestParam(required = false) Integer targetUserId,
            @RequestParam String dateFrom,
            @RequestParam String dateTo,
            @RequestParam Integer userId) {

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Collect data
            EmployeeLoadReportDTO reportDTO =
                    reportDataService.collectEmployeeLoadData(targetUserId, dateFrom, dateTo);

            // Generate report
            Report report = pdfReportService.generateEmployeeLoadReport(reportDTO, user);

            Map<String, Object> response = new HashMap<>();
            response.put("reportId", report.getId());
            response.put("fileName", report.getFileName());
            response.put("message", "Employee Load Report generated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating report: " + e.getMessage());
        }
    }

    /**
     * Generuje raport efektywności zespołu na podstawie podanych parametrów.
     *
     * @param dateFrom Data początkowa zakresu raportu
     * @param dateTo   Data końcowa zakresu raportu
     * @param userId   Identyfikator użytkownika generującego raport
     * @return ResponseEntity zawierający informacje o wygenerowanym raporcie lub błędzie
     */
    @PostMapping("/team-efficiency")
    public ResponseEntity<?> generateTeamEfficiencyReport(
            @RequestParam String dateFrom,
            @RequestParam String dateTo,
            @RequestParam Integer userId) {

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Collect data
            TeamEfficiencyReportDTO reportDTO =
                    reportDataService.collectTeamEfficiencyData(dateFrom, dateTo);

            // Generate report
            Report report = pdfReportService.generateTeamEfficiencyReport(reportDTO, user);

            Map<String, Object> response = new HashMap<>();
            response.put("reportId", report.getId());
            response.put("fileName", report.getFileName());
            response.put("message", "Team Efficiency Report generated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating report: " + e.getMessage());
        }
    }

    /**
     * Pobiera wygenerowany raport PDF.
     *
     * @param reportId Identyfikator raportu do pobrania
     * @return ResponseEntity zawierający plik raportu lub informację o błędzie
     */
    @GetMapping("/download/{reportId}")
    public ResponseEntity<Resource> downloadReport(@PathVariable Long reportId) {
        try {
            com.example.backend.dto.ReportDTO reportDTO = reportService.getReportById(reportId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));

            Path filePath = Paths.get(reportDTO.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Report file not found or not readable");
            }

            String contentType = "application/pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + reportDTO.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error downloading report", e);
        }
    }
}