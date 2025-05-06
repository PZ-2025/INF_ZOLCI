// src/services/systemSettingService.js
import apiService from './apiService';

const systemSettingService = {
    // Pobieranie wszystkich ustawień systemowych
    async getAllSystemSettings() {
        return await apiService.get('/database/system-settings');
    },

    // Pobieranie ustawienia po ID
    async getSystemSettingById(settingId) {
        return await apiService.get(`/database/system-settings/${settingId}`);
    },

    // Pobieranie ustawienia po kluczu
    async getSystemSettingByKey(key) {
        return await apiService.get(`/database/system-settings/key/${key}`);
    },

    // Pobieranie wartości ustawienia jako string
    async getSettingValue(key, defaultValue = '') {
        return await apiService.get(`/database/system-settings/value/${key}`, { defaultValue });
    },

    // Tworzenie ustawienia
    async createSystemSetting(settingData) {
        return await apiService.post('/database/system-settings', settingData);
    },

    // Tworzenie ustawienia z parametrów
    async createSystemSettingFromParams(params) {
        return await apiService.post('/database/system-settings/create', params);
    },

    // Aktualizacja ustawienia
    async updateSystemSetting(settingId, settingData) {
        return await apiService.put(`/database/system-settings/${settingId}`, settingData);
    },

    // Aktualizacja wartości ustawienia po kluczu
    async updateSettingValue(key, value, updatedById) {
        return await apiService.patch(`/database/system-settings/key/${key}`, { value, updatedById });
    },

    // Usuwanie ustawienia
    async deleteSystemSetting(settingId) {
        return await apiService.delete(`/database/system-settings/${settingId}`);
    }
};

export default systemSettingService;