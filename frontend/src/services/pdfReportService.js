// src/services/pdfReportService.js
import axios from 'axios';

// Pobranie URL API bezpośrednio, bez zależności od apiUrl.value
const API_URL = process.env.API_URL || 'http://localhost:8080';

const pdfReportService = {
    // Generowanie raportu postępu budowy
    async generateConstructionProgressReport(teamId, dateFrom, dateTo, userId) {
        try {
            const url = `${API_URL}/api/generate-report/construction-progress`;

            // Używamy query params zgodnie z URL w przykładzie
            const params = { teamId, dateFrom, dateTo, userId };
            const response = await axios.post(url, {}, { params });

            return response.data;
        } catch (error) {
            console.error('Błąd podczas generowania raportu postępu budowy:', error);
            throw error;
        }
    },

    // Generowanie raportu obciążenia pracownika
    async generateEmployeeLoadReport(targetUserId, dateFrom, dateTo, userId) {
        try {
            const url = `${API_URL}/api/generate-report/employee-load`;

            // Parametry z targetUserId (opcjonalnie)
            const params = { dateFrom, dateTo, userId };
            if (targetUserId) {
                params.targetUserId = targetUserId;
            }

            const response = await axios.post(url, {}, { params });

            return response.data;
        } catch (error) {
            console.error('Błąd podczas generowania raportu obciążenia pracownika:', error);
            throw error;
        }
    },

    // Generowanie raportu efektywności zespołu
    async generateTeamEfficiencyReport(dateFrom, dateTo, userId) {
        try {
            const url = `${API_URL}/api/generate-report/team-efficiency`;

            const params = { dateFrom, dateTo, userId };
            const response = await axios.post(url, {}, { params });

            return response.data;
        } catch (error) {
            console.error('Błąd podczas generowania raportu efektywności zespołu:', error);
            throw error;
        }
    },

    // Pobieranie wygenerowanego raportu PDF
    async downloadReport(reportId) {
        return `${API_URL}/api/generate-report/download/${reportId}`;
    }
};

export default pdfReportService;