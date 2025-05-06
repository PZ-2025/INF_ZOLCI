// src/services/priorityService.js
import apiService from './apiService';

const priorityService = {
    // Pobieranie wszystkich priorytetów
    async getAllPriorities() {
        return await apiService.get('/database/priorities');
    },

    // Pobieranie priorytetów posortowanych według wartości
    async getAllPrioritiesSorted() {
        return await apiService.get('/database/priorities/sorted');
    },

    // Pobieranie priorytetu po ID
    async getPriorityById(priorityId) {
        return await apiService.get(`/database/priorities/${priorityId}`);
    },

    // Pobieranie priorytetu po nazwie
    async getPriorityByName(name) {
        return await apiService.get(`/database/priorities/name/${name}`);
    },

    // Tworzenie priorytetu
    async createPriority(priorityData) {
        return await apiService.post('/database/priorities', priorityData);
    },

    // Tworzenie priorytetu z parametrów
    async createPriorityFromParams(name, value, colorCode) {
        return await apiService.post('/database/priorities/create', { name, value, colorCode });
    },

    // Aktualizacja priorytetu
    async updatePriority(priorityId, priorityData) {
        return await apiService.put(`/database/priorities/${priorityId}`, priorityData);
    },

    // Aktualizacja koloru priorytetu
    async updatePriorityColor(priorityId, colorCode) {
        return await apiService.patch(`/database/priorities/${priorityId}/color`, { colorCode });
    },

    // Usuwanie priorytetu
    async deletePriority(priorityId) {
        return await apiService.delete(`/database/priorities/${priorityId}`);
    }
};

export default priorityService;