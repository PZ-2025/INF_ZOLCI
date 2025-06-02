<template>
  <div class="bg-background min-h-screen p-8 text-text">
    <h1 class="text-3xl text-left font-bold text-primary mb-6">Konfiguracja Systemu</h1>

    <!-- Loading state -->
    <div v-if="loading" class="flex justify-center items-center h-64">
      <p class="text-primary text-xl">Ładowanie ustawień systemu...</p>
    </div>

    <!-- Error message -->
    <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6">
      <p>{{ error }}</p>
      <button @click="loadSettings" class="mt-2 bg-primary text-white px-4 py-1 rounded-md">
        Spróbuj ponownie
      </button>
    </div>

    <!-- Settings form -->
    <form v-else @submit.prevent="saveSettings" class="space-y-6">
      <!-- Success message -->
      <div v-if="successMessage" class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4">
        {{ successMessage }}
      </div>

      <div class="bg-surface p-6 rounded-lg shadow-md mb-6 border border-gray-200">
        <h2 class="text-xl font-semibold mb-4">Kryteria Statusów (1–100)</h2>
        <input
            type="range"
            min="1"
            max="100"
            v-model.number="settings.statusCriteriaThreshold"
            class="w-full"
        />
        <p class="mt-2 text-muted">Wybrany poziom: <span class="font-bold">{{ settings.statusCriteriaThreshold }}</span></p>
      </div>

      <div class="bg-surface p-6 rounded-lg shadow-md mb-6 border border-gray-200">
        <h2 class="text-xl font-semibold mb-4">Domyślne kryterium priorytetów</h2>
        <select
            v-model="settings.defaultPriority"
            class="w-full p-2 border border-gray-300 rounded bg-white focus:outline-none focus:ring-2 focus:ring-primary"
        >
          <option v-for="priority in priorities" :key="priority.value" :value="priority.value">
            {{ priority.name }}
          </option>
        </select>
      </div>

      <div class="bg-surface p-6 rounded-lg shadow-md border border-gray-200">
        <h2 class="text-xl font-semibold mb-4">Inne Ustawienia</h2>

        <div class="mb-4 flex items-center">
          <input
              type="checkbox"
              v-model="settings.autoArchiveTasks"
              class="mr-2"
              id="archive"
          >
          <label for="archive">Automatyczna archiwizacja zadań</label>
        </div>

        <div class="mb-4">
          <label class="block font-medium mb-1">Minimalna długość hasła</label>
          <input
              type="number"
              v-model.number="settings.minPasswordLength"
              min="4"
              max="32"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
          />
        </div>

        <div class="mb-4">
          <label class="block font-medium mb-1">Automatyczne wylogowanie (minuty)</label>
          <input
              type="number"
              v-model.number="settings.autoLogoutMinutes"
              min="1"
              max="120"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
          />
        </div>

        <div class="mb-4">
          <label class="block font-medium mb-1">Domyślny język systemu</label>
          <select
              v-model="settings.defaultLanguage"
              class="w-full p-2 border border-gray-300 rounded bg-white focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option v-for="language in languages" :key="language.value" :value="language.value">
              {{ language.name }}
            </option>
          </select>
        </div>

        <div class="mt-6">
          <button
              type="submit"
              class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-md transition w-full"
              :disabled="isSaving"
          >
            <span v-if="isSaving">Zapisywanie...</span>
            <span v-else>Zapisz Ustawienia</span>
          </button>
        </div>
      </div>
    </form>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import systemSettingService from '../services/systemSettingService';
import { authState } from '../../router/router.js';

// Define constants for system setting keys
const SYSTEM_SETTINGS = {
  STATUS_CRITERIA_THRESHOLD: 'status_criteria_threshold',
  DEFAULT_PRIORITY: 'default_priority',
  AUTO_ARCHIVE_TASKS: 'auto_archive_tasks',
  MIN_PASSWORD_LENGTH: 'min_password_length',
  AUTO_LOGOUT_MINUTES: 'auto_logout_minutes',
  DEFAULT_LANGUAGE: 'default_language'
};

export default {
  setup() {
    // State variables
    const settings = reactive({
      statusCriteriaThreshold: 50,
      defaultPriority: 'medium',
      autoArchiveTasks: true,
      minPasswordLength: 8,
      autoLogoutMinutes: 15,
      defaultLanguage: 'pl'
    });

    const loading = ref(true);
    const error = ref(null);
    const isSaving = ref(false);
    const successMessage = ref('');

    // Available options for select inputs
    const priorities = [
      { value: 'low', name: 'Niski' },
      { value: 'medium', name: 'Średni' },
      { value: 'high', name: 'Wysoki' }
    ];

    const languages = [
      { value: 'pl', name: 'Polski' },
      { value: 'en', name: 'English' }
    ];

    // Helper function to get setting value with default fallback
    const getSettingValue = (settingsMap, key, defaultValue) => {
      if (settingsMap[key] && settingsMap[key].value !== null && settingsMap[key].value !== undefined) {
        return settingsMap[key].value;
      }
      return defaultValue;
    };

    // Load settings from the backend
    const loadSettings = async () => {
      loading.value = true;
      error.value = null;
      successMessage.value = '';

      try {
        // Get all system settings
        const allSettings = await systemSettingService.getAllSystemSettings();
        const settingsMap = {};

        // Map the settings by their keys for easier access
        allSettings.forEach(setting => {
          settingsMap[setting.key] = setting;
        });

        // Update the reactive settings object with values from the backend
        settings.statusCriteriaThreshold = parseInt(getSettingValue(settingsMap, SYSTEM_SETTINGS.STATUS_CRITERIA_THRESHOLD, '50'), 10);
        settings.defaultPriority = getSettingValue(settingsMap, SYSTEM_SETTINGS.DEFAULT_PRIORITY, 'medium');
        settings.autoArchiveTasks = getSettingValue(settingsMap, SYSTEM_SETTINGS.AUTO_ARCHIVE_TASKS, 'true') === 'true';
        settings.minPasswordLength = parseInt(getSettingValue(settingsMap, SYSTEM_SETTINGS.MIN_PASSWORD_LENGTH, '8'), 10);
        settings.autoLogoutMinutes = parseInt(getSettingValue(settingsMap, SYSTEM_SETTINGS.AUTO_LOGOUT_MINUTES, '15'), 10);
        settings.defaultLanguage = getSettingValue(settingsMap, SYSTEM_SETTINGS.DEFAULT_LANGUAGE, 'pl');

        console.log('Loaded system settings:', settings);
      } catch (err) {
        console.error('Error loading system settings:', err);
        error.value = `Nie udało się załadować ustawień systemu: ${err.message}`;
      } finally {
        loading.value = false;
      }
    };

    // Update a single setting or create it if it doesn't exist
    const updateSetting = async (key, value, userId) => {
      try {
        // First try to update via PATCH
        return await systemSettingService.updateSettingValue(key, value, userId);
      } catch (err) {
        // If the setting doesn't exist, create it
        if (err.response && err.response.status === 404) {
          console.log(`Setting ${key} not found, creating it`);

          return await systemSettingService.createSystemSettingFromParams({
            key,
            value,
            description: `System setting for ${key}`,
            updatedById: userId
          });
        }
        throw err;
      }
    };

    // Save settings to the backend
    const saveSettings = async () => {
      isSaving.value = true;
      error.value = null;
      successMessage.value = '';

      try {
        // Validate settings
        if (settings.minPasswordLength < 4 || settings.minPasswordLength > 32) {
          throw new Error('Minimalna długość hasła musi być między 4 a 32 znakami');
        }

        if (settings.autoLogoutMinutes < 1 || settings.autoLogoutMinutes > 120) {
          throw new Error('Czas automatycznego wylogowania musi być między 1 a 120 minut');
        }

        // Get the current user ID for tracking who made the changes
        const userId = authState.user?.id || 1;

        // Save all settings
        const updates = [
          updateSetting(SYSTEM_SETTINGS.STATUS_CRITERIA_THRESHOLD, settings.statusCriteriaThreshold.toString(), userId),
          updateSetting(SYSTEM_SETTINGS.DEFAULT_PRIORITY, settings.defaultPriority, userId),
          updateSetting(SYSTEM_SETTINGS.AUTO_ARCHIVE_TASKS, settings.autoArchiveTasks.toString(), userId),
          updateSetting(SYSTEM_SETTINGS.MIN_PASSWORD_LENGTH, settings.minPasswordLength.toString(), userId),
          updateSetting(SYSTEM_SETTINGS.AUTO_LOGOUT_MINUTES, settings.autoLogoutMinutes.toString(), userId),
          updateSetting(SYSTEM_SETTINGS.DEFAULT_LANGUAGE, settings.defaultLanguage, userId)
        ];

        // Execute all updates concurrently
        await Promise.all(updates);

        // Show success message
        successMessage.value = 'Ustawienia zostały pomyślnie zapisane!';

        // Hide success message after 3 seconds
        setTimeout(() => {
          successMessage.value = '';
        }, 3000);
      } catch (err) {
        console.error('Error saving system settings:', err);
        error.value = `Nie udało się zapisać ustawień: ${err.message}`;
      } finally {
        isSaving.value = false;
      }
    };

    // Initialize the component
    onMounted(() => {
      loadSettings();
    });

    return {
      settings,
      priorities,
      languages,
      loading,
      error,
      isSaving,
      successMessage,
      loadSettings,
      saveSettings
    };
  }
};
</script>