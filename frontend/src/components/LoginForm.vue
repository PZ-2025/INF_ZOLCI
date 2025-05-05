<template>
  <div class="flex items-center justify-center h-full w-full bg-background">
    <div class="bg-surface p-8 rounded-2xl shadow-xl w-96 border border-gray-200">
      <h2 class="text-2xl font-bold text-center text-primary mb-6">BuildTask</h2>

      <form @submit.prevent="handleLogin">
        <!-- Komunikat błędu -->
        <div v-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4">
          {{ error }}
        </div>

        <div class="mb-4">
          <label for="username" class="block text-text font-semibold mb-2">Login</label>
          <input
              type="text"
              id="username"
              v-model="username"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white text-text"
              placeholder="Wpisz login"
              required
          >
        </div>

        <div class="mb-4">
          <label for="password" class="block text-text font-semibold mb-2">Hasło</label>
          <input
              type="password"
              id="password"
              v-model="password"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white text-text"
              placeholder="Wpisz hasło"
              required
          >
        </div>

        <button
            type="submit"
            class="w-full bg-primary hover:bg-secondary text-white font-bold py-2 rounded-lg transition"
            :disabled="loading"
        >
          <span v-if="loading">Logowanie...</span>
          <span v-else>Zaloguj</span>
        </button>
      </form>

      <!-- Przełącznik między logowaniem rzeczywistym a testowym -->
      <div class="mt-4 text-center">
        <button
            @click="toggleMockMode"
            class="text-sm text-primary hover:underline"
        >
          {{ useMockAuth ? "Przełącz na logowanie do backendu" : "Użyj konta testowego" }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { authState } from '../../router/router.js';
import authService from '../services/authService.js';

export default {
  setup() {
    const router = useRouter();
    const username = ref('');
    const password = ref('');
    const error = ref('');
    const loading = ref(false);
    const useMockAuth = ref(false);

    // Testowe konta do trybu lokalnego (bez backendu)
    const mockUsers = [
      {
        id: 1,
        username: 'jan',
        firstName: 'Jan',
        lastName: 'Kowalski',
        password: '1234',
        email: 'jan@example.com',
        role: 'employee'
      },
      {
        id: 2,
        username: 'manager',
        firstName: 'Admin',
        lastName: 'Manager',
        password: 'admin',
        email: 'manager@example.com',
        role: 'manager'
      },
    ];

    // Przełączanie między trybem testowym a rzeczywistym backendem
    const toggleMockMode = () => {
      useMockAuth.value = !useMockAuth.value;
      error.value = '';
    };

    // Obsługa logowania
    const handleLogin = async () => {
      // Sprawdź dane formularza
      if (!username.value || !password.value) {
        error.value = 'Proszę podać login i hasło';
        return;
      }

      error.value = '';
      loading.value = true;

      try {
        // Jeśli używamy trybu testowego
        if (useMockAuth.value) {
          const user = mockUsers.find(u =>
              u.username === username.value &&
              u.password === password.value
          );

          if (user) {
            console.log('Logowanie testowe udane:', user);

            // Aktualizacja stanu uwierzytelnienia
            authState.isAuthenticated = true;
            authState.user = {
              id: user.id,
              username: user.username,
              firstName: user.firstName,
              lastName: user.lastName,
              email: user.email,
              role: user.role
            };

            console.log('Stan uwierzytelnienia po logowaniu testowym:', authState);

            // Przekierowanie do strony głównej aplikacji
            await router.push('/teams');
          } else {
            error.value = 'Nieprawidłowy login lub hasło';
          }
        }
        // Logowanie przez backend
        else {
          // Sprawdź, czy backend jest dostępny
          const isConnected = await authService.checkConnection();

          if (!isConnected) {
            error.value = 'Serwer jest niedostępny. Użyj trybu testowego lub spróbuj ponownie później.';
            return;
          }

          // Logowanie przez backend
          await authService.login({
            username: username.value,
            password: password.value
          });

          console.log('Logowanie przez backend udane');

          // Przekierowanie do strony głównej aplikacji
          await router.push('/teams');
        }
      } catch (err) {
        console.error('Błąd logowania:', err);
        error.value = err.message || 'Błąd logowania. Spróbuj ponownie.';
      } finally {
        loading.value = false;
      }
    };

    return {
      username,
      password,
      error,
      loading,
      useMockAuth,
      handleLogin,
      toggleMockMode
    };
  }
};
</script>