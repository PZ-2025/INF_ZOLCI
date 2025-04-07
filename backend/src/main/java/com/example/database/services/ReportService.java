package com.example.database.services;

import com.example.database.models.Report;
import com.example.database.models.ReportType;
import com.example.database.models.User;
import com.example.database.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Serwis obsługujący operacje na encji {@link Report}.
 *
 * Klasa zawiera logikę biznesową związaną z zarządzaniem raportami,
 * wykorzystując {@link ReportRepository} do komunikacji z bazą danych.
 *
 * @author YourName
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    /**
     * Pobiera raporty po typie.
     *
     * @param type Typ raportu
     * @return Lista raportów danego typu
     */
    public List<Report> getReportsByType(ReportType type) {
        return reportRepository.findByType(type);
    }

    /**
     * Pobiera raporty utworzone przez danego użytkownika.
     *
     * @param user Użytkownik
     * @return Lista raportów utworzonych przez użytkownika
     */
    public List<Report> getReportsByCreatedBy(User user) {
        return reportRepository.findByCreatedBy(user);
    }

    /**
     * Zapisuje raport.
     *
     * @param report Raport do zapisania
     * @return Zapisany raport
     */
    public Report saveReport(Report report) {
        return reportRepository.save(report);
    }

    /**
     * Usuwa raport.
     *
     * @param report Raport do usunięcia
     */
    public void deleteReport(Report report) {
        reportRepository.delete(report);
    }

    /**
     * Pobiera raport po identyfikatorze.
     *
     * @param id Identyfikator raportu
     * @return Optional zawierający raport, jeśli istnieje
     */
    public Optional<Report> getReportById(Long id) {
        return reportRepository.findById(id);
    }

    /**
     * Pobiera wszystkie raporty.
     *
     * @return Lista wszystkich raportów
     */
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }
}