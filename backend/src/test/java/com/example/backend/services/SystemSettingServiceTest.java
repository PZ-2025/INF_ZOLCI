package com.example.backend.services;

import com.example.backend.models.SystemSetting;
import com.example.backend.models.User;
import com.example.backend.repository.SystemSettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SystemSettingServiceTest {

    @Mock
    private SystemSettingRepository systemSettingRepository;

    @InjectMocks
    private SystemSettingService systemSettingService;

    private User admin;
    private SystemSetting appNameSetting;
    private SystemSetting maxTasksSetting;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Inicjalizacja użytkownika testowego
        admin = new User();
        admin.setId(1);
        admin.setUsername("admin");

        // Inicjalizacja ustawień systemowych testowych
        appNameSetting = new SystemSetting();
        appNameSetting.setId(1);
        appNameSetting.setKey("app.name");
        appNameSetting.setValue("BuildTask");
        appNameSetting.setDescription("Nazwa aplikacji");
        appNameSetting.setUpdatedBy(admin);
        appNameSetting.setUpdatedAt(LocalDateTime.now());

        maxTasksSetting = new SystemSetting();
        maxTasksSetting.setId(2);
        maxTasksSetting.setKey("tasks.max_per_user");
        maxTasksSetting.setValue("10");
        maxTasksSetting.setDescription("Maksymalna liczba zadań na użytkownika");
        maxTasksSetting.setUpdatedBy(admin);
        maxTasksSetting.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getAllSystemSettings_ShouldReturnAllSettings() {
        // Given
        List<SystemSetting> expectedSettings = Arrays.asList(appNameSetting, maxTasksSetting);
        when(systemSettingRepository.findAll()).thenReturn(expectedSettings);

        // When
        List<SystemSetting> actualSettings = systemSettingService.getAllSystemSettings();

        // Then
        assertEquals(expectedSettings.size(), actualSettings.size());
        assertEquals(expectedSettings.get(0).getId(), actualSettings.get(0).getId());
        assertEquals(expectedSettings.get(1).getId(), actualSettings.get(1).getId());
        verify(systemSettingRepository, times(1)).findAll();
    }

    @Test
    void getSystemSettingById_WhenSettingExists_ShouldReturnSetting() {
        // Given
        when(systemSettingRepository.findById(1)).thenReturn(Optional.of(appNameSetting));

        // When
        Optional<SystemSetting> result = systemSettingService.getSystemSettingById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(appNameSetting.getId(), result.get().getId());
        assertEquals(appNameSetting.getKey(), result.get().getKey());
        verify(systemSettingRepository, times(1)).findById(1);
    }

    @Test
    void getSystemSettingById_WhenSettingDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(systemSettingRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<SystemSetting> result = systemSettingService.getSystemSettingById(99);

        // Then
        assertFalse(result.isPresent());
        verify(systemSettingRepository, times(1)).findById(99);
    }

    @Test
    void getSystemSettingByKey_WhenSettingExists_ShouldReturnSetting() {
        // Given
        when(systemSettingRepository.findByKey("app.name")).thenReturn(Optional.of(appNameSetting));

        // When
        Optional<SystemSetting> result = systemSettingService.getSystemSettingByKey("app.name");

        // Then
        assertTrue(result.isPresent());
        assertEquals(appNameSetting.getId(), result.get().getId());
        assertEquals("app.name", result.get().getKey());
        assertEquals("BuildTask", result.get().getValue());
        verify(systemSettingRepository, times(1)).findByKey("app.name");
    }

    @Test
    void saveSystemSetting_ShouldSaveAndReturnSetting() {
        // Given
        SystemSetting newSetting = new SystemSetting();
        newSetting.setKey("app.version");
        newSetting.setValue("1.0.0");
        newSetting.setDescription("Wersja aplikacji");
        newSetting.setUpdatedBy(admin);
        newSetting.setUpdatedAt(LocalDateTime.now());

        when(systemSettingRepository.save(any(SystemSetting.class))).thenReturn(newSetting);

        // When
        SystemSetting savedSetting = systemSettingService.saveSystemSetting(newSetting);

        // Then
        assertEquals(newSetting.getKey(), savedSetting.getKey());
        assertEquals(newSetting.getValue(), savedSetting.getValue());
        assertEquals(newSetting.getDescription(), savedSetting.getDescription());
        verify(systemSettingRepository, times(1)).save(newSetting);
    }

    @Test
    void createSystemSetting_ShouldCreateAndReturnSetting() {
        // Given
        String key = "app.version";
        String value = "1.0.0";
        String description = "Wersja aplikacji";

        when(systemSettingRepository.save(any(SystemSetting.class))).thenAnswer(invocation -> {
            SystemSetting saved = invocation.getArgument(0);
            saved.setId(3); // Symulacja nadania ID przez bazę danych
            return saved;
        });

        // When
        SystemSetting createdSetting = systemSettingService.createSystemSetting(key, value, description, admin);

        // Then
        assertNotNull(createdSetting.getId());
        assertEquals(key, createdSetting.getKey());
        assertEquals(value, createdSetting.getValue());
        assertEquals(description, createdSetting.getDescription());
        assertEquals(admin, createdSetting.getUpdatedBy());
        assertNotNull(createdSetting.getUpdatedAt());
        verify(systemSettingRepository, times(1)).save(any(SystemSetting.class));
    }

    @Test
    void updateValue_WhenSettingExists_ShouldUpdateAndReturnSetting() {
        // Given
        when(systemSettingRepository.findByKey("app.name")).thenReturn(Optional.of(appNameSetting));
        when(systemSettingRepository.save(any(SystemSetting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String newValue = "TaskManager";
        User updater = new User();
        updater.setId(2);
        updater.setUsername("manager");

        // When
        Optional<SystemSetting> result = systemSettingService.updateValue("app.name", newValue, updater);

        // Then
        assertTrue(result.isPresent());
        assertEquals(newValue, result.get().getValue());
        assertEquals(updater, result.get().getUpdatedBy());
        assertNotNull(result.get().getUpdatedAt());
        verify(systemSettingRepository, times(1)).findByKey("app.name");
        verify(systemSettingRepository, times(1)).save(any(SystemSetting.class));
    }

    @Test
    void updateValue_WhenSettingDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(systemSettingRepository.findByKey("nonexistent.key")).thenReturn(Optional.empty());

        // When
        Optional<SystemSetting> result = systemSettingService.updateValue("nonexistent.key", "value", admin);

        // Then
        assertFalse(result.isPresent());
        verify(systemSettingRepository, times(1)).findByKey("nonexistent.key");
        verify(systemSettingRepository, never()).save(any(SystemSetting.class));
    }

    @Test
    void deleteSystemSetting_ShouldCallRepository() {
        // When
        systemSettingService.deleteSystemSetting(1);

        // Then
        verify(systemSettingRepository, times(1)).deleteById(1);
    }

    @Test
    void existsByKey_WhenSettingExists_ShouldReturnTrue() {
        // Given
        when(systemSettingRepository.findByKey("app.name")).thenReturn(Optional.of(appNameSetting));

        // When
        boolean result = systemSettingService.existsByKey("app.name");

        // Then
        assertTrue(result);
        verify(systemSettingRepository, times(1)).findByKey("app.name");
    }

    @Test
    void existsByKey_WhenSettingDoesNotExist_ShouldReturnFalse() {
        // Given
        when(systemSettingRepository.findByKey("nonexistent.key")).thenReturn(Optional.empty());

        // When
        boolean result = systemSettingService.existsByKey("nonexistent.key");

        // Then
        assertFalse(result);
        verify(systemSettingRepository, times(1)).findByKey("nonexistent.key");
    }

    @Test
    void getStringValue_WhenSettingExists_ShouldReturnValue() {
        // Given
        when(systemSettingRepository.findByKey("app.name")).thenReturn(Optional.of(appNameSetting));

        // When
        String result = systemSettingService.getStringValue("app.name", "DefaultApp");

        // Then
        assertEquals("BuildTask", result);
        verify(systemSettingRepository, times(1)).findByKey("app.name");
    }

    @Test
    void getStringValue_WhenSettingDoesNotExist_ShouldReturnDefaultValue() {
        // Given
        when(systemSettingRepository.findByKey("nonexistent.key")).thenReturn(Optional.empty());

        // When
        String result = systemSettingService.getStringValue("nonexistent.key", "DefaultValue");

        // Then
        assertEquals("DefaultValue", result);
        verify(systemSettingRepository, times(1)).findByKey("nonexistent.key");
    }

    @Test
    void getIntValue_WhenSettingExistsWithValidInt_ShouldReturnIntValue() {
        // Given
        when(systemSettingRepository.findByKey("tasks.max_per_user")).thenReturn(Optional.of(maxTasksSetting));

        // When
        int result = systemSettingService.getIntValue("tasks.max_per_user", 5);

        // Then
        assertEquals(10, result);
        verify(systemSettingRepository, times(1)).findByKey("tasks.max_per_user");
    }

    @Test
    void getIntValue_WhenSettingExistsWithInvalidInt_ShouldReturnDefaultValue() {
        // Given
        SystemSetting invalidIntSetting = new SystemSetting();
        invalidIntSetting.setKey("invalid.int");
        invalidIntSetting.setValue("not-an-int");

        when(systemSettingRepository.findByKey("invalid.int")).thenReturn(Optional.of(invalidIntSetting));

        // When
        int result = systemSettingService.getIntValue("invalid.int", 5);

        // Then
        assertEquals(5, result);
        verify(systemSettingRepository, times(1)).findByKey("invalid.int");
    }

    @Test
    void getBooleanValue_WhenSettingExistsWithTrueValue_ShouldReturnTrue() {
        // Given
        SystemSetting trueSetting = new SystemSetting();
        trueSetting.setKey("feature.enabled");
        trueSetting.setValue("true");

        when(systemSettingRepository.findByKey("feature.enabled")).thenReturn(Optional.of(trueSetting));

        // When
        boolean result = systemSettingService.getBooleanValue("feature.enabled", false);

        // Then
        assertTrue(result);
        verify(systemSettingRepository, times(1)).findByKey("feature.enabled");
    }

    @Test
    void getBooleanValue_WhenSettingExistsWithNumericTrueValue_ShouldReturnTrue() {
        // Given
        SystemSetting numericTrueSetting = new SystemSetting();
        numericTrueSetting.setKey("feature.enabled");
        numericTrueSetting.setValue("1");

        when(systemSettingRepository.findByKey("feature.enabled")).thenReturn(Optional.of(numericTrueSetting));

        // When
        boolean result = systemSettingService.getBooleanValue("feature.enabled", false);

        // Then
        assertTrue(result);
        verify(systemSettingRepository, times(1)).findByKey("feature.enabled");
    }

    @Test
    void getBooleanValue_WhenSettingExistsWithFalseValue_ShouldReturnFalse() {
        // Given
        SystemSetting falseSetting = new SystemSetting();
        falseSetting.setKey("feature.enabled");
        falseSetting.setValue("false");

        when(systemSettingRepository.findByKey("feature.enabled")).thenReturn(Optional.of(falseSetting));

        // When
        boolean result = systemSettingService.getBooleanValue("feature.enabled", true);

        // Then
        assertFalse(result);
        verify(systemSettingRepository, times(1)).findByKey("feature.enabled");
    }

    @Test
    void getBooleanValue_WhenSettingDoesNotExist_ShouldReturnDefaultValue() {
        // Given
        when(systemSettingRepository.findByKey("feature.enabled")).thenReturn(Optional.empty());

        // When
        boolean result = systemSettingService.getBooleanValue("feature.enabled", true);

        // Then
        assertTrue(result);
        verify(systemSettingRepository, times(1)).findByKey("feature.enabled");
    }
}