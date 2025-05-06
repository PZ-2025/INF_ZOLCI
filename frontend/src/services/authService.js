// src/services/authService.js
import axios from 'axios';
import { authState } from '../../router/router.js';

// Bazowy URL API
const API_URL = process.env.API_URL || 'http://localhost:8080';

const authService = {
    /**
     * Logowanie użytkownika poprzez API backendu
     * @param {Object} credentials - dane logowania (username, password)
     * @returns {Promise} - obiekt zalogowanego użytkownika
     */
    async login(credentials) {
        try {
            console.log("Próba logowania z danymi:", credentials);

            // Wykonanie żądania logowania do backendu
            const response = await axios.post(`${API_URL}/api/auth/login`, credentials);

            console.log("Odpowiedź z serwera:", response.data);

            // Jeśli mamy poprawną odpowiedź
            if (response.data) {
                // Aktualizacja stanu uwierzytelnienia
                authState.isAuthenticated = true;
                authState.user = {
                    id: response.data.id,
                    username: response.data.username,
                    firstName: response.data.firstName,
                    lastName: response.data.lastName,
                    email: response.data.email,
                    role: response.data.role || 'employee' // Domyślna rola, jeśli nie określono
                };

                console.log("Stan uwierzytelnienia po logowaniu:", authState);

                // Zapisz token jeśli został zwrócony (opcjonalnie)
                if (response.data.token) {
                    authState.token = response.data.token;
                    localStorage.setItem('auth_token', response.data.token);
                }

                return response.data;
            }

            throw new Error('Nieprawidłowa odpowiedź z serwera');
        } catch (error) {
            console.error('Błąd logowania:', error);

            // Jeżeli mamy odpowiedź z serwera z komunikatem błędu
            if (error.response && error.response.data) {
                throw new Error(error.response.data.message || 'Błąd logowania');
            }

            throw new Error('Problem z połączeniem do serwera');
        }
    },

    /**
     * Sprawdzenie stanu połączenia z backendem
     * @returns {Promise<boolean>} - czy backend jest dostępny
     */
    async checkConnection() {
        try {
            const response = await axios.get(`${API_URL}/api/health`, { timeout: 3000 });
            return response.status === 200;
        } catch (error) {
            console.error('Błąd połączenia z backendem:', error);
            return false;
        }
    },

    /**
     * Wylogowanie użytkownika
     */
    logout() {
        // Czyszczenie stanu uwierzytelnienia
        authState.isAuthenticated = false;
        authState.user = null;
        authState.token = null;

        // Usunięcie zapisanego tokenu
        localStorage.removeItem('auth_token');
    }
};

export default authService;