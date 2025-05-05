// src/services/userService.js
import apiService from './apiService';

const userService = {
    // Get all users
    async getAllUsers() {
        return await apiService.get('/database/users');
    },

    // Get active users
    async getActiveUsers() {
        return await apiService.get('/database/users/active');
    },

    // Get user by ID
    async getUserById(userId) {
        return await apiService.get(`/database/users/${userId}`);
    },

    // Get user by username
    async getUserByUsername(username) {
        return await apiService.get(`/database/users/username/${username}`);
    },

    // Create user
    async createUser(userData) {
        return await apiService.post('/database/users', userData);
    },

    // Update user
    async updateUser(userId, userData) {
        return await apiService.put(`/database/users/${userId}`, userData);
    },

    // Delete user
    async deleteUser(userId) {
        return await apiService.delete(`/database/users/${userId}`);
    },

    // Deactivate user
    async deactivateUser(userId) {
        return await apiService.put(`/database/users/${userId}/deactivate`);
    },

    // Update last login
    async updateLastLogin(userId) {
        return await apiService.put(`/database/users/${userId}/login`);
    },

    async partialUpdateUser(userId, updateData) {
        return await apiService.patch(`/database/users/${userId}`, updateData);
    }
};

export default userService;