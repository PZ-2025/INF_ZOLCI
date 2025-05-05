<template>
  <div class="min-h-screen flex items-center justify-center bg-background text-text px-4">
    <div class="bg-surface p-6 rounded-lg shadow-md border border-gray-200 space-y-4 w-full max-w-2xl">
      <h1 class="text-2xl font-bold text-primary mb-4">Dodaj Pracownika</h1>

      <!-- Komunikaty -->
      <div v-if="successMessage" class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4">
        {{ successMessage }}
      </div>

      <div v-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4">
        {{ error }}
      </div>

      <form @submit.prevent="addEmployee" class="space-y-4">
        <div class="flex flex-col">
          <label for="firstName" class="block text-lg font-medium mb-2">Imię</label>
          <input
              v-model="user.firstName"
              id="firstName"
              type="text"
              required
              class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
              placeholder="Jan"
          />
        </div>

        <div class="flex flex-col">
          <label for="lastName" class="block text-lg font-medium mb-2">Nazwisko</label>
          <input
              v-model="user.lastName"
              id="lastName"
              type="text"
              required
              class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
              placeholder="Kowalski"
          />
        </div>

        <div class="flex flex-col">
          <label for="email" class="block text-lg font-medium mb-2">Email</label>
          <input
              v-model="user.email"
              id="email"
              type="email"
              required
              class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
              placeholder="jan.kowalski@example.com"
          />
        </div>

        <div class="flex flex-col">
          <label for="username" class="block text-lg font-medium mb-2">Nazwa użytkownika</label>
          <input
              v-model="user.username"
              id="username"
              type="text"
              required
              class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
              placeholder="jan.kowalski"
          />
        </div>

        <div class="flex flex-col">
          <label for="password" class="block text-lg font-medium mb-2">Hasło</label>
          <input
              v-model="user.password"
              id="password"
              type="password"
              required
              class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
              placeholder="Minimum 6 znaków"
              minlength="6"
          />
        </div>

        <div class="flex flex-col">
          <label for="role" class="block text-lg font-medium mb-2">Rola</label>
          <select
              v-model="user.role"
              id="role"
              required
              class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="employee">Pracownik</option>
            <option value="manager">Kierownik</option>
            <option value="admin">Administrator</option>
          </select>
        </div>

        <div class="flex justify-between mt-6">
          <button
              type="button"
              @click="goBack"
              class="bg-gray-500 hover:bg-gray-600 text-white px-6 py-2 rounded-md transition"
          >
            Anuluj
          </button>
          <button
              type="submit"
              class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-md transition"
              :disabled="loading"
          >
            <span v-if="loading">Dodawanie...</span>
            <span v-else>Dodaj Pracownika</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import userService from '../services/userService';

export default {
  setup() {
    const router = useRouter();

    // Dane użytkownika
    const user = ref({
      firstName: '',
      lastName: '',
      email: '',
      username: '',
      password: '',
      role: 'employee',
      isActive: true
    });

    // Stany komponentu
    const loading = ref(false);
    const error = ref('');
    const successMessage = ref('');

    // Dodawanie pracownika
    const addEmployee = async () => {
      loading.value = true;
      error.value = '';
      successMessage.value = '';

      try {
        // Walidacja
        if (user.value.password.length < 6) {
          error.value = 'Hasło musi mieć co najmniej 6 znaków';
          loading.value = false;
          return;
        }

        // Przekształcenie danych do formatu API
        const userData = {
          firstName: user.value.firstName,
          lastName: user.value.lastName,
          email: user.value.email,
          username: user.value.username,
          password: user.value.password,
          role: user.value.role,
          isActive: user.value.isActive
        };

        // Utworzenie użytkownika przez API
        const createdUser = await userService.createUser(userData);
        console.log('Pracownik został dodany:', createdUser);

        // Wyświetl komunikat sukcesu
        successMessage.value = 'Pracownik został pomyślnie dodany!';

        // Wyczyść formularz
        user.value = {
          firstName: '',
          lastName: '',
          email: '',
          username: '',
          password: '',
          role: 'employee',
          isActive: true
        };

        // Po 2 sekundach przekieruj do listy pracowników
        setTimeout(() => {
          router.push('/employees');
        }, 2000);

      } catch (err) {
        console.error('Błąd podczas dodawania pracownika:', err);
        error.value = `Nie udało się dodać pracownika: ${err.message}`;
      } finally {
        loading.value = false;
      }
    };

    // Powrót do poprzedniej strony
    const goBack = () => {
      router.back();
    };

    return {
      user,
      loading,
      error,
      successMessage,
      addEmployee,
      goBack
    };
  }
};
</script>