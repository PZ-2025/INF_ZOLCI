<template>
  <div class="min-h-screen bg-background text-text flex justify-center items-start px-4 py-10">
    <div class="bg-surface rounded-lg shadow-md border border-gray-200 p-6 w-full space-y-8">

      <h1 class="text-3xl font-bold text-primary">Historia Zadań</h1>

      <!-- Filtry -->
      <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="flex flex-col">
          <label class="mb-1 font-medium text-sm">Zespół:</label>
          <select v-model="activeFilters.team"
                  class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="">Wszystkie</option>
            <option v-for="team in teams" :key="team.id" :value="team.id">{{ team.name }}</option>
          </select>
        </div>

        <div class="flex flex-col">
          <label class="mb-1 font-medium text-sm">Priorytet:</label>
          <select v-model="activeFilters.priority"
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
          <select v-model="activeFilters.status"
                  class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="">Wszystkie</option>
            <option value="open">Rozpoczęte</option>
            <option value="in_progress">W toku</option>
            <option value="completed">Zakończone</option>
          </select>
        </div>

        <div class="flex flex-col">
          <label class="mb-1 font-medium text-sm">Deadline:</label>
          <input type="date" v-model="activeFilters.deadline"
                 class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
      </div>

      <div class="flex justify-between">
        <div class="flex items-center gap-2">
          <span class="text-sm text-muted">Znaleziono zadań: {{ filteredTasks.length }}</span>
          <button @click="resetFilters"
                  class="text-sm text-primary hover:text-secondary underline"
                  v-if="isFiltered">
            Resetuj filtry
          </button>
        </div>
        <button @click="refreshData"
                class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-md transition"
        >
          Odśwież dane
        </button>
      </div>

      <!-- Stan ładowania -->
      <div v-if="loading" class="flex justify-center items-center py-12">
        <p class="text-primary text-xl">Ładowanie zadań...</p>
      </div>

      <!-- Komunikat o błędzie -->
      <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 my-6">
        <p>{{ error }}</p>
        <button @click="fetchAllData" class="mt-2 bg-primary text-white px-4 py-1 rounded-md">
          Spróbuj ponownie
        </button>
      </div>

      <!-- Panel do debugowania -->
      <div v-if="showDebug" class="bg-gray-100 p-4 rounded-md text-sm">
        <h4 class="font-bold">Dane pierwszego zadania:</h4>
        <pre class="text-xs overflow-auto">{{ JSON.stringify(allTasks[0], null, 2) }}</pre>
        <button @click="showDebug = false" class="text-primary text-sm mt-2">Ukryj debugowanie</button>
      </div>

      <!-- Lista zadań -->
      <div v-else-if="filteredTasks.length === 0" class="text-center py-8 text-muted">
        <p>Brak zadań spełniających kryteria filtrowania</p>
        <button @click="showDebug = true" class="text-primary text-sm mt-2">Pokaż dane debugowania</button>
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
                  'bg-red-500': getPriorityValue(task) === 'high',
                  'bg-yellow-500': getPriorityValue(task) === 'medium',
                  'bg-blue-500': getPriorityValue(task) === 'low'
                }"
              >
                {{ getPriorityText(getPriorityValue(task)) }}
              </span>
              <span
                  class="text-xs px-2 py-1 rounded"
                  :class="{
                  'bg-green-100 text-green-800': getStatusValue(task) === 'completed',
                  'bg-yellow-100 text-yellow-800': getStatusValue(task) === 'in_progress',
                  'bg-blue-100 text-blue-800': getStatusValue(task) === 'open'
                }"
              >
                {{ getStatusText(getStatusValue(task)) }}
              </span>
            </div>
            <p v-if="task.deadline" class="text-xs text-muted mt-1">
              Termin: {{ formatDate(task.deadline) }}
            </p>
            <!-- Przycisk debugowania dla pojedynczego zadania -->
            <button @click="debugTask(task)" class="text-xs text-gray-400 mt-1">
              Debug
            </button>
          </div>
          <button @click="openTaskDetails(task)"
                  class="mt-4 bg-primary hover:bg-secondary text-white px-4 py-2 rounded-md transition w-full"
          >
            Szczegóły
          </button>
        </div>
      </div>

      <div class="flex justify-end pt-6">
        <router-link
            to="/addtask"
            class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-md transition"
        >
          Dodaj Zadanie
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import taskService from '../services/taskService';
import teamService from '../services/teamService';

export default {
  setup() {
    const router = useRouter();

    // Stan aplikacji
    const allTasks = ref([]); // Wszystkie zadania pobrane z API
    const teams = ref([]);    // Wszystkie zespoły
    const loading = ref(true);
    const error = ref(null);
    const showDebug = ref(false);

    // Aktywne filtry jako reaktywny obiekt
    const activeFilters = reactive({
      team: "",
      priority: "",
      status: "",
      deadline: ""
    });

    // Sprawdzenie, czy jakiekolwiek filtry są aktywne
    const isFiltered = computed(() => {
      return Object.values(activeFilters).some(value => value !== "");
    });

    // Funkcja pomocnicza do debugowania pojedynczego zadania
    const debugTask = (task) => {
      console.log('Debugowanie zadania:', task);
      console.log('Priority:', task.priority, task.priorityId);
      console.log('Status:', task.status, task.statusId);
      alert(`Id: ${task.id}, Priority: ${JSON.stringify(task.priority || task.priorityId)}, Status: ${JSON.stringify(task.status || task.statusId)}`);
    };

    // Główne funkcje do pobierania wartości priorytetów i statusów
    // Wersja najprostsza: sprawdza najpierw priorityId, potem priority
    const getPriorityValue = (task) => {
      // Najpierw sprawdź, czy mamy priorityId (liczba)
      if (task.priorityId !== undefined) {
        const map = { 1: 'low', 2: 'medium', 3: 'high' };
        return map[task.priorityId] || 'medium';
      }

      // Jeśli nie, spróbuj użyć property priority
      return getPriorityName(task.priority);
    };

    const getStatusValue = (task) => {
      // Najpierw sprawdź, czy mamy statusId (liczba)
      if (task.statusId !== undefined) {
        const map = { 1: 'open', 2: 'in_progress', 3: 'completed' };
        return map[task.statusId] || 'open';
      }

      // Jeśli nie, spróbuj użyć property status
      return getStatusName(task.status);
    };

    // Filtrowanie zadań na podstawie aktywnych filtrów
    const filteredTasks = computed(() => {
      // Jeśli nie ma aktywnych filtrów, zwróć wszystkie zadania
      if (!isFiltered.value) {
        return allTasks.value;
      }

      // Filtruj zadania według aktywnych filtrów
      return allTasks.value.filter(task => {
        // Filtrowanie po zespole
        if (activeFilters.team &&
            task.teamId !== parseInt(activeFilters.team) &&
            task.team?.id !== parseInt(activeFilters.team)) {
          return false;
        }

        // Filtrowanie po priorytecie
        if (activeFilters.priority &&
            getPriorityValue(task) !== activeFilters.priority) {
          return false;
        }

        // Filtrowanie po statusie
        if (activeFilters.status &&
            getStatusValue(task) !== activeFilters.status) {
          return false;
        }

        // Filtrowanie po deadline
        if (activeFilters.deadline &&
            task.deadline?.split('T')[0] !== activeFilters.deadline) {
          return false;
        }

        // Zadanie przeszło wszystkie filtry
        return true;
      });
    });

    // Pobranie wszystkich danych potrzebnych do aplikacji
    const fetchAllData = async () => {
      loading.value = true;
      error.value = null;

      try {
        // Równoległe pobieranie zadań i zespołów
        const [tasksResponse, teamsResponse] = await Promise.all([
          taskService.getAllTasks(),
          teamService.getAllTeams()
        ]);

        allTasks.value = tasksResponse;
        teams.value = teamsResponse;

        console.log('Pierwsze zadanie:', JSON.stringify(allTasks.value[0], null, 2));

        // Sprawdź strukturę danych zadań
        if (allTasks.value.length > 0) {
          const task = allTasks.value[0];
          console.log('Struktura zadania:', {
            hasStatusId: task.statusId !== undefined,
            hasStatus: task.status !== undefined,
            hasPriorityId: task.priorityId !== undefined,
            hasPriority: task.priority !== undefined
          });
        }
      } catch (err) {
        console.error('Błąd podczas pobierania danych:', err);
        error.value = `Nie udało się pobrać danych: ${err.message}`;

        // Dane demonstracyjne w przypadku błędu
        allTasks.value = [
          { id: 1, title: "Zadanie 1", description: "Praca nad frontendem aplikacji.", teamId: 1, priorityId: 3, statusId: 1, deadline: "2025-03-20" },
          { id: 2, title: "Zadanie 2", description: "Rozwój backendu aplikacji.", teamId: 2, priorityId: 2, statusId: 2, deadline: "2025-03-25" },
          { id: 3, title: "Zadanie 3", description: "Testowanie aplikacji.", teamId: 3, priorityId: 1, statusId: 3, deadline: "2025-03-15" },
          { id: 4, title: "Zadanie 4", description: "Code review aplikacji.", teamId: 1, priorityId: 3, statusId: 2, deadline: "2025-03-22" },
          { id: 5, title: "Zadanie 5", description: "Poprawa wydajności aplikacji.", teamId: 2, priorityId: 2, statusId: 1, deadline: "2025-03-28" },
          { id: 6, title: "Zadanie 6", description: "Testy użyteczności.", teamId: 3, priorityId: 1, statusId: 2, deadline: "2025-03-18" }
        ];

        teams.value = [
          { id: 1, name: 'Frontend Devs' },
          { id: 2, name: 'Backend Engineers' },
          { id: 3, name: 'QA Team' },
          { id: 4, name: 'Design Team' }
        ];
      } finally {
        loading.value = false;
      }
    };

    // Odświeżenie danych z serwera
    const refreshData = () => {
      fetchAllData();
    };

    // Resetowanie wszystkich filtrów
    const resetFilters = () => {
      Object.keys(activeFilters).forEach(key => {
        activeFilters[key] = "";
      });
    };

    // Otwieranie szczegółów zadania
    const openTaskDetails = (task) => {
      router.push({ name: 'taskDetails', params: { id: task.id.toString() } });
    };

    // Funkcje pomocnicze do prezentacji danych
    const getTeamName = (team) => {
      if (!team) return 'Nieznany';
      if (typeof team === 'object' && team.name) return team.name;

      // Jeśli mamy tylko ID zespołu, poszukajmy w liście zespołów
      const foundTeam = teams.value.find(t => t.id === parseInt(team));
      return foundTeam ? foundTeam.name : `Zespół #${team}`;
    };

    const getPriorityName = (priority) => {
      if (priority === undefined || priority === null) return 'medium';

      // Jeśli priorytet jest już stringiem (np. 'low', 'medium', 'high')
      if (typeof priority === 'string') {
        // Sprawdź, czy nie jest to string z liczbą
        if (['1', '2', '3'].includes(priority)) {
          const priorityMap = { '1': 'low', '2': 'medium', '3': 'high' };
          return priorityMap[priority];
        }
        return priority;
      }

      // Jeśli priorytet jest liczbą
      if (typeof priority === 'number') {
        const priorityMap = { 1: 'low', 2: 'medium', 3: 'high' };
        if (priority in priorityMap) {
          return priorityMap[priority];
        }
      }

      // Jeśli priorytet jest obiektem (np. { id: 2, name: 'Średni' } lub { value: 2 })
      if (typeof priority === 'object') {
        if (priority.value !== undefined) {
          const priorityMap = { 1: 'low', 2: 'medium', 3: 'high' };
          return priorityMap[priority.value] || 'medium';
        }
        if (priority.id !== undefined) {
          const priorityMap = { 1: 'low', 2: 'medium', 3: 'high' };
          return priorityMap[priority.id] || 'medium';
        }
        if (priority.priorityId !== undefined) {
          const priorityMap = { 1: 'low', 2: 'medium', 3: 'high' };
          return priorityMap[priority.priorityId] || 'medium';
        }
      }

      // W ostateczności zwróć 'medium'
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
      if (status === undefined || status === null) return 'open';

      // Jeśli status jest już stringiem (np. 'open', 'in_progress', 'completed')
      if (typeof status === 'string') {
        // Sprawdź, czy nie jest to string z liczbą
        if (['1', '2', '3'].includes(status)) {
          const statusMap = { '1': 'open', '2': 'in_progress', '3': 'completed' };
          return statusMap[status];
        }
        return status;
      }

      // Jeśli status jest liczbą
      if (typeof status === 'number') {
        const statusMap = { 1: 'open', 2: 'in_progress', 3: 'completed' };
        if (status in statusMap) {
          return statusMap[status];
        }
      }

      // Jeśli status jest obiektem (np. { id: 1, name: 'Rozpoczęte' } lub { value: 1 })
      if (typeof status === 'object') {
        if (status.value !== undefined) {
          const statusMap = { 1: 'open', 2: 'in_progress', 3: 'completed' };
          return statusMap[status.value] || 'open';
        }
        if (status.id !== undefined) {
          const statusMap = { 1: 'open', 2: 'in_progress', 3: 'completed' };
          return statusMap[status.id] || 'open';
        }
        if (status.statusId !== undefined) {
          const statusMap = { 1: 'open', 2: 'in_progress', 3: 'completed' };
          return statusMap[status.statusId] || 'open';
        }
      }

      // W ostateczności zwróć 'open'
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

    // Wywołaj pobieranie danych przy montowaniu komponentu
    onMounted(() => {
      fetchAllData();
    });

    // Obserwowanie zmian w filtrach
    watch(activeFilters, (newFilters) => {
      console.log('Zmieniono filtry:', newFilters);
      console.log('Liczba znalezionych zadań:', filteredTasks.value.length);
    }, { deep: true });

    return {
      // Dane
      allTasks,
      teams,
      loading,
      error,
      activeFilters,
      filteredTasks,
      isFiltered,
      showDebug,

      // Metody
      fetchAllData,
      refreshData,
      resetFilters,
      openTaskDetails,
      debugTask,

      // Funkcje pomocnicze
      getTeamName,
      getPriorityName,
      getPriorityText,
      getStatusName,
      getStatusText,
      formatDate,
      getPriorityValue,
      getStatusValue
    };
  }
};
</script>