package com.example.backend.services;

import com.example.backend.models.ReportType;
import com.example.backend.repository.ReportTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
     * Konstruktor wstrzykujący zależność do repozytorium typów raportów.
     *
     * @param reportTypeRepository Repozytorium typów raportów
     */
    @Autowired
    public ReportTypeService(ReportTypeRepository reportTypeRepository) {
        this.reportTypeRepository = reportTypeRepository;
    }

    /**
     * Pobiera wszystkie typy raportów.
     *
     * @return Lista wszystkich typów raportów
     */
    public List<ReportType> getAllReportTypes() {
        return reportTypeRepository.findAll();
    }

    /**
     * Pobiera typ raportu na podstawie jego identyfikatora.
     *
     * @param id Identyfikator typu raportu
     * @return Opcjonalny typ raportu, jeśli istnieje
     */
    public Optional<ReportType> getReportTypeById(Integer id) {
        return reportTypeRepository.findById(id);
    }

    /**
     * Pobiera typ raportu na podstawie jego nazwy.
     *
     * @param name Nazwa typu raportu
     * @return Opcjonalny typ raportu, jeśli istnieje
     */
    public Optional<ReportType> getReportTypeByName(String name) {
        return reportTypeRepository.findByName(name);
    }

    /**
     * Zapisuje nowy typ raportu lub aktualizuje istniejący.
     *
     * @param reportType Typ raportu do zapisania
     * @return Zapisany typ raportu
     */
    public ReportType saveReportType(ReportType reportType) {
        return reportTypeRepository.save(reportType);
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

    /**
     * Aktualizuje ścieżkę szablonu dla typu raportu.
     *
     * @param id           Identyfikator typu raportu
     * @param templatePath Nowa ścieżka szablonu
     * @return Zaktualizowany typ raportu lub Optional.empty() jeśli typ nie istnieje
     */
    public Optional<ReportType> updateTemplatePath(Integer id, String templatePath) {
        return reportTypeRepository.findById(id)
                .map(type -> {
                    type.setTemplatePath(templatePath);
                    return reportTypeRepository.save(type);
                });
    }
}