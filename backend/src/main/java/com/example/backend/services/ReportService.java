package com.example.backend.services;

import com.example.backend.dto.ReportDTO;
import com.example.backend.models.Report;
import com.example.backend.models.ReportType;
import com.example.backend.models.User;
import com.example.backend.repository.ReportRepository;
import com.example.backend.repository.ReportTypeRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serwis obsługujący operacje na encji {@link Report}.
 *
 * Klasa zawiera logikę biznesową związaną z zarządzaniem raportami,
 * wykorzystując {@link ReportRepository} do komunikacji z bazą danych.
 *
 * @author Karol
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportTypeRepository reportTypeRepository;
    private final UserRepository userRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository,
                         ReportTypeRepository reportTypeRepository,
                         UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.reportTypeRepository = reportTypeRepository;
        this.userRepository = userRepository;
    }

    /**
     * Mapuje encję Report na obiekt DTO.
     *
     * @param report Encja do mapowania
     * @return Obiekt DTO reprezentujący raport
     */
    public ReportDTO mapToDTO(Report report) {
        if (report == null) return null;

        ReportDTO dto = new ReportDTO();
        dto.setId(report.getId());
        dto.setName(report.getName());

        if (report.getType() != null) {
            dto.setTypeId(report.getType().getId());
            dto.setTypeName(report.getType().getName());
        }

        if (report.getCreatedBy() != null) {
            dto.setCreatedById(report.getCreatedBy().getId());
            dto.setCreatedByUsername(report.getCreatedBy().getUsername());
            dto.setCreatedByFullName(report.getCreatedBy().getFirstName() + " " +
                    report.getCreatedBy().getLastName());
        }

        dto.setParameters(report.getParameters());
        dto.setFileName(report.getFileName());
        dto.setFilePath(report.getFilePath());
        dto.setCreatedAt(report.getCreatedAt());

        return dto;
    }

    /**
     * Mapuje obiekt DTO na encję Report.
     *
     * @param dto Obiekt DTO do mapowania
     * @return Encja Report
     */
    public Report mapToEntity(ReportDTO dto) {
        if (dto == null) return null;

        Report report = new Report();
        report.setId(dto.getId());
        report.setName(dto.getName());
        report.setParameters(dto.getParameters());
        report.setFileName(dto.getFileName());
        report.setFilePath(dto.getFilePath());

        if (dto.getCreatedAt() != null) {
            report.setCreatedAt(dto.getCreatedAt());
        } else {
            report.setCreatedAt(LocalDateTime.now());
        }

        if (dto.getTypeId() != null) {
            reportTypeRepository.findById(dto.getTypeId())
                    .ifPresent(report::setType);
        }

        if (dto.getCreatedById() != null) {
            userRepository.findById(dto.getCreatedById())
                    .ifPresent(report::setCreatedBy);
        }

        return report;
    }

    /**
     * Pobiera raporty po typie.
     *
     * @param type Typ raportu
     * @return Lista raportów danego typu jako DTO
     */
    public List<ReportDTO> getReportsByType(ReportType type) {
        return reportRepository.findByType(type).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera raporty utworzone przez danego użytkownika.
     *
     * @param user Użytkownik
     * @return Lista raportów utworzonych przez użytkownika jako DTO
     */
    public List<ReportDTO> getReportsByCreatedBy(User user) {
        return reportRepository.findByCreatedBy(user).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Zapisuje raport.
     *
     * @param reportDTO Raport do zapisania jako DTO
     * @return Zapisany raport jako DTO
     */
    public ReportDTO saveReport(ReportDTO reportDTO) {
        Report report = mapToEntity(reportDTO);
        Report savedReport = reportRepository.save(report);
        return mapToDTO(savedReport);
    }

    /**
     * Usuwa raport.
     *
     * @param reportId Identyfikator raportu do usunięcia
     * @return true jeśli usunięto raport, false w przeciwnym razie
     */
    public boolean deleteReport(Long reportId) {
        return reportRepository.findById(reportId)
                .map(report -> {
                    reportRepository.delete(report);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Pobiera raport po identyfikatorze.
     *
     * @param id Identyfikator raportu
     * @return Optional zawierający raport jako DTO, jeśli istnieje
     */
    public Optional<ReportDTO> getReportById(Long id) {
        return reportRepository.findById(id)
                .map(this::mapToDTO);
    }

    /**
     * Pobiera wszystkie raporty.
     *
     * @return Lista wszystkich raportów jako DTO
     */
    public List<ReportDTO> getAllReports() {
        return reportRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}