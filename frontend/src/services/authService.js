// src/services/authService.js
import axios from 'axios';
import { authState } from '../../router/router.js';

// Bazowy URL API
const API_URL = process.env.API_URL || 'http://localhost:8080';

// Hierarchia ról (im wyższa liczba, tym większe uprawnienia)
const roleHierarchy = {
  'użytkownik': 1,    //employee
  'kierownik': 2,     //manager
  'administrator': 3  //admin
};

const authService = {
  /**
   * Logowanie użytkownika poprzez API backendu
   * @param {Object} credentials - dane logowania (username, password)
   * @returns {Promise<Object>} - obiekt zalogowanego użytkownika
   */
  async login(credentials) {
    try {
      console.log("Próba logowania z danymi:", credentials);

      const response = await axios.post(`${API_URL}/api/auth/login`, credentials);

      console.log("Odpowiedź z serwera:", response.data);

      if (response.data) {
        authState.isAuthenticated = true;
        authState.user = {
          id: response.data.id,
          username: response.data.username,
          firstName: response.data.firstName,
          lastName: response.data.lastName,
          email: response.data.email,
          role: response.data.role || 'użytkownik' 
        };

        console.log("Stan uwierzytelnienia po logowaniu:", authState);

        if (response.data.token) {
          authState.token = response.data.token;
          localStorage.setItem('auth_token', response.data.token);
        }

        return response.data;
      }

      throw new Error('Nieprawidłowa odpowiedź z serwera');
    } catch (error) {
      console.error('Błąd logowania:', error);
      if (error.response && error.response.data) {
        throw new Error(error.response.data.message || 'Błąd logowania');
      }
      throw new Error('Problem z połączeniem do serwera');
    }
  },

  /**
   * Sprawdzenie połączenia z backendem
   * @returns {Promise<boolean>}
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
    authState.isAuthenticated = false;
    authState.user = null;
    authState.token = null;
    localStorage.removeItem('auth_token');
  },

  /**
   * Zwraca poziom uprawnień użytkownika według hierarchii
   * @returns {number}
   */
  getUserRoleLevel() {
    const role = authState.user?.role;
    return roleHierarchy[role] || 0;
  },

  /**
   * Sprawdza, czy użytkownik ma uprawnienia co najmniej jakiejś roli
   * @param {string} minRole
   * @returns {boolean}
   */
  hasRoleAtLeast(minRole) {
    const requiredLevel = roleHierarchy[minRole] || 0;
    return this.getUserRoleLevel() >= requiredLevel;
  }
};

export default authService;
