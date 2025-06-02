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
            :disabled="isTaskCompletedAndLocked"
            :class="[
              'flex-1 p-2.5 border border-gray-300 rounded-md text-sm text-black focus:ring-2 focus:ring-primary focus:outline-none',
              isTaskCompletedAndLocked ? 'bg-gray-200 cursor-not-allowed opacity-50' : 'bg-white'
            ]"
          >
            <option disabled value="">Wybierz status</option>
            <option v-for="status in statuses" :key="status.id" :value="status.id">{{ status.name }}</option>
          </select>
          <!-- Ikona informacyjna dla zablokowanego statusu -->
          <div 
            v-if="isTaskCompletedAndLocked" 
            class="flex items-center text-gray-600 text-sm"
            title="Nie można zmienić statusu zadania zakończonego"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
            <span>Zablokowane</span>
          </div>
        </div>
      </div>

      <!-- Przycisk odblokowania dla zadań zakończonych (tylko dla uprawnionego użytkownika) -->
      <div v-if="isTaskCompletedAndLocked && canUnlockCompletedTasks" class="flex items-center space-x-4 bg-yellow-50 border-l-4 border-yellow-400 p-4 rounded">
        <div class="flex">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-yellow-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.082 16.5c-.77.833.192 2.5 1.732 2.5z" />
          </svg>
        </div>
        <div class="flex-1">
          <p class="text-sm text-yellow-800">
            <strong>Zadanie zakończone:</strong> To zadanie jest zablokowane do edycji, ponieważ zostało oznaczone jako zakończone.
          </p>
        </div>
        <button
          @click="unlockTask"
          type="button"
          class="px-4 py-2 bg-yellow-600 hover:bg-yellow-700 text-white text-sm rounded-md transition-colors"
        >
          Odblokuj zadanie
        </button>
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
              :auto-apply="true"
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
              :auto-apply="true"
              placeholder="Wybierz deadline..."
            />
          </div>
        </div>
      </div>

      <!-- Data zakończenia z legendą -->
      <div class="space-y-4">
        <div class="flex items-start space-x-4">
          <label for="completedDate" class="w-48 text-black text-lg font-medium pt-2">Data zakończenia</label>
          <div class="flex-1">
            <Datepicker
              v-model="task.completedDate"
              :format="'yyyy-MM-dd'"
              :inputClass="completedDateInputClass"
              :clearable="true"
              :auto-apply="true"
              :close-on-select="true"
              :close-on-auto-apply="true"
              placeholder="Wybierz datę zakończenia (opcjonalne)..."
              :disabled="isTaskCompletedAndLocked && !canUnlockCompletedTasks"
              @update:model-value="onCompletedDateChange"
            />
            <!-- Legenda/instrukcja -->
            <div class="mt-2 p-3 bg-blue-50 border-l-4 border-blue-400 rounded">
              <p class="text-sm text-blue-800">
                <strong>💡 Wskazówka:</strong> Jeśli podasz datę zakończenia, automatycznie ustaw status na "Zakończone". 
                Gdy status to "Zakończone", data zakończenia jest wymagana.
              </p>
            </div>
            <!-- Błąd walidacji -->
            <div v-if="completedDateValidationError" class="mt-2 p-2 bg-red-50 border-l-4 border-red-400 rounded">
              <p class="text-sm text-red-800">{{ completedDateValidationError }}</p>
            </div>
          </div>
        </div>

        <!-- Debug info -->
        <!-- <div v-if="showDebugInfo" class="text-xs text-gray-500 bg-gray-50 p-2 rounded">
          <p><strong>Debug:</strong></p>
          <p>task.completedDate: {{ task.completedDate }}</p>
          <p>isCompletedStatus: {{ isCompletedStatus }}</p>
          <p>completedDateValidationError: {{ completedDateValidationError }}</p>
          <p>isTaskCompletedAndLocked: {{ isTaskCompletedAndLocked }}</p>
          <p>canUnlockCompletedTasks: {{ canUnlockCompletedTasks }}</p>
        </div> -->
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
        <!-- <button
          type="button"
          @click="toggleDebugInfo"
          class="px-3 py-2 rounded-md bg-blue-500 text-white text-xs hover:bg-blue-600 transition"
        >
          {{ showDebugInfo ? 'Ukryj' : 'Debug' }}
        </button> -->
        <button
          type="submit"
          class="px-6 py-2 rounded-md bg-primary hover:bg-secondary text-white text-sm font-semibold transition"
          :disabled="isSaving"
        >
          <span v-if="isSaving">Zapisywanie...</span>
          <span v-else">Zapisz zmiany</span>
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
      :cancel-text="modalConfig.cancelText"
      :show-cancel-button="modalConfig.showCancelButton"
      :auto-close="modalConfig.autoClose"
      :auto-close-delay="modalConfig.autoCloseDelay"
      @close="hideModal"
      @confirm="modalConfig.onClose"
      @cancel="modalConfig.onCancel"
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
      completedDate: null,
      createdById: null
    });

    // Checkbox do sterowania completedDate - USUNIĘTE
    // const completedChecked = ref(false);
    
    // Debug mode
    const showDebugInfo = ref(false);
    
    // Pending status change - do obsługi potwierdzenia zmiany statusu
    const pendingStatusChange = ref(null);

    const teams = ref([]);
    const priorities = ref([]);
    const statuses = ref([]);
    const loading = ref(true);
    const error = ref(null);
    const isSaving = ref(false);

    // Status modal
    const { showModal, modalConfig, showStatus, hideModal } = useStatusModal();

    // Computed properties for logic
    const isTaskCompletedAndLocked = computed(() => {
      // Tu można dodać logikę sprawdzającą czy zadanie jest zablokowane
      // Na razie zwracamy false, żeby nie blokować edycji
      return false;
    });

    const canUnlockCompletedTasks = computed(() => {
      // Tu można dodać logikę sprawdzającą uprawnienia użytkownika
      const userRole = authState.user?.role;
      return userRole === 'administrator' || userRole === 'kierownik';
    });

    // Sprawdź czy wybrany status to "Zakończone"
    const isCompletedStatus = computed(() => {
      if (!task.value.statusId || !statuses.value.length) return false;
      
      const selectedStatus = statuses.value.find(s => s.id === task.value.statusId);
      if (!selectedStatus) return false;
      
      const statusName = selectedStatus.name.toLowerCase();
      return statusName.includes('zakończ') || statusName.includes('completed') || statusName.includes('done');
    });

    // Walidacja daty zakończenia
    const completedDateValidationError = computed(() => {
      // Jeśli status to "Zakończone" ale brak daty zakończenia
      if (isCompletedStatus.value && !task.value.completedDate) {
        return 'Data zakończenia jest wymagana gdy status to "Zakończone"';
      }
      
      // Jeśli jest data zakończenia ale status nie jest "Zakończone"
      if (task.value.completedDate && !isCompletedStatus.value) {
        return 'Gdy podano datę zakończenia, status powinien być ustawiony na "Zakończone"';
      }
      
      return null;
    });

    // Dynamic input class for completed date
    const completedDateInputClass = computed(() => {
      const baseClass = 'w-full p-2.5 border border-gray-300 rounded-md text-sm text-black focus:ring-2 focus:ring-primary focus:outline-none';
      const bgClass = (isTaskCompletedAndLocked.value && !canUnlockCompletedTasks.value) ? 'bg-gray-200' : 'bg-white';
      return `${baseClass} ${bgClass}`;
    });

    // Handler for completed date change
    const onCompletedDateChange = (newDate) => {
      console.log('💼 Completed date changed:', newDate);
      task.value.completedDate = newDate;
      
      // Jeśli ustawiono datę zakończenia, automatycznie znajdź i ustaw status "Zakończone"
      if (newDate && !isCompletedStatus.value && statuses.value.length > 0) {
        const completedStatus = statuses.value.find(
          s => s.name?.toLowerCase().includes('zakończ') || 
               s.name?.toLowerCase().includes('completed') || 
               s.name?.toLowerCase().includes('done')
        );
        
        if (completedStatus) {
          task.value.statusId = completedStatus.id;
          console.log('🎯 Auto-set status to completed:', completedStatus.name);
        }
      }
    };

    // Funkcja do potwierdzenia usunięcia daty zakończenia
    const confirmClearCompletedDate = (newStatusId, oldStatusId) => {
      pendingStatusChange.value = { newStatusId, oldStatusId };
      
      showStatus({
        type: 'info', // Zmienione z 'warning' na 'info' dla niebieskiego koloru
        title: 'Potwierdzenie zmiany statusu',
        message: 'Zmiana statusu z "Zakończone" na inny spowoduje usunięcie daty zakończenia. Czy kontynuować?',
        buttonText: 'Tak, kontynuuj',
        cancelText: 'Anuluj',
        showCancelButton: true,
        autoClose: false,
        onClose: () => handleStatusChangeConfirmation(true),
        onCancel: () => handleStatusChangeConfirmation(false)
      });
    };

    // Funkcja obsługująca odpowiedź użytkownika na potwierdzenie
    const handleStatusChangeConfirmation = (confirmed) => {
      if (!pendingStatusChange.value) return;

      if (confirmed) {
        // Użytkownik potwierdził - usuń datę zakończenia i zmień status
        task.value.completedDate = null;
        task.value.statusId = pendingStatusChange.value.newStatusId;
        console.log('🗑️ Cleared completed date due to status change from completed to non-completed');
      } else {
        // Użytkownik odrzucił - przywróć poprzedni status
        task.value.statusId = pendingStatusChange.value.oldStatusId;
        console.log('↩️ Reverted status change due to user cancellation');
      }

      // Wyczyść pending change
      pendingStatusChange.value = null;
    };

    // Funkcja do anulowania zmiany statusu (przycisk "Anuluj" w modalu)
    const cancelStatusChange = () => {
      if (pendingStatusChange.value) {
        task.value.statusId = pendingStatusChange.value.oldStatusId;
        console.log('↩️ Cancelled status change');
        pendingStatusChange.value = null;
      }
      hideModal();
    };

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
          completedDate: taskData.completedDate ? new Date(taskData.completedDate) : null,
          createdById: taskData.createdById || (taskData.createdBy?.id) || null
        };
        
        // Ustaw datę zakończenia na podstawie pobranych danych
        task.value.completedDate = taskData.completedDate ? new Date(taskData.completedDate) : null;
        
        console.log('📅 Task dates after mapping:', {
          startDate: task.value.startDate,
          deadline: task.value.deadline,
          completedDate: task.value.completedDate
        });
        
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

    // POPRAWIONA AKTUALIZACJA ZADANIA Z WALIDACJĄ
    const updateTask = async () => {
      isSaving.value = true;

      try {
        if (!taskId.value) {
          throw new Error('ID zadania jest wymagane');
        }

        // WALIDACJA: Sprawdź zgodność statusu i daty zakończenia
        if (completedDateValidationError.value) {
          showStatus({
            type: 'error',
            title: 'Błąd walidacji',
            message: completedDateValidationError.value,
            buttonText: 'Zamknij'
          });
          isSaving.value = false;
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
            isSaving.value = false;
            return;
          }
        }

        // Walidacja daty zakończenia względem daty rozpoczęcia
        if (task.value.completedDate && task.value.startDate) {
          const completedDate = new Date(task.value.completedDate);
          const startDate = new Date(task.value.startDate);

          if (completedDate < startDate) {
            showStatus({
              type: 'error',
              title: 'Błąd',
              message: 'Data zakończenia nie może być wcześniejsza niż data rozpoczęcia.',
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

        // Przygotuj dane zadania do wysłania
        const taskData = {
          title: task.value.title,
          description: task.value.description,
          teamId: parseInt(task.value.teamId),
          priorityId: parseInt(task.value.priorityId),
          statusId: parseInt(task.value.statusId),
          startDate: formatDate(task.value.startDate),
          deadline: formatDate(task.value.deadline),
          completedDate: task.value.completedDate ? formatDate(task.value.completedDate) : null
        };

        console.log('📤 Wysyłanie aktualizacji zadania:', taskData);

        // Używamy pełnej aktualizacji (PUT)
        const updatedTask = await taskService.updateTask(taskId.value, taskData);
        console.log('✅ Zaktualizowane zadanie:', updatedTask);

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

    // Zapamiętaj ostatni nie-zakończony status
    const lastNonCompletedStatusId = ref(null);

    // Toggle debug info
    const toggleDebugInfo = () => {
      showDebugInfo.value = !showDebugInfo.value;
    };

    // Watcher: automatyczna zmiana statusu na "Zakończone" przy zmianie statusu
    watch(() => task.value.statusId, (newStatusId, oldStatusId) => {
      if (!statuses.value.length) return;
      
      // Ignoruj zmiany spowodowane przez pending status change
      if (pendingStatusChange.value) return;
      
      const selectedStatus = statuses.value.find(s => s.id === newStatusId);
      const previousStatus = oldStatusId ? statuses.value.find(s => s.id === oldStatusId) : null;
      
      if (!selectedStatus) return;
      
      const isCompletedStatusSelected = selectedStatus.name?.toLowerCase().includes('zakończ') ||
                                      selectedStatus.name?.toLowerCase().includes('completed') ||
                                      selectedStatus.name?.toLowerCase().includes('done');

      const wasPreviouslyCompleted = previousStatus ? 
        (previousStatus.name?.toLowerCase().includes('zakończ') ||
         previousStatus.name?.toLowerCase().includes('completed') ||
         previousStatus.name?.toLowerCase().includes('done')) : false;
      
      console.log('🔄 Status changed:', {
        from: previousStatus?.name || 'unknown',
        to: selectedStatus.name,
        isCompletedNow: isCompletedStatusSelected,
        wasPreviouslyCompleted: wasPreviouslyCompleted
      });
      
      // Jeśli zmieniono status na "Zakończone" ale brak daty zakończenia, ustaw dzisiejszą datę
      if (isCompletedStatusSelected && !task.value.completedDate) {
        task.value.completedDate = new Date();
        console.log('📅 Auto-set completed date to today');
      }
      
      // NOWE: Jeśli zmieniono status Z "Zakończone" NA inny, pokaż modal potwierdzenia
      if (wasPreviouslyCompleted && !isCompletedStatusSelected && task.value.completedDate) {
        // Przywróć tymczasowo poprzedni status do czasu potwierdzenia
        task.value.statusId = oldStatusId;
        
        // Pokaż modal potwierdzenia
        confirmClearCompletedDate(newStatusId, oldStatusId);
      }
    });

    // Watcher dla daty zakończenia - automatyczne ustawianie statusu
    watch(() => task.value.completedDate, (newDate) => {
      if (!statuses.value.length) return;
      
      // Jeśli dodano datę zakończenia ale status nie jest "Zakończone"
      if (newDate && !isCompletedStatus.value) {
        const completedStatus = statuses.value.find(
          s => s.name?.toLowerCase().includes('zakończ') ||
               s.name?.toLowerCase().includes('completed') ||
               s.name?.toLowerCase().includes('done')
        );
        
        if (completedStatus) {
          task.value.statusId = completedStatus.id;
          console.log('🎯 Auto-set status to completed due to date:', completedStatus.name);
        }
      }
    });

    // Inicjalizacja komponentu
    onMounted(() => {
      console.log('🚀 TaskEdit component mounted, route params:', route.params);
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
      showDebugInfo,
      isTaskCompletedAndLocked,
      canUnlockCompletedTasks,
      isCompletedStatus,
      completedDateValidationError,
      completedDateInputClass,
      fetchTaskDetails,
      updateTask,
      goBack,
      onCompletedDateChange,
      toggleDebugInfo,
      confirmClearCompletedDate,
      handleStatusChangeConfirmation,
      cancelStatusChange,
      showModal,
      modalConfig,
      hideModal
    };
  }
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

/* Specjalne style dla datepicker w sekcji completed date */
:deep(.datepicker-wrapper) {
  width: 100%;
}

:deep(.datepicker-wrapper input) {
  width: 100% !important;
  box-sizing: border-box !important;
}
</style>