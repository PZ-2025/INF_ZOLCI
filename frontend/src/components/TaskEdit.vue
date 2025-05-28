<template>
  <div class="min-h-screen bg-background flex flex-col items-start justify-start px-10 py-10 space-y-8">
    <!-- Nagłówek -->
    <h1 class="text-3xl font-bold text-primary">Edytuj zadanie</h1>

    <!-- Loader -->
    <div v-if="loading" class="text-primary text-xl">Ładowanie zadania...</div>

    <!-- Błąd -->
    <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 w-full max-w-3xl">
      <p>{{ error }}</p>
      <button @click="fetchTaskDetails" class="mt-2 bg-primary text-white px-4 py-1 rounded-md">
        Spróbuj ponownie
      </button>
    </div>

    <!-- Formularz -->
    <form
      v-else
      @submit.prevent="updateTask"
      class="space-y-5 w-full max-w-3xl"
    >
      <!-- Tytuł -->
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

      <!-- Opis -->
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

      <!-- Zespół -->
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
          <div class="flex-1 datepicker-wrapper">
            <Datepicker
              v-model="task.startDate"
              :input-class="'w-full p-2.5 border border-gray-300 rounded-md bg-white text-sm text-black focus:ring-2 focus:ring-primary focus:outline-none datepicker-input'"
              :format="'yyyy-MM-dd'"
              :id="'startDate'"
              required
            />
          </div>
        </div>
        <div class="flex items-center space-x-4">
          <label for="deadline" class="w-48 text-black text-lg font-medium">Deadline</label>
          <div class="flex-1 datepicker-wrapper">
            <Datepicker
              v-model="task.deadline"
              :input-class="'w-full p-2.5 border border-gray-300 rounded-md bg-white text-sm text-black focus:ring-2 focus:ring-primary focus:outline-none datepicker-input'"
              :format="'yyyy-MM-dd'"
              :id="'deadline'"
              required
            />
          </div>
        </div>
      </div>

      <!-- Zakończone zadanie -->
      <div class="flex items-center space-x-4">
        <input
          id="completedCheckbox"
          type="checkbox"
          v-model="completedChecked"
          class="w-5 h-5 text-primary border-gray-300 rounded focus:ring-primary"
        />
        <label for="completedCheckbox" class="text-black text-lg font-medium">Zadanie zakończone</label>
        <div v-if="completedChecked" class="flex-1 datepicker-wrapper">
          <Datepicker
            v-model="task.completedDate"
            :input-class="'w-full p-2.5 border border-gray-300 rounded-md bg-white text-sm text-black focus:ring-2 focus:ring-primary focus:outline-none datepicker-input'"
            :format="'yyyy-MM-dd'"
            :id="'completedDate'"
            placeholder="Data zakończenia"
          />
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
          :disabled="isSaving"
        >
          <span v-if="isSaving">Zapisywanie...</span>
          <span v-else>Zapisz zmiany</span>
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

<script>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import taskService from '../services/taskService';
import teamService from '../services/teamService';
import priorityService from '../services/priorityService';
import taskStatusService from '../services/taskStatusService';
import { authState } from '../../router/router.js';
import StatusModal from './StatusModal.vue';
import { useStatusModal } from '../composables/useStatusModal';
import Datepicker from 'vue3-datepicker';

export default {
  name: 'TaskEdit',
  components: {
    StatusModal,
    Datepicker
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
      startDate: null,
      deadline: null,
      completedDate: null, // dodane pole
      createdById: null
    });

    // Checkbox do sterowania completedDate
    const completedChecked = ref(false);

    const teams = ref([]);
    const priorities = ref([]);
    const statuses = ref([]);
    const loading = ref(true);
    const error = ref(null);
    const isSaving = ref(false);

    // Status modal
    const { showModal, modalConfig, showStatus, hideModal } = useStatusModal();

    // Pobieranie zespołów
    const fetchTeams = async () => {
      try {
        teams.value = await teamService.getAllTeams();
        console.log('Pobrane zespoły:', teams.value);
      } catch (err) {
        console.error('Błąd podczas pobierania zespołów:', err);
        teams.value = [
          { id: 1, name: 'Zespół remontowy' },
          { id: 2, name: 'Zespół instalacyjny' },
          { id: 3, name: 'Zespół projektowy' }
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

        // Pobierz dane referencyjne równolegle z danymi zadania
        const [taskData] = await Promise.all([
          taskService.getTaskById(taskId.value),
          fetchTeams(),
          fetchPriorities(),
          fetchStatuses()
        ]);

        console.log('Pobrane dane zadania:', taskData);

        // Mapuj dane zadania do formularza
        task.value = {
          id: taskData.id,
          title: taskData.title || '',
          description: taskData.description || '',
          teamId: taskData.teamId || (taskData.team?.id) || null,
          priorityId: taskData.priorityId || (taskData.priority?.id) || null,
          statusId: taskData.statusId || (taskData.status?.id) || null,
          startDate: taskData.startDate ? new Date(taskData.startDate) : null,
          deadline: taskData.deadline ? new Date(taskData.deadline) : null,
          completedDate: taskData.completedDate ? new Date(taskData.completedDate) : null, // dodane
          createdById: taskData.createdById || (taskData.createdBy?.id) || null
        };
        completedChecked.value = !!task.value.completedDate;
      } catch (err) {
        console.error('Błąd podczas ładowania zadania:', err);
        error.value = `Nie udało się załadować zadania: ${err.message}`;

        // Pobierz dane referencyjne nawet w przypadku błędu
        await Promise.all([
          fetchTeams(),
          fetchPriorities(),
          fetchStatuses()
        ]);
      } finally {
        loading.value = false;
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

        // Formatowanie dat do yyyy-MM-dd (lokalnie, nie UTC)
        const formatDate = (date) => {
          if (!date) return '';
          const year = date.getFullYear();
          const month = String(date.getMonth() + 1).padStart(2, '0');
          const day = String(date.getDate()).padStart(2, '0');
          return `${year}-${month}-${day}`;
        };

        // Przygotuj dane zadania do wysłania (upewnij się o poprawnych typach)
        const taskData = {
          title: task.value.title,
          description: task.value.description,
          teamId: parseInt(task.value.teamId),
          priorityId: parseInt(task.value.priorityId),
          statusId: parseInt(task.value.statusId),
          startDate: formatDate(task.value.startDate),
          deadline: formatDate(task.value.deadline),
          completedDate: completedChecked.value && task.value.completedDate ? formatDate(task.value.completedDate) : null
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

    // Zapamiętaj ostatni nie-zakończony status
    const lastNonCompletedStatusId = ref(null);

    // Watcher: automatyczna zmiana statusu na "Zakończone" po ustawieniu daty zakończenia
    watch([() => task.value.completedDate, completedChecked, statuses], ([completedDate, checked, statusesVal]) => {
      if (checked && completedDate) {
        // Znajdź status "Zakończone"
        const completedStatus = statusesVal.find(
          s => s.name?.toLowerCase().includes('zakończ')
        );
        if (completedStatus && task.value.statusId !== completedStatus.id) {
          // Zapamiętaj ostatni nie-zakończony status
          if (task.value.statusId && !lastNonCompletedStatusId.value && (!completedStatus || task.value.statusId !== completedStatus.id)) {
            lastNonCompletedStatusId.value = task.value.statusId;
          }
          task.value.statusId = completedStatus.id;
        }
      } else if (!checked || !completedDate) {
        // Jeśli odznaczono zakończenie, przywróć poprzedni status jeśli był zapamiętany
        if (lastNonCompletedStatusId.value) {
          task.value.statusId = lastNonCompletedStatusId.value;
          lastNonCompletedStatusId.value = null;
        }
      }
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
      hideModal,
      completedChecked
    };
  }
};
</script>