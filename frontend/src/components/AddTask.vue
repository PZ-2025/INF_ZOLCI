<template>
  <div class="min-h-screen bg-background flex flex-col items-start justify-start px-10 py-10 space-y-8">
    <!-- Nagłówek -->
    <h1 class="text-3xl font-bold text-primary">Dodaj zadanie</h1>

    <!-- Loader -->
    <div v-if="dataLoading" class="text-primary text-xl">Ładowanie danych...</div>

    <!-- Formularz -->
    <form v-else @submit.prevent="addTask" class="space-y-5 w-full max-w-3xl">
      
      <!-- Pole: Tytuł -->
      <div class="flex items-center space-x-4">
        <label for="title" class="w-48 text-black text-lg font-medium">Tytuł zadania</label>
        <input
          v-model="task.title"
          id="title"
          type="text"
          required
          class="flex-1 p-2.5 border border-gray-300 rounded-md bg-white text-sm text-black placeholder-gray-400 focus:ring-2 focus:ring-primary focus:outline-none"
          placeholder="np. Remont mieszkania ul. Załęska 76"
        />
      </div>

      <!-- Pole: Opis -->
      <div class="flex items-start space-x-4">
        <label for="description" class="w-48 text-black text-lg font-medium pt-2">Opis zadania</label>
        <textarea
          v-model="task.description"
          id="description"
          rows="4"
          class="flex-1 p-2.5 border border-gray-300 rounded-md bg-white text-sm text-black placeholder-gray-400 focus:ring-2 focus:ring-primary focus:outline-none"
          placeholder="np. Klient ma problemy z wilgocią..."
        ></textarea>
      </div>

      <!-- Pole: Zespół -->
      <div class="flex items-center space-x-4">
        <label for="teamId" class="w-48 text-black text-lg font-medium">Zespół</label>
        <select
          v-model.number="task.teamId"
          id="teamId"
          required
          class="flex-1 p-2.5 border border-gray-300 rounded-md bg-white text-sm text-black focus:ring-2 focus:ring-primary focus:outline-none"
        >
          <option disabled value="">Wybierz zespół</option>
          <option v-for="team in teams" :key="team.id" :value="team.id">{{ team.name }}</option>
        </select>
      </div>

      <!-- Priorytet i Status -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div class="flex items-center space-x-4">
          <label for="priorityId" class="w-48 text-black text-lg font-medium">Priorytet</label>
          <select
            v-model.number="task.priorityId"
            id="priorityId"
            required
            class="flex-1 p-2.5 border border-gray-300 rounded-md bg-white text-sm text-black focus:ring-2 focus:ring-primary focus:outline-none"
          >
            <option disabled value="">Wybierz priorytet</option>
            <option v-for="priority in priorities" :key="priority.id" :value="priority.id">{{ priority.name }}</option>
          </select>
        </div>

        <div class="flex items-center space-x-4">
          <label for="statusId" class="w-48 text-black text-lg font-medium">Status</label>
          <select
            v-model.number="task.statusId"
            id="statusId"
            required
            class="flex-1 p-2.5 border border-gray-300 rounded-md bg-white text-sm text-black focus:ring-2 focus:ring-primary focus:outline-none"
          >
            <option disabled value="">Wybierz status</option>
            <option v-for="status in statuses" :key="status.id" :value="status.id">{{ status.name }}</option>
          </select>
        </div>
      </div>

      <!-- Daty -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div class="flex items-center space-x-4">
          <label for="startDate" class="w-48 text-black text-lg font-medium">Data rozpoczęcia</label>
          <div class="flex-1">
            <Datepicker
              v-model="task.startDate"
              :format="'yyyy-MM-dd'"
              :inputClass="'w-full p-2.5 border border-gray-300 rounded-md bg-white text-sm text-black focus:ring-2 focus:ring-primary focus:outline-none'"
              :clearable="true"
              placeholder="Wybierz datę rozpoczęcia..."
            />
          </div>
        </div>

        <div class="flex items-center space-x-4">
          <label for="deadline" class="w-48 text-black text-lg font-medium">Deadline</label>
          <div class="flex-1">
            <Datepicker
              v-model="task.deadline"
              :format="'yyyy-MM-dd'"
              :inputClass="'w-full p-2.5 border border-gray-300 rounded-md bg-white text-sm text-black focus:ring-2 focus:ring-primary focus:outline-none'"
              :clearable="true"
              placeholder="Wybierz deadline..."
            />
          </div>
        </div>
      </div>

      <!-- Przyciski -->
      <div class="flex justify-end gap-4 pt-4">
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import taskService from '../services/taskService';
import teamService from '../services/teamService';
import priorityService from '../services/priorityService';
import taskStatusService from '../services/taskStatusService';
import { authState } from '../../router/router.js';
import StatusModal from './StatusModal.vue';
import { useStatusModal } from '../composables/useStatusModal';
import Datepicker from 'vue3-datepicker';

const router = useRouter();

// Dane zadania dopasowane do formatu JSON z backendu
const task = ref({
  title: '',
  description: '',
  teamId: '',
  priorityId: '',
  statusId: '',
  startDate: null,
  deadline: null
  // Pola id, createdById i createdAt będą dodane przez backend
});

// Dane do formularza
const teams = ref([]);
const priorities = ref([]);
const statuses = ref([]);

// Stany komponentu
const loading = ref(false);
const dataLoading = ref(true);

// Użycie composable do obsługi modalu
const { showModal, modalConfig, showStatus, hideModal } = useStatusModal();

// Pobieranie danych referencyjnych (zespoły, priorytety, statusy)
const fetchReferenceData = async () => {
  dataLoading.value = true;

  try {
    // Pobierz zespoły z API
    try {
      const fetchedTeams = await teamService.getAllTeams();
      teams.value = fetchedTeams;
      console.log('Pobrano zespoły:', teams.value);
    } catch (err) {
      console.error('Błąd podczas pobierania zespołów:', err);
      teams.value = [
        { id: 1, name: 'Zespół remontowy' },
        { id: 2, name: 'Zespół instalacyjny' },
        { id: 3, name: 'Zespół projektowy' }
      ];
    }

    // Pobierz priorytety z API
    try {
      const fetchedPriorities = await priorityService.getAllPriorities();
      priorities.value = fetchedPriorities;
      console.log('Pobrano priorytety:', priorities.value);
    } catch (err) {
      console.error('Błąd podczas pobierania priorytetów:', err);
      priorities.value = [
        { id: 1, name: 'Niski' },
        { id: 2, name: 'Średni' },
        { id: 3, name: 'Wysoki' }
      ];
    }

    // Pobierz statusy z API
    try {
      const fetchedStatuses = await taskStatusService.getAllTaskStatuses();
      statuses.value = fetchedStatuses;
      console.log('Pobrano statusy:', statuses.value);
    } catch (err) {
      console.error('Błąd podczas pobierania statusów:', err);
      statuses.value = [
        { id: 1, name: 'Rozpoczęte' },
        { id: 2, name: 'W toku' },
        { id: 3, name: 'Zakończone' }
      ];
    }

    // Ustaw domyślne wartości formularza
    if (statuses.value.length > 0) {
      // Domyślnie wybierz pierwszy status (zwykle "Rozpoczęte")
      task.value.statusId = statuses.value[0].id;
    }

    if (priorities.value.length > 0) {
      // Domyślnie wybierz średni priorytet
      const mediumPriority = priorities.value.find(p => p.name.toLowerCase().includes('średni'));
      task.value.priorityId = mediumPriority ? mediumPriority.id : priorities.value[0].id;
    }

  } catch (err) {
    console.error('Błąd podczas pobierania danych referencyjnych:', err);
    showStatus({
      type: 'error',
      title: 'Błąd',
      message: 'Wystąpił problem podczas ładowania danych. Niektóre opcje mogą być niedostępne.',
      buttonText: 'Zamknij'
    });
  } finally {
    dataLoading.value = false;
  }
};

// Inicjalizacja - pobranie danych referencyjnych
onMounted(async () => {
  // Ustawienie domyślnej daty rozpoczęcia na dzisiaj
  const today = new Date();
  task.value.startDate = today;

  // Ustawienie domyślnego deadlinu na tydzień od dziś
  const nextWeek = new Date();
  nextWeek.setDate(nextWeek.getDate() + 7);
  task.value.deadline = nextWeek;

  // Pobierz dane referencyjne
  await fetchReferenceData();
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

    // Logowanie do konsoli dla celów debugowania
    console.log('Dane zadania do wysłania:', {
      title: task.value.title,
      description: task.value.description,
      teamId: task.value.teamId,
      priorityId: task.value.priorityId,
      statusId: task.value.statusId,
      startDate: task.value.startDate,
      deadline: task.value.deadline,
      createdById: userId
    });

    // Przekształcenie danych do formatu API zgodnego z oczekiwanym JSON
    const formatDate = (date) => {
      if (!date) return '';
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    };

    const taskData = {
      title: task.value.title,
      description: task.value.description || '',
      teamId: task.value.teamId,
      priorityId: task.value.priorityId,
      statusId: task.value.statusId,
      startDate: formatDate(task.value.startDate),
      deadline: formatDate(task.value.deadline),
      createdById: userId
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
      statusId: statuses.value.length > 0 ? statuses.value[0].id : 1,
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
    console.error('Błąd podczas dodawania zadania:',  err);
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

<style scoped>
.transition {
  transition: all 0.2s ease-in-out;
}
/* Podstawowe style dla vue3-datepicker */
:deep(.datepicker input) {
  width: 100% !important;
  background-color: white !important;
  color: black !important;
}
</style>