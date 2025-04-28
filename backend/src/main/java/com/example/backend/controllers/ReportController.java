package com.example.backend.controllers;

import com.example.backend.dto.ReportDTO;
import com.example.backend.models.ReportType;
import com.example.backend.models.User;
import com.example.backend.services.ReportService;
import com.example.backend.services.ReportTypeService;
import com.example.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Kontroler REST dla operacji na raportach.
 * <p>
 * Umożliwia operacje CRUD na encji {@link com.example.backend.models.Report}.
 *
 * @author Karol
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/database/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportTypeService reportTypeService;
    private final UserService userService;

    @Autowired
    public ReportController(ReportService reportService, ReportTypeService reportTypeService, UserService userService) {
        this.reportService = reportService;
        this.reportTypeService = reportTypeService;
        this.userService = userService;
    }

    /**
     * Pobiera wszystkie raporty.
     *
     * @return Lista wszystkich raportów
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ReportDTO>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    /**
     * Pobiera raport na podstawie jego identyfikatora.
     *
     * @param id Identyfikator raportu
     * @return Raport lub status 404, jeśli nie istnieje
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReportDTO> getReportById(@PathVariable Long id) {
        return reportService.getReportById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Tworzy nowy raport.
     *
     * @param reportDTO Dane nowego raportu
     * @return Utworzony raport
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReportDTO> createReport(@Valid @RequestBody ReportDTO reportDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.saveReport(reportDTO));
    }

    /**
     * Usuwa raport na podstawie jego identyfikatora.
     *
     * @param id Identyfikator raportu do usunięcia
     * @return Status 204 po pomyślnym usunięciu lub 404, jeśli nie istnieje
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        boolean deleted = reportService.deleteReport(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Pobiera raporty o określonym typie na podstawie ID typu.
     *
     * @param typeId ID typu raportu
     * @return Lista raportów o określonym typie lub status 404, jeśli typ nie istnieje
     */
    @GetMapping(value = "/type/{typeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ReportDTO>> getReportsByTypeId(@PathVariable Integer typeId) {
        return reportTypeService.getReportTypeById(typeId)
                .map(typeDTO -> {
                    ReportType type = new ReportType();
                    type.setId(typeDTO.getId());
                    type.setName(typeDTO.getName());
                    List<ReportDTO> reports = reportService.getReportsByType(type);
                    return ResponseEntity.ok(reports);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Pobiera raporty utworzone przez określonego użytkownika.
     *
     * @param userId ID użytkownika
     * @return Lista raportów utworzonych przez użytkownika lub status 404, jeśli użytkownik nie istnieje
     */
    @GetMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ReportDTO>> getReportsByUserId(@PathVariable Integer userId) {
        return userService.getUserById(userId)
                .map(userDTO -> {
                    User user = new User();
                    user.setId(userDTO.getId());
                    List<ReportDTO> reports = reportService.getReportsByCreatedBy(user);
                    return ResponseEntity.ok(reports);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Aktualizuje istniejący raport.
     *
     * @param id        Identyfikator raportu
     * @param reportDTO Zaktualizowane dane raportu
     * @return Zaktualizowany raport lub status 404, jeśli nie istnieje
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReportDTO> updateReport(@PathVariable Long id, @Valid @RequestBody ReportDTO reportDTO) {
        return reportService.getReportById(id)
                .map(existingReport -> {
                    reportDTO.setId(existingReport.getId().intValue());
                    ReportDTO updatedReport = reportService.saveReport(reportDTO);
                    return ResponseEntity.ok(updatedReport);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}