package com.example.backend.controllers;

import com.example.backend.models.ReportType;
import com.example.backend.services.ReportTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    @GetMapping
    public ResponseEntity<List<ReportType>> getAllReportTypes() {
        List<ReportType> reportTypes = reportTypeService.getAllReportTypes();
        return new ResponseEntity<>(reportTypes, HttpStatus.OK);
    }

    /**
     * Pobiera typ raportu na podstawie jego identyfikatora.
     *
     * @param id Identyfikator typu raportu
     * @return Typ raportu lub status 404, jeśli nie istnieje
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportType> getReportTypeById(@PathVariable Integer id) {
        return reportTypeService.getReportTypeById(id)
                .map(reportType -> new ResponseEntity<>(reportType, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Pobiera typ raportu na podstawie jego nazwy.
     *
     * @param name Nazwa typu raportu
     * @return Typ raportu lub status 404, jeśli nie istnieje
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ReportType> getReportTypeByName(@PathVariable String name) {
        return reportTypeService.getReportTypeByName(name)
                .map(reportType -> new ResponseEntity<>(reportType, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Tworzy nowy typ raportu.
     *
     * @param reportType Dane nowego typu raportu
     * @return Utworzony typ raportu
     */
    @PostMapping
    public ResponseEntity<ReportType> createReportType(@RequestBody ReportType reportType) {
        if (reportTypeService.existsByName(reportType.getName())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        ReportType savedReportType = reportTypeService.saveReportType(reportType);
        return new ResponseEntity<>(savedReportType, HttpStatus.CREATED);
    }

    /**
     * Tworzy nowy typ raportu na podstawie podanych parametrów.
     *
     * @param payload Mapa zawierająca name, description, templatePath
     * @return Utworzony typ raportu lub status błędu
     */
    @PostMapping("/create")
    public ResponseEntity<ReportType> createReportTypeFromParams(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        String description = (String) payload.get("description");
        String templatePath = (String) payload.get("templatePath");

        if (name == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (reportTypeService.existsByName(name)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        ReportType newReportType = reportTypeService.createReportType(
                name, description, templatePath);

        return new ResponseEntity<>(newReportType, HttpStatus.CREATED);
    }

    /**
     * Aktualizuje istniejący typ raportu.
     *
     * @param id         Identyfikator typu raportu
     * @param reportType Zaktualizowane dane typu raportu
     * @return Zaktualizowany typ raportu lub status 404, jeśli nie istnieje
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReportType> updateReportType(@PathVariable Integer id,
                                                       @RequestBody ReportType reportType) {
        if (!reportTypeService.getReportTypeById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        reportType.setId(id);
        ReportType updatedReportType = reportTypeService.saveReportType(reportType);
        return new ResponseEntity<>(updatedReportType, HttpStatus.OK);
    }

    /**
     * Aktualizuje ścieżkę szablonu dla typu raportu.
     *
     * @param id        Identyfikator typu raportu
     * @param payload   Mapa zawierająca templatePath
     * @return Zaktualizowany typ raportu lub status 404, jeśli nie istnieje
     */
    @PatchMapping("/{id}/template")
    public ResponseEntity<ReportType> updateTemplatePath(@PathVariable Integer id,
                                                         @RequestBody Map<String, String> payload) {
        String templatePath = payload.get("templatePath");

        if (templatePath == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return reportTypeService.updateTemplatePath(id, templatePath)
                .map(reportType -> new ResponseEntity<>(reportType, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
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
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            reportTypeService.deleteReportType(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }
}