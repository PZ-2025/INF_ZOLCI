<template>
  <div class="min-h-screen bg-background flex items-center justify-center px-4 py-10">
    <div class="w-full max-w-2xl bg-surface border border-gray-200 rounded-2xl shadow-xl p-8 space-y-6">
      <h1 class="text-3xl font-bold text-primary">Dodaj zadanie</h1>

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
            v-model.number="task.teamId"
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
              v-model.number="task.priorityId"
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
              v-model.number="task.statusId"
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
          <div class="flex gap-4">
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
        </div>
      </form>
    </div>
  </div>
  
  <!-- Status Modal -->
  <StatusModal
    :show="showModal"
    :type="modalConfig.type"
    :title="modalConfig.title"
    :message="modalConfig.message"
    :button-text="modalConfig.buttonText"
    :auto-close="modalConfig.autoClose"
    :auto-close-delay="modalConfig.autoCloseDelay"
    @close="hideModal"
  />
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import taskService from '../services/taskService';
import teamService from '../services/teamService';
import { authState } from '../../router/router.js';
import StatusModal from './StatusModal.vue';
import { useStatusModal } from '../composables/useStatusModal';

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

// Użycie composable do obsługi modalu
const { showModal, modalConfig, showStatus, hideModal } = useStatusModal();

// Inicjalizacja - pobranie danych zespołów
onMounted(async () => {
  try {
    // Pobierz zespoły z API
    const fetchedTeams = await teamService.getAllTeams();
    teams.value = fetchedTeams;
  } catch (err) {
    console.error('Błąd podczas pobierania zespołów:', err);
    showStatus({
      type: 'error',
      title: 'Błąd',
      message: 'Nie udało się pobrać listy zespołów. Spróbuj odświeżyć stronę.',
      buttonText: 'Zamknij'
    });

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
  loading.value = true;

  // Sprawdzenie czy wszystkie wymagane pola są wypełnione 
  if (!task.value.title || !task.value.teamId || !task.value.priorityId || !task.value.statusId) {
    showStatus({
      type: 'error',
      title: 'Błąd',
      message: 'Wszystkie pola wymagane (tytuł, zespół, priorytet, status) muszą być wypełnione.',
      buttonText: 'Zamknij'
    });
    loading.value = false;
    return;
  }

  // Walidacja dat
  if (task.value.startDate && task.value.deadline) {
    const startDate = new Date(task.value.startDate);
    const deadline = new Date(task.value.deadline);

    if (deadline < startDate) {
      showStatus({
        type: 'error',
        title: 'Błąd',
        message: 'Deadline nie może być wcześniejszy niż data rozpoczęcia.',
        buttonText: 'Zamknij'
      });
      loading.value = false;
      return;
    }
  }

  try {
    // Pobierz ID zalogowanego użytkownika
    const userId = authState.user?.id || 1; // Domyślnie 1, jeśli brak zalogowanego użytkownika

    // Logowanie do konsoli dla celów debugowania (z wersji main)
    console.log('Wartości przed konwersją:', {
      teamId: task.value.teamId,
      priorityId: task.value.priorityId,
      statusId: task.value.statusId
    });

    // Przekształcenie danych do formatu API zgodnego z oczekiwanym JSON
    const taskData = {
      title: task.value.title,
      description: task.value.description || '', // Upewnienie się, że opis nie jest undefined
      teamId: task.value.teamId, // v-model.number już konwertuje do liczby
      priorityId: task.value.priorityId,
      statusId: task.value.statusId,
      startDate: task.value.startDate,
      deadline: task.value.deadline,
      createdById: userId // Dodanie ID twórcy zadania
    };

    // Zapis przez API
    const createdTask = await taskService.createTask(taskData);
    console.log('Zadanie zostało dodane:', createdTask);

    // Wyczyść formularz
    task.value = {
      title: '',
      description: '',
      teamId: '',
      priorityId: '',
      statusId: 1, // Domyślny status "Rozpoczęte"
      startDate: task.value.startDate, // Pozostaw bieżącą datę
      deadline: task.value.deadline // Pozostaw domyślny deadline
    };

    // Wyświetl komunikat sukcesu i przekieruj po zamknięciu
    showStatus({
      type: 'success',
      title: 'Sukces',
      message: 'Zadanie zostało pomyślnie dodane!',
      buttonText: 'OK',
      autoClose: true,
      autoCloseDelay: 2000,
      onClose: () => router.push('/taskshistory')
    });

  } catch (err) {
    console.error('Błąd podczas dodawania zadania:', err);
    showStatus({
      type: 'error',
      title: 'Błąd',
      message: `Nie udało się dodać zadania: ${err.message}`,
      buttonText: 'Zamknij'
    });
  } finally {
    loading.value = false;
  }
};

// Powrót do poprzedniej strony
const goBack = () => {
  router.back();
};
</script>