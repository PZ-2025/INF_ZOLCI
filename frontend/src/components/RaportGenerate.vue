<template>
  <div class="bg-background min-h-screen p-8 text-text">
    <h1 class="text-3xl font-bold text-primary mb-6">Konfiguracja Systemu</h1>

    <div v-if="loading" class="flex justify-center items-center h-64">
      <p class="text-primary text-xl">Ładowanie ustawień...</p>
    </div>

    <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6">
      <p>{{ error }}</p>
      <button @click="loadSettings" class="mt-2 bg-primary text-white px-4 py-1 rounded-md">
        Spróbuj ponownie
      </button>
    </div>

    <div v-else>
      <div class="bg-surface p-6 rounded-lg shadow-md mb-6 border border-gray-200">
        <h2 class="text-xl font-semibold mb-4">Kryteria Statusów (1–100)</h2>
        <input
            type="range"
            min="1"
            max="100"
            v-model="statusValue"
            class="w-full"
        />
        <p class="mt-2 text-muted">Wybrany poziom: <span class="font-bold">{{ statusValue }}</span></p>
      </div>

      <div class="bg-surface p-6 rounded-lg shadow-md mb-6 border border-gray-200">
        <h2 class="text-xl font-semibold mb-4">Globalne Kryteria Priorytetów</h2>
        <select
            v-model="selectedPriority"
            class="w-full p-2 border border-gray-300 rounded bg-white focus:outline-none focus:ring-2 focus:ring-primary"
        >
          <option v-for="priority in priorities" :key="priority.id" :value="priority.id">
            {{ priority.name }}
          </option>
        </select>
      </div>

      <div class="bg-surface p-6 rounded-lg shadow-md border border-gray-200">
        <h2 class="text-xl font-semibold mb-4">Inne Ustawienia</h2>

        <div class="mb-4 flex items-center">
          <input type="checkbox" v-model="autoArchive" class="mr-2" id="archive">
          <label for="archive">Automatyczna archiwizacja zadań</label>
        </div>

        <div class="mb-4">
          <label class="block font-medium mb-1">Minimalna długość hasła</label>
          <input
              type="number"
              v-model="minPasswordLength"
              min="4"
              max="32"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
          />
        </div>

        <div class="mb-4">
          <label class="block font-medium mb-1">Automatyczne wylogowanie (minuty)</label>
          <input
              type="number"
              v-model="autoLogout"
              min="1"
              max="120"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
          />
        </div>

        <div class="mb-4">
          <label class="block font-medium mb-1">Domyślny język systemu</label>
          <select
              v-model="language"
              class="w-full p-2 border border-gray-300 rounded bg-white focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="pl">Polski</option>
            <option value="en">English</option>
          </select>
        </div>

        <div class="mt-6">
          <button
              @click="saveSettings"
              :disabled="isSaving"
              class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-md transition w-full disabled:bg-gray-400"
          >
            <span v-if="isSaving">Zapisywanie...</span>
            <span v-else>Zapisz Ustawienia</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import systemSettingService from '../services/systemSettingService';
import priorityService from '../services/priorityService';
import { authState } from '../../router/router.js';

export default {
  setup() {
    // State variables
    const statusValue = ref(50);
    const selectedPriority = ref(null);
    const priorities = ref([]);
    const autoArchive = ref(true);
    const minPasswordLength = ref(8);
    const autoLogout = ref(15);
    const language = ref('pl');
    const loading = ref(true);
    const error = ref(null);
    const isSaving = ref(false);

    // Settings keys in the database
    const SETTING_KEYS = {
      STATUS_VALUE: 'status_threshold',
      DEFAULT_PRIORITY: 'default_priority',
      AUTO_ARCHIVE: 'auto_archive_tasks',
      MIN_PASSWORD_LENGTH: 'min_password_length',
      AUTO_LOGOUT: 'auto_logout_minutes',
      DEFAULT_LANGUAGE: 'default_language'
    };

    // Load system settings from backend
    const loadSettings = async () => {
      loading.value = true;
      error.value = null;

      try {
        // Load priorities for selection
        const prioritiesData = await priorityService.getAllPrioritiesSorted();
        priorities.value = prioritiesData;

        // Load all settings
        const settingsData = await systemSettingService.getAllSystemSettings();

        // Process settings
        for (const setting of settingsData) {
          switch (setting.key) {
            case SETTING_KEYS.STATUS_VALUE:
              statusValue.value = parseInt(setting.value) || 50;
              break;
            case SETTING_KEYS.DEFAULT_PRIORITY:
              selectedPriority.value = parseInt(setting.value) ||
                  (priorities.value.length > 0 ? priorities.value[0].id : null);
              break;
            case SETTING_KEYS.AUTO_ARCHIVE:
              autoArchive.value = setting.value === 'true';
              break;
            case SETTING_KEYS.MIN_PASSWORD_LENGTH:
              minPasswordLength.value = parseInt(setting.value) || 8;
              break;
            case SETTING_KEYS.AUTO_LOGOUT:
              autoLogout.value = parseInt(setting.value) || 15;
              break;
            case SETTING_KEYS.DEFAULT_LANGUAGE:
              language.value = setting.value || 'pl';
              break;
          }
        }

        // If no priority is selected but we have priorities, select the first one
        if (!selectedPriority.value && priorities.value.length > 0) {
          selectedPriority.value = priorities.value[0].id;
        }

      } catch (err) {
        console.error('Error loading settings:', err);
        error.value = `Błąd ładowania ustawień: ${err.message}`;
      } finally {
        loading.value = false;
      }
    };

    // Save system settings to backend
    const saveSettings = async () => {
      if (isSaving.value) return;

      isSaving.value = true;
      error.value = null;

      try {
        if (!authState.user || !authState.user.id) {
          throw new Error('Nie jesteś zalogowany');
        }

        const userId = authState.user.id;

        // Prepare and save each setting
        const settingsToUpdate = [
          { key: SETTING_KEYS.STATUS_VALUE, value: statusValue.value.toString() },
          { key: SETTING_KEYS.DEFAULT_PRIORITY, value: selectedPriority.value.toString() },
          { key: SETTING_KEYS.AUTO_ARCHIVE, value: autoArchive.value.toString() },
          { key: SETTING_KEYS.MIN_PASSWORD_LENGTH, value: minPasswordLength.value.toString() },
          { key: SETTING_KEYS.AUTO_LOGOUT, value: autoLogout.value.toString() },
          { key: SETTING_KEYS.DEFAULT_LANGUAGE, value: language.value }
        ];

        // Update each setting
        for (const setting of settingsToUpdate) {
          try {
            // Check if setting exists
            const existingSetting = await systemSettingService.getSystemSettingByKey(setting.key);

            if (existingSetting) {
              // Update existing setting
              await systemSettingService.updateSettingValue(
                  setting.key,
                  setting.value,
                  userId
              );
            } else {
              // Create new setting
              await systemSettingService.createSystemSettingFromParams({
                key: setting.key,
                value: setting.value,
                description: `System setting for ${setting.key}`,
                updatedById: userId
              });
            }
          } catch (settingError) {
            console.error(`Error updating setting ${setting.key}:`, settingError);
          }
        }

        alert('Ustawienia zostały zapisane pomyślnie!');
      } catch (err) {
        console.error('Error saving settings:', err);
        error.value = `Błąd zapisywania ustawień: ${err.message}`;
        alert(`Błąd: ${err.message}`);
      } finally {
        isSaving.value = false;
      }
    };

    // Initialize component
    onMounted(loadSettings);

    return {
      statusValue,
      selectedPriority,
      priorities,
      autoArchive,
      minPasswordLength,
      autoLogout,
      language,
      loading,
      error,
      isSaving,
      loadSettings,
      saveSettings
    };
  }
};
</script>