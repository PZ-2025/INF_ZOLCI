<template>
  <div class="min-h-screen flex items-center justify-center bg-background text-text px-4">
    <div class="bg-surface p-6 rounded-lg shadow-md border border-gray-200 space-y-6 w-full max-w-xl">
      <h1 class="text-2xl font-bold text-primary mb-2">Dodaj Zespół</h1>

      <!-- Komunikaty -->
      <div v-if="successMessage" class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4">
        {{ successMessage }}
      </div>

      <div v-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4">
        {{ error }}
      </div>

      <form @submit.prevent="addTeam" class="space-y-6">
        <div class="flex flex-col">
          <label for="teamName" class="text-lg font-medium mb-2">Nazwa zespołu:</label>
          <input
              v-model="team.name"
              id="teamName"
              type="text"
              placeholder="np. Zespół remontowy"
              required
              class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>

        <div class="flex flex-col">
          <label for="manager" class="text-lg font-medium mb-2">Kierownik:</label>
          <select
              v-model="team.managerId"
              id="manager"
              required
              class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option disabled selected value="">Wybierz kierownika</option>
            <option v-for="manager in managers" :key="manager.id" :value="manager.id">
              {{ manager.firstName }} {{ manager.lastName }}
            </option>
          </select>
        </div>

        <div class="flex justify-between pt-4">
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
            <span v-else>Dodaj Zespół</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import teamService from '../services/teamService';
import userService from '../services/userService';

export default {
  setup() {
    const router = useRouter();

    // Dane zespołu
    const team = ref({
      name: '',
      managerId: '' // używamy camelCase zamiast snake_case dla zgodności z API
    });

    // Dane kierowników
    const managers = ref([]);

    // Stany komponentu
    const loading = ref(false);
    const error = ref('');
    const successMessage = ref('');

    // Pobranie listy kierowników z API
    const fetchManagers = async () => {
      try {
        // Pobierz tylko aktywnych użytkowników (dla kierowników muszą być aktywni)
        const users = await userService.getActiveUsers();

        // Filtruj tylko użytkowników z rolą kierownika lub admina
        managers.value = users.filter(user =>
            user.role === 'manager' || user.role === 'admin'
        );

        console.log('Pobrano kierowników:', managers.value);
      } catch (err) {
        console.error('Błąd podczas pobierania kierowników:', err);
        error.value = 'Nie udało się pobrać listy kierowników. Spróbuj odświeżyć stronę.';

        // Dane awaryjne w przypadku błędu
        managers.value = [
          { id: 1, firstName: 'Jan', lastName: 'Kowalski' },
          { id: 2, firstName: 'Fifonż', lastName: 'Wiśniewski' },
          { id: 3, firstName: 'Anna', lastName: 'Nowak' }
        ];
      }
    };

    // Dodawanie zespołu
    const addTeam = async () => {
      loading.value = true;
      error.value = '';
      successMessage.value = '';

      // Walidacja danych
      if (!team.value.name.trim()) {
        error.value = 'Nazwa zespołu jest wymagana';
        loading.value = false;
        return;
      }

      if (!team.value.managerId) {
        error.value = 'Wybór kierownika jest wymagany';
        loading.value = false;
        return;
      }

      try {
        // Przygotowanie danych do wysłania
        const teamData = {
          name: team.value.name.trim(),
          managerId: parseInt(team.value.managerId) // Upewniamy się, że to liczba
        };

        // Wysłanie danych do API
        const createdTeam = await teamService.createTeam(teamData);
        console.log('Zespół został utworzony:', createdTeam);

        // Wyświetl komunikat sukcesu
        successMessage.value = 'Zespół został pomyślnie utworzony!';

        // Wyczyść formularz
        team.value = {
          name: '',
          managerId: ''
        };

        // Po 2 sekundach przekieruj do listy zespołów
        setTimeout(() => {
          router.push('/teams');
        }, 2000);

      } catch (err) {
        console.error('Błąd podczas tworzenia zespołu:', err);
        error.value = `Nie udało się utworzyć zespołu: ${err.message}`;
      } finally {
        loading.value = false;
      }
    };

    // Powrót do poprzedniej strony
    const goBack = () => {
      router.back();
    };

    // Inicjalizacja komponentu
    onMounted(() => {
      fetchManagers();
    });

    return {
      team,
      managers,
      loading,
      error,
      successMessage,
      addTeam,
      goBack
    };
  }
};
</script>