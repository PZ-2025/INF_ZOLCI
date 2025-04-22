// src/services/api.js
import { ref } from 'vue';

// Create a singleton API service
const apiService = {
    apiUrl: ref(''),
    isLoaded: ref(false),

    /**
     * Initialize the API service with the URL from Electron's contextBridge
     */
    async init() {
        if (window.api?.getApiUrl) {
            try {
                this.apiUrl.value = await window.api.getApiUrl();
                this.isLoaded.value = true;
                console.log(`API Service initialized with URL: ${this.apiUrl.value}`);
            } catch (error) {
                console.error("Failed to get API URL:", error);
                this.isLoaded.value = false;
            }
        } else {
            console.error("API bridge not available");
            this.isLoaded.value = false;
        }
    },

    /**
     * Generic GET request method
     * @param {string} endpoint - API endpoint path
     * @param {Object} params - Query parameters
     * @returns {Promise} - Promise resolving to the response data
     */
    async get(endpoint, params = {}) {
        await this.ensureInit();

        const url = new URL(`${this.apiUrl.value}${endpoint}`);

        // Add query parameters if any
        if (params) {
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null)
                    url.searchParams.append(key, params[key]);
            });
        }

        try {
            const response = await fetch(url);
            if (!response.ok) {
                throw new Error(`API Error: ${response.status} ${response.statusText}`);
            }
            return await response.json();
        } catch (error) {
            console.error(`Error fetching ${url}:`, error);
            throw error;
        }
    },

    /**
     * Generic POST request method
     * @param {string} endpoint - API endpoint path
     * @param {Object} data - Request body
     * @returns {Promise} - Promise resolving to the response data
     */
    async post(endpoint, data = {}) {
        await this.ensureInit();

        try {
            const response = await fetch(`${this.apiUrl.value}${endpoint}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data),
            });

            if (!response.ok) {
                throw new Error(`API Error: ${response.status} ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error(`Error posting to ${endpoint}:`, error);
            throw error;
        }
    },

    /**
     * Generic PUT request method
     * @param {string} endpoint - API endpoint path
     * @param {Object} data - Request body
     * @returns {Promise} - Promise resolving to the response data
     */
    async put(endpoint, data = {}) {
        await this.ensureInit();

        try {
            const response = await fetch(`${this.apiUrl.value}${endpoint}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data),
            });

            if (!response.ok) {
                throw new Error(`API Error: ${response.status} ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error(`Error putting to ${endpoint}:`, error);
            throw error;
        }
    },

    /**
     * Generic DELETE request method
     * @param {string} endpoint - API endpoint path
     * @returns {Promise} - Promise resolving to the response data
     */
    async delete(endpoint) {
        await this.ensureInit();

        try {
            const response = await fetch(`${this.apiUrl.value}${endpoint}`, {
                method: 'DELETE',
            });

            if (!response.ok) {
                throw new Error(`API Error: ${response.status} ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error(`Error deleting at ${endpoint}:`, error);
            throw error;
        }
    },

    /**
     * Ensure the API service is initialized
     * @private
     */
    async ensureInit() {
        if (!this.isLoaded.value) {
            await this.init();
        }

        if (!this.isLoaded.value) {
            throw new Error("API service is not available");
        }
    }
};

// Export the singleton instance
export default apiService;