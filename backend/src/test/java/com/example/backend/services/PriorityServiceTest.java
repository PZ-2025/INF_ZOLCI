package com.example.backend.services;

import com.example.backend.models.Priority;
import com.example.backend.models.Task;
import com.example.backend.repository.PriorityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PriorityServiceTest {

    @Mock
    private PriorityRepository priorityRepository;

    @InjectMocks
    private PriorityService priorityService;

    private Priority lowPriority;
    private Priority mediumPriority;
    private Priority highPriority;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Inicjalizacja priorytetów testowych
        lowPriority = new Priority();
        lowPriority.setId(1);
        lowPriority.setName("Niski");
        lowPriority.setValue(1);
        lowPriority.setColorCode("#28a745");

        mediumPriority = new Priority();
        mediumPriority.setId(2);
        mediumPriority.setName("Średni");
        mediumPriority.setValue(2);
        mediumPriority.setColorCode("#ffc107");

        highPriority = new Priority();
        highPriority.setId(3);
        highPriority.setName("Wysoki");
        highPriority.setValue(3);
        highPriority.setColorCode("#dc3545");
    }

    @Test
    void getAllPriorities_ShouldReturnAllPriorities() {
        // Given
        List<Priority> expectedPriorities = Arrays.asList(lowPriority, mediumPriority, highPriority);
        when(priorityRepository.findAll()).thenReturn(expectedPriorities);

        // When
        List<Priority> actualPriorities = priorityService.getAllPriorities();

        // Then
        assertEquals(expectedPriorities.size(), actualPriorities.size());
        assertEquals(expectedPriorities.get(0).getId(), actualPriorities.get(0).getId());
        assertEquals(expectedPriorities.get(1).getId(), actualPriorities.get(1).getId());
        assertEquals(expectedPriorities.get(2).getId(), actualPriorities.get(2).getId());
        verify(priorityRepository, times(1)).findAll();
    }

    @Test
    void getAllPrioritiesSortedByValue_ShouldReturnSortedPriorities() {
        // Given
        List<Priority> priorities = Arrays.asList(lowPriority, mediumPriority, highPriority);
        when(priorityRepository.findAll()).thenReturn(priorities);

        // When
        List<Priority> sortedPriorities = priorityService.getAllPrioritiesSortedByValue();

        // Then
        assertEquals(3, sortedPriorities.size());
        assertEquals(highPriority.getId(), sortedPriorities.get(0).getId()); // Highest first
        assertEquals(mediumPriority.getId(), sortedPriorities.get(1).getId());
        assertEquals(lowPriority.getId(), sortedPriorities.get(2).getId()); // Lowest last
        verify(priorityRepository, times(1)).findAll();
    }

    @Test
    void getPriorityById_WhenPriorityExists_ShouldReturnPriority() {
        // Given
        when(priorityRepository.findById(1)).thenReturn(Optional.of(lowPriority));

        // When
        Optional<Priority> result = priorityService.getPriorityById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(lowPriority.getId(), result.get().getId());
        assertEquals(lowPriority.getName(), result.get().getName());
        verify(priorityRepository, times(1)).findById(1);
    }

    @Test
    void getPriorityById_WhenPriorityDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(priorityRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<Priority> result = priorityService.getPriorityById(99);

        // Then
        assertFalse(result.isPresent());
        verify(priorityRepository, times(1)).findById(99);
    }

    @Test
    void getPriorityByName_WhenPriorityExists_ShouldReturnPriority() {
        // Given
        when(priorityRepository.findByName("Wysoki")).thenReturn(Optional.of(highPriority));

        // When
        Optional<Priority> result = priorityService.getPriorityByName("Wysoki");

        // Then
        assertTrue(result.isPresent());
        assertEquals(highPriority.getId(), result.get().getId());
        assertEquals("Wysoki", result.get().getName());
        verify(priorityRepository, times(1)).findByName("Wysoki");
    }

    @Test
    void savePriority_ShouldSaveAndReturnPriority() {
        // Given
        Priority newPriority = new Priority();
        newPriority.setName("Krytyczny");
        newPriority.setValue(4);
        newPriority.setColorCode("#ff0000");

        when(priorityRepository.save(any(Priority.class))).thenReturn(newPriority);

        // When
        Priority savedPriority = priorityService.savePriority(newPriority);

        // Then
        assertEquals(newPriority.getName(), savedPriority.getName());
        assertEquals(newPriority.getValue(), savedPriority.getValue());
        assertEquals(newPriority.getColorCode(), savedPriority.getColorCode());
        verify(priorityRepository, times(1)).save(newPriority);
    }

    @Test
    void createPriority_ShouldCreateAndReturnPriority() {
        // Given
        String name = "Krytyczny";
        Integer value = 4;
        String colorCode = "#ff0000";

        when(priorityRepository.save(any(Priority.class))).thenAnswer(invocation -> {
            Priority saved = invocation.getArgument(0);
            saved.setId(4); // Symulacja nadania ID przez bazę danych
            return saved;
        });

        // When
        Priority createdPriority = priorityService.createPriority(name, value, colorCode);

        // Then
        assertNotNull(createdPriority.getId());
        assertEquals(name, createdPriority.getName());
        assertEquals(value, createdPriority.getValue());
        assertEquals(colorCode, createdPriority.getColorCode());
        verify(priorityRepository, times(1)).save(any(Priority.class));
    }

    @Test
    void deletePriority_WithNoTasks_ShouldDeletePriority() {
        // Given
        Priority priorityWithNoTasks = new Priority();
        priorityWithNoTasks.setId(1);
        priorityWithNoTasks.setName("Niski");
        priorityWithNoTasks.setTasks(new HashSet<>());

        when(priorityRepository.findById(1)).thenReturn(Optional.of(priorityWithNoTasks));
        doNothing().when(priorityRepository).deleteById(1);

        // When & Then
        assertDoesNotThrow(() -> priorityService.deletePriority(1));
        verify(priorityRepository, times(1)).findById(1);
        verify(priorityRepository, times(1)).deleteById(1);
    }

    @Test
    void deletePriority_WithTasks_ShouldThrowException() {
        // Given
        Priority priorityWithTasks = new Priority();
        priorityWithTasks.setId(1);
        priorityWithTasks.setName("Niski");

        Set<Task> tasks = new HashSet<>();
        tasks.add(new Task());
        priorityWithTasks.setTasks(tasks);

        when(priorityRepository.findById(1)).thenReturn(Optional.of(priorityWithTasks));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> priorityService.deletePriority(1));

        assertTrue(exception.getMessage().contains("Nie można usunąć priorytetu"));
        verify(priorityRepository, times(1)).findById(1);
        verify(priorityRepository, never()).deleteById(anyInt());
    }

    @Test
    void existsByName_WhenPriorityExists_ShouldReturnTrue() {
        // Given
        when(priorityRepository.findByName("Wysoki")).thenReturn(Optional.of(highPriority));

        // When
        boolean result = priorityService.existsByName("Wysoki");

        // Then
        assertTrue(result);
        verify(priorityRepository, times(1)).findByName("Wysoki");
    }

    @Test
    void existsByName_WhenPriorityDoesNotExist_ShouldReturnFalse() {
        // Given
        when(priorityRepository.findByName("Nieistniejący")).thenReturn(Optional.empty());

        // When
        boolean result = priorityService.existsByName("Nieistniejący");

        // Then
        assertFalse(result);
        verify(priorityRepository, times(1)).findByName("Nieistniejący");
    }

    @Test
    void updateColor_WhenPriorityExists_ShouldUpdateAndReturnPriority() {
        // Given
        when(priorityRepository.findById(1)).thenReturn(Optional.of(lowPriority));
        when(priorityRepository.save(any(Priority.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String newColorCode = "#00ff00";

        // When
        Optional<Priority> result = priorityService.updateColor(1, newColorCode);

        // Then
        assertTrue(result.isPresent());
        assertEquals(newColorCode, result.get().getColorCode());
        verify(priorityRepository, times(1)).findById(1);
        verify(priorityRepository, times(1)).save(any(Priority.class));
    }

    @Test
    void updateColor_WhenPriorityDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(priorityRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<Priority> result = priorityService.updateColor(99, "#00ff00");

        // Then
        assertFalse(result.isPresent());
        verify(priorityRepository, times(1)).findById(99);
        verify(priorityRepository, never()).save(any(Priority.class));
    }
}