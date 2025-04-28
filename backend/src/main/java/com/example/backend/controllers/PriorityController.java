package com.example.backend.controllers;

import com.example.backend.dto.PriorityDTO;
import com.example.backend.services.PriorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Kontroler REST dla operacji na priorytetach zadań.
 * <p>
 * Klasa zapewnia endpoints do zarządzania priorytetami zadań w systemie,
 * w tym tworzenie, odczyt, aktualizację i usuwanie priorytetów.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/database/priorities")
public class PriorityController {

    private final PriorityService priorityService;

    /**
     * Konstruktor wstrzykujący zależność do serwisu priorytetów.
     *
     * @param priorityService Serwis priorytetów
     */
    @Autowired
    public PriorityController(PriorityService priorityService) {
        this.priorityService = priorityService;
    }

    /**
     * Pobiera wszystkie priorytety.
     *
     * @return Lista wszystkich priorytetów
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PriorityDTO>> getAllPriorities() {
        List<PriorityDTO> priorities = priorityService.getAllPriorities();
        return new ResponseEntity<>(priorities, HttpStatus.OK);
    }

    /**
     * Pobiera wszystkie priorytety posortowane według wartości (od najwyższego do najniższego).
     *
     * @return Lista priorytetów posortowanych według wartości
     */
    @GetMapping(value = "/sorted", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PriorityDTO>> getAllPrioritiesSorted() {
        List<PriorityDTO> priorities = priorityService.getAllPrioritiesSortedByValue();
        return new ResponseEntity<>(priorities, HttpStatus.OK);
    }

    /**
     * Pobiera priorytet na podstawie jego identyfikatora.
     *
     * @param id Identyfikator priorytetu
     * @return Priorytet lub status 404, jeśli nie istnieje
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PriorityDTO> getPriorityById(@PathVariable Integer id) {
        return priorityService.getPriorityById(id)
                .map(priority -> new ResponseEntity<>(priority, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Pobiera priorytet na podstawie jego nazwy.
     *
     * @param name Nazwa priorytetu
     * @return Priorytet lub status 404, jeśli nie istnieje
     */
    @GetMapping(value = "/name/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PriorityDTO> getPriorityByName(@PathVariable String name) {
        return priorityService.getPriorityByName(name)
                .map(priority -> new ResponseEntity<>(priority, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Tworzy nowy priorytet.
     *
     * @param priorityDTO Dane nowego priorytetu
     * @return Utworzony priorytet
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PriorityDTO> createPriority(@Valid @RequestBody PriorityDTO priorityDTO) {
        if (priorityService.existsByName(priorityDTO.getName())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        PriorityDTO savedPriority = priorityService.savePriority(priorityDTO);
        return new ResponseEntity<>(savedPriority, HttpStatus.CREATED);
    }

    /**
     * Tworzy nowy priorytet na podstawie podanych parametrów.
     *
     * @param payload Mapa zawierająca name, value, colorCode
     * @return Utworzony priorytet lub status błędu
     */
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PriorityDTO> createPriorityFromParams(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        Integer value = (Integer) payload.get("value");
        String colorCode = (String) payload.get("colorCode");

        if (name == null || value == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (priorityService.existsByName(name)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        PriorityDTO newPriority = priorityService.createPriority(name, value, colorCode);
        return new ResponseEntity<>(newPriority, HttpStatus.CREATED);
    }

    /**
     * Aktualizuje istniejący priorytet.
     *
     * @param id         Identyfikator priorytetu
     * @param priorityDTO Zaktualizowane dane priorytetu
     * @return Zaktualizowany priorytet lub status 404, jeśli nie istnieje
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PriorityDTO> updatePriority(@PathVariable Integer id,
                                                      @Valid @RequestBody PriorityDTO priorityDTO) {
        if (!priorityService.getPriorityById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        priorityDTO.setId(id);
        PriorityDTO updatedPriority = priorityService.savePriority(priorityDTO);
        return new ResponseEntity<>(updatedPriority, HttpStatus.OK);
    }

    /**
     * Aktualizuje kolor priorytetu.
     *
     * @param id        Identyfikator priorytetu
     * @param payload   Mapa zawierająca colorCode
     * @return Zaktualizowany priorytet lub status 404, jeśli nie istnieje
     */
    @PatchMapping(value = "/{id}/color", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PriorityDTO> updatePriorityColor(@PathVariable Integer id,
                                                           @RequestBody Map<String, String> payload) {
        String colorCode = payload.get("colorCode");

        if (colorCode == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return priorityService.updateColor(id, colorCode)
                .map(priority -> new ResponseEntity<>(priority, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Usuwa priorytet na podstawie jego identyfikatora.
     *
     * @param id Identyfikator priorytetu do usunięcia
     * @return Status 204 po pomyślnym usunięciu, 404 jeśli nie istnieje, lub 409 jeśli są przypisane zadania
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePriority(@PathVariable Integer id) {
        if (!priorityService.getPriorityById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            priorityService.deletePriority(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }
}