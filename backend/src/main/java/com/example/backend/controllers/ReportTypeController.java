package com.example.backend.controllers;

import com.example.backend.dto.ReportTypeDTO;
import com.example.backend.services.ReportTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kontroler REST dla operacji na typach raportów.
 * <p>
 * Klasa zapewnia endpoints do zarządzania typami raportów w systemie,
 * w tym tworzenie, odczyt, aktualizację i usuwanie typów raportów.
 *
 * @author Karol
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/database/report-types")
public class ReportTypeController {

    private final ReportTypeService reportTypeService;

    /**
     * Konstruktor wstrzykujący zależność do serwisu typów raportów.
     *
     * @param reportTypeService Serwis typów raportów
     */
    @Autowired
    public ReportTypeController(ReportTypeService reportTypeService) {
        this.reportTypeService = reportTypeService;
    }

    /**
     * Pobiera wszystkie typy raportów.
     *
     * @return Lista wszystkich typów raportów
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ReportTypeDTO>> getAllReportTypes() {
        return ResponseEntity.ok(reportTypeService.getAllReportTypes());
    }

    /**
     * Pobiera typ raportu na podstawie jego identyfikatora.
     *
     * @param id Identyfikator typu raportu
     * @return Typ raportu lub status 404, jeśli nie istnieje
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReportTypeDTO> getReportTypeById(@PathVariable Integer id) {
        return reportTypeService.getReportTypeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Tworzy nowy typ raportu.
     *
     * @param reportTypeDTO Dane nowego typu raportu
     * @return Utworzony typ raportu
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReportTypeDTO> createReportType(@Valid @RequestBody ReportTypeDTO reportTypeDTO) {
        if (reportTypeService.existsByName(reportTypeDTO.getName())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        ReportTypeDTO savedDto = reportTypeService.mapToDTO(reportTypeService.saveReportType(reportTypeDTO));
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    /**
     * Aktualizuje istniejący typ raportu.
     *
     * @param id           Identyfikator typu raportu
     * @param reportTypeDTO Zaktualizowane dane typu raportu
     * @return Zaktualizowany typ raportu lub status 404, jeśli nie istnieje
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReportTypeDTO> updateReportType(
            @PathVariable Integer id,
            @Valid @RequestBody ReportTypeDTO reportTypeDTO) {
        if (!reportTypeService.getReportTypeById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        reportTypeDTO.setId(id);
        ReportTypeDTO savedDto = reportTypeService.mapToDTO(reportTypeService.saveReportType(reportTypeDTO));
        return ResponseEntity.ok(savedDto);
    }

    /**
     * Usuwa typ raportu na podstawie jego identyfikatora.
     *
     * @param id Identyfikator typu raportu do usunięcia
     * @return Status 204 po pomyślnym usunięciu, 404 jeśli nie istnieje, lub 409 jeśli są przypisane raporty
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReportType(@PathVariable Integer id) {
        if (!reportTypeService.getReportTypeById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        try {
            reportTypeService.deleteReportType(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}