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
            const task = await apiService.get(`/database/tasks/${taskId}`);
            console.log('Pobrane zadanie z API:', task);
            
            // Jeśli nie ma komentarzy w odpowiedzi, spróbuj je pobrać osobno
            if (!task.comments && !task.taskComments && !task.task_comments) {
                try {
                    const comments = await this.getTaskComments(taskId);
                    if (Array.isArray(comments)) {
                        task.comments = comments;
                    }
                } catch (commentsErr) {
                    console.error('Nie udało się pobrać komentarzy:', commentsErr);
                }
            }
            
            return task;
        } catch (error) {
            console.error('Error fetching task:', error);
            throw new Error('Nie udało się pobrać szczegółów zadania');
        }
    },
    
    // Pobieranie komentarzy dla zadania
    async getTaskComments(taskId) {
        try {
            return await apiService.get(`/database/task-comments/task/${taskId}`);
        } catch (error) {
            console.error('Error fetching task comments:', error);
            throw new Error('Nie udało się pobrać komentarzy zadania');
        }
    },
    
    // Tworzenie zadania
    async createTask(taskData) {
        // Sprawdzamy, czy dane mają prawidłowy format przed wysłaniem
        console.log('Dane otrzymane do utworzenia zadania:', taskData);

        // Upewniamy się, że wartości liczbowe są liczbami
        const cleanedData = {
            ...taskData,
            teamId: Number(taskData.teamId),
            priorityId: Number(taskData.priorityId),
            statusId: Number(taskData.statusId),
            createdById: Number(taskData.createdById || 1)
        };

        console.log('Oczyszczone dane zadania do wysłania:', cleanedData);
        return await apiService.post('/database/tasks', cleanedData);
    },
    
    // Aktualizacja zadania
    async updateTask(taskId, taskData) {
        // Również tutaj zapewniamy poprawne typy danych
        const cleanedData = {
            ...taskData,
            teamId: Number(taskData.teamId),
            priorityId: Number(taskData.priorityId),
            statusId: Number(taskData.statusId),
            createdById: Number(taskData.createdById || 1)
        };

        return await apiService.put(`/database/tasks/${taskId}`, cleanedData);
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
                    comment: commentData.content  // Zwróć uwagę na nazwę pola - 'comment' a nie 'content'
                }
            );
            console.log('Odpowiedź z API:', response);
            return response;
        } catch (error) {
            console.error('API error:', error);
            throw new Error(error.response?.data?.message || 'Nie udało się dodać komentarza');
        }
    },

    // Usuwanie komentarza
    async deleteComment(commentId) {
        try {
            console.log('Usuwanie komentarza o ID:', commentId);
            const response = await apiService.delete(`/database/task-comments/${commentId}`);
            console.log('Odpowiedź z API po usunięciu komentarza:', response);
            return response;
        } catch (error) {
            console.error('API error:', error);
            throw new Error(error.response?.data?.message || 'Nie udało się usunąć komentarza');
        }
    },
};

export default taskService;