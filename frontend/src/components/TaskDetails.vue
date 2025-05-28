<template>
  <div class="min-h-screen bg-background flex items-center justify-center px-4 py-10">
    <div v-if="loading" class="text-center">
      <p class="text-xl text-primary">Ładowanie szczegółów zadania...</p>
    </div>

    <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4">
      <p>{{ error }}</p>
      <button @click="fetchTaskDetails" class="mt-2 bg-primary text-white px-4 py-1 rounded-md">
        Spróbuj ponownie
      </button>
    </div>

    <div v-else class="w-full max-w-2xl bg-surface border border-gray-200 rounded-2xl shadow-xl p-8 space-y-6">
      <h2 class="text-3xl font-bold text-primary text-center">Szczegóły Zadania</h2>

      <div class="space-y-4">
        <p><span class="font-semibold text-black">Tytuł: </span> <span class="text-black">{{ task.title }}</span></p>
        <p><span class="font-semibold text-black">Opis: </span> <span class="text-black">{{ task.description }}</span></p>
        
        <!-- Status z przyciskiem do zmiany -->
        <div class="flex items-center justify-between">
          <p class="flex-grow">
            <span class="font-semibold text-black">Status: </span>
            <span :class="getStatusClass(task.statusId || task.status?.id)">
              {{ getStatusText(task.statusId || task.status?.id) }}
            </span>
          </p>
          <button 
            v-if="canShowChangeStatusButton"
            @click="openStatusModal"
            class="ml-4 px-3 py-1 bg-primary hover:bg-secondary text-white text-xs rounded-md transition-colors"
          >
            Zmień
          </button>
        </div>
        
        <p>
          <span class="font-semibold text-black">Priorytet: </span>
          <span :class="getPriorityClass(task.priorityId || task.priority?.id)">
            {{ getPriorityText(task.priorityId || task.priority?.id) }}
          </span>
        </p>
        <p>
          <span class="font-semibold text-black">Termin: </span>
          <span class="text-black">{{ formatDate(task.deadline) }}</span>
        </p>
        <p>
          <span class="font-semibold text-black">Data rozpoczęcia: </span>
          <span class="text-black">{{ formatDate(task.startDate) }}</span>
        </p>
        <p v-if="task.completedDate">
          <span class="font-semibold text-black">Data zakończenia: </span>
          <span class="text-black">{{ formatDate(task.completedDate) }}</span>
        </p>
        <p><span class="font-semibold text-black">Zespół: </span> <span class="text-black">{{ teamName }}</span></p>
      </div>

      <!-- Debugowanie struktury zadania -->
      <div v-if="debugMode" class="bg-gray-100 p-4 rounded-lg mb-4 text-xs overflow-auto max-h-40">
        <pre>{{ JSON.stringify(task, null, 2) }}</pre>
      </div>

      <!-- Lista komentarzy -->
      <div class="space-y-4">
        <div class="flex justify-between items-center">
          <h3 class="text-xl font-semibold text-primary">Komentarze</h3>
          <span class="text-sm text-gray-500">{{ taskComments.length }} {{ commentCountText }}</span>
        </div>

        <div v-if="!taskComments || taskComments.length === 0" class="text-center text-gray-500">
          Brak komentarzy
        </div>

        <div v-else
             class="space-y-4 overflow-y-auto transition-all"
             :class="{'max-h-60': taskComments.length > 3 && !showAllComments, 'scrollbar-custom': taskComments.length > 3}">
          <ul class="space-y-4">
            <li v-for="comment in taskComments" :key="comment.id" class="bg-gray-100 p-4 rounded-lg">
              <div class="flex justify-between">
                <div class="text-sm text-gray-600 mb-1">
                  {{ comment.userFullName || comment.username || 'Użytkownik' }} — {{ formatDate(comment.createdAt) }}
                </div>
                <!-- Przycisk usuwania komentarza - widoczny tylko dla uprawnionych -->
                <button
                    v-if="canDeleteComment(comment)"
                    @click="deleteComment(comment.id)"
                    class="bg-red-600 hover:bg-red-700 text-white p-2 transition-colors duration-200"
                    :disabled="isDeletingComment"
                    title="Usuń komentarz"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>
              <p class="text-black">{{ comment.content || comment.comment }}</p>
            </li>
          </ul>
        </div>

        <!-- Przycisk "Pokaż więcej" gdy liczba komentarzy > 3 -->
        <div v-if="taskComments.length > 3" class="text-center pt-2">
          <button @click="toggleComments" class="bg-primary hover:bg-secondary text-white px-4 py-2 rounded-md transition">
            {{ showAllComments ? 'Pokaż mniej' : 'Pokaż wszystkie' }}
          </button>
        </div>
      </div>

      <!-- Formularz dodawania komentarza -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <h4 class="text-lg font-semibold text-primary mb-3">Dodaj komentarz </h4>
        <form @submit.prevent="addComment" class="space-y-3">
          <textarea
              v-model.trim="newComment"
              class="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary focus:outline-none bg-white text-black placeholder-gray-500"
              rows="3"
              placeholder="Wpisz komentarz..."
              required
          ></textarea>
          <button
              type="submit"
              class="w-full bg-primary hover:bg-secondary text-white px-4 py-2 rounded-md transition"
              :disabled="isAddingComment || !newComment.trim()"
          >
            {{ isAddingComment ? 'Dodawanie...' : 'Dodaj komentarz' }}
          </button>
        </form>
      </div>

      <div class="flex justify-between">
        <!-- schowanie przycisku do debugu -->
        <!-- <button @click="toggleDebug"
                class="px-4 py-2 bg-gray-200 hover:bg-gray-300 text-gray-700 rounded-lg text-sm transition">
          {{ debugMode ? 'Ukryj debug' : 'Debug' }}
        </button> -->

        <div class="space-x-4">
          <button @click="editTask"
                  class="px-4 py-2 bg-primary hover:bg-secondary text-white rounded-lg text-sm transition">
            Edytuj
          </button>
          <button @click="deleteTask"
                  class="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm transition">
            Usuń zadanie
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- Modal do zmiany statusu -->
  <div 
    v-if="showStatusChangeModal" 
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
    @click="closeStatusModal"
  >
    <div 
      class="bg-white rounded-lg shadow-xl max-w-md w-full p-6 transform transition-all"
      @click.stop
    >
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-semibold text-gray-900">Zmień status zadania</h3>
        <button 
          @click="closeStatusModal"
          class="text-white bg-primary hover:text-secondary transition-colors"
        >
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
      </div>
      
      <div class="mb-4">
        <p class="text-sm text-gray-600 mb-2">Aktualny status:</p>
        <div class="flex items-center justify-center">
          <span :class="getStatusClass(task.statusId || task.status?.id)" class="font-semibold">
            {{ getStatusText(task.statusId || task.status?.id) }}
          </span>
        </div>
      </div>
      
      <div class="mb-6">
        <label class="block text-sm font-medium text-gray-700 mb-2">
          Nowy status:
        </label>
        <select
          v-model="selectedStatusId"
          class="w-full bg-white border border-gray-300 rounded-md px-3 py-2 text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="" disabled>Wybierz nowy status</option>
          <option
            v-for="status in availableStatuses"
            :key="status.id"
            :value="status.id"
            class="text-gray-900"
          >
            {{ status.name }}
          </option>
        </select>
      </div>
      
      <div class="flex space-x-3 justify-end">
        <button
          @click="closeStatusModal"
          class="text-white bg-primary hover:text-secondary transition-colors"
        >
          Anuluj
        </button>
        <button
          @click="confirmStatusChange"
          :disabled="!selectedStatusId || isUpdatingStatus"
          class="px-4 py-2 bg-blue-500 hover:bg-blue-600 disabled:bg-gray-300 disabled:cursor-not-allowed text-white rounded-md transition-colors"
        >
          {{ isUpdatingStatus ? 'Zapisywanie...' : 'Zapisz' }}
        </button>
      </div>
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
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import taskService from '../services/taskService';
import teamService from '../services/teamService';
import taskStatusService from '../services/taskStatusService';
import priorityService from '../services/priorityService';
import authService from '../services/authService';
import { authState } from '../../router/router';
import StatusModal from './StatusModal.vue';
import { useStatusModal } from '../composables/useStatusModal';

export default {
  name: 'TaskDetails',
  components: {
    StatusModal
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const task = ref({});
    const loading = ref(true);
    const error = ref(null);
    const newComment = ref('');
    const isAddingComment = ref(false);
    const isDeletingComment = ref(false);
    const debugMode = ref(false);
    const showAllComments = ref(false);
    const statuses = ref([]);
    const priorities = ref([]);
    const selectedStatusId = ref(null);
    const teams = ref([]);
    
    // Modal state
    const showStatusChangeModal = ref(false);
    const isUpdatingStatus = ref(false);

    // Modal setup
    const { showModal, modalConfig, showStatus, hideModal } = useStatusModal();

    // Sprawdzenie czy użytkownik może usuwać komentarze (manager lub admin)
    const canDeleteComments = computed(() => {
      return authService.hasRoleAtLeast('manager');
    });

    // Nowa funkcja sprawdzająca uprawnienia do usuwania pojedynczego komentarza
    const canDeleteComment = (comment) => {
      const user = authState.user;
      if (!user) return false;

      // 1. Administrator systemu
      if (user.role === 'administrator') return true;

      if (user.role === 'kierownik') return true;

      // 2. Autor komentarza
      if (comment.userId && user.id === comment.userId) return true;

      return false;
    };

    // Computed property do obsługi komentarzy
    const taskComments = computed(() => {
      // Sprawdza różne możliwe struktury komentarzy
      if (task.value?.comments && Array.isArray(task.value.comments)) {
        return task.value.comments;
      }
      if (task.value?.taskComments && Array.isArray(task.value.taskComments)) {
        return task.value.taskComments;
      }
      // Sprawdza, czy komentarze są w innym formacie lub zagnieżdżeniu
      if (task.value?.task_comments && Array.isArray(task.value.task_comments)) {
        return task.value.task_comments;
      }
      return [];
    });

    // Ustal uprawnienia
    const userRole = computed(() => authState.user?.role || '');
    const isManager = computed(() => userRole.value === 'kierownik' || userRole.value === 'administrator');
    const isEmployee = computed(() => userRole.value === 'pracownik');

    // Czy zadanie jest zakończone
    const isTaskCompleted = computed(() => {
      const statusId = task.value.statusId || task.value.status?.id;
      const foundStatus = statuses.value.find(s => s.id === Number(statusId));
      if (foundStatus) {
        const name = foundStatus.name.toLowerCase();
        return name.includes('zakończ') || name.includes('completed') || name.includes('done');
      }
      return statusId === 3;
    });

    // Widoczność przycisku zmiany statusu
    const canShowChangeStatusButton = computed(() => {
      if (!authState.user) return false;
      if (!isTaskCompleted.value) return canEditStatus.value; // dowolny uprawniony
      // Jeśli zakończone, tylko kierownik/administrator
      return isManager.value;
    });

    // Czy użytkownik może edytować status
    const canEditStatus = computed(() => {
      if (!authState.user) return false;
      // Kierownik lub administrator może zawsze
      if (isManager.value) return true;
      // Pracownik może, ale nie na "Zakończone"
      if (isEmployee.value) return true;
      return false;
    });

    // Lista statusów dostępnych do wyboru
    const availableStatuses = computed(() => {
      if (isManager.value) {
        return statuses.value;
      }
      // Pracownik - bez statusu "Zakończone"
      return statuses.value.filter(s => !s.name?.toLowerCase().includes('zakończ'));
    });

    // Funkcje modala
    const openStatusModal = () => {
      selectedStatusId.value = task.value.statusId || task.value.status?.id;
      showStatusChangeModal.value = true;
    };

    const closeStatusModal = () => {
      showStatusChangeModal.value = false;
      selectedStatusId.value = null;
      isUpdatingStatus.value = false;
    };

    const confirmStatusChange = async () => {
      if (!selectedStatusId.value || !task.value.id) return;
      
      isUpdatingStatus.value = true;
      
      try {
        // Pobierz aktualne dane zadania
        const currentTask = await taskService.getTaskById(task.value.id);

        // Przygotuj dane do aktualizacji
        const updatedTask = {
          ...currentTask,
          statusId: Number(selectedStatusId.value)
        };

        await taskService.updateTask(task.value.id, updatedTask);
        
        // Zaktualizuj lokalny stan
        task.value.statusId = Number(selectedStatusId.value);
        
        // Zamknij modal
        closeStatusModal();
        
        // Pokaż komunikat sukcesu
        showStatus({
          type: 'success',
          title: 'Sukces',
          message: 'Status zadania został zaktualizowany',
          buttonText: 'OK',
          autoClose: true,
          autoCloseDelay: 1200
        });
        
      } catch (err) {
        console.error('Błąd podczas aktualizacji statusu:', err);
        let msg = 'Nie udało się zaktualizować statusu: ' + (err.response?.data?.message || err.message);
        showStatus({
          type: 'error',
          title: 'Błąd',
          message: msg,
          buttonText: 'Zamknij'
        });
      } finally {
        isUpdatingStatus.value = false;
      }
    };

    const toggleDebug = () => {
      debugMode.value = !debugMode.value;
    };

    // Pobieranie danych referencyjnych (statusy, priorytety)
    const fetchReferenceData = async () => {
      try {
        // Pobierz statusy zadań
        try {
          const statusesResponse = await taskStatusService.getAllTaskStatuses();
          statuses.value = statusesResponse;
          console.log('Pobrano statusy zadań:', statuses.value);
        } catch (err) {
          console.error('Błąd podczas pobierania statusów zadań:', err);
          // Dane awaryjne
          statuses.value = [
            { id: 1, name: 'Rozpoczęte' },
            { id: 2, name: 'W toku' },
            { id: 3, name: 'Zakończone' }
          ];
        }

        // Pobierz priorytety
        try {
          const prioritiesResponse = await priorityService.getAllPriorities();
          priorities.value = prioritiesResponse;
          console.log('Pobrano priorytety zadań:', priorities.value);
        } catch (err) {
          console.error('Błąd podczas pobierania priorytetów zadań:', err);
          // Dane awaryjne
          priorities.value = [
            { id: 1, name: 'Niski' },
            { id: 2, name: 'Średni' },
            { id: 3, name: 'Wysoki' }
          ];
        }
      } catch (err) {
        console.error('Błąd podczas pobierania danych referencyjnych:', err);
      }
    };

    const fetchTaskDetails = async () => {
      loading.value = true;
      error.value = null;

      try {
        // Pobierz ID zadania z parametrów URL
        const taskId = parseInt(route.params.id);

        if (!taskId || isNaN(taskId)) {
          throw new Error('Nieprawidłowe ID zadania');
        }

        console.log('Pobieranie zadania o ID:', taskId);

        // Pobierz dane zadania
        const response = await taskService.getTaskById(taskId);
        console.log('Odpowiedź z backendu:', response);

        // Zapisz odpowiedź i pokaż w konsoli strukturę
        task.value = response;
        console.log('Struktura task:', task.value);

        // Sprawdź, czy są komentarze i wyświetl ich strukturę
        if (task.value.comments) {
          console.log('Struktura komentarzy:', task.value.comments);
        } else if (task.value.taskComments) {
          console.log('Struktura taskComments:', task.value.taskComments);
        } else if (task.value.task_comments) {
          console.log('Struktura task_comments:', task.value.task_comments);
        } else {
          console.log('Brak komentarzy w odpowiedzi API');

          // Spróbuj pobrać komentarze osobno, jeśli nie ma ich w głównej odpowiedzi
          try {
            const commentsResponse = await taskService.getTaskComments(taskId);
            console.log('Pobrane komentarze:', commentsResponse);

            // Dodaj komentarze do obiektu zadania
            if (Array.isArray(commentsResponse)) {
              task.value.comments = commentsResponse;
            }
          } catch (commentsErr) {
            console.error('Błąd podczas pobierania komentarzy:', commentsErr);
          }
        }
      } catch (err) {
        console.error('Błąd podczas pobierania szczegółów zadania:', err);
        error.value = `Nie udało się pobrać szczegółów zadania: ${err.message}`;
      } finally {
        loading.value = false;
      }
    };

    // Pobierz zespoły
    const fetchTeams = async () => {
      try {
        teams.value = await teamService.getAllTeams();
      } catch (err) {
        teams.value = [];
      }
    };

    // Poprawione computed teamName
    const teamName = computed(() => {
      if (!task.value) return 'Nieznany zespół';
      if (task.value.team?.name) return task.value.team.name;
      // Szukaj po teamId w liście zespołów
      const teamId = task.value.teamId || task.value.team?.id;
      if (teamId && teams.value.length > 0) {
        const found = teams.value.find(t => t.id === teamId);
        if (found) return found.name;
      }
      return `Zespół #${teamId || ''}`;
    });

    const addComment = async () => {
      if (!newComment.value.trim()) return;

      isAddingComment.value = true;
      try {
        if (!authState.user?.id) {
          throw new Error('Użytkownik nie jest zalogowany');
        }

        if (!task.value.id) {
          throw new Error('Brak ID zadania');
        }

        const commentData = {
          taskId: task.value.id,
          userId: authState.user.id,
          content: newComment.value.trim()
        };

        console.log('Wysyłanie komentarza:', commentData);
        await taskService.addComment(commentData);

        // Odśwież zadanie aby pobrać nowe komentarze
        await fetchTaskDetails();

        // Wyczyść pole komentarza
        newComment.value = '';

        // Wyświetl potwierdzenie
        showStatus({
          type: 'success',
          title: 'Sukces',
          message: 'Komentarz został dodany',
          buttonText: 'OK',
          autoClose: true,
          autoCloseDelay: 1500
        });

      } catch (err) {
        console.error('Błąd podczas dodawania komentarza:', err);

        showStatus({
          type: 'error',
          title: 'Błąd',
          message: `Nie udało się dodać komentarza: ${err.message}`,
          buttonText: 'Zamknij'
        });
      } finally {
        isAddingComment.value = false;
      }
    };

    // Funkcja do usuwania komentarza
    const deleteComment = async (commentId) => {
      if (!commentId) {
        console.error('Brak ID komentarza');
        return;
      }

      // Potwierdzenie usunięcia
      if (!confirm('Czy na pewno chcesz usunąć ten komentarz?')) {
        return;
      }

      isDeletingComment.value = true;
      try {
        // Wywołaj usługę usuwania komentarza
        await taskService.deleteComment(commentId);

        // Odśwież zadanie, aby zaktualizować listę komentarzy
        await fetchTaskDetails();

        // Wyświetl potwierdzenie
        showStatus({
          type: 'success',
          title: 'Sukces',
          message: 'Komentarz został usunięty',
          buttonText: 'OK',
          autoClose: true,
          autoCloseDelay: 1500
        });

      } catch (err) {
        console.error('Błąd podczas usuwania komentarza:', err);

        showStatus({
          type: 'error',
          title: 'Błąd',
          message: `Nie udało się usunąć komentarza: ${err.message}`,
          buttonText: 'Zamknij'
        });
      } finally {
        isDeletingComment.value = false;
      }
    };

    // Funkcja zwracająca tekst statusu na podstawie ID
    const getStatusText = (statusId) => {
      if (statusId === null || statusId === undefined) return 'Nieznany';

      // Najpierw szukaj w pobranych danych
      const foundStatus = statuses.value.find(s => s.id === Number(statusId));
      if (foundStatus) return foundStatus.name;

      // Jeśli nie znajdziemy, użyj mapowania awaryjnego
      const statusMap = {
        1: 'Rozpoczęte',
        2: 'W toku',
        3: 'Zakończone'
      };

      return statusMap[statusId] || 'Nieznany status';
    };

    // Funkcja zwracająca klasę CSS dla statusu
    const getStatusClass = (statusId) => {
      if (statusId === null || statusId === undefined) return 'text-gray-500 font-semibold';

      // Mapowanie ID na klasy
      const statusClasses = {
        1: 'text-blue-500 font-semibold',
        2: 'text-yellow-500 font-semibold',
        3: 'text-green-500 font-semibold'
      };

      return statusClasses[statusId] || 'text-gray-500 font-semibold';
    };

    // Funkcja zwracająca tekst priorytetu na podstawie ID
    const getPriorityText = (priorityId) => {
      if (priorityId === null || priorityId === undefined) return 'Nieznany';

      // Najpierw szukaj w pobranych danych
      const foundPriority = priorities.value.find(p => p.id === Number(priorityId));
      if (foundPriority) return foundPriority.name;

      // Jeśli nie znajdziemy, użyj mapowania awaryjnego
      const priorityMap = {
        1: 'Niski',
        2: 'Średni',
        3: 'Wysoki'
      };

      return priorityMap[priorityId] || 'Nieznany priorytet';
    };

    // Funkcja zwracająca klasę CSS dla priorytetu
    const getPriorityClass = (priorityId) => {
      if (priorityId === null || priorityId === undefined) return 'text-gray-500 font-semibold';

      // Mapowanie ID na klasy
      const priorityClasses = {
        1: 'text-blue-500 font-semibold',
        2: 'text-yellow-500 font-semibold',
        3: 'text-red-500 font-semibold'
      };

      return priorityClasses[priorityId] || 'text-gray-500 font-semibold';
    };

    const formatDate = (dateString) => {
      if (!dateString) return 'Nie określono';
      try {
        const date = new Date(dateString);
        return date.toLocaleDateString('pl-PL', {
          year: 'numeric',
          month: 'long',
          day: 'numeric'
        });
      } catch (err) {
        return dateString;
      }
    };

    const editTask = () => {
      if (!task.value || !task.value.id) {
        console.error('Nie można edytować zadania - brak ID');
        showStatus({
          type: 'error',
          title: 'Błąd',
          message: 'Nie można edytować zadania - brak ID',
          buttonText: 'Zamknij'
        });
        return;
      }

      const taskId = task.value.id.toString();
      console.log('Przekierowanie do edycji zadania o ID:', taskId);

      // Przekierowanie do edycji zadania
      router.push({
        name: 'editTask',
        params: { id: taskId }
      });
    };

    const deleteTask = async () => {
      if (!confirm('Czy na pewno chcesz usunąć to zadanie?')) return;

      try {
        await taskService.deleteTask(task.value.id);

        showStatus({
          type: 'success',
          title: 'Sukces',
          message: 'Zadanie zostało usunięte',
          buttonText: 'OK',
          autoClose: true,
          autoCloseDelay: 1500,
          onClose: () => router.push('/taskshistory')
        });
      } catch (err) {
        console.error('Błąd podczas usuwania zadania:', err);

        showStatus({
          type: 'error',
          title: 'Błąd',
          message: `Nie udało się usunąć zadania: ${err.message}`,
          buttonText: 'Zamknij'
        });
      }
    };

    // Przełącznik wyświetlania wszystkich komentarzy
    const toggleComments = () => {
      showAllComments.value = !showAllComments.value;
    };

    // Oblicza tekst z liczbą komentarzy
    const commentCountText = computed(() => {
      const count = taskComments.value.length;
      if (count === 1) return 'komentarz';
      else if (count > 1 && count < 5) return 'komentarze';
      else return 'komentarzy';
    });

    onMounted(async () => {
      console.log('TaskDetails component mounted, route params:', route.params);
      // Pobierz dane referencyjne (statusy, priorytety)
      await fetchReferenceData();
      // Pobierz szczegóły zadania
      await fetchTaskDetails();
      // Pobierz zespoły
      await fetchTeams();

      // Dodanie stylów dla niestandardowego scrollbara
      const style = document.createElement('style');
      style.textContent = `
        .scrollbar-custom::-webkit-scrollbar {
          width: 6px;
        }
        .scrollbar-custom::-webkit-scrollbar-track {
          background: #f1f1f1;
          border-radius: 10px;
        }
        .scrollbar-custom::-webkit-scrollbar-thumb {
          background: #888;
          border-radius: 10px;
        }
        .scrollbar-custom::-webkit-scrollbar-thumb:hover {
          background: #555;
        }
      `;
      document.head.appendChild(style);
    });

    return {
      task,
      loading,
      error,
      newComment,
      isAddingComment,
      isDeletingComment,
      teamName,
      taskComments,
      commentCountText,
      showAllComments,
      toggleComments,
      debugMode,
      toggleDebug,
      canDeleteComments,
      canDeleteComment,
      addComment,
      deleteComment,
      editTask,
      deleteTask,
      getStatusText,
      getStatusClass,
      getPriorityText,
      getPriorityClass,
      formatDate,
      fetchTaskDetails,
      showModal,
      modalConfig,
      hideModal,
      selectedStatusId,
      canEditStatus,
      availableStatuses,
      showStatusChangeModal,
      isUpdatingStatus,
      openStatusModal,
      closeStatusModal,
      confirmStatusChange,
      isTaskCompleted,
      canShowChangeStatusButton,
      teams
    };
  }
};
</script>