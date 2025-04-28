package com.example.backend.services;

import com.example.backend.dto.PriorityDTO;
import com.example.backend.models.Priority;
import com.example.backend.models.Task;
import com.example.backend.repository.PriorityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriorityServiceTest {

    @Mock
    private PriorityRepository priorityRepository;

    @InjectMocks
    private PriorityService priorityService;

    private Priority priority;
    private PriorityDTO priorityDTO;

    @BeforeEach
    void setUp() {
        // Initialize test data
        priority = new Priority();
        priority.setId(1);
        priority.setName("High");
        priority.setValue(3);
        priority.setColorCode("#FF0000");
        priority.setTasks(new HashSet<>());

        priorityDTO = new PriorityDTO();
        priorityDTO.setId(1);
        priorityDTO.setName("High");
        priorityDTO.setValue(3);
        priorityDTO.setColorCode("#FF0000");
    }

    @Test
    void getAllPriorities_ShouldReturnAllPriorities() {
        // Arrange
        when(priorityRepository.findAll()).thenReturn(Arrays.asList(priority));

        // Act
        List<PriorityDTO> result = priorityService.getAllPriorities();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("High", result.get(0).getName());
        assertEquals(3, result.get(0).getValue());
        assertEquals("#FF0000", result.get(0).getColorCode());
    }

    @Test
    void getAllPrioritiesSortedByValue_ShouldReturnSortedPriorities() {
        // Arrange
        Priority lowPriority = new Priority();
        lowPriority.setId(2);
        lowPriority.setName("Low");
        lowPriority.setValue(1);
        lowPriority.setColorCode("#00FF00");

        Priority mediumPriority = new Priority();
        mediumPriority.setId(3);
        mediumPriority.setName("Medium");
        mediumPriority.setValue(2);
        mediumPriority.setColorCode("#FFFF00");

        when(priorityRepository.findAll()).thenReturn(Arrays.asList(lowPriority, mediumPriority, priority));

        // Act
        List<PriorityDTO> result = priorityService.getAllPrioritiesSortedByValue();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        // Should be sorted by value in descending order
        assertEquals("High", result.get(0).getName());
        assertEquals("Medium", result.get(1).getName());
        assertEquals("Low", result.get(2).getName());
    }

    @Test
    void getPriorityById_WhenExists_ShouldReturnPriorityDTO() {
        // Arrange
        when(priorityRepository.findById(1)).thenReturn(Optional.of(priority));

        // Act
        Optional<PriorityDTO> result = priorityService.getPriorityById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("High", result.get().getName());
        assertEquals(3, result.get().getValue());
    }

    @Test
    void getPriorityById_WhenNotExists_ShouldReturnEmptyOptional() {
        // Arrange
        when(priorityRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<PriorityDTO> result = priorityService.getPriorityById(999);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getPriorityByName_WhenExists_ShouldReturnPriorityDTO() {
        // Arrange
        when(priorityRepository.findByName("High")).thenReturn(Optional.of(priority));

        // Act
        Optional<PriorityDTO> result = priorityService.getPriorityByName("High");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals(3, result.get().getValue());
    }

    @Test
    void getPriorityByName_WhenNotExists_ShouldReturnEmptyOptional() {
        // Arrange
        when(priorityRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<PriorityDTO> result = priorityService.getPriorityByName("Nonexistent");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void savePriority_ShouldMapAndSavePriority() {
        // Arrange
        when(priorityRepository.save(any(Priority.class))).thenReturn(priority);

        // Act
        PriorityDTO result = priorityService.savePriority(priorityDTO);

        // Assert
        assertNotNull(result);
        assertEquals("High", result.getName());
        assertEquals(3, result.getValue());
        assertEquals("#FF0000", result.getColorCode());
        verify(priorityRepository).save(any(Priority.class));
    }

    @Test
    void createPriority_ShouldCreateAndSaveNewPriority() {
        // Arrange
        when(priorityRepository.save(any(Priority.class))).thenReturn(priority);

        // Act
        PriorityDTO result = priorityService.createPriority("High", 3, "#FF0000");

        // Assert
        assertNotNull(result);
        assertEquals("High", result.getName());
        assertEquals(3, result.getValue());
        assertEquals("#FF0000", result.getColorCode());
        verify(priorityRepository).save(any(Priority.class));
    }

    @Test
    void deletePriority_WhenPriorityHasNoTasks_ShouldDelete() {
        // Arrange
        when(priorityRepository.findById(1)).thenReturn(Optional.of(priority));

        // Act
        priorityService.deletePriority(1);

        // Assert
        verify(priorityRepository).deleteById(1);
    }

    @Test
    void deletePriority_WhenPriorityHasTasks_ShouldThrowException() {
        // Arrange
        Priority priorityWithTasks = new Priority();
        priorityWithTasks.setId(1);
        priorityWithTasks.setName("High");

        Set<Task> tasks = new HashSet<>();
        tasks.add(new Task());
        priorityWithTasks.setTasks(tasks);

        when(priorityRepository.findById(1)).thenReturn(Optional.of(priorityWithTasks));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> priorityService.deletePriority(1));

        assertEquals("Nie można usunąć priorytetu, do którego przypisane są zadania", exception.getMessage());
        verify(priorityRepository, never()).deleteById(anyInt());
    }

    @Test
    void existsByName_WhenNameExists_ShouldReturnTrue() {
        // Arrange
        when(priorityRepository.findByName("High")).thenReturn(Optional.of(priority));

        // Act
        boolean result = priorityService.existsByName("High");

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByName_WhenNameDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(priorityRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

        // Act
        boolean result = priorityService.existsByName("Nonexistent");

        // Assert
        assertFalse(result);
    }

    @Test
    void updateColor_WhenPriorityExists_ShouldUpdateAndReturnPriority() {
        // Arrange
        when(priorityRepository.findById(1)).thenReturn(Optional.of(priority));

        // Configure save to update the color
        when(priorityRepository.save(any(Priority.class))).thenAnswer(invocation -> {
            Priority savedPriority = invocation.getArgument(0);
            savedPriority.setColorCode("#00FF00");
            return savedPriority;
        });

        // Act
        Optional<PriorityDTO> result = priorityService.updateColor(1, "#00FF00");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("#00FF00", result.get().getColorCode());
        verify(priorityRepository).save(any(Priority.class));
    }

    @Test
    void updateColor_WhenPriorityDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(priorityRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<PriorityDTO> result = priorityService.updateColor(999, "#00FF00");

        // Assert
        assertFalse(result.isPresent());
        verify(priorityRepository, never()).save(any(Priority.class));
    }
}