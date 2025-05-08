<template>
  <div class="min-h-screen bg-background text-text flex justify-center items-start px-4 py-10">
    <div class="bg-surface rounded-lg shadow-md border border-gray-200 p-6 w-full space-y-8">
      <div class="flex justify-between items-center">
        <h1 class="text-3xl font-bold text-primary">Historia Zadań</h1>
        <router-link
            to="/addtask"
            class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-md transition"
        >
          Dodaj Zadanie
        </router-link>
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
            <option value="high">Wysoki</option>
            <option value="medium">Średni</option>
            <option value="low">Niski</option>
          </select>
        </div>

        <div class="flex flex-col">
          <label class="mb-1 font-medium text-sm">Status:</label>
          <select v-model="filters.status"
                  class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="">Wszystkie</option>
            <option value="open">Rozpoczęte</option>
            <option value="in_progress">W toku</option>
            <option value="completed">Zakończony</option>
          </select>
        </div>

        <div class="flex flex-col">
          <label class="mb-1 font-medium text-sm">Deadline:</label>
          <input type="date" v-model="filters.deadline"
                 class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
      </div>

      <div class="flex justify-end">
        <button @click="applyFilters"
                class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-md transition"
        >
          Filtruj
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

      <!-- Lista zadań -->
      <div v-else-if="filteredTasks.length === 0" class="text-center py-8 text-muted">
        <p>Brak zadań spełniających kryteria filtrowania</p>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
            v-for="task in filteredTasks"
            :key="task.id"
            class="bg-white border border-gray-200 p-4 rounded-lg shadow-sm transition hover:shadow-lg"
        >
          <div>
            <h3 class="text-xl font-semibold text-primary">{{ task.title || task.name }}</h3>
            <p class="text-sm text-muted">{{ task.description }}</p>
            <div class="mt-2 flex flex-wrap gap-2">
              <span class="text-xs px-2 py-1 rounded bg-gray-100">
                Zespół: {{ getTeamName(task.team || task.teamId) }}
              </span>
              <span
                  class="text-xs px-2 py-1 rounded text-white"
                  :class="{
                  'bg-red-500': getPriorityName(task.priorityId) === 'high',
                  'bg-yellow-500': getPriorityName(task.priorityId) === 'medium',
                  'bg-blue-500': getPriorityName(task.priorityId) === 'low'
                  }"
              >
                  {{ getPriorityText(getPriorityName(task.priorityId)) }}
              </span>
              <span
                  class="text-xs px-2 py-1 rounded"
                  :class="{
                  'bg-green-100 text-green-800': getStatusName(task.statusId) === 'completed',
                  'bg-yellow-100 text-yellow-800': getStatusName(task.statusId) === 'in_progress',
                  'bg-blue-100 text-blue-800': getStatusName(task.statusId) === 'open'
                  }"
              >
                  {{ getStatusText(getStatusName(task.statusId)) }}
              </span>
            </div>
            <p v-if="task.deadline" class="text-xs text-muted mt-1">
              Termin: {{ formatDate(task.deadline) }}
            </p>
          </div>
          <button @click="openTaskDetails(task)"
                  class="mt-4 bg-primary hover:bg-secondary text-white px-4 py-2 rounded-md transition w-full"
          >
            Szczegóły
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import taskService from '../services/taskService';
import teamService from '../services/teamService';

export default {
  setup() {
    const router = useRouter();
    const tasks = ref([]);
    const teams = ref([]);
    const loading = ref(true);
    const error = ref(null);
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
        console.log('Otrzymane zadania z API:', JSON.stringify(response, null, 2));
        tasks.value = response;
      } catch (err) {
        console.error('Błąd podczas pobierania zadań:', err);
        error.value = `Nie udało się pobrać zadań: ${err.message}`;
      } finally {
        loading.value = false;
      }
    };

    // Pobieranie zespołów dla filtru
    const fetchTeams = async () => {
      try {
        const response = await teamService.getAllTeams();
        teams.value = response;
      } catch (err) {
        console.error('Błąd podczas pobierania zespołów:', err);
        teams.value = [
          { id: 1, name: 'Zespół szybkiego reagowania A1' },
          { id: 2, name: 'Zespół ekspertów budowlanych E1' },
          { id: 3, name: 'Zespół szybkiego reagowania A2' }
        ];
      }
    };

    // Filtrowanie zadań na podstawie wybranych filtrów
    const filteredTasks = computed(() => {
      return tasks.value.filter(task => {
        const teamMatch = !filters.value.team ||
            (task.team?.id === filters.value.team) ||
            (task.teamId === filters.value.team);

        const priorityMatch = !filters.value.priority ||
            getPriorityName(task.priorityId) === filters.value.priority;

        const statusMatch = !filters.value.status ||
            getStatusName(task.statusId) === filters.value.status;

        const deadlineMatch = !filters.value.deadline ||
            task.deadline?.split('T')[0] === filters.value.deadline;

        return teamMatch && priorityMatch && statusMatch && deadlineMatch;
      });
    });

    // Aplikowanie filtrów na serwerze (w przypadku dużej ilości danych)
    const applyFilters = async () => {
      console.log("Zastosowano filtry:", filters.value);

      // Jeśli mamy tylko filtr zespołu, możemy użyć dedykowanego endpointu
      if (filters.value.team && !filters.value.priority && !filters.value.status && !filters.value.deadline) {
        loading.value = true;
        try {
          const teamTasks = await taskService.getTasksByTeamId(filters.value.team);
          tasks.value = teamTasks;
        } catch (err) {
          console.error('Błąd podczas pobierania zadań zespołu:', err);
          error.value = `Nie udało się pobrać zadań zespołu: ${err.message}`;
        } finally {
          loading.value = false;
        }
      }
      // Jeśli mamy tylko filtr priorytetu, możemy użyć dedykowanego endpointu
      else if (filters.value.priority && !filters.value.team && !filters.value.status && !filters.value.deadline) {
        loading.value = true;
        try {
          // Przekształć nazwę priorytetu na ID
          const priorityIds = { 'low': 1, 'medium': 2, 'high': 3 };
          const priorityId = priorityIds[filters.value.priority];

          if (priorityId) {
            const priorityTasks = await taskService.getTasksByPriorityId(priorityId);
            tasks.value = priorityTasks;
          }
        } catch (err) {
          console.error('Błąd podczas pobierania zadań o danym priorytecie:', err);
          error.value = `Nie udało się pobrać zadań: ${err.message}`;
        } finally {
          loading.value = false;
        }
      }
      // Jeśli mamy tylko filtr statusu, możemy użyć dedykowanego endpointu
      else if (filters.value.status && !filters.value.team && !filters.value.priority && !filters.value.deadline) {
        loading.value = true;
        try {
          // Przekształć nazwę statusu na ID
          const statusIds = { 'open': 1, 'in_progress': 2, 'completed': 3 };
          const statusId = statusIds[filters.value.status];

          if (statusId) {
            const statusTasks = await taskService.getTasksByStatusId(statusId);
            tasks.value = statusTasks;
          }
        } catch (err) {
          console.error('Błąd podczas pobierania zadań o danym statusie:', err);
          error.value = `Nie udało się pobrać zadań: ${err.message}`;
        } finally {
          loading.value = false;
        }
      }
      // Jeśli mamy tylko filtr deadline, możemy użyć dedykowanego endpointu
      else if (filters.value.deadline && !filters.value.team && !filters.value.priority && !filters.value.status) {
        loading.value = true;
        try {
          const deadlineTasks = await taskService.getTasksWithDeadlineBefore(filters.value.deadline);
          tasks.value = deadlineTasks;
        } catch (err) {
          console.error('Błąd podczas pobierania zadań z terminem:', err);
          error.value = `Nie udało się pobrać zadań: ${err.message}`;
        } finally {
          loading.value = false;
        }
      }
      // W przeciwnym razie pobieramy wszystkie zadania i filtrujemy lokalnie
      else {
        fetchTasks();
      }
    };

    // Otwieranie szczegółów zadania
    const openTaskDetails = (task) => {
      router.push({ name: 'taskDetails', params: { id: task.id.toString() } });
    };

    // Funkcje pomocnicze
    const getTeamName = (team) => {
      if (!team) return 'Nieznany';
      if (typeof team === 'object' && team.name) return team.name;

      // Jeśli mamy tylko ID zespołu, poszukajmy w liście zespołów
      const foundTeam = teams.value.find(t => t.id === team);
      return foundTeam ? foundTeam.name : `Zespół #${team}`;
    };

    const getPriorityName = (priority) => {
      if (priority === null || priority === undefined) return 'medium';

      if (typeof priority === 'number') {
        const priorityMap = {
          1: 'low',
          2: 'medium',
          3: 'high'
        };
        return priorityMap[priority] || 'medium';
      }

      return 'medium';
    };

    const getPriorityText = (priority) => {
      const priorityTexts = {
        'low': 'Niski',
        'medium': 'Średni',
        'high': 'Wysoki'
      };
      return priorityTexts[priority] || 'Średni';
    };

    const getStatusName = (status) => {
      if (status === null || status === undefined) return 'open';

      if (typeof status === 'number') {
        const statusMap = {
          1: 'open',
          2: 'in_progress',
          3: 'completed'
        };
        return statusMap[status] || 'open';
      }

      return 'open';
    };

    const getStatusText = (status) => {
      const statusTexts = {
        'open': 'Rozpoczęte',
        'in_progress': 'W toku',
        'completed': 'Zakończone'
      };
      return statusTexts[status] || 'Rozpoczęte';
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

    // Inicjalizacja komponentu
    onMounted(() => {
      fetchTasks();
      fetchTeams();
    });

    return {
      tasks,
      teams,
      loading,
      error,
      filters,
      filteredTasks,
      applyFilters,
      openTaskDetails,
      getTeamName,
      getPriorityName,
      getPriorityText,
      getStatusName,
      getStatusText,
      formatDate,
      fetchTasks
    };
  }
};
</script>