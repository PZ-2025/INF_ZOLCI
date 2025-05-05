// src/services/taskStatusService.js
import apiService from './apiService';

const taskStatusService = {
    // Pobieranie wszystkich statusów zadań
    async getAllTaskStatuses() {
        return await apiService.get('/database/task-statuses');
    },

    // Pobieranie statusów posortowanych według kolejności wyświetlania
    async getAllTaskStatusesSorted() {
        return await apiService.get('/database/task-statuses/sorted');
    },

    // Pobieranie statusu po ID
    async getTaskStatusById(statusId) {
        return await apiService.get(`/database/task-statuses/${statusId}`);
    },

    // Pobieranie statusu po nazwie
    async getTaskStatusByName(name) {
        return await apiService.get(`/database/task-statuses/name/${name}`);
    },

    // Tworzenie statusu
    async createTaskStatus(statusData) {
        return await apiService.post('/database/task-statuses', statusData);
    },

    // Tworzenie statusu z parametrów
    async createTaskStatusFromParams(name, progressMin, progressMax, displayOrder) {
        return await apiService.post('/database/task-statuses/create', {
            name, progressMin, progressMax, displayOrder
        });
    },

    // Aktualizacja statusu
    async updateTaskStatus(statusId, statusData) {
        return await apiService.put(`/database/task-statuses/${statusId}`, statusData);
    },

    // Aktualizacja kolejności wyświetlania
    async updateDisplayOrder(statusId, displayOrder) {
        return await apiService.patch(`/database/task-statuses/${statusId}/display-order`, {
            displayOrder
        });
    },

    // Usuwanie statusu
    async deleteTaskStatus(statusId) {
        return await apiService.delete(`/database/task-statuses/${statusId}`);
    }
};

export default taskStatusService;