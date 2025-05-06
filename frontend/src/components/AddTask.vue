<template>
  <div class="min-h-screen flex items-center justify-center bg-background text-text px-4">
    <div class="bg-surface p-6 rounded-lg shadow-md border border-gray-200 space-y-4 w-full max-w-2xl">
      <h1 class="text-2xl font-bold text-primary mb-4">Dodaj Zadanie</h1>

      <!-- Komunikaty -->
      <div v-if="successMessage" class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4">
        {{ successMessage }}
      </div>

      <div v-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4">
        {{ error }}
      </div>

      <!-- Diagnostyka - możesz usunąć po rozwiązaniu problemu -->
      <div v-if="showDebug" class="bg-gray-100 p-4 rounded-md mb-4">
        <h3 class="font-bold mb-2">Dane formularza:</h3>
        <pre class="text-xs overflow-auto">{{ JSON.stringify(task, null, 2) }}</pre>
        <button @click="showDebug = false" class="text-primary text-sm mt-2">Ukryj debugowanie</button>
      </div>

      <form @submit.prevent="addTask" class="space-y-4">
        <div class="flex flex-col">
          <label for="title" class="block text-lg font-medium mb-2">Tytuł zadania</label>
          <input
              v-model="task.title"
              id="title"
              type="text"
              required
              class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
              placeholder="np. Remont mieszkania ul. Załęska 76"
          />
        </div>

        <div class="flex flex-col">
          <label for="description" class="block text-lg font-medium mb-2">Opis zadania</label>
          <textarea
              v-model="task.description"
              id="description"
              rows="3"
              class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
              placeholder="np. Klient ma problemy z wilgocią, najprawdopodobniej po ostatniej powodzi"
          ></textarea>
        </div>

        <div class="flex flex-col">
          <label for="teamId" class="block text-lg font-medium mb-2">Zespół</label>
          <select
              v-model.number="task.teamId"
              id="teamId"
              required
              class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option disabled value="">Wybierz zespół</option>
            <option v-for="team in teams" :key="team.id" :value="team.id">{{ team.name }}</option>
          </select>
        </div>

        <div class="flex flex-col">
          <label for="priorityId" class="block text-lg font-medium mb-2">Priorytet</label>
          <select
              v-model.number="task.priorityId"
              id="priorityId"
              required
              class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option disabled value="">Wybierz priorytet</option>
            <option v-for="priority in priorities" :key="priority.id" :value="priority.id">{{ priority.name }}</option>
          </select>
        </div>

        <div class="flex flex-col">
          <label for="statusId" class="block text-lg font-medium mb-2">Status</label>
          <select
              v-model.number="task.statusId"
              id="statusId"
              required
              class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option disabled value="">Wybierz status</option>
            <option v-for="status in statuses" :key="status.id" :value="status.id">{{ status.name }}</option>
          </select>
        </div>

        <div class="flex flex-col">
          <label for="startDate" class="block text-lg font-medium mb-2">Data rozpoczęcia</label>
          <input
              v-model="task.startDate"
              id="startDate"
              type="date"
              required
              class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>

        <div class="flex flex-col">
          <label for="deadline" class="block text-lg font-medium mb-2">Deadline</label>
          <input
              v-model="task.deadline"
              id="deadline"
              type="date"
              required
              class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>

        <div class="flex justify-between mt-6">
          <button
              type="button"
              @click="showDebug = !showDebug"
              class="text-sm text-gray-500 underline"
          >
            {{ showDebug ? 'Ukryj dane' : 'Pokaż dane' }}
          </button>

          <div class="flex gap-4">
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
              <span v-else>Dodaj Zadanie</span>
            </button>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import taskService from '../services/taskService';
import teamService from '../services/teamService';
import { authState } from '../../router/router.js';

export default {
  setup() {
    const router = useRouter();

    // Dane zadania dopasowane do formatu JSON z backendu
    const task = ref({
      title: '',
      description: '',
      teamId: '',
      priorityId: '',
      statusId: '',
      startDate: '',
      deadline: ''
    });

    // Panel debugowania
    const showDebug = ref(false);

    // Dane do formularza
    const teams = ref([]);
    const priorities = ref([
      { id: 1, name: 'Niski' },
      { id: 2, name: 'Średni' },
      { id: 3, name: 'Wysoki' }
    ]);
    const statuses = ref([
      { id: 1, name: 'Rozpoczęte' },
      { id: 2, name: 'W toku' },
      { id: 3, name: 'Zakończone' }
    ]);

    // Stany komponentu
    const loading = ref(false);
    const error = ref('');
    const successMessage = ref('');

    // Inicjalizacja - pobranie danych zespołów
    onMounted(async () => {
      try {
        // Pobierz zespoły z API
        const fetchedTeams = await teamService.getAllTeams();
        teams.value = fetchedTeams;
      } catch (err) {
        console.error('Błąd podczas pobierania zespołów:', err);
        error.value = 'Nie udało się pobrać listy zespołów. Spróbuj odświeżyć stronę.';

        // Dane awaryjne w przypadku błędu
        teams.value = [
          { id: 1, name: 'Zespół remontowy' },
          { id: 2, name: 'Zespół instalacyjny' },
          { id: 3, name: 'Zespół projektowy' }
        ];
      }

      // Ustawienie domyślnej daty rozpoczęcia na dzisiaj
      const today = new Date();
      task.value.startDate = today.toISOString().split('T')[0];

      // Ustawienie domyślnego deadlinu na tydzień od dziś
      const nextWeek = new Date();
      nextWeek.setDate(nextWeek.getDate() + 7);
      task.value.deadline = nextWeek.toISOString().split('T')[0];
    });

    // Dodawanie zadania
    const addTask = async () => {
      error.value = '';
      successMessage.value = '';
      loading.value = true;

      // Sprawdzenie czy wszystkie wymagane pola są wypełnione
      if (!task.value.title || !task.value.teamId || !task.value.priorityId || !task.value.statusId) {
        error.value = 'Wszystkie pola wymagane (tytuł, zespół, priorytet, status) muszą być wypełnione.';
        loading.value = false;
        return;
      }

      // Walidacja dat
      if (task.value.startDate && task.value.deadline) {
        const startDate = new Date(task.value.startDate);
        const deadline = new Date(task.value.deadline);

        if (deadline < startDate) {
          error.value = 'Deadline nie może być wcześniejszy niż data rozpoczęcia.';
          loading.value = false;
          return;
        }
      }

      try {
        // Pobierz ID zalogowanego użytkownika
        const userId = authState.user?.id || 1; // Domyślnie 1, jeśli brak zalogowanego użytkownika

        // Logowanie do konsoli dla celów debugowania
        console.log('Wartości przed konwersją:', {
          teamId: task.value.teamId,
          priorityId: task.value.priorityId,
          statusId: task.value.statusId
        });

        // Przekształcenie danych do formatu API zgodnego z oczekiwanym JSON
        const taskData = {
          title: task.value.title,
          description: task.value.description || '', // Upewnienie się, że opis nie jest undefined
          teamId: task.value.teamId, // Nie używamy parseInt - v-model.number już konwertuje do liczby
          priorityId: task.value.priorityId,
          statusId: task.value.statusId,
          startDate: task.value.startDate,
          deadline: task.value.deadline,
          createdById: userId
        };

        // Logowanie do konsoli dla celów debugowania
        console.log('Dane zadania wysyłane do API:', taskData);

        // Zapis przez API
        const createdTask = await taskService.createTask(taskData);
        console.log('Zadanie zostało dodane:', createdTask);

        // Wyświetl komunikat sukcesu
        successMessage.value = 'Zadanie zostało pomyślnie dodane!';

        // Wyczyść formularz
        task.value = {
          title: '',
          description: '',
          teamId: '',
          priorityId: '',
          statusId: '',
          startDate: task.value.startDate, // Pozostaw bieżącą datę
          deadline: task.value.deadline // Pozostaw domyślny deadline
        };

        // Po 2 sekundach przekieruj do listy zadań
        setTimeout(() => {
          router.push('/tasks');
        }, 2000);

      } catch (err) {
        console.error('Błąd podczas dodawania zadania:', err);
        error.value = `Nie udało się dodać zadania: ${err.message}`;
      } finally {
        loading.value = false;
      }
    };

    // Powrót do poprzedniej strony
    const goBack = () => {
      router.back();
    };

    return {
      task,
      teams,
      priorities,
      statuses,
      loading,
      error,
      successMessage,
      showDebug,
      addTask,
      goBack
    };
  }
};
</script>