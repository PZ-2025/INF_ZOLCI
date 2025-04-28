package com.example.backend.services;

import com.example.backend.dto.PriorityDTO;
import com.example.backend.models.Priority;
import com.example.backend.repository.PriorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serwis obsługujący operacje dla encji {@link Priority}.
 * <p>
 * Klasa zawiera logikę biznesową związaną z priorytetami zadań.
 * Implementuje operacje tworzenia, odczytu, aktualizacji i usuwania priorytetów,
 * a także metody sortowania i wyszukiwania priorytetów.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@Transactional
public class PriorityService {

    private final PriorityRepository priorityRepository;

    /**
     * Konstruktor wstrzykujący zależność do repozytorium priorytetów.
     *
     * @param priorityRepository Repozytorium priorytetów
     */
    @Autowired
    public PriorityService(PriorityRepository priorityRepository) {
        this.priorityRepository = priorityRepository;
    }

    /**
     * Mapuje encję Priority na obiekt DTO.
     *
     * @param priority Encja do mapowania
     * @return Obiekt DTO reprezentujący priorytet
     */
    private PriorityDTO mapToDTO(Priority priority) {
        if (priority == null) return null;

        PriorityDTO dto = new PriorityDTO();
        dto.setId(priority.getId());
        dto.setName(priority.getName());
        dto.setValue(priority.getValue());
        dto.setColorCode(priority.getColorCode());

        return dto;
    }

    /**
     * Mapuje obiekt DTO na encję Priority.
     *
     * @param dto Obiekt DTO do mapowania
     * @return Encja reprezentująca priorytet
     */
    private Priority mapToEntity(PriorityDTO dto) {
        if (dto == null) return null;

        Priority priority = new Priority();
        priority.setId(dto.getId());
        priority.setName(dto.getName());
        priority.setValue(dto.getValue());
        priority.setColorCode(dto.getColorCode());

        return priority;
    }

    /**
     * Pobiera wszystkie priorytety jako obiekty DTO.
     *
     * @return Lista wszystkich priorytetów jako DTO
     */
    public List<PriorityDTO> getAllPrioritiesDTO() {
        return priorityRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera wszystkie priorytety.
     *
     * @return Lista wszystkich priorytetów
     * @deprecated Użyj {@link #getAllPrioritiesDTO()} zamiast tej metody
     */
    @Deprecated
    public List<Priority> getAllPriorities() {
        return priorityRepository.findAll();
    }

    /**
     * Pobiera wszystkie priorytety posortowane według wartości (od najwyższego do najniższego)
     * jako obiekty DTO.
     *
     * @return Lista priorytetów posortowanych według wartości jako DTO
     */
    public List<PriorityDTO> getAllPrioritiesSortedByValueDTO() {
        return priorityRepository.findAll().stream()
                .sorted(Comparator.comparing(Priority::getValue).reversed())
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera wszystkie priorytety posortowane według wartości (od najwyższego do najniższego).
     *
     * @return Lista priorytetów posortowanych według wartości
     * @deprecated Użyj {@link #getAllPrioritiesSortedByValueDTO()} zamiast tej metody
     */
    @Deprecated
    public List<Priority> getAllPrioritiesSortedByValue() {
        return priorityRepository.findAll().stream()
                .sorted(Comparator.comparing(Priority::getValue).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Pobiera priorytet na podstawie jego identyfikatora jako obiekt DTO.
     *
     * @param id Identyfikator priorytetu
     * @return Opcjonalny priorytet jako DTO, jeśli istnieje
     */
    public Optional<PriorityDTO> getPriorityDTOById(Integer id) {
        return priorityRepository.findById(id)
                .map(this::mapToDTO);
    }

    /**
     * Pobiera priorytet na podstawie jego identyfikatora.
     *
     * @param id Identyfikator priorytetu
     * @return Opcjonalny priorytet, jeśli istnieje
     * @deprecated Użyj {@link #getPriorityDTOById(Integer)} zamiast tej metody
     */
    @Deprecated
    public Optional<Priority> getPriorityById(Integer id) {
        return priorityRepository.findById(id);
    }

    /**
     * Pobiera priorytet na podstawie jego nazwy jako obiekt DTO.
     *
     * @param name Nazwa priorytetu
     * @return Opcjonalny priorytet jako DTO, jeśli istnieje
     */
    public Optional<PriorityDTO> getPriorityDTOByName(String name) {
        return priorityRepository.findByName(name)
                .map(this::mapToDTO);
    }

    /**
     * Pobiera priorytet na podstawie jego nazwy.
     *
     * @param name Nazwa priorytetu
     * @return Opcjonalny priorytet, jeśli istnieje
     * @deprecated Użyj {@link #getPriorityDTOByName(String)} zamiast tej metody
     */
    @Deprecated
    public Optional<Priority> getPriorityByName(String name) {
        return priorityRepository.findByName(name);
    }

    /**
     * Zapisuje nowy priorytet lub aktualizuje istniejący na podstawie DTO.
     *
     * @param priorityDTO DTO priorytetu do zapisania
     * @return Zapisany priorytet jako DTO
     */
    public PriorityDTO savePriorityDTO(PriorityDTO priorityDTO) {
        Priority priority = mapToEntity(priorityDTO);
        Priority savedPriority = priorityRepository.save(priority);
        return mapToDTO(savedPriority);
    }

    /**
     * Zapisuje nowy priorytet lub aktualizuje istniejący.
     *
     * @param priority Priorytet do zapisania
     * @return Zapisany priorytet
     * @deprecated Użyj {@link #savePriorityDTO(PriorityDTO)} zamiast tej metody
     */
    @Deprecated
    public Priority savePriority(Priority priority) {
        return priorityRepository.save(priority);
    }

    /**
     * Tworzy nowy priorytet z podanymi parametrami i zwraca go jako DTO.
     *
     * @param name      Nazwa priorytetu
     * @param value     Wartość liczbowa priorytetu
     * @param colorCode Kod koloru w formacie HEX
     * @return Utworzony priorytet jako DTO
     */
    public PriorityDTO createPriorityDTO(String name, Integer value, String colorCode) {
        Priority priority = new Priority();
        priority.setName(name);
        priority.setValue(value);
        priority.setColorCode(colorCode);

        Priority savedPriority = priorityRepository.save(priority);
        return mapToDTO(savedPriority);
    }

    /**
     * Tworzy nowy priorytet z podanymi parametrami.
     *
     * @param name      Nazwa priorytetu
     * @param value     Wartość liczbowa priorytetu
     * @param colorCode Kod koloru w formacie HEX
     * @return Utworzony priorytet
     * @deprecated Użyj {@link #createPriorityDTO(String, Integer, String)} zamiast tej metody
     */
    @Deprecated
    public Priority createPriority(String name, Integer value, String colorCode) {
        Priority priority = new Priority();
        priority.setName(name);
        priority.setValue(value);
        priority.setColorCode(colorCode);

        return priorityRepository.save(priority);
    }

    /**
     * Usuwa priorytet na podstawie jego identyfikatora.
     *
     * @param id Identyfikator priorytetu do usunięcia
     * @throws IllegalStateException jeśli do priorytetu są przypisane zadania
     */
    public void deletePriority(Integer id) {
        Optional<Priority> priorityOpt = priorityRepository.findById(id);

        if (priorityOpt.isPresent()) {
            Priority priority = priorityOpt.get();

            if (priority.getTasks() != null && !priority.getTasks().isEmpty()) {
                throw new IllegalStateException("Nie można usunąć priorytetu, do którego przypisane są zadania");
            }

            priorityRepository.deleteById(id);
        }
    }

    /**
     * Sprawdza, czy priorytet o podanej nazwie już istnieje.
     *
     * @param name Nazwa priorytetu do sprawdzenia
     * @return true jeśli priorytet istnieje, false w przeciwnym razie
     */
    public boolean existsByName(String name) {
        return priorityRepository.findByName(name).isPresent();
    }

    /**
     * Aktualizuje kolor priorytetu i zwraca zaktualizowany priorytet jako DTO.
     *
     * @param id        Identyfikator priorytetu
     * @param colorCode Nowy kod koloru w formacie HEX
     * @return Zaktualizowany priorytet jako DTO lub Optional.empty() jeśli priorytet nie istnieje
     */
    public Optional<PriorityDTO> updateColorDTO(Integer id, String colorCode) {
        return priorityRepository.findById(id)
                .map(priority -> {
                    priority.setColorCode(colorCode);
                    Priority updatedPriority = priorityRepository.save(priority);
                    return mapToDTO(updatedPriority);
                });
    }

    /**
     * Aktualizuje kolor priorytetu.
     *
     * @param id        Identyfikator priorytetu
     * @param colorCode Nowy kod koloru w formacie HEX
     * @return Zaktualizowany priorytet lub Optional.empty() jeśli priorytet nie istnieje
     * @deprecated Użyj {@link #updateColorDTO(Integer, String)} zamiast tej metody
     */
    @Deprecated
    public Optional<Priority> updateColor(Integer id, String colorCode) {
        return priorityRepository.findById(id)
                .map(priority -> {
                    priority.setColorCode(colorCode);
                    return priorityRepository.save(priority);
                });
    }
}