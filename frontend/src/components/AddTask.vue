<template>
  <div class="min-h-screen bg-background flex items-center justify-center px-4 py-10">
    <div class="w-full max-w-2xl bg-surface border border-gray-200 rounded-2xl shadow-xl p-8 space-y-6">
      <h1 class="text-3xl font-bold text-primary">Dodaj zadanie</h1>

      <!-- Komunikaty -->
      <div v-if="successMessage" class="p-4 bg-green-100 border border-green-300 text-green-800 rounded-lg">
        {{ successMessage }}
      </div>

      <div v-if="error" class="p-4 bg-red-100 border border-red-300 text-red-800 rounded-lg">
        {{ error }}
      </div>

      <form @submit.prevent="addTask" class="space-y-5">
        <div>
          <label for="title" class="block text-lg text-black font-medium mb-2">Tytuł zadania</label>
          <input
            v-model="task.title"
            id="title"
            type="text"
            required
            class="w-full p-2.5 border border-gray-300 rounded-md bg-white text-sm text-black placeholder-gray-400 focus:ring-2 focus:ring-primary focus:outline-none"
            placeholder="np. Remont mieszkania ul. Załęska 76"
          />
        </div>

        <div>
          <label for="description" class="block text-lg text-black font-medium mb-2">Opis zadania</label>
          <textarea
            v-model="task.description"
            id="description"
            rows="4"
            class="w-full p-2.5 border border-gray-300 rounded-md bg-white text-sm text-black placeholder-gray-400 focus:ring-2 focus:ring-primary focus:outline-none"
            placeholder="np. Klient ma problemy z wilgocią..."
          ></textarea>
        </div>

        <div>
          <label for="teamId" class="block text-lg text-black font-medium mb-2">Zespół</label>
          <select
            v-model="task.teamId"
            id="teamId"
            required
            class="w-full p-2.5 border border-gray-300 rounded-md bg-white text-sm text-black focus:ring-2 focus:ring-primary focus:outline-none"
          >
            <option disabled value="" class="text-gray-400">Wybierz zespół</option>
            <option v-for="team in teams" :key="team.id" :value="team.id">{{ team.name }}</option>
          </select>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label for="priorityId" class="block text-lg text-black font-medium mb-2">Priorytet</label>
            <select
              v-model="task.priorityId"
              id="priorityId"
              required
              class="w-full p-2.5 border border-gray-300 rounded-md bg-white text-sm text-black focus:ring-2 focus:ring-primary focus:outline-none"
            >
              <option disabled value="" class="text-gray-400">Wybierz priorytet</option>
              <option v-for="priority in priorities" :key="priority.id" :value="priority.id">{{ priority.name }}</option>
            </select>
          </div>

          <div>
            <label for="statusId" class="block text-lg text-black font-medium mb-2">Status</label>
            <select
              v-model="task.statusId"
              id="statusId"
              required
              class="w-full p-2.5 border border-gray-300 rounded-md bg-white text-sm text-black focus:ring-2 focus:ring-primary focus:outline-none"
            >
              <option disabled value="" class="text-gray-400">Wybierz status</option>
              <option v-for="status in statuses" :key="status.id" :value="status.id">{{ status.name }}</option>
            </select>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label for="startDate" class="block text-lg text-black font-medium mb-2">Data rozpoczęcia</label>
            <div class="relative">
              <input
                v-model="task.startDate"
                id="startDate"
                type="date"
                required
                class="w-full p-2.5 pl-10 cursor-pointer border border-gray-300 rounded-md bg-white text-sm text-black focus:ring-2 focus:ring-primary focus:outline-none"
              />
              <span class="absolute inset-y-0 left-3 flex items-center text-gray-500">
                <i class="fas fa-calendar-alt"></i> <!-- Ikona Font Awesome -->
              </span>
            </div>
          </div>

          <div>
            <label for="deadline" class="block text-lg text-black font-medium mb-2">Deadline</label>
            <div class="relative">
              <input
                v-model="task.deadline"
                id="deadline"
                type="date"
                required
                class="w-full p-2.5 pl-10 cursor-pointer border border-gray-300 rounded-md bg-white text-sm text-black focus:ring-2 focus:ring-primary focus:outline-none"
              />
              <span class="absolute inset-y-0 left-3 flex items-center text-gray-500">
                <i class="fas fa-calendar-alt"></i> <!-- Ikona Font Awesome -->
              </span>
            </div>
          </div>
        </div>

        <div class="flex justify-between pt-4">
          <button
            type="button"
            @click="goBack"
            class="px-5 py-2 rounded-md bg-gray-500 text-white text-sm hover:bg-gray-600 transition"
          >
            Anuluj
          </button>
          <button
            type="submit"
            class="px-6 py-2 rounded-md bg-primary hover:bg-secondary text-white text-sm font-semibold transition"
            :disabled="loading"
          >
            <span v-if="loading">Dodawanie...</span>
            <span v-else>Dodaj zadanie</span>
          </button>
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
      // Pola id, createdById i createdAt będą dodane przez backend
    });

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

        // Przekształcenie danych do formatu API zgodnego z oczekiwanym JSON
        const taskData = {
          title: task.value.title,
          description: task.value.description,
          teamId: parseInt(task.value.teamId), // Zapewnienie, że wartość jest liczbą
          priorityId: parseInt(task.value.priorityId),
          statusId: parseInt(task.value.statusId),
          startDate: task.value.startDate,
          deadline: task.value.deadline,
          createdById: userId // Dodanie ID twórcy zadania
          // createdAt zostanie dodane automatycznie przez backend
        };

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
          statusId: 1,
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
      addTask,
      goBack
    };
  }
};
</script>