
import axios from 'axios';
import { ref } from 'vue';
import { authState } from '../../router/router.js';

// API error types for better error handling
export const API_ERROR_TYPES = {
    DATABASE: 'DATABASE_ERROR',
    NETWORK: 'NETWORK_ERROR',
    TIMEOUT: 'TIMEOUT_ERROR',
    AUTH: 'AUTH_ERROR',
    SERVER: 'SERVER_ERROR',
    CLIENT: 'CLIENT_ERROR',
    UNKNOWN: 'UNKNOWN_ERROR'
};

// Create axios instance with default config
const apiClient = axios.create({
    baseURL: process.env.API_URL || 'http://localhost:8080',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
});

// State for API
const state = {
    isConnected: ref(false),
    isChecking: ref(false),
    lastError: ref(null),
    errorType: ref(null)
};

// Interceptor for handling request
apiClient.interceptors.request.use(
    config => {
        // Add auth token if available
        if (authState.isAuthenticated && authState.token) {
            config.headers.Authorization = `Bearer ${authState.token}`;
        }
        return config;
    },
    error => {
        console.error('Request error:', error);
        return Promise.reject(error);
    }
);

// Interceptor for handling response
apiClient.interceptors.response.use(
    response => {
        state.isConnected.value = true;
        state.lastError.value = null;
        state.errorType.value = null;
        return response.data;
    },
    error => {
        console.error('API error:', error);
        state.lastError.value = error;

        // Classify error type
        if (error.response) {
            // The server responded with a status code outside the 2xx range
            if (error.response.status === 503) {
                state.errorType.value = API_ERROR_TYPES.DATABASE;
            } else if (error.response.status === 401 || error.response.status === 403) {
                state.errorType.value = API_ERROR_TYPES.AUTH;
                // Handle token expiration
                if (authState.isAuthenticated) {
                    authState.isAuthenticated = false;
                    authState.token = null;
                    // Redirect to login page if needed
                    // router.push('/');
                }
            } else if (error.response.status >= 500) {
                state.errorType.value = API_ERROR_TYPES.SERVER;
            } else if (error.response.status >= 400) {
                state.errorType.value = API_ERROR_TYPES.CLIENT;
            } else {
                state.errorType.value = API_ERROR_TYPES.UNKNOWN;
            }

            state.isConnected.value = true; // Server responded, so connection is ok
        } else if (error.request) {
            // The request was made but no response was received
            state.isConnected.value = false;
            if (error.code === 'ECONNABORTED') {
                state.errorType.value = API_ERROR_TYPES.TIMEOUT;
            } else {
                state.errorType.value = API_ERROR_TYPES.NETWORK;
            }
        } else {
            // Something happened in setting up the request
            state.isConnected.value = false;
            state.errorType.value = API_ERROR_TYPES.UNKNOWN;
        }

        return Promise.reject(error);
    }
);

// API service methods
const apiService = {
    // Connection state
    ...state,

    // Check connection to backend
    async checkConnection() {
        if (state.isChecking.value) return state.isConnected.value;

        state.isChecking.value = true;

        try {
            await apiClient.get('/api/health');
            state.isConnected.value = true;
            return true;
        } catch (error) {
            state.isConnected.value = false;
            return false;
        } finally {
            state.isChecking.value = false;
        }
    },

    // Retry connection
    async retry() {
        return await this.checkConnection();
    },

    // Authentication
    async login(credentials) {
        try {
            const response = await apiClient.post('/api/auth/login', credentials);

            if (response && response.id) {
                // Update auth state
                authState.isAuthenticated = true;
                authState.user = {
                    id: response.id,
                    username: response.username,
                    firstName: response.firstName,
                    lastName: response.lastName,
                    email: response.email,
                    role: response.role
                };
                authState.token = response.token || 'mock-token'; // Handle if your API doesn't return a token

                return response;
            }

            throw new Error('Invalid login response');
        } catch (error) {
            state.errorType.value = API_ERROR_TYPES.AUTH;
            throw error;
        }
    },

    // Register new user
    async register(userData) {
        return await apiClient.post('/api/auth/register', userData);
    },

    // Logout user
    logout() {
        authState.isAuthenticated = false;
        authState.user = null;
        authState.token = null;
    },

    // Generic API methods
    async get(endpoint, params = {}) {
        return await apiClient.get(endpoint, { params });
    },

    async post(endpoint, data = {}) {
        return await apiClient.post(endpoint, data);
    },

    async put(endpoint, data = {}) {
        return await apiClient.put(endpoint, data);
    },

    async delete(endpoint) {
        return await apiClient.delete(endpoint);
    },

    async patch(endpoint, data = {}) {
        return await apiClient.patch(endpoint, data);
    }
};

export default apiService;