<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { authState } from '../router/router.js';
import Navbar from './components/Navbar.vue';
import LoginForm from './components/LoginForm.vue';
import apiService from './services/api.js';

// Pobierz URL z pliku .env
const API_URL = process.env.API_URL || 'http://localhost:8080';

const backendStatus = ref('checking');
const retryInProgress = ref(false);

// Funkcja do ponownego próbowania połączenia
async function retryConnection() {
  console.log("Próba ponownego połączenia - rozpoczęta");
  retryInProgress.value = true;

  try {
    const connected = await apiService.retry();
    console.log("Wynik próby połączenia:", connected);
    backendStatus.value = connected ? 'connected' : 'disconnected';
  } catch (error) {
    console.error("Błąd podczas próby połączenia:", error);
    backendStatus.value = 'disconnected';
  } finally {
    retryInProgress.value = false;
    console.log("Próba ponownego połączenia - zakończona");
  }
}

// Funkcja sprawdzająca połączenie
async function checkBackendConnection() {
  console.log("Sprawdzanie połączenia - rozpoczęte");
  backendStatus.value = 'checking';

  try {
    // Sprawdź połączenie za pomocą endpointu /database/users
    const checkUrl = `${API_URL}/database/users`;

    try {
      // Timeout na 3 sekundy
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 3000);

      // Próba połączenia
      const response = await fetch(checkUrl, { signal: controller.signal });
      clearTimeout(timeoutId);

      // Sprawdź czy odpowiedź jest OK
      if (response.ok) {
        console.log("Połączenie z backendem działa");
        backendStatus.value = 'connected';
      } else {
        console.warn("Backend zwrócił błąd:", response.status);
        backendStatus.value = 'disconnected';
      }
    } catch (error) {
      console.error("Nie można połączyć się z backendem:", error);
      backendStatus.value = 'disconnected';
    }
  } catch (error) {
    console.error("Błąd podczas sprawdzania połączenia:", error);
    backendStatus.value = 'disconnected';
  }

  console.log("Sprawdzanie połączenia - zakończone, status:", backendStatus.value);
}

onMounted(async () => {
  console.log("Vue app loaded");

  // Sprawdź połączenie
  await checkBackendConnection();

  // Ustaw timer na sprawdzanie co 10 sekund
  const intervalId = setInterval(checkBackendConnection, 10000);

  // Będziemy czyścili ten interwał gdy komponent zostanie zniszczony
  onUnmounted(() => {
    clearInterval(intervalId);
  });
});
</script>

<template>
  <div class="flex h-screen w-screen">
<!--    &lt;!&ndash; Pasek ostrzeżenia na górze &ndash;&gt;-->
<!--    <div-->
<!--        v-if="backendStatus === 'disconnected'"-->
<!--        class="fixed top-0 left-0 right-0 bg-warning text-red-800 p-1 text-center text-sm z-50 flex items-center justify-center"-->
<!--    >-->
<!--      <span>⚠️ Brak połączenia z backendem. Aplikacja działa w trybie demonstracyjnym z przykładowymi danymi.</span>-->

<!--      <button-->
<!--          @click="retryConnection"-->
<!--          class="ml-4 px-2 py-0.5 bg-blue-500 text-white rounded text-xs hover:bg-blue-600 focus:outline-none"-->
<!--          :disabled="retryInProgress || backendStatus === 'checking'"-->
<!--      >-->
<!--        <span v-if="retryInProgress || backendStatus === 'checking'">-->
<!--          Łączenie...-->
<!--        </span>-->
<!--        <span v-else>-->
<!--          Połącz ponownie-->
<!--        </span>-->
<!--      </button>-->
<!--    </div>-->

    <!-- Komponent wyświetlający błąd API (tylko jeśli użytkownik nie jest zalogowany) -->
    <div v-if="backendStatus === 'disconnected' && !authState.isAuthenticated" class="fixed top-20 right-5 z-40 bg-white shadow-lg rounded-lg p-4 max-w-sm">
      <div class="flex flex-col">
        <div class="flex items-center text-red-600 mb-2">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
          <h3 class="font-bold">Problem z połączeniem</h3>
        </div>

        <p class="text-gray-700 mb-4">Nie można połączyć się z serwerem API. Możesz korzystać z aplikacji w trybie demonstracyjnym lub spróbować ponownie.</p>

        <button
            @click="retryConnection"
            class="bg-blue-500 hover:bg-blue-600 text-white py-2 px-4 rounded self-end"
            :disabled="retryInProgress || backendStatus === 'checking'"
        >
          <span v-if="retryInProgress || backendStatus === 'checking'">
            Próbuję...
          </span>
          <span v-else>
            Spróbuj ponownie
          </span>
        </button>
      </div>
    </div>

    <!-- Login Form zawsze wyświetlany dla niezalogowanych użytkowników -->
    <LoginForm v-if="!authState.isAuthenticated" />

    <!-- Panel aplikacji dla zalogowanych użytkowników -->
    <div v-else class="flex h-screen w-screen">
      <Navbar class="w-64 bg-gray-800 text-white h-full flex-shrink-0" />
      <div class="flex-1 overflow-auto bg-gray-100">
        <router-view />
      </div>
    </div>
  </div>
</template>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
}

.bg-warning {
  background-color: #fff3cd;
}
</style>