<template>
  <div class="min-h-screen bg-background flex items-center justify-center px-4 py-10">
    <div v-if="loading" class="text-center">
      <p class="text-xl text-primary">Ładowanie zadania...</p>
    </div>

    <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4">
      <p>{{ error }}</p>
      <button @click="fetchTaskDetails" class="mt-2 bg-primary text-white px-4 py-1 rounded-md">
        Spróbuj ponownie
      </button>
    </div>

    <div v-else class="w-full max-w-2xl bg-surface border border-gray-200 rounded-2xl shadow-xl p-8 space-y-6">
      <h2 class="text-3xl font-bold text-primary text-center">Edytuj Zadanie</h2>

      <form @submit.prevent="updateTask" class="space-y-4">
        <div>
          <label class="block text-lg font-medium mb-2">Tytuł</label>
          <input
              v-model="task.title"
              type="text"
              placeholder="Wprowadź tytuł zadania"
              class="p-2 border border-gray-300 rounded-md w-full bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
              required
          />
        </div>

        <div>
          <label class="block text-lg font-medium mb-2">Opis</label>
          <textarea
              v-model="task.description"
              placeholder="Wprowadź opis zadania"
              class="p-2 border border-gray-300 rounded-md w-full bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
              rows="4"
          ></textarea>
        </div>

        <div>
          <label class="block text-lg font-medium mb-2">Zespół</label>
          <select
              v-model="task.teamId"
              required
              class="p-2 border border-gray-300 rounded-md w-full bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option disabled value="">Wybierz zespół</option>
            <option v-for="team in teams" :key="team.id" :value="team.id">{{ team.name }}</option>
          </select>
        </div>

        <div>
          <label class="block text-lg font-medium mb-2">Priorytet</label>
          <select
              v-model="task.priorityId"
              required
              class="p-2 border border-gray-300 rounded-md w-full bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option disabled value="">Wybierz priorytet</option>
            <option v-for="priority in priorities" :key="priority.id" :value="priority.id">{{ priority.name }}</option>
          </select>
        </div>

        <div>
          <label class="block text-lg font-medium mb-2">Status</label>
          <select
              v-model="task.statusId"
              required
              class="p-2 border border-gray-300 rounded-md w-full bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option disabled value="">Wybierz status</option>
            <option v-for="status in statuses" :key="status.id" :value="status.id">{{ status.name }}</option>
          </select>
        </div>

        <div class="flex flex-col md:flex-row gap-4">
          <div class="flex-1">
            <label class="block text-lg font-medium mb-2">Data rozpoczęcia</label>
            <input
                type="date"
                v-model="task.startDate"
                class="p-2 border border-gray-300 rounded-md w-full bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
          <div class="flex-1">
            <label class="block text-lg font-medium mb-2">Deadline</label>
            <input
                type="date"
                v-model="task.deadline"
                class="p-2 border border-gray-300 rounded-md w-full bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
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
              class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-md transition w-1/2"
              :disabled="isSaving"
          >
            <span v-if="isSaving">Zapisywanie...</span>
            <span v-else>Zapisz zmiany</span>
          </button>
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

<script>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import taskService from '../services/taskService';
import teamService from '../services/teamService';
import priorityService from '../services/priorityService';
import taskStatusService from '../services/taskStatusService';
import { authState } from '../../router/router.js';
import StatusModal from './StatusModal.vue';
import { useStatusModal } from '../composables/useStatusModal';

export default {
  name: 'TaskEdit',
  components: {
    StatusModal
  },
  props: {
    id: {
      type: [String, Number],
      default: null
    }
  },
  setup(props) {
    const route = useRoute();
    const router = useRouter();

    // Pobierz ID zadania z parametrów URL lub props
    const taskId = computed(() => {
      // Sprawdź różne źródła ID zadania
      const id = props.id || route.params.id || route.query.id;
      console.log('TaskEdit - ID zadania (props, params, query):', props.id, route.params.id, route.query.id);
      return id;
    });

    // Stan komponentu
    const task = ref({
      id: null,
      title: '',
      description: '',
      teamId: null,
      priorityId: null,
      statusId: null,
      startDate: '',
      deadline: '',
      createdById: null
    });

    const teams = ref([]);
    const priorities = ref([]);
    const statuses = ref([]);
    const loading = ref(true);
    const error = ref(null);
    const isSaving = ref(false);

    // Status modal
    const { showModal, modalConfig, showStatus, hideModal } = useStatusModal();

    // Pobieranie szczegółów zadania
    const fetchTaskDetails = async () => {
      loading.value = true;
      error.value = null;

      try {
        // Sprawdź czy mamy ID zadania
        if (!taskId.value) {
          console.error('Brak ID zadania w parametrach URL:', route.params, route.query);
          throw new Error('ID zadania jest wymagane');
        }

        console.log('Pobieranie zadania o ID:', taskId.value);

        // Pobierz dane zadania z API
        const taskData = await taskService.getTaskById(taskId.value);
        console.log('Pobrane dane zadania:', taskData);

        // Mapuj dane zadania do formularza
        task.value = {
          id: taskData.id,
          title: taskData.title || '',
          description: taskData.description || '',
          teamId: taskData.teamId || (taskData.team?.id) || null,
          priorityId: taskData.priorityId || (taskData.priority?.id) || null,
          statusId: taskData.statusId || (taskData.status?.id) || null,
          startDate: taskData.startDate ? taskData.startDate.split('T')[0] : '',
          deadline: taskData.deadline ? taskData.deadline.split('T')[0] : '',
          createdById: taskData.createdById || (taskData.createdBy?.id) || null
        };

        // Pobierz powiązane dane
        await Promise.all([
          fetchTeams(),
          fetchPriorities(),
          fetchStatuses()
        ]);
      } catch (err) {
        console.error('Błąd podczas ładowania zadania:', err);
        error.value = `Nie udało się załadować zadania: ${err.message}`;

        // Dane awaryjne
        teams.value = [
          { id: 1, name: 'Zespół A' },
          { id: 2, name: 'Zespół B' }
        ];

        priorities.value = [
          { id: 1, name: 'Niski' },
          { id: 2, name: 'Średni' },
          { id: 3, name: 'Wysoki' }
        ];

        statuses.value = [
          { id: 1, name: 'Rozpoczęte' },
          { id: 2, name: 'W toku' },
          { id: 3, name: 'Zakończone' }
        ];
      } finally {
        loading.value = false;
      }
    };

    // Pobieranie zespołów
    const fetchTeams = async () => {
      try {
        teams.value = await teamService.getAllTeams();
        console.log('Pobrane zespoły:', teams.value);
      } catch (err) {
        console.error('Błąd podczas pobierania zespołów:', err);
        teams.value = [
          { id: 1, name: 'Zespół A' },
          { id: 2, name: 'Zespół B' }
        ];
      }
    };

    // Pobieranie priorytetów
    const fetchPriorities = async () => {
      try {
        priorities.value = await priorityService.getAllPriorities();
        console.log('Pobrane priorytety:', priorities.value);
      } catch (err) {
        console.error('Błąd podczas pobierania priorytetów:', err);
        priorities.value = [
          { id: 1, name: 'Niski' },
          { id: 2, name: 'Średni' },
          { id: 3, name: 'Wysoki' }
        ];
      }
    };

    // Pobieranie statusów
    const fetchStatuses = async () => {
      try {
        statuses.value = await taskStatusService.getAllTaskStatuses();
        console.log('Pobrane statusy:', statuses.value);
      } catch (err) {
        console.error('Błąd podczas pobierania statusów:', err);
        statuses.value = [
          { id: 1, name: 'Rozpoczęte' },
          { id: 2, name: 'W toku' },
          { id: 3, name: 'Zakończone' }
        ];
      }
    };

    // Aktualizacja zadania
    const updateTask = async () => {
      isSaving.value = true;

      try {
        if (!taskId.value) {
          throw new Error('ID zadania jest wymagane');
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
            isSaving.value = false;
            return;
          }
        }

        // Przygotuj dane zadania do wysłania (upewnij się o poprawnych typach)
        const taskData = {
          title: task.value.title,
          description: task.value.description,
          teamId: parseInt(task.value.teamId),
          priorityId: parseInt(task.value.priorityId),
          statusId: parseInt(task.value.statusId),
          startDate: task.value.startDate,
          deadline: task.value.deadline
        };

        console.log('Wysyłanie aktualizacji zadania:', taskData);

        // Używamy pełnej aktualizacji (PUT) zamiast próby używania PATCH
        const updatedTask = await taskService.updateTask(taskId.value, taskData);
        console.log('Zaktualizowane zadanie:', updatedTask);

        // Pokaż komunikat powodzenia
        showStatus({
          type: 'success',
          title: 'Sukces',
          message: 'Zadanie zostało pomyślnie zaktualizowane!',
          buttonText: 'OK',
          autoClose: true,
          autoCloseDelay: 2000,
          onClose: () => router.push(`/taskdetails/${taskId.value}`)
        });
      } catch (err) {
        console.error('Błąd podczas aktualizacji zadania:', err);

        showStatus({
          type: 'error',
          title: 'Błąd',
          message: `Nie udało się zaktualizować zadania: ${err.message}`,
          buttonText: 'Zamknij'
        });
      } finally {
        isSaving.value = false;
      }
    };

    // Powrót do poprzedniej strony
    const goBack = () => {
      router.back();
    };

    // Inicjalizacja komponentu
    onMounted(() => {
      console.log('TaskEdit component mounted, route params:', route.params);
      fetchTaskDetails();
    });

    return {
      task,
      teams,
      priorities,
      statuses,
      loading,
      error,
      isSaving,
      fetchTaskDetails,
      updateTask,
      goBack,
      showModal,
      modalConfig,
      hideModal
    };
  }
};
</script>