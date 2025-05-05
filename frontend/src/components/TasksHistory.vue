<template>
  <div class="bg-background text-text min-h-screen p-6">
    <h1 class="text-3xl font-bold text-primary mb-6">Historia Zadań</h1>

    <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div class="flex flex-col">
        <label class="mb-1 font-medium">Zespół:</label>
        <select v-model="filters.team" class="p-2 border border-gray-300 rounded-md bg-white text-text">
          <option value="">Wszystkie</option>
          <option v-for="team in teams" :key="team.id" :value="team.id">
            {{ team.name }}
          </option>
        </select>
      </div>

      <div class="flex flex-col">
        <label class="mb-1 font-medium">Priorytet:</label>
        <select v-model="filters.priority" class="p-2 border border-gray-300 rounded-md bg-white text-text">
          <option value="">Wszystkie</option>
          <option v-for="priority in priorities" :key="priority.id" :value="priority.id">
            {{ priority.name }}
          </option>
        </select>
      </div>

      <div class="flex flex-col">
        <label class="mb-1 font-medium">Status:</label>
        <select v-model="filters.status" class="p-2 border border-gray-300 rounded-md bg-white text-text">
          <option value="">Wszystkie</option>
          <option v-for="status in statuses" :key="status.id" :value="status.id">
            {{ status.name }}
          </option>
        </select>
      </div>

      <div class="flex flex-col">
        <label class="mb-1 font-medium">Deadline:</label>
        <input type="date" v-model="filters.deadline" class="p-2 border border-gray-300 rounded-md bg-white text-text" />
      </div>
    </div>

    <button @click="filterTasks" class="mb-6 bg-primary text-white px-6 py-2 rounded-md hover:bg-secondary transition">Filtruj</button>

    <div v-if="loading" class="flex justify-center items-center h-64">
      <p class="text-primary text-xl">Ładowanie zadań...</p>
    </div>

    <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6">
      <p>{{ error }}</p>
      <button @click="filterTasks" class="mt-2 bg-primary text-white px-4 py-1 rounded-md">
        Spróbuj ponownie
      </button>
    </div>

    <div v-else-if="filteredTasks.length === 0" class="text-center p-8 bg-surface rounded-lg border border-gray-200">
      <p>Brak zadań spełniających wybrane kryteria</p>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="task in filteredTasks" :key="task.id" class="bg-surface border border-gray-200 p-4 rounded-lg shadow hover:scale-105 transition">
        <div>
          <h3 class="text-xl font-semibold text-primary">{{ task.title }}</h3>
          <p class="text-muted">{{ task.description || 'Brak opisu' }}</p>
          <div class="mt-2 flex items-center">
            <span
                class="px-2 py-1 rounded-md text-xs font-bold text-white mr-2"
                :style="{ backgroundColor: getPriorityColor(task.priorityId) }"
            >
              {{ getPriorityName(task.priorityId) }}
            </span>
            <span class="text-xs">Deadline: {{ formatDate(task.deadline) }}</span>
          </div>
        </div>
        <button @click="openTaskDetails(task)" class="mt-4 bg-primary text-white px-4 py-2 rounded-md hover:bg-secondary transition">Szczegóły</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue';
import taskService from '../services/taskService';
import teamService from '../services/teamService';
import priorityService from '../services/priorityService';
import taskStatusService from '../services/taskStatusService';

export default {
  setup() {
    const tasks = ref([]);
    const teams = ref([]);
    const priorities = ref([]);
    const statuses = ref([]);
    const loading = ref(true);
    const error = ref(null);

    const filters = reactive({
      team: "",
      priority: "",
      status: "",
      deadline: ""
    });

    // Compute filtered tasks based on current filters
    const filteredTasks = computed(() => {
      return tasks.value.filter(task => {
        return (
            (!filters.team || task.teamId == filters.team) &&
            (!filters.priority || task.priorityId == filters.priority) &&
            (!filters.status || task.statusId == filters.status) &&
            (!filters.deadline || new Date(task.deadline) <= new Date(filters.deadline))
        );
      });
    });

    // Load initial data
    const loadInitialData = async () => {
      loading.value = true;
      error.value = null;

      try {
        // Fetch reference data for filters
        const [teamsData, prioritiesData, statusesData] = await Promise.all([
          teamService.getAllTeams(),
          priorityService.getAllPriorities(),
          taskStatusService.getAllTaskStatuses()
        ]);

        teams.value = teamsData;
        priorities.value = prioritiesData;
        statuses.value = statusesData;

        // Initial load of all tasks
        await fetchAllTasks();
      } catch (err) {
        console.error('Error loading initial data:', err);
        error.value = `Błąd podczas ładowania danych: ${err.message}`;

        // Fallback to demo data
        teams.value = [
          { id: 1, name: 'Frontend Devs' },
          { id: 2, name: 'Backend Engineers' },
          { id: 3, name: 'QA Team' },
          { id: 4, name: 'Design Team' }
        ];

        priorities.value = [
          { id: 1, name: 'Niski', value: 1, colorCode: '#28a745' },
          { id: 2, name: 'Średni', value: 2, colorCode: '#ffc107' },
          { id: 3, name: 'Wysoki', value: 3, colorCode: '#dc3545' }
        ];

        statuses.value = [
          { id: 1, name: 'Rozpoczęte', progressMin: 0, progressMax: 30 },
          { id: 2, name: 'W toku', progressMin: 31, progressMax: 70 },
          { id: 3, name: 'Zakończony', progressMin: 71, progressMax: 100 }
        ];

        // Demo tasks
        tasks.value = [
          {
            id: 1,
            title: "Zadanie 1",
            description: "Praca nad frontendem aplikacji.",
            teamId: 1,
            priorityId: 3,
            statusId: 1,
            deadline: "2025-03-20"
          },
          {
            id: 2,
            title: "Zadanie 2",
            description: "Rozwój backendu aplikacji.",
            teamId: 2,
            priorityId: 2,
            statusId: 2,
            deadline: "2025-03-25"
          },
          {
            id: 3,
            title: "Zadanie 3",
            description: "Testowanie aplikacji.",
            teamId: 3,
            priorityId: 1,
            statusId: 3,
            deadline: "2025-03-15"
          }
        ];
      } finally {
        loading.value = false;
      }
    };

    // Fetch all tasks
    const fetchAllTasks = async () => {
      loading.value = true;
      error.value = null;

      try {
        tasks.value = await taskService.getAllTasks();
      } catch (err) {
        console.error('Error fetching tasks:', err);
        error.value = `Błąd podczas ładowania zadań: ${err.message}`;
      } finally {
        loading.value = false;
      }
    };

    // Apply filters and fetch filtered tasks
    const filterTasks = async () => {
      loading.value = true;
      error.value = null;

      try {
        // If only one filter is active, use the specific endpoint
        if (filters.team && !filters.priority && !filters.status && !filters.deadline) {
          tasks.value = await taskService.getTasksByTeamId(filters.team);
        } else if (!filters.team && filters.priority && !filters.status && !filters.deadline) {
          tasks.value = await taskService.getTasksByPriorityId(filters.priority);
        } else if (!filters.team && !filters.priority && filters.status && !filters.deadline) {
          tasks.value = await taskService.getTasksByStatusId(filters.status);
        } else if (!filters.team && !filters.priority && !filters.status && filters.deadline) {
          tasks.value = await taskService.getTasksWithDeadlineBefore(filters.deadline);
        } else if (Object.values(filters).every(f => !f)) {
          // If all filters are empty, fetch all tasks
          await fetchAllTasks();
        } else {
          // For multiple active filters, fetch all and filter client-side
          await fetchAllTasks();
        }
      } catch (err) {
        console.error('Error applying filters:', err);
        error.value = `Błąd podczas filtrowania zadań: ${err.message}`;
      } finally {
        loading.value = false;
      }
    };

    // Helper methods
    const getPriorityName = (priorityId) => {
      const priority = priorities.value.find(p => p.id == priorityId);
      return priority ? priority.name : 'Nieznany';
    };

    const getPriorityColor = (priorityId) => {
      const priority = priorities.value.find(p => p.id == priorityId);
      return priority ? priority.colorCode || '#999' : '#999';
    };

    const formatDate = (dateString) => {
      if (!dateString) return 'Brak terminu';

      try {
        const date = new Date(dateString);
        return date.toLocaleDateString('pl-PL');
      } catch (e) {
        return dateString;
      }
    };

    const openTaskDetails = (task) => {
      // For future implementation, navigate to task details
      alert(`Otwieram szczegóły zadania: ${task.title}`);
    };

    // Initialize component
    onMounted(loadInitialData);

    return {
      tasks,
      teams,
      priorities,
      statuses,
      filters,
      filteredTasks,
      loading,
      error,
      filterTasks,
      getPriorityName,
      getPriorityColor,
      formatDate,
      openTaskDetails
    };
  }
};
</script>