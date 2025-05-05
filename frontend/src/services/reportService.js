// src/services/reportService.js
import apiService from './apiService';

const reportService = {
    // Pobieranie wszystkich raportów
    async getAllReports() {
        return await apiService.get('/database/reports');
    },

    // Pobieranie raportu po ID
    async getReportById(reportId) {
        return await apiService.get(`/database/reports/${reportId}`);
    },

    // Tworzenie raportu
    async createReport(reportData) {
        return await apiService.post('/database/reports', reportData);
    },

    // Usuwanie raportu
    async deleteReport(reportId) {
        return await apiService.delete(`/database/reports/${reportId}`);
    },

    // Pobieranie raportów określonego typu
    async getReportsByType(typeId) {
        return await apiService.get(`/database/reports/type/${typeId}`);
    },

    // Pobieranie raportów utworzonych przez określonego użytkownika
    async getReportsByUser(userId) {
        return await apiService.get(`/database/reports/user/${userId}`);
    },

    // Aktualizacja raportu
    async updateReport(reportId, reportData) {
        return await apiService.put(`/database/reports/${reportId}`, reportData);
    },

    // Pobieranie wszystkich typów raportów
    async getAllReportTypes() {
        return await apiService.get('/database/report-types');
    },

    // Pobieranie typu raportu po ID
    async getReportTypeById(typeId) {
        return await apiService.get(`/database/report-types/${typeId}`);
    }
};

export default reportService;