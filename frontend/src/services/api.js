// src/services/api.js
import { ref } from 'vue';

// Pobierz URL z pliku .env
const API_URL = process.env.API_URL || 'http://localhost:8080';

// Create a singleton API service
const apiService = {
    apiUrl: ref(API_URL),
    isLoaded: ref(true), // Domyślnie true, ponieważ mamy URL z .env
    isConnected: ref(false),
    isChecking: ref(false),
    lastError: ref(null),

    /**
     * Initialize the API service with the URL from Electron's contextBridge
     */
    async init() {
        if (window.api?.getApiUrl) {
            try {
                const customUrl = await window.api.getApiUrl();
                // Jeśli mamy niestandardowy URL z Electron, użyj go
                if (customUrl) {
                    this.apiUrl.value = customUrl;
                }
                this.isLoaded.value = true;
                console.log(`API Service initialized with URL: ${this.apiUrl.value}`);
                return true;
            } catch (error) {
                console.error("Failed to get API URL from Electron, using default:", API_URL);
                // Nawet jeśli nie udało się pobrać URL z Electron, nadal używamy domyślnego
                this.apiUrl.value = API_URL;
                this.isLoaded.value = true;
                return true;
            }
        } else {
            console.info("API bridge not available, using default URL:", API_URL);
            // Jeśli brak API bridge, nadal używamy domyślnego URL
            this.apiUrl.value = API_URL;
            this.isLoaded.value = true;
            return true;
        }
    },

    /**
     * Prosta metoda do sprawdzenia połączenia
     */
    async checkConnection() {
        if (this.isChecking.value) return this.isConnected.value;

        this.isChecking.value = true;

        try {
            // Sprawdź, czy API jest inicjalizowane
            if (!this.isLoaded.value) {
                await this.init();
            }

            // Użyj pełnego adresu do sprawdzenia połączenia
            const checkUrl = `${this.apiUrl.value}/database/users`;
            console.log("Sprawdzanie połączenia z:", checkUrl);

            // Timeout + próba połączenia
            const timeoutPromise = new Promise((_, reject) =>
                setTimeout(() => reject(new Error('Connection timeout')), 3000)
            );

            const fetchPromise = fetch(checkUrl);

            // Wyścig między timeoutem a fetch
            const response = await Promise.race([fetchPromise, timeoutPromise]);

            // Jeśli dotarliśmy tutaj, połączenie działa
            if (response.ok) {
                this.isConnected.value = true;
                this.lastError.value = null;
                console.log("✅ Connection check: CONNECTED");
                return true;
            } else {
                throw new Error(`API responded with status: ${response.status}`);
            }
        } catch (error) {
            console.error("❌ Connection check failed:", error);
            this.isConnected.value = false;
            this.lastError.value = error;
            return false;
        } finally {
            this.isChecking.value = false;
        }
    },

    /**
     * Prosta metoda do ponownej próby połączenia
     */
    async retry() {
        console.log("Attempting connection retry...");
        return await this.checkConnection();
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

            this.isConnected.value = true;
            return await response.json();
        } catch (error) {
            console.error(`Error fetching ${url}:`, error);
            this.isConnected.value = false;
            this.lastError.value = error;
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