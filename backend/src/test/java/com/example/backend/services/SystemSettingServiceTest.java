package com.example.backend.services;

import com.example.backend.dto.SystemSettingDTO;
import com.example.backend.models.SystemSetting;
import com.example.backend.models.User;
import com.example.backend.repository.SystemSettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemSettingServiceTest {

    @Mock
    private SystemSettingRepository systemSettingRepository;

    @InjectMocks
    private SystemSettingService systemSettingService;

    private SystemSetting systemSetting;
    private SystemSettingDTO systemSettingDTO;
    private User user;

    @BeforeEach
    void setUp() {
        // Initialize test data
        user = new User();
        user.setId(1);
        user.setUsername("admin");
        user.setFirstName("Admin");
        user.setLastName("User");

        systemSetting = new SystemSetting();
        systemSetting.setId(1);
        systemSetting.setKey("app.name");
        systemSetting.setValue("BuildTask");
        systemSetting.setDescription("Application name");
        systemSetting.setUpdatedBy(user);
        systemSetting.setUpdatedAt(LocalDateTime.now());

        systemSettingDTO = new SystemSettingDTO();
        systemSettingDTO.setId(1);
        systemSettingDTO.setKey("app.name");
        systemSettingDTO.setValue("BuildTask");
        systemSettingDTO.setDescription("Application name");
        systemSettingDTO.setUpdatedById(1);
        systemSettingDTO.setUpdatedAt(LocalDateTime.now());
        systemSettingDTO.setUpdatedByUsername("admin");
        systemSettingDTO.setUpdatedByFullName("Admin User");
    }

    @Test
    void getAllSystemSettings_ShouldReturnAllSettings() {
        // Arrange
        when(systemSettingRepository.findAll()).thenReturn(Arrays.asList(systemSetting));

        // Act
        List<SystemSettingDTO> result = systemSettingService.getAllSystemSettings();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("app.name", result.get(0).getKey());
        assertEquals("BuildTask", result.get(0).getValue());
        assertEquals("Application name", result.get(0).getDescription());
    }

    @Test
    void getSystemSettingById_WhenExists_ShouldReturnSetting() {
        // Arrange
        when(systemSettingRepository.findById(1)).thenReturn(Optional.of(systemSetting));

        // Act
        Optional<SystemSettingDTO> result = systemSettingService.getSystemSettingById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("app.name", result.get().getKey());
        assertEquals("BuildTask", result.get().getValue());
    }

    @Test
    void getSystemSettingById_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(systemSettingRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<SystemSettingDTO> result = systemSettingService.getSystemSettingById(999);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getSystemSettingByKey_WhenExists_ShouldReturnSetting() {
        // Arrange
        when(systemSettingRepository.findByKey("app.name")).thenReturn(Optional.of(systemSetting));

        // Act
        Optional<SystemSettingDTO> result = systemSettingService.getSystemSettingByKey("app.name");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("BuildTask", result.get().getValue());
    }

    @Test
    void getSystemSettingByKey_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(systemSettingRepository.findByKey("nonexistent.key")).thenReturn(Optional.empty());

        // Act
        Optional<SystemSettingDTO> result = systemSettingService.getSystemSettingByKey("nonexistent.key");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void saveSystemSetting_WithNewSetting_ShouldCreateAndSave() {
        // Arrange
        when(systemSettingRepository.save(any(SystemSetting.class))).thenReturn(systemSetting);

        // Create DTO without ID (new setting)
        SystemSettingDTO newSettingDTO = new SystemSettingDTO();
        newSettingDTO.setKey("new.setting");
        newSettingDTO.setValue("New Value");
        newSettingDTO.setDescription("New setting description");
        newSettingDTO.setUpdatedById(1);

        // Act
        SystemSettingDTO result = systemSettingService.saveSystemSetting(newSettingDTO);

        // Assert
        assertNotNull(result);
        assertEquals("app.name", result.getKey()); // Mock returns original setting
        verify(systemSettingRepository).save(any(SystemSetting.class));
    }

    @Test
    void saveSystemSetting_WithExistingId_ShouldUpdateAndSave() {
        // Arrange
        when(systemSettingRepository.findById(1)).thenReturn(Optional.of(systemSetting));
        when(systemSettingRepository.save(any(SystemSetting.class))).thenReturn(systemSetting);

        // Update value in DTO
        systemSettingDTO.setValue("UpdatedValue");

        // Act
        SystemSettingDTO result = systemSettingService.saveSystemSetting(systemSettingDTO);

        // Assert
        assertNotNull(result);
        assertEquals("app.name", result.getKey());
        assertEquals("BuildTask", result.getValue()); // Mock returns original setting
        verify(systemSettingRepository).save(any(SystemSetting.class));
    }

    @Test
    void saveSystemSetting_WithExistingKey_ShouldUpdateAndSave() {
        // Arrange
        when(systemSettingRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(systemSettingRepository.findByKey("app.name")).thenReturn(Optional.of(systemSetting));
        when(systemSettingRepository.save(any(SystemSetting.class))).thenReturn(systemSetting);

        // Create DTO with existing key but no ID
        SystemSettingDTO existingKeyDTO = new SystemSettingDTO();
        existingKeyDTO.setKey("app.name");
        existingKeyDTO.setValue("UpdatedValue");
        existingKeyDTO.setUpdatedById(1);

        // Act
        SystemSettingDTO result = systemSettingService.saveSystemSetting(existingKeyDTO);

        // Assert
        assertNotNull(result);
        assertEquals("app.name", result.getKey());
        assertEquals("BuildTask", result.getValue()); // Mock returns original setting
        verify(systemSettingRepository).save(any(SystemSetting.class));
    }

    @Test
    void createSystemSetting_ShouldCreateAndReturnSetting() {
        // Arrange
        when(systemSettingRepository.save(any(SystemSetting.class))).thenReturn(systemSetting);

        // Act
        SystemSettingDTO result = systemSettingService.createSystemSetting(
                "app.name", "BuildTask", "Application name", user);

        // Assert
        assertNotNull(result);
        assertEquals("app.name", result.getKey());
        assertEquals("BuildTask", result.getValue());
        assertEquals("Application name", result.getDescription());
        verify(systemSettingRepository).save(any(SystemSetting.class));
    }

    @Test
    void updateValue_WhenSettingExists_ShouldUpdateAndReturnSetting() {
        // Arrange
        when(systemSettingRepository.findByKey("app.name")).thenReturn(Optional.of(systemSetting));
        when(systemSettingRepository.save(any(SystemSetting.class))).thenReturn(systemSetting);

        // Act
        Optional<SystemSettingDTO> result = systemSettingService.updateValue("app.name", "NewAppName", user);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("app.name", result.get().getKey());
        assertEquals("BuildTask", result.get().getValue()); // Mock returns original setting
        verify(systemSettingRepository).save(any(SystemSetting.class));
    }

    @Test
    void updateValue_WhenSettingDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(systemSettingRepository.findByKey("nonexistent.key")).thenReturn(Optional.empty());

        // Act
        Optional<SystemSettingDTO> result = systemSettingService.updateValue("nonexistent.key", "Value", user);

        // Assert
        assertFalse(result.isPresent());
        verify(systemSettingRepository, never()).save(any(SystemSetting.class));
    }

    @Test
    void deleteSystemSetting_ShouldCallRepositoryDelete() {
        // Act
        systemSettingService.deleteSystemSetting(1);

        // Assert
        verify(systemSettingRepository).deleteById(1);
    }

    @Test
    void existsByKey_WhenKeyExists_ShouldReturnTrue() {
        // Arrange
        when(systemSettingRepository.findByKey("app.name")).thenReturn(Optional.of(systemSetting));

        // Act
        boolean result = systemSettingService.existsByKey("app.name");

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByKey_WhenKeyDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(systemSettingRepository.findByKey("nonexistent.key")).thenReturn(Optional.empty());

        // Act
        boolean result = systemSettingService.existsByKey("nonexistent.key");

        // Assert
        assertFalse(result);
    }

    @Test
    void getStringValue_WhenSettingExists_ShouldReturnValue() {
        // Arrange
        when(systemSettingRepository.findByKey("app.name")).thenReturn(Optional.of(systemSetting));

        // Act
        String result = systemSettingService.getStringValue("app.name", "DefaultValue");

        // Assert
        assertEquals("BuildTask", result);
    }

    @Test
    void getStringValue_WhenSettingDoesNotExist_ShouldReturnDefaultValue() {
        // Arrange
        when(systemSettingRepository.findByKey("nonexistent.key")).thenReturn(Optional.empty());

        // Act
        String result = systemSettingService.getStringValue("nonexistent.key", "DefaultValue");

        // Assert
        assertEquals("DefaultValue", result);
    }

    @Test
    void getIntValue_WhenSettingExistsWithValidInt_ShouldReturnIntValue() {
        // Arrange
        SystemSetting intSetting = new SystemSetting();
        intSetting.setKey("app.maxUsers");
        intSetting.setValue("100");

        when(systemSettingRepository.findByKey("app.maxUsers")).thenReturn(Optional.of(intSetting));

        // Act
        int result = systemSettingService.getIntValue("app.maxUsers", 50);

        // Assert
        assertEquals(100, result);
    }

    @Test
    void getIntValue_WhenSettingExistsWithInvalidInt_ShouldReturnDefaultValue() {
        // Arrange
        SystemSetting invalidIntSetting = new SystemSetting();
        invalidIntSetting.setKey("app.invalid");
        invalidIntSetting.setValue("not-a-number");

        when(systemSettingRepository.findByKey("app.invalid")).thenReturn(Optional.of(invalidIntSetting));

        // Act
        int result = systemSettingService.getIntValue("app.invalid", 50);

        // Assert
        assertEquals(50, result);
    }

    @Test
    void getIntValue_WhenSettingDoesNotExist_ShouldReturnDefaultValue() {
        // Arrange
        when(systemSettingRepository.findByKey("nonexistent.key")).thenReturn(Optional.empty());

        // Act
        int result = systemSettingService.getIntValue("nonexistent.key", 50);

        // Assert
        assertEquals(50, result);
    }

    @Test
    void getBooleanValue_WhenSettingExistsWithTrueValue_ShouldReturnTrue() {
        // Arrange
        SystemSetting trueSetting = new SystemSetting();
        trueSetting.setKey("app.feature.enabled");
        trueSetting.setValue("true");

        when(systemSettingRepository.findByKey("app.feature.enabled")).thenReturn(Optional.of(trueSetting));

        // Act
        boolean result = systemSettingService.getBooleanValue("app.feature.enabled", false);

        // Assert
        assertTrue(result);
    }

    @Test
    void getBooleanValue_WhenSettingExistsWithOneValue_ShouldReturnTrue() {
        // Arrange
        SystemSetting oneSetting = new SystemSetting();
        oneSetting.setKey("app.feature.enabled");
        oneSetting.setValue("1");

        when(systemSettingRepository.findByKey("app.feature.enabled")).thenReturn(Optional.of(oneSetting));

        // Act
        boolean result = systemSettingService.getBooleanValue("app.feature.enabled", false);

        // Assert
        assertTrue(result);
    }

    @Test
    void getBooleanValue_WhenSettingExistsWithOtherValue_ShouldReturnFalse() {
        // Arrange
        SystemSetting falseSetting = new SystemSetting();
        falseSetting.setKey("app.feature.enabled");
        falseSetting.setValue("disabled");

        when(systemSettingRepository.findByKey("app.feature.enabled")).thenReturn(Optional.of(falseSetting));

        // Act
        boolean result = systemSettingService.getBooleanValue("app.feature.enabled", true);

        // Assert
        assertFalse(result);
    }

    @Test
    void getBooleanValue_WhenSettingDoesNotExist_ShouldReturnDefaultValue() {
        // Arrange
        when(systemSettingRepository.findByKey("nonexistent.key")).thenReturn(Optional.empty());

        // Act
        boolean result = systemSettingService.getBooleanValue("nonexistent.key", true);

        // Assert
        assertTrue(result);
    }
}