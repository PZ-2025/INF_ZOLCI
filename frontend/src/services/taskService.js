// src/services/taskService.js
import apiService from './apiService';

const taskService = {
    // Pobieranie wszystkich zadań
    async getAllTasks() {
        return await apiService.get('/database/tasks');
    },

    // Pobieranie zadania po ID
    async getTaskById(taskId) {
        try {
            return await apiService.get(`/database/tasks/${taskId}`);
        } catch (error) {
            console.error('Error fetching task:', error);
            throw new Error('Nie udało się pobrać szczegółów zadania');
        }
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
        try {
            return await apiService.delete(`/database/tasks/${taskId}`);
        } catch (error) {
            console.error('Error deleting task:', error);
            throw new Error('Nie udało się usunąć zadania');
        }
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
    },

    // Dodawanie komentarza do zadania
    async addComment(commentData) {
        try {
            console.log('Wysyłanie komentarza do API:', commentData);
            const response = await apiService.post(
                '/database/task-comments', 
                {
                    taskId: parseInt(commentData.taskId),
                    userId: parseInt(commentData.userId),
                    comment: commentData.content
                }
            );
            console.log('Odpowiedź z API:', response);
            return response;
        } catch (error) {
            console.error('API error:', error);
            throw new Error(error.response?.data?.message || 'Nie udało się dodać komentarza');
        }
    },
};

export default taskService;