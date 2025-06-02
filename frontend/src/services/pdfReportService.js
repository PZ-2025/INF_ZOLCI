// src/services/pdfReportService.js
import axios from 'axios';

const API_URL = process.env.API_URL || 'http://localhost:8080';

const pdfReportService = {
    async generateConstructionProgressReport(teamId, dateFrom, dateTo, userId) {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🌐 [${requestId}] Wywołanie API: generateConstructionProgressReport`, { teamId, dateFrom, dateTo, userId });
        
        try {
            const url = `${API_URL}/api/generate-report/construction-progress`;
            const params = { teamId, dateFrom, dateTo, userId };
            
            console.log(`📤 [${requestId}] POST ${url}`, params);
            
            const response = await axios.post(url, {}, { 
                params,
                timeout: 30000
            });

            console.log(`📨 [${requestId}] Odpowiedź:`, response.data);
            return response.data;
        } catch (error) {
            console.error(`❌ [${requestId}] Błąd podczas generowania raportu postępu budowy:`, error);
            throw error;
        }
    },

    async generateEmployeeLoadReport(targetUserId, dateFrom, dateTo, userId) {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🌐 [${requestId}] Wywołanie API: generateEmployeeLoadReport`, { targetUserId, dateFrom, dateTo, userId });
        
        try {
            const url = `${API_URL}/api/generate-report/employee-load`;
            const params = { dateFrom, dateTo, userId };
            if (targetUserId) {
                params.targetUserId = targetUserId;
            }

            console.log(`📤 [${requestId}] POST ${url}`, params);
            
            const response = await axios.post(url, {}, { 
                params,
                timeout: 30000
            });

            console.log(`📨 [${requestId}] Odpowiedź:`, response.data);
            return response.data;
        } catch (error) {
            console.error(`❌ [${requestId}] Błąd podczas generowania raportu obciążenia pracownika:`, error);
            throw error;
        }
    },

    async generateTeamEfficiencyReport(dateFrom, dateTo, userId) {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🌐 [${requestId}] Wywołanie API: generateTeamEfficiencyReport`, { dateFrom, dateTo, userId });
        
        try {
            const url = `${API_URL}/api/generate-report/team-efficiency`;
            const params = { dateFrom, dateTo, userId };
            
            console.log(`📤 [${requestId}] POST ${url}`, params);
            
            const response = await axios.post(url, {}, { 
                params,
                timeout: 30000
            });

            console.log(`📨 [${requestId}] Odpowiedź:`, response.data);
            return response.data;
        } catch (error) {
            console.error(`❌ [${requestId}] Błąd podczas generowania raportu efektywności zespołu:`, error);
            throw error;
        }
    },

    async downloadReportBlob(reportId) {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`📥 [${requestId}] Pobieranie blob raportu ID: ${reportId}`);
        
        try {
            const response = await axios.get(`${API_URL}/api/generate-report/download/${reportId}`, {
                responseType: 'blob',
                timeout: 30000
            });
            
            console.log(`✅ [${requestId}] Pobrano blob, rozmiar:`, response.data.size);
            return response.data;
        } catch (error) {
            console.error(`❌ [${requestId}] Błąd podczas pobierania raportu:`, error);
            throw error;
        }
    },

    async downloadReport(reportId) {
        return `${API_URL}/api/generate-report/download/${reportId}`;
    },

    async downloadAndSaveReport(reportId, fileName = null) {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`💾 [${requestId}] downloadAndSaveReport - reportId: ${reportId}, fileName: ${fileName}`);
        
        try {
            const blob = await this.downloadReportBlob(reportId);
            
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = fileName || `raport_${reportId}.pdf`;
            
            console.log(`📋 [${requestId}] Tworzenie linku do pobierania: ${link.download}`);
            
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            
            window.URL.revokeObjectURL(url);
            
            console.log(`✅ [${requestId}] Plik ${link.download} zapisany pomyślnie`);
            return true;
        } catch (error) {
            console.error(`❌ [${requestId}] Błąd podczas pobierania i zapisywania raportu:`, error);
            throw error;
        }
    }
};

export default pdfReportService;