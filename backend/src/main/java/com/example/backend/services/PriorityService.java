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
    public List<PriorityDTO> getAllPriorities() {
        return priorityRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera wszystkie priorytety posortowane według wartości (od najwyższego do najniższego)
     * jako obiekty DTO.
     *
     * @return Lista priorytetów posortowanych według wartości jako DTO
     */
    public List<PriorityDTO> getAllPrioritiesSortedByValue() {
        return priorityRepository.findAll().stream()
                .sorted(Comparator.comparing(Priority::getValue).reversed())
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera priorytet na podstawie jego identyfikatora jako obiekt DTO.
     *
     * @param id Identyfikator priorytetu
     * @return Opcjonalny priorytet jako DTO, jeśli istnieje
     */
    public Optional<PriorityDTO> getPriorityById(Integer id) {
        return priorityRepository.findById(id)
                .map(this::mapToDTO);
    }

    /**
     * Pobiera priorytet na podstawie jego nazwy jako obiekt DTO.
     *
     * @param name Nazwa priorytetu
     * @return Opcjonalny priorytet jako DTO, jeśli istnieje
     */
    public Optional<PriorityDTO> getPriorityByName(String name) {
        return priorityRepository.findByName(name)
                .map(this::mapToDTO);
    }

    /**
     * Zapisuje nowy priorytet lub aktualizuje istniejący na podstawie DTO.
     *
     * @param priorityDTO DTO priorytetu do zapisania
     * @return Zapisany priorytet jako DTO
     */
    public PriorityDTO savePriority(PriorityDTO priorityDTO) {
        Priority priority = mapToEntity(priorityDTO);
        Priority savedPriority = priorityRepository.save(priority);
        return mapToDTO(savedPriority);
    }

    /**
     * Tworzy nowy priorytet z podanymi parametrami i zwraca go jako DTO.
     *
     * @param name      Nazwa priorytetu
     * @param value     Wartość liczbowa priorytetu
     * @param colorCode Kod koloru w formacie HEX
     * @return Utworzony priorytet jako DTO
     */
    public PriorityDTO createPriority(String name, Integer value, String colorCode) {
        Priority priority = new Priority();
        priority.setName(name);
        priority.setValue(value);
        priority.setColorCode(colorCode);

        Priority savedPriority = priorityRepository.save(priority);
        return mapToDTO(savedPriority);
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
    public Optional<PriorityDTO> updateColor(Integer id, String colorCode) {
        return priorityRepository.findById(id)
                .map(priority -> {
                    priority.setColorCode(colorCode);
                    Priority updatedPriority = priorityRepository.save(priority);
                    return mapToDTO(updatedPriority);
                });
    }
}