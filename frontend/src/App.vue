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

    // Jeśli połączenie działa, resetuj błędy
    if (connected) {
      connectionErrorType.value = null;
      lastError.value = null;
      isDatabaseError.value = false;
    }

    return connected;
  } catch (error) {
    console.error("Błąd podczas próby połączenia:", error);
    backendStatus.value = 'disconnected';
    return false;
  } finally {
    retryInProgress.value = false;
    console.log("Próba ponownego połączenia - zakończona");
  }
}

const ERROR_TYPES = {
  DATABASE: 'DATABASE_ERROR',
  NETWORK: 'NETWORK_ERROR',
  TIMEOUT: 'TIMEOUT_ERROR',
  SERVER: 'SERVER_ERROR',
  UNKNOWN: 'UNKNOWN_ERROR'
};

// Dodatkowe zmienne stanu dla obsługi błędów
const connectionErrorType = ref(null);
const lastError = ref(null);
const isDatabaseError = ref(false);

// Funkcja do uzyskania przyjaznego komunikatu błędu
function getFriendlyErrorMessage() {
  if (isDatabaseError.value) {
    return "Serwer ma problemy z połączeniem do bazy danych. Spróbuj ponownie za kilka minut.";
  } else if (connectionErrorType.value === ERROR_TYPES.NETWORK) {
    return "Nie można połączyć się z serwerem API. Sprawdź swoje połączenie sieciowe.";
  } else if (connectionErrorType.value === ERROR_TYPES.TIMEOUT) {
    return "Upłynął limit czasu odpowiedzi z serwera. Spróbuj ponownie.";
  } else if (connectionErrorType.value === ERROR_TYPES.SERVER) {
    return "Wystąpił błąd po stronie serwera. Prosimy spróbować później.";
  } else {
    return "Nie można połączyć się z serwerem API. Prosimy spróbować później.";
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

      // Sprawdź specyficzne kody statusu
      if (response.ok) {
        console.log("Połączenie z backendem działa");
        backendStatus.value = 'connected';
        connectionErrorType.value = null;
        isDatabaseError.value = false;
        lastError.value = null;
      } else {
        console.warn("Backend zwrócił błąd:", response.status);
        backendStatus.value = 'disconnected';

        // Sprawdź czy to błąd bazy danych (503)
        if (response.status === 503) {
          connectionErrorType.value = ERROR_TYPES.DATABASE;
          isDatabaseError.value = true;
          console.warn("Wykryto problem z bazą danych (503)");
        } else if (response.status >= 500) {
          connectionErrorType.value = ERROR_TYPES.SERVER;
          isDatabaseError.value = false;
        } else {
          connectionErrorType.value = ERROR_TYPES.UNKNOWN;
          isDatabaseError.value = false;
        }

        // Zapisz odpowiedź do analizy
        try {
          const errorData = await response.json();
          lastError.value = {
            status: response.status,
            message: errorData?.message || 'Błąd połączenia z serwerem',
            data: errorData
          };
        } catch (e) {
          lastError.value = {
            status: response.status,
            message: 'Błąd połączenia z serwerem'
          };
        }
      }
    } catch (error) {
      console.error("Nie można połączyć się z backendem:", error);
      backendStatus.value = 'disconnected';

      // Klasyfikuj błędy połączenia
      if (error.name === 'AbortError') {
        connectionErrorType.value = ERROR_TYPES.TIMEOUT;
      } else if (!navigator.onLine) {
        connectionErrorType.value = ERROR_TYPES.NETWORK;
      } else {
        connectionErrorType.value = ERROR_TYPES.UNKNOWN;
      }

      isDatabaseError.value = false;
      lastError.value = error;
    }
  } catch (error) {
    console.error("Błąd podczas sprawdzania połączenia:", error);
    backendStatus.value = 'disconnected';
    connectionErrorType.value = ERROR_TYPES.UNKNOWN;
    lastError.value = error;
  }

  console.log("Sprawdzanie połączenia - zakończone, status:", backendStatus.value);
  return backendStatus.value === 'connected';
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

    <!-- Komponent wyświetlający błąd API -->
    <div
      v-if="backendStatus === 'disconnected'"
      class="fixed top-20 right-5 z-50 w-full max-w-md bg-white border border-red-300 shadow-2xl rounded-xl overflow-hidden animate-fade-in"
    >
      <!-- Nagłówek z gradientem -->
      <div class="bg-gradient-to-r from-red-500 to-red-600 px-4 py-3 flex items-center text-white">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path v-if="isDatabaseError" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 11l7-7 7 7M5 19l7-7 7 7" />
          <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
        <h3 class="text-lg font-semibold">
          {{ isDatabaseError ? 'Błąd bazy danych' : 'Brak połączenia z API' }}
        </h3>
      </div>

      <!-- Treść -->
      <div class="p-4 space-y-3 text-sm text-gray-800">
        <p>{{ getFriendlyErrorMessage() }}</p>

        <div v-if="isDatabaseError" class="bg-yellow-50 border-l-4 border-yellow-400 p-3 rounded text-yellow-800">
          Serwer ma obecnie problem z połączeniem z bazą danych. Prosimy o cierpliwość — sytuacja powinna się wkrótce poprawić.
        </div>

        <div class="flex justify-end mt-4">
          <button
            @click="retryConnection"
            class="bg-blue-500 hover:bg-blue-600 text-white font-medium px-4 py-2 rounded-lg transition duration-200"
            :disabled="retryInProgress || backendStatus === 'checking'"
          >
            <span v-if="retryInProgress || backendStatus === 'checking'">Próbuję...</span>
            <span v-else>Spróbuj ponownie</span>
          </button>
        </div>
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