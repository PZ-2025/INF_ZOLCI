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

    // Check if email is available (not taken by another user)
    async checkEmailAvailability(email) {
        return await apiService.get(`/api/auth/check/email/${encodeURIComponent(email)}`);
    },

    // Check if username is available (not taken by another user)
    async checkUsernameAvailability(username) {
        return await apiService.get(`/api/auth/check/username/${encodeURIComponent(username)}`);
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

    // Partial update user
    async partialUpdateUser(userId, updateData) {
        return await apiService.patch(`/database/users/${userId}`, updateData);
    }
};

export default userService;