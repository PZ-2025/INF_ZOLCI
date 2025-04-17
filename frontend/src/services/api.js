// src/services/api.js
import { ref } from 'vue';

// Create a singleton API service
const apiService = {
    apiUrl: ref('http://localhost:8080'), // Domyślny adres backendu Spring Boot
    isLoaded: ref(true), // Domyślnie załadowane - mamy statyczny URL

    /**
     * Initialize the API service
     */
    async init() {
        // Jeśli aplikacja działa w Electron, spróbuj pobrać URL z contextBridge
        if (window.api?.getApiUrl) {
            try {
                const apiUrl = await window.api.getApiUrl();
                this.apiUrl.value = apiUrl || 'http://localhost:8080';
                console.log(`API Service initialized with URL: ${this.apiUrl.value}`);
            } catch (error) {
                console.error("Failed to get API URL, using default:", error);
                this.apiUrl.value = 'http://localhost:8080';
            }
        } else {
            // W środowisku przeglądarki używamy domyślnego URL
            console.log("Using default API URL: http://localhost:8080");
        }
        this.isLoaded.value = true;
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

            // Próbuj zwrócić JSON jeśli jest dostępny, w przeciwnym razie null
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.indexOf("application/json") !== -1) {
                return await response.json();
            }
            return null;
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
    }
};

// Export the singleton instance
export default apiService;