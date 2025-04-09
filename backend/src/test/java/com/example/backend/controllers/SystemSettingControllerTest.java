package com.example.backend.controllers;

import com.example.backend.models.SystemSetting;
import com.example.backend.models.User;
import com.example.backend.services.SystemSettingService;
import com.example.backend.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

// Konkretne importy zamiast ogólnych static imports
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

class SystemSettingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SystemSettingService systemSettingService;

    @Mock
    private UserService userService;

    @InjectMocks
    private SystemSettingController systemSettingController;

    private ObjectMapper objectMapper;
    private User admin;
    private SystemSetting appNameSetting;
    private SystemSetting maxTasksSetting;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(systemSettingController)
                .build();

        objectMapper = new ObjectMapper();
        // Dodanie modułu obsługującego Java 8 date/time API
        objectMapper.registerModule(new JavaTimeModule());
        // Opcjonalnie: ustawienie formatu daty zamiast timestampów
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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
    void getAllSystemSettings_ShouldReturnAllSettings() throws Exception {
        // Given
        List<SystemSetting> settings = Arrays.asList(appNameSetting, maxTasksSetting);
        when(systemSettingService.getAllSystemSettings()).thenReturn(settings);

        // When & Then
        mockMvc.perform(get("/database/system-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].key", is("app.name")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].key", is("tasks.max_per_user")));

        verify(systemSettingService, times(1)).getAllSystemSettings();
    }

    @Test
    void getSystemSettingById_WhenSettingExists_ShouldReturnSetting() throws Exception {
        // Given
        when(systemSettingService.getSystemSettingById(1)).thenReturn(Optional.of(appNameSetting));

        // When & Then
        mockMvc.perform(get("/database/system-settings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.key", is("app.name")))
                .andExpect(jsonPath("$.value", is("BuildTask")))
                .andExpect(jsonPath("$.description", is("Nazwa aplikacji")));

        verify(systemSettingService, times(1)).getSystemSettingById(1);
    }

    @Test
    void getSystemSettingById_WhenSettingDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(systemSettingService.getSystemSettingById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/database/system-settings/99"))
                .andExpect(status().isNotFound());

        verify(systemSettingService, times(1)).getSystemSettingById(99);
    }

    @Test
    void getSystemSettingByKey_WhenSettingExists_ShouldReturnSetting() throws Exception {
        // Given
        when(systemSettingService.getSystemSettingByKey("app.name")).thenReturn(Optional.of(appNameSetting));

        // When & Then
        mockMvc.perform(get("/database/system-settings/key/app.name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.key", is("app.name")))
                .andExpect(jsonPath("$.value", is("BuildTask")));

        verify(systemSettingService, times(1)).getSystemSettingByKey("app.name");
    }

    @Test
    void getSystemSettingByKey_WhenSettingDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(systemSettingService.getSystemSettingByKey("nonexistent.key")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/database/system-settings/key/nonexistent.key"))
                .andExpect(status().isNotFound());

        verify(systemSettingService, times(1)).getSystemSettingByKey("nonexistent.key");
    }

    @Test
    void getSettingValue_WhenSettingExists_ShouldReturnValue() throws Exception {
        // Given
        when(systemSettingService.getStringValue(eq("app.name"), anyString())).thenReturn("BuildTask");

        // When & Then
        mockMvc.perform(get("/database/system-settings/value/app.name"))
                .andExpect(status().isOk())
                .andExpect(content().string("BuildTask"));

        verify(systemSettingService, times(1)).getStringValue(eq("app.name"), eq(""));
    }

    @Test
    void getSettingValue_WhenSettingDoesNotExist_ShouldReturnDefaultValue() throws Exception {
        // Given
        when(systemSettingService.getStringValue(eq("nonexistent.key"), eq("DefaultValue")))
                .thenReturn("DefaultValue");

        // When & Then
        mockMvc.perform(get("/database/system-settings/value/nonexistent.key")
                        .param("defaultValue", "DefaultValue"))
                .andExpect(status().isOk())
                .andExpect(content().string("DefaultValue"));

        verify(systemSettingService, times(1)).getStringValue(eq("nonexistent.key"), eq("DefaultValue"));
    }

    @Test
    void createSystemSetting_WhenKeyIsUnique_ShouldReturnCreatedSetting() throws Exception {
        // Given
        SystemSetting newSetting = new SystemSetting();
        newSetting.setKey("app.version");
        newSetting.setValue("1.0.0");
        newSetting.setDescription("Wersja aplikacji");
        newSetting.setUpdatedBy(admin);

        when(systemSettingService.existsByKey("app.version")).thenReturn(false);
        when(systemSettingService.saveSystemSetting(any(SystemSetting.class))).thenReturn(newSetting);

        // When & Then
        mockMvc.perform(post("/database/system-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSetting)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key", is("app.version")))
                .andExpect(jsonPath("$.value", is("1.0.0")))
                .andExpect(jsonPath("$.description", is("Wersja aplikacji")));

        verify(systemSettingService, times(1)).existsByKey("app.version");
        verify(systemSettingService, times(1)).saveSystemSetting(any(SystemSetting.class));
    }

    @Test
    void createSystemSetting_WhenKeyExists_ShouldReturnConflict() throws Exception {
        // Given
        SystemSetting newSetting = new SystemSetting();
        newSetting.setKey("app.name");
        newSetting.setValue("NewName");
        newSetting.setDescription("Nowa nazwa");

        when(systemSettingService.existsByKey("app.name")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/database/system-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSetting)))
                .andExpect(status().isConflict());

        verify(systemSettingService, times(1)).existsByKey("app.name");
        verify(systemSettingService, never()).saveSystemSetting(any(SystemSetting.class));
    }

    @Test
    void createSystemSettingFromParams_WithValidData_ShouldReturnCreatedSetting() throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("key", "app.version");
        payload.put("value", "1.0.0");
        payload.put("description", "Wersja aplikacji");
        payload.put("updatedById", 1);

        SystemSetting createdSetting = new SystemSetting();
        createdSetting.setId(3);
        createdSetting.setKey("app.version");
        createdSetting.setValue("1.0.0");
        createdSetting.setDescription("Wersja aplikacji");
        createdSetting.setUpdatedBy(admin);
        createdSetting.setUpdatedAt(LocalDateTime.now());

        when(systemSettingService.existsByKey("app.version")).thenReturn(false);
        when(userService.getUserById(1)).thenReturn(Optional.of(admin));
        when(systemSettingService.createSystemSetting(
                eq("app.version"),
                eq("1.0.0"),
                eq("Wersja aplikacji"),
                eq(admin)))
                .thenReturn(createdSetting);

        // When & Then
        mockMvc.perform(post("/database/system-settings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.key", is("app.version")))
                .andExpect(jsonPath("$.value", is("1.0.0")))
                .andExpect(jsonPath("$.description", is("Wersja aplikacji")));

        verify(systemSettingService, times(1)).existsByKey("app.version");
        verify(userService, times(1)).getUserById(1);
        verify(systemSettingService, times(1)).createSystemSetting(
                eq("app.version"),
                eq("1.0.0"),
                eq("Wersja aplikacji"),
                eq(admin));
    }

    @Test
    void createSystemSettingFromParams_WithMissingKey_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        // Missing key
        payload.put("value", "1.0.0");
        payload.put("description", "Wersja aplikacji");
        payload.put("updatedById", 1);

        // When & Then
        mockMvc.perform(post("/database/system-settings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());

        verify(systemSettingService, never()).existsByKey(anyString());
        verify(userService, never()).getUserById(anyInt());
        verify(systemSettingService, never()).createSystemSetting(
                anyString(), anyString(), anyString(), any(User.class));
    }

    @Test
    void updateSystemSetting_WhenSettingExists_ShouldReturnUpdatedSetting() throws Exception {
        // Given
        SystemSetting updatedSetting = new SystemSetting();
        updatedSetting.setId(1);
        updatedSetting.setKey("app.name");
        updatedSetting.setValue("TaskManager");
        updatedSetting.setDescription("Nowa nazwa aplikacji");
        updatedSetting.setUpdatedBy(admin);
        updatedSetting.setUpdatedAt(LocalDateTime.now());

        when(systemSettingService.getSystemSettingById(1)).thenReturn(Optional.of(appNameSetting));
        when(systemSettingService.saveSystemSetting(any(SystemSetting.class))).thenReturn(updatedSetting);

        // When & Then
        mockMvc.perform(put("/database/system-settings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedSetting)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.key", is("app.name")))
                .andExpect(jsonPath("$.value", is("TaskManager")))
                .andExpect(jsonPath("$.description", is("Nowa nazwa aplikacji")));

        verify(systemSettingService, times(1)).getSystemSettingById(1);
        verify(systemSettingService, times(1)).saveSystemSetting(any(SystemSetting.class));
    }

    @Test
    void updateSystemSetting_WhenSettingDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        SystemSetting updatedSetting = new SystemSetting();
        updatedSetting.setId(99);
        updatedSetting.setKey("nonexistent.key");
        updatedSetting.setValue("value");

        when(systemSettingService.getSystemSettingById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/database/system-settings/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedSetting)))
                .andExpect(status().isNotFound());

        verify(systemSettingService, times(1)).getSystemSettingById(99);
        verify(systemSettingService, never()).saveSystemSetting(any(SystemSetting.class));
    }

    @Test
    void updateSettingValue_WhenSettingExists_ShouldReturnUpdatedSetting() throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("value", "TaskManager");
        payload.put("updatedById", 1);

        SystemSetting updatedSetting = new SystemSetting();
        updatedSetting.setId(1);
        updatedSetting.setKey("app.name");
        updatedSetting.setValue("TaskManager");
        updatedSetting.setDescription("Nazwa aplikacji");
        updatedSetting.setUpdatedBy(admin);
        updatedSetting.setUpdatedAt(LocalDateTime.now());

        when(userService.getUserById(1)).thenReturn(Optional.of(admin));
        when(systemSettingService.updateValue(eq("app.name"), eq("TaskManager"), eq(admin)))
                .thenReturn(Optional.of(updatedSetting));

        // When & Then
        mockMvc.perform(patch("/database/system-settings/key/app.name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.key", is("app.name")))
                .andExpect(jsonPath("$.value", is("TaskManager")));

        verify(userService, times(1)).getUserById(1);
        verify(systemSettingService, times(1)).updateValue(eq("app.name"), eq("TaskManager"), eq(admin));
    }

    @Test
    void updateSettingValue_WhenSettingDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("value", "value");
        payload.put("updatedById", 1);

        when(userService.getUserById(1)).thenReturn(Optional.of(admin));
        when(systemSettingService.updateValue(eq("nonexistent.key"), eq("value"), eq(admin)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(patch("/database/system-settings/key/nonexistent.key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(1);
        verify(systemSettingService, times(1)).updateValue(eq("nonexistent.key"), eq("value"), eq(admin));
    }

    @Test
    void updateSettingValue_WithMissingData_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        // Missing value or updatedById

        // When & Then
        mockMvc.perform(patch("/database/system-settings/key/app.name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).getUserById(anyInt());
        verify(systemSettingService, never()).updateValue(anyString(), anyString(), any(User.class));
    }

    @Test
    void updateSettingValue_WithNonexistentUser_ShouldReturnNotFound() throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("value", "TaskManager");
        payload.put("updatedById", 99);

        when(userService.getUserById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(patch("/database/system-settings/key/app.name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(99);
        verify(systemSettingService, never()).updateValue(anyString(), anyString(), any(User.class));
    }

    @Test
    void deleteSystemSetting_WhenSettingExists_ShouldReturnNoContent() throws Exception {
        // Given
        when(systemSettingService.getSystemSettingById(1)).thenReturn(Optional.of(appNameSetting));
        doNothing().when(systemSettingService).deleteSystemSetting(1);

        // When & Then
        mockMvc.perform(delete("/database/system-settings/1"))
                .andExpect(status().isNoContent());

        verify(systemSettingService, times(1)).getSystemSettingById(1);
        verify(systemSettingService, times(1)).deleteSystemSetting(1);
    }

    @Test
    void deleteSystemSetting_WhenSettingDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(systemSettingService.getSystemSettingById(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/database/system-settings/99"))
                .andExpect(status().isNotFound());

        verify(systemSettingService, times(1)).getSystemSettingById(99);
        verify(systemSettingService, never()).deleteSystemSetting(anyInt());
    }
}