<template>
  <div class="flex items-center justify-center h-full w-full bg-background">
    <div class="bg-surface p-8 rounded-2xl shadow-xl w-96 border border-gray-200">
      <img src="/src/assets/buildtask_logo_full.png" alt="BuildTask" />
      <form @submit.prevent="handleLogin">
        <div class="mb-4">
          <label for="username" class="block text-text font-semibold mb-2">Login</label>
          <input
              type="text"
              id="username"
              v-model="credentials.username"
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
              v-model="credentials.password"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white text-text"
              placeholder="Wpisz hasło"
              required
          >
        </div>

        <p v-if="errorMessage" class="text-red-500 text-sm mb-4">{{ errorMessage }}</p>

        <p v-if="connectionError" class="text-amber-500 text-sm mb-4">{{ connectionError }}</p>
        <button
            type="submit"
            class="w-full bg-primary hover:bg-secondary text-white font-bold py-2 rounded-lg transition"
            :disabled="isLoggingIn"
        >
          <span v-if="isLoggingIn">Logowanie...</span>
          <span v-else>Zaloguj</span>
        </button>
      </form>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import authService from '../services/authService';

export default {
  setup() {
    const router = useRouter();
    const credentials = ref({ username: '', password: '' });
    const errorMessage = ref('');
    const connectionError = ref('');
    const isLoggingIn = ref(false);
    let intervalId = null;

    const checkBackendConnection = async () => {
      try {
        const isConnected = await authService.checkConnection();
        if (!isConnected) {
          connectionError.value = 'Problem z połączeniem do serwera. Niektóre funkcje mogą być niedostępne.';
        } else {
          connectionError.value = '';
        }
      } catch (error) {
        connectionError.value = 'Nie można połączyć się z serwerem. Spróbuj ponownie później.';
        console.error('Błąd podczas sprawdzania połączenia:', error);
      }
    };

    onMounted(() => {
      checkBackendConnection(); 
      intervalId = setInterval(checkBackendConnection, 2000); 
    });

    onUnmounted(() => {
      clearInterval(intervalId); 
    });

    const handleLogin = async () => {
      errorMessage.value = '';
      isLoggingIn.value = true;

      try {
        await authService.login(credentials.value);
        router.push('/teams');
      } catch (error) {
        errorMessage.value = error.message || 'Nieprawidłowy login lub hasło';
        console.error('Błąd logowania:', error);

        if (error.message.includes('Problem z połączeniem')) {
          await checkBackendConnection();
        }
      } finally {
        isLoggingIn.value = false;
      }
    };

    return {
      credentials,
      errorMessage,
      connectionError,
      isLoggingIn,
      handleLogin
    };
  }
};

</script>