<template>
  <div class="min-h-screen bg-background text-text flex justify-center items-start">
    <div class="rounded-lg p-6 w-full space-y-8">
      <div class="flex justify-between items-center">
        <h1 class="text-3xl font-bold text-primary">Historia Zadań</h1>
      </div>

      <!-- Filtry -->
      <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="flex flex-col">
          <label class="mb-1 font-medium text-sm">Zespół:</label>
          <select v-model="filters.team"
                  class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="">Wszystkie</option>
            <option v-for="team in teams" :key="team.id" :value="team.id">{{ team.name }}</option>
          </select>
        </div>

        <div class="flex flex-col">
          <label class="mb-1 font-medium text-sm">Priorytet:</label>
          <select v-model="filters.priority"
                  class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="">Wszystkie</option>
            <option v-for="priority in priorities" :key="priority.id" :value="priority.id">
              {{ priority.name }}
            </option>
          </select>
        </div>

        <div class="flex flex-col">
          <label class="mb-1 font-medium text-sm">Status:</label>
          <select v-model="filters.status"
                  class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="">Wszystkie</option>
            <option v-for="status in statuses" :key="status.id" :value="status.id">
              {{ status.name }}
            </option>
          </select>
        </div>

        <div class="flex flex-col">
          <label class="mb-1 font-medium text-sm">Deadline:</label>
          <input type="date" v-model="filters.deadline"
                 class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>

        <button @click="applyFilters"
                class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-md transition"
        >
          Filtruj
        </button>

        <button @click="resetFilters"
                class="bg-gray-500 hover:bg-gray-600 text-white px-6 py-2 rounded-md transition"
        >
          Resetuj filtry
        </button>
      </div>

      <!-- Stan ładowania -->
      <div v-if="loading" class="flex justify-center items-center py-12">
        <p class="text-primary text-xl">Ładowanie zadań...</p>
      </div>

      <!-- Komunikat o błędzie -->
      <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 my-6">
        <p>{{ error }}</p>
        <button @click="fetchTasks" class="mt-2 bg-primary text-white px-4 py-1 rounded-md">
          Spróbuj ponownie
        </button>
      </div>

      <!-- Diagnostyka danych (dla debugowania) -->
      <div v-if="isDebugMode" class="bg-gray-100 p-4 rounded-lg mb-4 text-xs overflow-auto max-h-40">
        <p class="font-bold mb-2">Dane diagnostyczne:</p>
        <pre>{{ JSON.stringify({filters: filters, tasksCount: tasks.length, filteredCount: filteredTasks.length}, null, 2) }}</pre>
        <button @click="isDebugMode = false" class="text-xs text-primary mt-2">Ukryj</button>
      </div>

      <!-- Lista zadań -->
      <div v-else-if="filteredTasks.length === 0" class="text-center py-8 text-muted">
        <p>Brak zadań spełniających kryteria filtrowania</p>
        <button @click="isDebugMode = true" class="text-xs text-primary mt-2">Pokaż diagnostykę</button>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
            v-for="task in filteredTasks"
            :key="task.id"
            class="bg-white border border-gray-200 p-4 rounded-lg shadow-sm transition hover:shadow-lg flex flex-col h-full"
        >
          <div class="flex-grow flex flex-col">
            <h3 class="text-xl font-semibold text-primary">{{ task.title || task.name }}</h3>
            <p class="text-sm text-muted">{{ task.description }}</p>
            <div class="mt-2 flex flex-wrap gap-2">
              <span class="text-xs px-2 py-1 rounded bg-gray-100">
                Zespół: {{ getTeamName(task.team || task.teamId) }}
              </span>
              <span
                  class="text-xs px-2 py-1 rounded text-white"
                  :class="getPriorityClass(task.priorityId || task.priority?.id)"
              >
                  {{ getPriorityText(task.priorityId || task.priority?.id) }}
              </span>
              <span
                  class="text-xs px-2 py-1 rounded"
                  :class="getStatusClass(task.statusId || task.status?.id)"
              >
                  {{ getStatusText(task.statusId || task.status?.id) }}
              </span>
            </div>
            <p v-if="task.deadline" class="text-xs text-muted mt-1">
              Termin: {{ formatDate(task.deadline) }}
            </p>
          </div>
          <button @click="openTaskDetails(task)"
                  class="mt-auto bg-primary hover:bg-secondary text-white px-4 py-2 rounded-md transition w-full"
          >
            Szczegóły
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import taskService from '../services/taskService';
import teamService from '../services/teamService';
import priorityService from '../services/priorityService';
import taskStatusService from '../services/taskStatusService';

export default {
  setup() {
    const router = useRouter();
    const tasks = ref([]);
    const teams = ref([]);
    const priorities = ref([]);
    const statuses = ref([]);
    const loading = ref(true);
    const error = ref(null);
    const isDebugMode = ref(false);

    const filters = ref({
      team: "",
      priority: "",
      status: "",
      deadline: ""
    });

    // Pobieranie zadań z API
    const fetchTasks = async () => {
      loading.value = true;
      error.value = null;

      try {
        const response = await taskService.getAllTasks();
        console.log('Otrzymane zadania z API:', response);
        tasks.value = response;
      } catch (err) {
        console.error('Błąd podczas pobierania zadań:', err);
        error.value = `Nie udało się pobrać zadań: ${err.message}`;
      } finally {
        loading.value = false;
      }
    };

    // Pobieranie danych referencyjnych
    const fetchReferenceData = async () => {
      try {
        // Pobierz zespoły
        const teamsResponse = await teamService.getAllTeams();
        teams.value = teamsResponse;

        // Pobierz priorytety
        try {
          const prioritiesResponse = await priorityService.getAllPriorities();
          priorities.value = prioritiesResponse;
        } catch (err) {
          console.error('Błąd podczas pobierania priorytetów:', err);
          // Dane awaryjne
          priorities.value = [
            { id: 1, name: 'Niski' },
            { id: 2, name: 'Średni' },
            { id: 3, name: 'Wysoki' }
          ];
        }

        // Pobierz statusy
        try {
          const statusesResponse = await taskStatusService.getAllTaskStatuses();
          statuses.value = statusesResponse;
        } catch (err) {
          console.error('Błąd podczas pobierania statusów:', err);
          // Dane awaryjne
          statuses.value = [
            { id: 1, name: 'Rozpoczęte' },
            { id: 2, name: 'W toku' },
            { id: 3, name: 'Zakończone' }
          ];
        }
      } catch (err) {
        console.error('Błąd podczas pobierania danych referencyjnych:', err);
        // Dane awaryjne
        teams.value = [
          { id: 1, name: 'Zespół A' },
          { id: 2, name: 'Zespół B' },
          { id: 3, name: 'Zespół C' }
        ];
      }
    };

    // Filtrowanie zadań na podstawie wybranych filtrów
    const filteredTasks = computed(() => {
      return tasks.value.filter(task => {
        // Filtrowanie po zespole
        const teamMatch = !filters.value.team ||
            String(task.team?.id) === String(filters.value.team) ||
            String(task.teamId) === String(filters.value.team);

        // Filtrowanie po priorytecie
        const priorityMatch = !filters.value.priority ||
            String(task.priority?.id) === String(filters.value.priority) ||
            String(task.priorityId) === String(filters.value.priority);

        // Filtrowanie po statusie
        const statusMatch = !filters.value.status ||
            String(task.status?.id) === String(filters.value.status) ||
            String(task.statusId) === String(filters.value.status);

        // Filtrowanie po deadline
        let deadlineMatch = true;
        if (filters.value.deadline && task.deadline) {
          const taskDeadline = task.deadline.split('T')[0];
          deadlineMatch = taskDeadline === filters.value.deadline;
        }

        return teamMatch && priorityMatch && statusMatch && deadlineMatch;
      });
    });

    // Reset filtrów
    const resetFilters = () => {
      filters.value = {
        team: "",
        priority: "",
        status: "",
        deadline: ""
      };
    };

    // Aplikowanie filtrów z możliwością użycia API
    const applyFilters = async () => {
      console.log("Zastosowano filtry:", filters.value);

      // Sprawdź czy można użyć dedykowanego endpointu API
      const hasTeamFilter = !!filters.value.team;
      const hasPriorityFilter = !!filters.value.priority;
      const hasStatusFilter = !!filters.value.status;
      const hasDeadlineFilter = !!filters.value.deadline;

      const filterCount = [hasTeamFilter, hasPriorityFilter, hasStatusFilter, hasDeadlineFilter]
          .filter(Boolean).length;

      // Użyj dedykowanych endpointów tylko gdy jest dokładnie jeden filtr
      if (filterCount === 1) {
        loading.value = true;

        try {
          // Filtrowanie po zespole
          if (hasTeamFilter) {
            const teamTasks = await taskService.getTasksByTeamId(filters.value.team);
            tasks.value = teamTasks;
          }
          // Filtrowanie po priorytecie
          else if (hasPriorityFilter) {
            const priorityTasks = await taskService.getTasksByPriorityId(filters.value.priority);
            tasks.value = priorityTasks;
          }
          // Filtrowanie po statusie
          else if (hasStatusFilter) {
            const statusTasks = await taskService.getTasksByStatusId(filters.value.status);
            tasks.value = statusTasks;
          }
          // Filtrowanie po deadline
          else if (hasDeadlineFilter) {
            const deadlineTasks = await taskService.getTasksWithDeadlineBefore(filters.value.deadline);
            tasks.value = deadlineTasks;
          }
        } catch (err) {
          console.error('Błąd podczas filtrowania zadań przez API:', err);
          error.value = `Nie udało się pobrać zadań: ${err.message}`;

          // W przypadku błędu pobierz wszystkie zadania i filtruj lokalnie
          await fetchTasks();
        } finally {
          loading.value = false;
        }
      }
      // Dla wielu filtrów pobierz wszystkie zadania i filtruj lokalnie
      else if (filterCount > 1) {
        // Pobierz wszystkie zadania i filtruj lokalnie
        await fetchTasks();
      }
    };

    // Otwieranie szczegółów zadania
    const openTaskDetails = (task) => {
      router.push({ name: 'taskDetails', params: { id: task.id.toString() } });
    };

    // Funkcje pomocnicze
    const getTeamName = (teamData) => {
      if (!teamData) return 'Nieznany zespół';

      // Jeśli to obiekt zespołu
      if (typeof teamData === 'object' && teamData.name) {
        return teamData.name;
      }

      // Jeśli to ID zespołu
      const teamId = Number(teamData);
      const foundTeam = teams.value.find(t => t.id === teamId);
      return foundTeam ? foundTeam.name : `Zespół #${teamId}`;
    };

    // Tekstowa reprezentacja priorytetu
    const getPriorityText = (priorityId) => {
      if (priorityId === null || priorityId === undefined) return 'Średni';

      // Znajdź priorytet po ID
      const foundPriority = priorities.value.find(p => p.id === Number(priorityId));
      if (foundPriority) return foundPriority.name;

      // Awaryjne mapowanie jeśli nie znaleziono
      const priorityMap = {
        1: 'Niski',
        2: 'Średni',
        3: 'Wysoki'
      };

      return priorityMap[priorityId] || 'Średni';
    };

    // Klasa CSS dla priorytetu
    const getPriorityClass = (priorityId) => {
      if (priorityId === null || priorityId === undefined) return 'bg-yellow-500'; // Domyślny

      // Mapowanie ID priorytetów na klasy CSS
      const priorityClasses = {
        1: 'bg-blue-500',   // Niski
        2: 'bg-yellow-500', // Średni
        3: 'bg-red-500'     // Wysoki
      };

      return priorityClasses[priorityId] || 'bg-yellow-500';
    };

    // Tekstowa reprezentacja statusu
    const getStatusText = (statusId) => {
      if (statusId === null || statusId === undefined) return 'Rozpoczęte';

      // Znajdź status po ID
      const foundStatus = statuses.value.find(s => s.id === Number(statusId));
      if (foundStatus) return foundStatus.name;

      // Awaryjne mapowanie jeśli nie znaleziono
      const statusMap = {
        1: 'Rozpoczęte',
        2: 'W toku',
        3: 'Zakończone'
      };

      return statusMap[statusId] || 'Rozpoczęte';
    };

    // Klasa CSS dla statusu
    const getStatusClass = (statusId) => {
      if (statusId === null || statusId === undefined) return 'bg-blue-100 text-blue-800'; // Domyślny

      // Mapowanie ID statusów na klasy CSS
      const statusClasses = {
        1: 'bg-blue-100 text-blue-800',      // Rozpoczęte
        2: 'bg-yellow-100 text-yellow-800',  // W toku
        3: 'bg-green-100 text-green-800'     // Zakończone
      };

      return statusClasses[statusId] || 'bg-blue-100 text-blue-800';
    };

    const formatDate = (dateString) => {
      if (!dateString) return 'Brak terminu';

      try {
        const date = new Date(dateString);
        return date.toLocaleDateString('pl-PL');
      } catch (err) {
        return dateString;
      }
    };

    // Monitoruj zmiany filtrów dla trybu debugowania
    watch(filters, (newFilters) => {
      console.log('Zmiana filtrów:', newFilters);
    });

    // Inicjalizacja komponentu
    onMounted(async () => {
      // Pobierz dane referencyjne (zespoły, priorytety, statusy)
      await fetchReferenceData();
      // Pobierz zadania
      await fetchTasks();
    });

    return {
      tasks,
      teams,
      priorities,
      statuses,
      loading,
      error,
      filters,
      isDebugMode,
      filteredTasks,
      resetFilters,
      applyFilters,
      openTaskDetails,
      getTeamName,
      getPriorityText,
      getStatusText,
      getPriorityClass,
      getStatusClass,
      formatDate,
      fetchTasks
    };
  }
};
</script>