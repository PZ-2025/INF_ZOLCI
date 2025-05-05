// src/services/teamService.js
import apiService from './apiService.js';

const teamService = {
    // Get all teams
    async getAllTeams() {
        return await apiService.get('/database/teams');
    },

    // Get team details
    async getTeamById(teamId) {
        return await apiService.get(`/database/teams/${teamId}`);
    },

    // Get team members
    async getTeamMembers(teamId) {
        return await apiService.get(`/database/team-members/team/${teamId}`);
    },

    // Get team tasks
    async getTeamTasks(teamId) {
        return await apiService.get(`/database/tasks/team/${teamId}`);
    },

    // Create new team
    async createTeam(teamData) {
        return await apiService.post('/database/teams', teamData);
    },

    // Update team
    async updateTeam(teamId, teamData) {
        return await apiService.put(`/database/teams/${teamId}`, teamData);
    },

    // Activate team
    async activateTeam(teamId) {
        return await apiService.patch(`/database/teams/${teamId}/activate`);
    },

    // Deactivate team
    async deactivateTeam(teamId) {
        return await apiService.patch(`/database/teams/${teamId}/deactivate`);
    },
    // Dodaj członka do zespołu
    async addTeamMember(teamId, userId) {
        return await apiService.post(`/database/team-members`, {
            teamId: teamId,
            userId: userId
        });
    },

    // Usuń członka z zespołu
    async removeTeamMember(teamMemberId) {
        return await apiService.delete(`/database/team-members/${teamMemberId}`);
    },

    // Pobierz wszystkich niewykorzystanych pracowników (nienależących do żadnego zespołu)
    async getAvailableEmployees() {
        return await apiService.get(`/database/users/not-in-team`);
    }
};

export default teamService;