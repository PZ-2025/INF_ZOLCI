// src/services/taskService.js
import apiService from './apiService';

const taskService = {
    // Pobieranie wszystkich zadań
    async getAllTasks() {
        return await apiService.get('/database/tasks');
    },

    // Pobieranie zadania po ID
    async getTaskById(taskId) {
        return await apiService.get(`/database/tasks/${taskId}`);
    },

    // Tworzenie zadania
    async createTask(taskData) {
        return await apiService.post('/database/tasks', taskData);
    },

    // Aktualizacja zadania
    async updateTask(taskId, taskData) {
        return await apiService.put(`/database/tasks/${taskId}`, taskData);
    },

    // Usuwanie zadania
    async deleteTask(taskId) {
        return await apiService.delete(`/database/tasks/${taskId}`);
    },

    // Pobieranie zadań dla zespołu
    async getTasksByTeamId(teamId) {
        return await apiService.get(`/database/tasks/team/${teamId}`);
    },

    // Pobieranie zadań o określonym statusie
    async getTasksByStatusId(statusId) {
        return await apiService.get(`/database/tasks/status/${statusId}`);
    },

    // Pobieranie zadań o określonym priorytecie
    async getTasksByPriorityId(priorityId) {
        return await apiService.get(`/database/tasks/priority/${priorityId}`);
    },

    // Pobieranie zadania po tytule
    async getTaskByTitle(title) {
        return await apiService.get(`/database/tasks/title/${title}`);
    },

    // Pobieranie zadań z terminem przed określoną datą
    async getTasksWithDeadlineBefore(date) {
        return await apiService.get(`/database/tasks/deadline-before/${date}`);
    }
};

export default taskService;