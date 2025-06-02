<template>
  <div class="bg-background text-text min-h-screen p-6">
    <h1 class="text-3xl text-left font-bold text-primary mb-6">Historia Zadań</h1>

    <!-- Stan ładowania -->
    <div v-if="loading" class="flex justify-center items-center h-64">
      <p class="text-primary text-xl">Ładowanie zadań...</p>
    </div>

    <!-- Komunikat o błędzie -->
    <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6">
      <p>{{ error }}</p>
      <button @click="fetchTasks" class="mt-2 bg-primary text-white px-4 py-1 rounded-md">
        Spróbuj ponownie
      </button>
    </div>

    <div v-else>
      <!-- Sekcja filtrów i sortowania -->
      <div class="bg-surface p-4 rounded-lg shadow-md border border-gray-200 mb-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-semibold">Filtrowanie i sortowanie zadań</h2>
          <div class="flex items-center gap-4">
            <label class="font-medium text-sm">Sortuj według:</label>
            <select v-model="sortBy"
                    class="p-2 border border-gray-300 rounded-md bg-white text-text focus:ring-2 focus:ring-primary"
            >
              <option value="">Domyślnie</option>
              <option value="deadline">Termin (najbliższe)</option>
              <option value="overdue">Opóźnione najpierw</option>
              <option value="priority">Priorytet</option>
              <option value="status">Status</option>
            </select>
          </div>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 items-end relative">
          <div class="flex flex-col">
            <label for="deadlineFilter" class="block text-sm font-medium mb-1">Termin:</label>
            <div class="datepicker-container w-full">
              <Datepicker
                v-model="filters.deadline"
                class="w-full"
                inputClass="w-full p-2 border border-gray-300 rounded-md bg-white text-text focus:ring-2 focus:ring-primary focus:border-primary"
                format="yyyy-MM-dd"
                id="deadlineFilter"
                placeholder="Wybierz termin..."
                :clear-button="true"
                :auto-apply="true"
                :close-on-select="true"
                :close-on-auto-apply="true"
              />
            </div>
          </div>

          <div class="flex flex-col min-w-0">
            <label for="teamFilter" class="block text-sm font-medium mb-1">Zespół:</label>
            <select 
              id="teamFilter"
              v-model="filters.team"
              class="w-full p-2 border border-gray-300 rounded-md bg-white text-text focus:ring-2 focus:ring-primary focus:border-primary"
            >
              <option value="">Wszystkie zespoły</option>
              <option v-for="team in teams" :key="team.id" :value="team.id">{{ team.name }}</option>
            </select>
          </div>

          <div class="flex flex-col min-w-0">
            <label for="priorityFilter" class="block text-sm font-medium mb-1">Priorytet:</label>
            <select 
              id="priorityFilter"
              v-model="filters.priority"
              class="w-full p-2 border border-gray-300 rounded-md bg-white text-text focus:ring-2 focus:ring-primary focus:border-primary"
            >
              <option value="">Wszystkie priorytety</option>
              <option v-for="priority in priorities" :key="priority.id" :value="priority.id">
                {{ priority.name }}
              </option>
            </select>
          </div>

          <div class="flex flex-col min-w-0">
            <label for="statusFilter" class="block text-sm font-medium mb-1">Status:</label>
            <select 
              id="statusFilter"
              v-model="filters.status"
              class="w-full p-2 border border-gray-300 rounded-md bg-white text-text focus:ring-2 focus:ring-primary focus:border-primary"
            >
              <option value="">Wszystkie statusy</option>
              <option v-for="status in statuses" :key="status.id" :value="status.id">
                {{ status.name }}
              </option>
            </select>
          </div>
        </div>

        <div class="mt-4 flex justify-end">
          <button 
            @click="clearFilters" 
            class="bg-gray-500 text-white px-6 py-2 rounded-md hover:bg-gray-600 transition"
            :disabled="!hasActiveFilters"
            :class="{ 'opacity-50 cursor-not-allowed': !hasActiveFilters }"
          >
            Wyczyść filtry
          </button>
        </div>

        <!-- Podsumowanie filtrów -->
        <div v-if="hasActiveFilters" class="mt-4 p-3 bg-blue-50 border-l-4 border-blue-400 text-blue-700">
          <p class="text-sm">
            <strong>Aktywne filtry:</strong>
            <span v-if="filters.team" class="ml-2">Zespół: {{ getTeamName(filters.team) }}</span>
            <span v-if="filters.priority" class="ml-2">Priorytet: {{ getPriorityText(filters.priority) }}</span>
            <span v-if="filters.status" class="ml-2">Status: {{ getStatusText(filters.status) }}</span>
            <span v-if="filters.deadline" class="ml-2">Termin: {{ formatFilterDate(filters.deadline) }}</span>
          </p>
          <p class="text-sm mt-1">Znaleziono {{ filteredTasks.length }} z {{ tasks.length }} zadań</p>
        </div>

        <!-- Diagnostyka danych (dla debugowania) -->
        <div v-if="isDebugMode" class="mt-4 bg-gray-100 p-4 rounded-lg text-xs overflow-auto max-h-40">
          <p class="font-bold mb-2">Dane diagnostyczne:</p>
          <pre>{{ JSON.stringify({filters: filters, tasksCount: tasks.length, filteredCount: filteredTasks.length}, null, 2) }}</pre>
          <button @click="isDebugMode = false" class="text-xs text-primary mt-2">Ukryj</button>
        </div>
      </div>

      <!-- Lista zadań -->
      <div v-if="tasks.length === 0" class="text-center py-8 text-muted">
        <div class="bg-gray-100 p-6 rounded-lg">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 mx-auto mb-4 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
          <p class="text-lg font-medium mb-2">Brak zadań</p>
          <p class="text-sm">Nie masz jeszcze żadnych przypisanych zadań.</p>
        </div>
      </div>

      <div v-else-if="filteredTasks.length === 0" class="text-center py-8 text-muted">
        <div class="bg-gray-100 p-6 rounded-lg">
          <p class="text-lg mb-2">Brak zadań spełniających kryteria</p>
          <p v-if="hasActiveFilters" class="text-sm">Spróbuj zmienić filtry lub wyczyść je aby zobaczyć wszystkie zadania</p>
          <button @click="isDebugMode = true" class="text-xs text-primary mt-2">Pokaż diagnostykę</button>
        </div>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
            v-for="task in filteredTasks"
            :key="task.id"
            class="bg-surface border border-gray-200 p-4 rounded-lg shadow hover:scale-105 transition cursor-pointer flex flex-col h-full"
            @click="openTaskDetails(task)"
        >
          <!-- Górna część karty -->
          <div class="flex-grow">
            <h3 class="text-xl font-semibold text-primary">{{ task.title || task.name }}</h3>
            <p class="text-muted text-sm mt-1">{{ task.description }}</p>
            <div class="mt-2 space-y-1 text-xs text-muted">
              <p><strong>Zespół:</strong> {{ getTeamName(task.team || task.teamId) }}</p>
              <p><strong>Priorytet:</strong> {{ getPriorityText(task.priorityId || task.priority?.id) }}</p>
              <p><strong>Status:</strong> {{ getStatusText(task.statusId || task.status?.id) }}</p>
              <p v-if="task.deadline"><strong>Termin:</strong> {{ formatDate(task.deadline) }}
                <span v-if="isOverdue(task)" class="text-red-500 font-semibold ml-1">(Opóźnione!)</span>
              </p>
            </div>
          </div>
          
          <!-- Dolna część - przyciski zawsze na dole -->
          <div class="mt-4 flex space-x-3 flex-shrink-0">
            <button 
              @click.stop="openTaskDetails(task)" 
              class="bg-primary text-white px-4 py-2 rounded-md hover:bg-secondary transition flex-1"
            >
              Szczegóły zadania
            </button>
          </div>
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
import { authState } from '../../router/router';
import Datepicker from 'vue3-datepicker';

export default {
  components: {
    Datepicker
  },
  setup() {
    const router = useRouter();
    const tasks = ref([]);
    const teams = ref([]);
    const priorities = ref([]);
    const statuses = ref([]);
    const loading = ref(true);
    const error = ref(null);
    const isDebugMode = ref(false);
    const userTeams = ref([]);

    // Filtry z automatycznym działaniem
    const filters = ref({
      team: "",
      priority: "",
      status: "",
      deadline: null // Zmiana: używamy null dla datepicker
    });

    const sortBy = ref('');

    // Klasa CSS dla inputów datepicker - identyczna jak w select-ach
    // const datepickerInputClass = computed(() => 
    //   'w-full max-w-full p-2 border border-gray-300 rounded-md bg-white text-text focus:ring-2 focus:ring-primary focus:border-primary datepicker-input-custom'
    // );

    // Sprawdź czy są aktywne filtry
    const hasActiveFilters = computed(() => {
      return filters.value.team || filters.value.priority || filters.value.status || filters.value.deadline;
    });

    // Formatuj datę z datepicker do formatu YYYY-MM-DD dla porównań
    const formatDateForComparison = (date) => {
      if (!date) return '';
      
      try {
        if (date instanceof Date) {
          const year = date.getFullYear();
          const month = String(date.getMonth() + 1).padStart(2, '0');
          const day = String(date.getDate()).padStart(2, '0');
          return `${year}-${month}-${day}`;
        }
        return '';
      } catch (error) {
        console.error('Błąd formatowania daty:', error);
        return '';
      }
    };

    // Formatowanie daty dla filtra (wyświetlanie)
    const formatFilterDate = (date) => {
      if (!date) return '';
      
      try {
        if (date instanceof Date) {
          return date.toLocaleDateString('pl-PL', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
          });
        }
        return date.toString();
      } catch (error) {
        return date.toString();
      }
    };

    // Pobieranie danych referencyjnych
    const fetchReferenceData = async () => {
      try {
        console.log('🔄 Pobieranie danych referencyjnych...');
        
        // KROK 1: Zawsze pobierz wszystkie zespoły z API
        const allTeamsResponse = await teamService.getAllTeams();
        console.log('📋 Wszystkie zespoły z API:', allTeamsResponse);

        // KROK 2: Określ które zespoły powinien widzieć użytkownik
        if (authState.user) {
          const userRole = authState.user.role;
          console.log('👤 Rola użytkownika:', userRole);
          
          if (userRole === 'administrator' || userRole === 'admin') {
            // Administrator widzi wszystkie zespoły
            teams.value = allTeamsResponse;
            console.log('👑 Administrator - pokazuje wszystkie zespoły:', teams.value.length);
          } else {
            // Kierownik/Pracownik - znajdź zespoły użytkownika
            console.log('🔍 Wyszukiwanie zespołów dla użytkownika ID:', authState.user.id);
            
            const userId = authState.user.id;
            const userSpecificTeams = [];

            // Sprawdź zespoły gdzie użytkownik jest kierownikiem
            for (const team of allTeamsResponse) {
              if (team.managerId === userId) {
                userSpecificTeams.push(team);
                console.log(`✅ Znaleziono zespół jako kierownik: ${team.name} (ID: ${team.id})`);
              }
            }

            // Sprawdź zespoły gdzie użytkownik jest członkiem
            for (const team of allTeamsResponse) {
              try {
                const members = await teamService.getTeamMembers(team.id);
                if (members && members.some(member => member.userId === userId)) {
                  // Sprawdź czy zespół już nie został dodany jako kierownik
                  if (!userSpecificTeams.find(t => t.id === team.id)) {
                    userSpecificTeams.push(team);
                    console.log(`✅ Znaleziono zespół jako członek: ${team.name} (ID: ${team.id})`);
                  }
                }
              } catch (err) {
                console.error(`❌ Błąd podczas sprawdzania członków zespołu ${team.id}:`, err);
              }
            }

            teams.value = userSpecificTeams;
            userTeams.value = userSpecificTeams; // Zaktualizuj także userTeams dla spójności
            
            console.log(`👥 Kierownik/Pracownik - pokazuje ${teams.value.length} zespołów:`, 
              teams.value.map(t => `${t.name} (ID: ${t.id})`));
          }
        } else {
          console.log('❌ Brak zalogowanego użytkownika');
          teams.value = [];
        }

        // KROK 3: Pobierz priorytety
        try {
          const prioritiesResponse = await priorityService.getAllPriorities();
          priorities.value = prioritiesResponse;
          console.log('⚡ Pobrane priorytety:', priorities.value.length);
        } catch (err) {
          console.error('❌ Błąd podczas pobierania priorytetów:', err);
          priorities.value = [
            { id: 1, name: 'Niski' },
            { id: 2, name: 'Średni' },
            { id: 3, name: 'Wysoki' }
          ];
        }

        // KROK 4: Pobierz statusy
        try {
          const statusesResponse = await taskStatusService.getAllTaskStatuses();
          statuses.value = statusesResponse;
          console.log('📊 Pobrane statusy:', statuses.value.length);
        } catch (err) {
          console.error('❌ Błąd podczas pobierania statusów:', err);
          statuses.value = [
            { id: 1, name: 'Rozpoczęte' },
            { id: 2, name: 'W toku' },
            { id: 3, name: 'Zakończone' }
          ];
        }

      } catch (err) {
        console.error('❌ Błąd podczas pobierania danych referencyjnych:', err);
        
        // Dane awaryjne
        teams.value = [
          { id: 1, name: 'Zespół A' },
          { id: 2, name: 'Zespół B' },
          { id: 3, name: 'Zespół C' }
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
      }
    };

    // Pobieranie zadań z API
    const fetchTasks = async () => {
      loading.value = true;
      error.value = null;

      try {
        console.log('🔄 Pobieranie zadań z API...');
        const response = await taskService.getAllTasks();
        console.log('✅ Pobrane zadania:', response.length);

        if (authState.user) {
          const userRole = authState.user.role;
          
          if (userRole === 'administrator' || userRole === 'admin') {
            // Administrator widzi wszystkie zadania
            tasks.value = response;
            console.log('👑 Administrator - pokazuje wszystkie zadania:', tasks.value.length);
          } else {
            // Kierownik/Pracownik - tylko zadania swoich zespołów
            const userTeamIds = teams.value.map(team => team.id);
            console.log('🎯 ID zespołów użytkownika:', userTeamIds);
            
            tasks.value = response.filter(task => {
              const taskTeamId = task.teamId || task.team?.id;
              const belongs = userTeamIds.includes(taskTeamId);
              if (!belongs) {
                console.log(`⏭️ Zadanie ${task.id} (zespół ${taskTeamId}) odfiltrowane`);
              }
              return belongs;
            });
            
            console.log(`👥 Kierownik/Pracownik - pokazuje ${tasks.value.length} zadań ze swoich zespołów`);
          }
        } else {
          console.log('❌ Brak zalogowanego użytkownika');
          tasks.value = [];
        }
      } catch (err) {
        console.error('❌ Błąd podczas pobierania zadań:', err);
        error.value = `Nie udało się pobrać zadań: ${err.message}`;
        tasks.value = [];
      } finally {
        loading.value = false;
      }
    };

    // Pobieranie zespołów użytkownika
    const fetchUserTeams = async () => {
      console.log('ℹ️ fetchUserTeams wywołana - logika przeniesiona do fetchReferenceData');
      // if (!authState.user) return;

      // try {
      //   const allTeams = await teamService.getAllTeams();
      //   const userId = authState.user.id;

      //   userTeams.value = allTeams.filter(team => {
      //     if (team.managerId === userId) return true;
      //     return false;
      //   });

      //   for (const team of allTeams) {
      //     try {
      //       const members = await teamService.getTeamMembers(team.id);
      //       if (members && members.some(member => member.userId === userId)) {
      //         if (!userTeams.value.find(t => t.id === team.id)) {
      //           userTeams.value.push(team);
      //         }
      //       }
      //     } catch (err) {
      //       console.error(`Błąd podczas sprawdzania członków zespołu ${team.id}:`, err);
      //     }
      //   }
      // } catch (err) {
      //   console.error('Błąd podczas pobierania zespołów użytkownika:', err);
      // }
    };

    // Automatyczne filtrowanie zadań
    const filteredTasks = computed(() => {
      let filtered = [...tasks.value];

      console.log(`🔍 Automatyczne filtrowanie ${filtered.length} zadań...`);
      console.log('📋 Bieżące filtry:', filters.value);

      // Filtrowanie po zespole
      if (filters.value.team) {
        filtered = filtered.filter(task => {
          const taskTeamId = String(task.team?.id || task.teamId);
          const filterTeamId = String(filters.value.team);
          const match = taskTeamId === filterTeamId;
          if (!match) console.log(`❌ Zadanie ${task.id} odfiltrowane przez zespół: ${taskTeamId} !== ${filterTeamId}`);
          return match;
        });
        console.log(`👥 Po filtrze zespołu: ${filtered.length} zadań`);
      }

      // Filtrowanie po priorytecie
      if (filters.value.priority) {
        filtered = filtered.filter(task => {
          const taskPriorityId = String(task.priority?.id || task.priorityId);
          const filterPriorityId = String(filters.value.priority);
          const match = taskPriorityId === filterPriorityId;
          if (!match) console.log(`❌ Zadanie ${task.id} odfiltrowane przez priorytet: ${taskPriorityId} !== ${filterPriorityId}`);
          return match;
        });
        console.log(`⚡ Po filtrze priorytetu: ${filtered.length} zadań`);
      }

      // Filtrowanie po statusie
      if (filters.value.status) {
        filtered = filtered.filter(task => {
          const taskStatusId = String(task.status?.id || task.statusId);
          const filterStatusId = String(filters.value.status);
          const match = taskStatusId === filterStatusId;
          if (!match) console.log(`❌ Zadanie ${task.id} odfiltrowane przez status: ${taskStatusId} !== ${filterStatusId}`);
          return match;
        });
        console.log(`📊 Po filtrze statusu: ${filtered.length} zadań`);
      }

      // Filtrowanie po deadline
      if (filters.value.deadline) {
        const selectedDateStr = formatDateForComparison(filters.value.deadline);
        if (selectedDateStr) {
          filtered = filtered.filter(task => {
            if (!task.deadline) return false;
            
            const taskDeadline = task.deadline.split('T')[0];
            const match = taskDeadline === selectedDateStr;
            if (!match) console.log(`❌ Zadanie ${task.id} odfiltrowane przez termin: ${taskDeadline} !== ${selectedDateStr}`);
            return match;
          });
          console.log(`📅 Po filtrze terminu: ${filtered.length} zadań`);
        }
      }

      // Sortowanie
      if (sortBy.value) {
        filtered = [...filtered].sort((a, b) => {
          switch (sortBy.value) {
            case 'deadline':
              if (!a.deadline) return 1;
              if (!b.deadline) return -1;
              return new Date(a.deadline) - new Date(b.deadline);

            case 'overdue':
              const today = new Date();
              today.setHours(0, 0, 0, 0);

              const aOverdue = a.deadline && new Date(a.deadline) < today && !isTaskCompleted(a);
              const bOverdue = b.deadline && new Date(b.deadline) < today && !isTaskCompleted(b);

              if (aOverdue && !bOverdue) return -1;
              if (!aOverdue && bOverdue) return 1;
              if (!a.deadline) return 1;
              if (!b.deadline) return -1;
              return new Date(a.deadline) - new Date(b.deadline);

            case 'priority':
              const aPriority = a.priorityId || a.priority?.id || 0;
              const bPriority = b.priorityId || b.priority?.id || 0;
              return bPriority - aPriority;

            case 'status':
              const aStatus = a.statusId || a.status?.id || 0;
              const bStatus = b.statusId || b.status?.id || 0;
              return aStatus - bStatus;

            default:
              return 0;
          }
        });
      }

      console.log(`✅ Końcowy wynik automatycznego filtrowania: ${filtered.length} zadań`);
      return filtered;
    });

    // Czyszczenie filtrów
    const clearFilters = () => {
      console.log('🧹 Czyszczenie filtrów...');
      
      filters.value = {
        team: "",
        priority: "",
        status: "",
        deadline: null
      };
      sortBy.value = '';

      console.log('✅ Filtry wyczyszczone - automatyczne filtrowanie zadziała');
    };

    // Otwieranie szczegółów zadania
    const openTaskDetails = (task) => {
      console.log(`🔍 Otwieranie szczegółów zadania ID: ${task.id}`);
      router.push({ name: 'taskDetails', params: { id: task.id.toString() } });
    };

    // Funkcje pomocnicze
    const getTeamName = (teamData) => {
      if (!teamData) return 'Nieznany zespół';

      if (typeof teamData === 'object' && teamData.name) {
        return teamData.name;
      }

      const teamId = Number(teamData);
      const foundTeam = teams.value.find(t => t.id === teamId);
      return foundTeam ? foundTeam.name : `Zespół #${teamId}`;
    };

    const getPriorityText = (priorityId) => {
      if (priorityId === null || priorityId === undefined) return 'Średni';

      const foundPriority = priorities.value.find(p => p.id === Number(priorityId));
      if (foundPriority) return foundPriority.name;

      const priorityMap = {
        1: 'Niski',
        2: 'Średni',
        3: 'Wysoki'
      };

      return priorityMap[priorityId] || 'Średni';
    };

    const getStatusText = (statusId) => {
      if (statusId === null || statusId === undefined) return 'Nieznany';

      const foundStatus = statuses.value.find(s => s.id === Number(statusId));
      if (foundStatus) return foundStatus.name;

      const statusMap = {
        1: 'Rozpoczęte',
        2: 'W toku',
        3: 'Zakończone'
      };

      return statusMap[statusId] || 'Nieznany';
    };

    const isTaskCompleted = (task) => {
      const statusId = task.statusId || task.status?.id;
      
      const foundStatus = statuses.value.find(s => s.id === Number(statusId));
      if (foundStatus) {
        const statusName = foundStatus.name.toLowerCase();
        return statusName.includes('zakończ') || statusName.includes('completed') || statusName.includes('done');
      }
      
      return statusId === 3;
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

    const isOverdue = (task) => {
      if (!task.deadline) return false;
      if (isTaskCompleted(task)) return false;

      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const deadline = new Date(task.deadline);
      deadline.setHours(0, 0, 0, 0);

      return deadline < today;
    };

    // Watchers dla automatycznego filtrowania
    watch([() => filters.value.team, () => filters.value.priority, () => filters.value.status, () => filters.value.deadline, sortBy], 
      ([newTeam, newPriority, newStatus, newDeadline, newSort], [oldTeam, oldPriority, oldStatus, oldDeadline, oldSort]) => {
        console.log('🔄 Zmiana filtrów/sortowania - automatyczne przefiltrowanie:', {
          team: { old: oldTeam, new: newTeam },
          priority: { old: oldPriority, new: newPriority },
          status: { old: oldStatus, new: newStatus },
          deadline: { old: oldDeadline, new: newDeadline },
          sort: { old: oldSort, new: newSort }
        });
      }
    );

    // Inicjalizacja komponentu
    onMounted(async () => {
      await fetchReferenceData();
      await fetchTasks();
      console.log('✅ Komponent TasksHistory załadowany - automatyczne filtrowanie aktywne');
    });

    return {
      tasks,
      teams,
      priorities,
      statuses,
      loading,
      error,
      filters,
      sortBy,
      isDebugMode,
      //datepickerInputClass,
      hasActiveFilters,
      filteredTasks,
      clearFilters,
      openTaskDetails,
      getTeamName,
      getPriorityText,
      getStatusText,
      formatDate,
      formatFilterDate,
      isOverdue,
      fetchTasks
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
</style>