package com.example.backend.controllers;

import com.example.backend.dto.SystemSettingDTO;
import com.example.backend.dto.UserDTO;
import com.example.backend.models.User;
import com.example.backend.services.SystemSettingService;
import com.example.backend.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class SystemSettingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private SystemSettingService systemSettingService;

    @Mock
    private UserService userService;

    @InjectMocks
    private SystemSettingController systemSettingController;

    private SystemSettingDTO systemSettingDTO;
    private List<SystemSettingDTO> systemSettingDTOList;
    private UserDTO userDTO;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(systemSettingController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDateTime serialization

        // Initialize test data
        userDTO = new UserDTO();
        userDTO.setId(1);
        userDTO.setUsername("admin");
        userDTO.setFirstName("Admin");
        userDTO.setLastName("User");

        systemSettingDTO = new SystemSettingDTO();
        systemSettingDTO.setId(1);
        systemSettingDTO.setKey("app.name");
        systemSettingDTO.setValue("BuildTask");
        systemSettingDTO.setDescription("Application name");
        systemSettingDTO.setUpdatedById(1);
        systemSettingDTO.setUpdatedAt(LocalDateTime.now());
        systemSettingDTO.setUpdatedByUsername("admin");
        systemSettingDTO.setUpdatedByFullName("Admin User");

        SystemSettingDTO settingDTO2 = new SystemSettingDTO();
        settingDTO2.setId(2);
        settingDTO2.setKey("app.version");
        settingDTO2.setValue("1.0.0");
        settingDTO2.setDescription("Application version");
        settingDTO2.setUpdatedById(1);

        systemSettingDTOList = Arrays.asList(systemSettingDTO, settingDTO2);
    }

    @Test
    public void getAllSystemSettings_ShouldReturnListOfSettings() throws Exception {
        // Arrange
        when(systemSettingService.getAllSystemSettings()).thenReturn(systemSettingDTOList);

        // Act & Assert
        mockMvc.perform(get("/database/system-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("app.name"))
                .andExpect(jsonPath("$[1].key").value("app.version"));
    }

    @Test
    public void getSystemSettingById_WhenExists_ShouldReturnSetting() throws Exception {
        // Arrange
        when(systemSettingService.getSystemSettingById(1)).thenReturn(Optional.of(systemSettingDTO));

        // Act & Assert
        mockMvc.perform(get("/database/system-settings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("app.name"))
                .andExpect(jsonPath("$.value").value("BuildTask"));
    }

    @Test
    public void getSystemSettingById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(systemSettingService.getSystemSettingById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/system-settings/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getSystemSettingByKey_WhenExists_ShouldReturnSetting() throws Exception {
        // Arrange
        when(systemSettingService.getSystemSettingByKey("app.name")).thenReturn(Optional.of(systemSettingDTO));

        // Act & Assert
        mockMvc.perform(get("/database/system-settings/key/app.name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.value").value("BuildTask"));
    }

    @Test
    public void getSystemSettingByKey_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(systemSettingService.getSystemSettingByKey("nonexistent.key")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/database/system-settings/key/nonexistent.key"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getSettingValue_WhenSettingExists_ShouldReturnValue() throws Exception {
        // Arrange
        when(systemSettingService.getStringValue(eq("app.name"), anyString())).thenReturn("BuildTask");

        // Act & Assert
        mockMvc.perform(get("/database/system-settings/value/app.name"))
                .andExpect(status().isOk())
                .andExpect(content().string("BuildTask"));
    }

    @Test
    public void getSettingValue_WhenSettingDoesNotExist_ShouldReturnDefaultValue() throws Exception {
        // Arrange
        when(systemSettingService.getStringValue(eq("nonexistent.key"), anyString())).thenReturn("Default");

        // Act & Assert
        mockMvc.perform(get("/database/system-settings/value/nonexistent.key")
                        .param("defaultValue", "Default"))
                .andExpect(status().isOk())
                .andExpect(content().string("Default"));
    }

    @Test
    public void createSystemSetting_WithValidData_ShouldReturnCreatedSetting() throws Exception {
        // Arrange
        when(systemSettingService.existsByKey("app.name")).thenReturn(false);
        when(userService.getUserById(1)).thenReturn(Optional.of(userDTO));
        when(systemSettingService.saveSystemSetting(any(SystemSettingDTO.class))).thenReturn(systemSettingDTO);

        // Act & Assert
        mockMvc.perform(post("/database/system-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(systemSettingDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value("app.name"))
                .andExpect(jsonPath("$.value").value("BuildTask"));
    }

    @Test
    public void createSystemSetting_WithExistingKey_ShouldReturnConflict() throws Exception {
        // Arrange
        when(systemSettingService.existsByKey("app.name")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/database/system-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(systemSettingDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    public void createSystemSetting_WithNonexistentUser_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(systemSettingService.existsByKey("app.name")).thenReturn(false);
        when(userService.getUserById(1)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/database/system-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(systemSettingDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createSystemSettingFromParams_WithValidParams_ShouldReturnCreatedSetting() throws Exception {
        // Arrange
        Map<String, Object> params = new HashMap<>();
        params.put("key", "app.name");
        params.put("value", "BuildTask");
        params.put("description", "Application name");
        params.put("updatedById", 1);

        when(systemSettingService.existsByKey("app.name")).thenReturn(false);
        when(userService.getUserById(1)).thenReturn(Optional.of(userDTO));
        when(systemSettingService.createSystemSetting(anyString(), anyString(), anyString(), any(User.class)))
                .thenReturn(systemSettingDTO);

        // Act & Assert
        mockMvc.perform(post("/database/system-settings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value("app.name"))
                .andExpect(jsonPath("$.value").value("BuildTask"));
    }

    @Test
    public void createSystemSettingFromParams_WithMissingParams_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Map<String, Object> params = new HashMap<>();
        params.put("key", "app.name");
        // Missing required updatedById parameter

        // Act & Assert
        mockMvc.perform(post("/database/system-settings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateSystemSetting_WhenSettingExists_ShouldReturnUpdatedSetting() throws Exception {
        // Arrange
        when(systemSettingService.getSystemSettingById(1)).thenReturn(Optional.of(systemSettingDTO));
        when(userService.getUserById(1)).thenReturn(Optional.of(userDTO));
        when(systemSettingService.saveSystemSetting(any(SystemSettingDTO.class))).thenReturn(systemSettingDTO);

        // Update value in DTO
        systemSettingDTO.setValue("UpdatedBuildTask");

        // Act & Assert
        mockMvc.perform(put("/database/system-settings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(systemSettingDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("app.name"))
                .andExpect(jsonPath("$.value").value("BuildTask")); // Mock returns original

        // Verify that the ID was set in the DTO before saving
        verify(systemSettingService).saveSystemSetting(argThat(dto -> dto.getId() == 1));
    }

    @Test
    public void updateSystemSetting_WhenSettingDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(systemSettingService.getSystemSettingById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/database/system-settings/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(systemSettingDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateSettingValue_WhenSettingExists_ShouldReturnUpdatedSetting() throws Exception {
        // Arrange
        Map<String, Object> valueUpdate = new HashMap<>();
        valueUpdate.put("value", "NewBuildTask");
        valueUpdate.put("updatedById", 1);

        when(userService.getUserById(1)).thenReturn(Optional.of(userDTO));
        when(systemSettingService.updateValue(eq("app.name"), anyString(), any(User.class)))
                .thenReturn(Optional.of(systemSettingDTO));

        // Act & Assert
        mockMvc.perform(patch("/database/system-settings/key/app.name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valueUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("app.name"));
    }

    @Test
    public void updateSettingValue_WithMissingParams_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Map<String, Object> incompleteUpdate = new HashMap<>();
        incompleteUpdate.put("value", "NewValue");
        // Missing required updatedById

        // Act & Assert
        mockMvc.perform(patch("/database/system-settings/key/app.name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incompleteUpdate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateSettingValue_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        Map<String, Object> valueUpdate = new HashMap<>();
        valueUpdate.put("value", "NewBuildTask");
        valueUpdate.put("updatedById", 99);

        when(userService.getUserById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(patch("/database/system-settings/key/app.name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valueUpdate)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateSettingValue_WhenSettingDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        Map<String, Object> valueUpdate = new HashMap<>();
        valueUpdate.put("value", "NewValue");
        valueUpdate.put("updatedById", 1);

        when(userService.getUserById(1)).thenReturn(Optional.of(userDTO));
        when(systemSettingService.updateValue(eq("nonexistent.key"), anyString(), any(User.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(patch("/database/system-settings/key/nonexistent.key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valueUpdate)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteSystemSetting_WhenSettingExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(systemSettingService.getSystemSettingById(1)).thenReturn(Optional.of(systemSettingDTO));
        doNothing().when(systemSettingService).deleteSystemSetting(1);

        // Act & Assert
        mockMvc.perform(delete("/database/system-settings/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteSystemSetting_WhenSettingDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(systemSettingService.getSystemSettingById(99)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/database/system-settings/99"))
                .andExpect(status().isNotFound());
    }
}