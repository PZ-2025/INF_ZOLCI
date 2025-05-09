<template>
  <div class="min-h-screen flex items-center justify-center bg-background text-text px-4">
    <div class="bg-surface p-6 rounded-lg shadow-md border border-gray-200 space-y-6 w-full max-w-2xl">
      <h1 class="text-2xl font-bold text-primary mb-4">Dodaj Zespół</h1>

      <!-- Komunikaty -->
      <div v-if="successMessage" class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4">
        {{ successMessage }}
      </div>

      <div v-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4">
        {{ error }}
      </div>

      <form @submit.prevent="addTeam" class="space-y-4">
        <!-- Nazwa zespołu -->
        <div>
          <label for="teamName" class="block text-lg font-medium mb-2">Nazwa zespołu</label>
          <input
            id="teamName"
            v-model="team.name"
            type="text"
            required
            placeholder="np. Zespół remontowy"
            class="p-2 border border-gray-300 rounded-md w-full bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>

        <!-- Opis zespołu -->
        <div>
          <label for="teamDescription" class="block text-lg font-medium mb-2">Opis</label>
          <textarea
            id="teamDescription"
            v-model="team.description"
            class="p-2 border border-gray-300 rounded-md w-full bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          ></textarea>
        </div>

        <!-- Kierownik zespołu -->
        <div>
          <label for="teamManager" class="block text-lg font-medium mb-2">Kierownik zespołu</label>
          <select
            id="teamManager"
            v-model="team.managerId"
            required
            class="p-2 border border-gray-300 rounded-md w-full bg-white text-black focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option disabled selected value="">Wybierz kierownika</option>
            <option v-for="manager in managers" :key="manager.id" :value="manager.id">
              {{ manager.firstName }} {{ manager.lastName }}
            </option>
          </select>
        </div>

        <!-- Członkowie zespołu -->
        <div>
          <label for="teamMembers" class="block text-lg font-medium mb-2">Członkowie</label>
          <div id="teamMembers" class="space-y-2 max-h-40 overflow-y-auto border border-gray-300 rounded-md p-2 bg-white">
            <div v-for="employee in employees" :key="employee.id" class="flex items-center space-x-2">
              <input
                type="checkbox"
                v-model="selectedMembers"
                :value="employee.id"
                :id="`employee-${employee.id}`"
                class="form-checkbox h-5 w-5 text-primary focus:ring-primary"
              />
              <label :for="`employee-${employee.id}`" class="text-text">{{ employee.firstName }} {{ employee.lastName }} ({{ employee.email }})</label>
            </div>
          </div>
        </div>

        <!-- Przyciski -->
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

    const team = ref({
      name: '',
      description: '',
      managerId: ''
    });

    const managers = ref([]);
    const employees = ref([]);
    const selectedMembers = ref([]);

    const loading = ref(false);
    const error = ref('');
    const successMessage = ref('');

    // Pobranie listy kierowników
    const fetchManagers = async () => {
      try {
        const users = await userService.getActiveUsers();
        managers.value = users.filter(user => user.role === 'manager' || user.role === 'admin');
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

    // Pobranie listy wszystkich pracowników
    const fetchEmployees = async () => {
      try {
        const users = await userService.getActiveUsers();
        employees.value = users;
        console.log('Pobrano pracowników:', employees.value);
      } catch (err) {
        console.error('Błąd podczas pobierania pracowników:', err);
        error.value = 'Nie udało się pobrać listy pracowników. Spróbuj odświeżyć stronę.';
        
        // Dane awaryjne w przypadku błędu
        employees.value = [
          { id: 1, firstName: 'Jan', lastName: 'Kowalski', email: 'jan.kowalski@firma.pl' },
          { id: 2, firstName: 'Fifonż', lastName: 'Wiśniewski', email: 'fifonz.wisniewski@firma.pl' },
          { id: 3, firstName: 'Anna', lastName: 'Nowak', email: 'anna.nowak@firma.pl' },
          { id: 4, firstName: 'Piotr', lastName: 'Dąbrowski', email: 'piotr.dabrowski@firma.pl' }
        ];
      }
    };

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
          description: team.value.description?.trim() || '',
          managerId: parseInt(team.value.managerId), // Upewniamy się, że to liczba
          memberIds: selectedMembers.value
        };

        // Wysłanie danych do API
        const createdTeam = await teamService.createTeam(teamData);
        console.log('Zespół został utworzony:', createdTeam);
        
        // Wyświetl komunikat sukcesu
        successMessage.value = 'Zespół został pomyślnie utworzony!';

        // Wyczyść formularz
        team.value = {
          name: '',
          description: '',
          managerId: ''
        };
        selectedMembers.value = [];

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

    const goBack = () => {
      router.back();
    };

    onMounted(() => {
      fetchManagers();
      fetchEmployees();
    });

    return {
      team,
      managers,
      employees,
      selectedMembers,
      loading,
      error,
      successMessage,
      addTeam,
      goBack
    };
  }
};
</script>