package com.example.backend.services;

import com.example.backend.dto.ReportTypeDTO;
import com.example.backend.models.ReportType;
import com.example.backend.repository.ReportTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serwis obsługujący operacje dla encji {@link ReportType}.
 * <p>
 * Klasa zawiera logikę biznesową związaną z typami raportów.
 * Implementuje operacje tworzenia, odczytu, aktualizacji i usuwania typów raportów.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@Transactional
public class ReportTypeService {

    private final ReportTypeRepository reportTypeRepository;

    /**
     * Konstruktor wstrzykujący zależności do repozytorium typów raportów.
     *
     * @param reportTypeRepository Repozytorium typów raportów
     */
    @Autowired
    public ReportTypeService(ReportTypeRepository reportTypeRepository) {
        this.reportTypeRepository = reportTypeRepository;
    }

    /**
     * Konwertuje encję ReportType na obiekt DTO.
     *
     * @param entity Encja ReportType do konwersji
     * @return Obiekt DTO reprezentujący typ raportu
     */
    public ReportTypeDTO mapToDTO(ReportType entity) {
        if (entity == null) {
            return null;
        }
        
        ReportTypeDTO dto = new ReportTypeDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setTemplatePath(entity.getTemplatePath());
        return dto;
    }
    
    /**
     * Konwertuje obiekt DTO na encję ReportType.
     *
     * @param dto Obiekt DTO do konwersji
     * @return Encja ReportType
     */
    public ReportType mapToEntity(ReportTypeDTO dto) {
        if (dto == null) {
            return null;
        }
        
        ReportType entity = new ReportType();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setTemplatePath(dto.getTemplatePath());
        return entity;
    }

    /**
     * Pobiera wszystkie typy raportów.
     *
     * @return Lista wszystkich typów raportów jako DTO
     */
    public List<ReportTypeDTO> getAllReportTypes() {
        return reportTypeRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera typ raportu na podstawie jego identyfikatora.
     *
     * @param id Identyfikator typu raportu
     * @return Opcjonalny typ raportu jako DTO, jeśli istnieje
     */
    public Optional<ReportTypeDTO> getReportTypeById(Integer id) {
        return reportTypeRepository.findById(id)
                .map(this::mapToDTO);
    }

    /**
     * Pobiera typ raportu na podstawie jego nazwy.
     *
     * @param name Nazwa typu raportu
     * @return Opcjonalny typ raportu, jeśli istnieje
     */
    public Optional<ReportTypeDTO> getReportTypeByName(String name) {
        return reportTypeRepository.findByName(name)
                .map(this::mapToDTO);
    }

    /**
     * Zapisuje nowy typ raportu lub aktualizuje istniejący.
     *
     * @param dto Typ raportu jako DTO do zapisania
     * @return Zapisany typ raportu jako DTO
     */
    public ReportType saveReportType(ReportTypeDTO dto) {
        ReportType entity = mapToEntity(dto);
        return reportTypeRepository.save(entity);
    }

    /**
     * Tworzy nowy typ raportu z podanymi parametrami.
     *
     * @param name         Nazwa typu raportu
     * @param description  Opis typu raportu
     * @param templatePath Ścieżka do szablonu raportu
     * @return Utworzony typ raportu
     */
    public ReportType createReportType(String name, String description, String templatePath) {
        ReportType reportType = new ReportType();
        reportType.setName(name);
        reportType.setDescription(description);
        reportType.setTemplatePath(templatePath);

        return reportTypeRepository.save(reportType);
    }

    /**
     * Usuwa typ raportu na podstawie jego identyfikatora.
     *
     * @param id Identyfikator typu raportu do usunięcia
     * @throws IllegalStateException jeśli do typu raportu są przypisane raporty
     */
    public void deleteReportType(Integer id) {
        Optional<ReportType> typeOpt = reportTypeRepository.findById(id);
        if (typeOpt.isPresent()) {
            ReportType type = typeOpt.get();
            if (type.getReports() != null && !type.getReports().isEmpty()) {
                throw new IllegalStateException("Nie można usunąć typu raportu, do którego przypisane są raporty");
            }
            reportTypeRepository.deleteById(id);
        }
    }

    /**
     * Sprawdza, czy typ raportu o podanej nazwie już istnieje.
     *
     * @param name Nazwa typu raportu do sprawdzenia
     * @return true jeśli typ raportu istnieje, false w przeciwnym razie
     */
    public boolean existsByName(String name) {
        return reportTypeRepository.findByName(name).isPresent();
    }
}