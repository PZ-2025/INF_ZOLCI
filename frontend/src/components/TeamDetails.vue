<template>
  <div class="p-6 bg-background min-h-screen text-text overflow-auto">
    <div v-if="loading" class="flex justify-center items-center h-64">
      <p class="text-primary text-xl">Ładowanie szczegółów zespołu...</p>
    </div>

    <div v-else-if="error" class="bg-danger p-4 rounded-xl text-white">
      <p>Błąd podczas ładowania szczegółów zespołu: {{ error }}</p>
      <button @click="fetchTeamDetails" class="bg-warning mt-2 px-4 py-2 rounded-md">Spróbuj ponownie</button>
    </div>

    <div v-else>
      <!-- Nagłówek zespołu -->
      <div class="bg-surface rounded-xl p-4 flex justify-between items-center mb-6 shadow border border-gray-200">
        <div class="flex items-center">
          <div class="w-12 h-12 rounded-xl mr-4 flex items-center justify-center text-white font-bold" :style="{ backgroundColor: getTeamColor(currentTeam) }">
            {{ getTeamShortName(currentTeam) }}
          </div>
          <div>
            <h1 class="text-2xl font-bold text-primary">{{ currentTeam.name || 'Zespół' }}</h1>
            <p class="text-muted text-sm">{{ getTeamMembersCount() }} członków</p>
          </div>
        </div>
        <div class="flex gap-2">
          <button
            v-if="canManageMembers"
            @click="navigateToManageMembers"
            class="bg-primary text-white px-4 py-2 rounded-md shadow-md hover:bg-secondary transition"
          >
            Zarządzaj członkami
          </button>
          <button
            v-if="canManageMembers"
            @click="navigateToAddTask"
            class="bg-primary text-white px-4 py-2 rounded-md shadow-md hover:bg-secondary transition"
          >
            Dodaj zadanie
          </button>
        </div>
      </div>

      <!-- Główna siatka -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Członkowie Zespołu -->
        <div class="bg-surface rounded-xl p-6 shadow border border-gray-200">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-xl font-bold text-primary">Członkowie Zespołu</h2>
            <span class="bg-primary text-white px-2 py-1 rounded-md text-sm">
              {{ teamMembers.length }}
            </span>
          </div>

          <div v-if="teamMembers.length === 0" class="text-muted text-center">
            Brak członków zespołu
          </div>
          <div v-else class="space-y-4 max-h-96 overflow-y-auto pr-2">
            <div
                v-for="member in teamMembers"
                :key="member.id"
                class="flex items-center bg-gray-50 p-3 rounded-lg hover:bg-gray-100 transition"
            >
              <div 
                class="w-10 h-10 rounded-xl mr-3 flex items-center justify-center text-white font-bold relative"
                :style="{ backgroundColor: getMemberColor(member) }"
                :class="{ 'ring-2 ring-yellow-400 ring-offset-2': member.isManager }"
              >
                {{ getMemberInitials(member) }}
                <!-- Ikona korony dla kierownika -->
                <div 
                  v-if="member.isManager" 
                  class="absolute -top-1 -right-1 w-4 h-4 bg-yellow-400 rounded-full flex items-center justify-center"
                >
                  <svg class="w-2 h-2 text-white" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M5 4a1 1 0 00-.64 1.78L6 7.56V15a1 1 0 001 1h6a1 1 0 001-1V7.56l1.64-1.78A1 1 0 0015 4H5z"/>
                  </svg>
                </div>
              </div>
              <div class="flex-grow">
                <h3 class="font-semibold text-text">{{ member.userFullName || member.username }}</h3>
                <p class="text-sm text-muted">{{ member.username }}</p>
                <span v-if="member.isManager" class="text-xs bg-primary text-white px-2 py-0.5 rounded">
                  Kierownik
                </span>
              </div>
              <div v-if="member.isActive === false" class="ml-2 px-2 py-1 bg-red-100 text-red-800 rounded text-xs">
                Nieaktywny
              </div>
            </div>
          </div>
        </div>

        <!-- Nadchodzące Zadania -->
        <div class="bg-surface rounded-xl p-6 shadow border border-gray-200">
          <h2 class="text-xl font-bold text-primary mb-4">Zadania</h2>
          <div v-if="upcomingTasks.length === 0" class="text-muted text-center">
            Brak nadchodzących zadań
          </div>
          <div v-else class="space-y-4 max-h-96 overflow-y-auto pr-2">
            <div
                v-for="task in upcomingTasks"
                :key="task.id"
                class="bg-gray-50 p-3 rounded-lg flex justify-between items-start"
            >
              <div class="flex-grow pr-3">
                <h3 class="font-semibold text-text">{{ task.title }}</h3>
                <p class="text-sm text-muted">{{ task.assignedTo }}</p>
              </div>
              <div class="flex flex-col items-end space-y-2 min-w-max">
                <span
                    class="px-2 py-1 rounded-md text-xs font-bold text-white w-full text-center"
                    :class="getPriorityClass(task.priority)"
                >
                  {{ getPriorityText(task.priority) }}
                </span>
                <span
                    class="px-2 py-1 rounded-md text-xs font-bold w-full text-center"
                    :class="getStatusClass(task.status)"
                >
                  {{ getStatusText(task.status) }}
                </span>
                <button
                    @click="navigateToTaskDetails(task.id)"
                    class="bg-primary text-white px-3 py-1 rounded-md text-xs hover:bg-secondary transition w-full"
                >
                  Szczegóły
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Zakończone Zadania na całą szerokość -->
      <div class="bg-surface rounded-xl p-6 shadow border border-gray-200 mt-6">
        <h2 class="text-xl font-bold text-primary mb-4">Zakończone Zadania</h2>
        <div v-if="completedTasks.length === 0" class="text-muted text-center">
          Brak zakończonych zadań
        </div>
        <div v-else class="space-y-4 max-h-96 overflow-y-auto pr-2">
          <div
            v-for="task in completedTasks"
            :key="task.id"
            class="bg-gray-50 p-3 rounded-lg flex justify-between items-start"
          >
            <div class="flex-grow pr-3">
              <h3 class="font-semibold text-text">{{ task.title }}</h3>
              <p class="text-sm text-muted">{{ task.assignedTo }}</p>
            </div>
            <div class="flex flex-col items-end space-y-2 min-w-max">
              <span
                class="px-2 py-1 rounded-md text-xs font-bold text-white w-full text-center"
                :class="getPriorityClass(task.priority)"
              >
                {{ getPriorityText(task.priority) }}
              </span>
              <span
                class="px-2 py-1 rounded-md text-xs font-bold w-full text-center"
                :class="getStatusClass(task.status)"
              >
                {{ getStatusText(task.status) }}
              </span>
              <button
                @click="navigateToTaskDetails(task.id)"
                class="bg-primary text-white px-3 py-1 rounded-md text-xs hover:bg-secondary transition w-full"
              >
                Szczegóły
              </button>
            </div>
          </div>
        </div>
      </div>
      <!-- Koniec zakończonych zadań -->
    </div>
  </div>
</template>


<script>
import { ref, onMounted, watch, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import apiService from '../services/api.js';
import priorityService from '../services/priorityService';
import taskStatusService from '../services/taskStatusService';
import { authState } from '../../router/router.js';
import authService from '../services/authService.js'; 

export default {
  name: 'TeamDetails',
  props: {
    id: {
      type: [String, Number],
      default: null
    }
  },
  setup(props) {
    const route = useRoute();
    const router = useRouter();
    const currentTeam = ref({});
    const teamMembers = ref([]);
    const teamActivities = ref([]);
    const teamTasks = ref([]);
    const upcomingTasks = ref([]);
    const completedTasks = ref([]);
    const loading = ref(true);
    const error = ref(null);

    // Dane referencyjne
    const priorities = ref([]);
    const statuses = ref([]);

    // Nawigacja do zarządzania członkami zespołu
    const navigateToManageMembers = () => {
      const teamId = route.params.id;
      console.log("Navigating to manage members for team ID:", teamId);
      if (teamId) {
        router.push({ name: 'teamMembers', params: { id: teamId } });
      } else {
        console.error('Nie można nawigować - brak ID zespołu');
      }
    };

    // Navigate to TaskDetails view
    const navigateToTaskDetails = (taskId) => {
      router.push({ name: 'taskDetails', params: { id: taskId } });
    };

    // Nawigacja do dodawania zadania
    const navigateToAddTask = () => {
      const teamId = route.params.id;
      if (teamId) {
        // Przekieruj do widoku dodawania zadania z id zespołu jako query param
        router.push({ name: 'addTask', query: { teamId } });
      } else {
        router.push({ name: 'addTask' });
      }
    };

    // Metoda pozyskiwania ID zespołu
    const getTeamId = () => {
      let teamId = route.params.id;
      console.log("TeamDetails - route.params.id:", teamId);
      return teamId ? teamId.toString() : null;
    };

    // Pobieranie danych referencyjnych
    const fetchReferenceData = async () => {
      try {
        // Pobierz priorytety
        const prioritiesResponse = await priorityService.getAllPriorities();
        priorities.value = prioritiesResponse;
        console.log('Pobrane priorytety:', priorities.value);

        // Pobierz statusy
        const statusesResponse = await taskStatusService.getAllTaskStatuses();
        statuses.value = statusesResponse;
        console.log('Pobrane statusy:', statuses.value);
      } catch (err) {
        console.error('Błąd podczas pobierania danych referencyjnych:', err);
        // Dane awaryjne
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

    // Funkcja pobierająca szczegóły zespołu
    const fetchTeamDetails = async () => {
      const teamId = getTeamId();

      if (!teamId) {
        error.value = "ID zespołu jest wymagane";
        loading.value = false;
        console.error("BŁĄD - Brak ID zespołu, nie mogę pobrać danych");
        return;
      }

      console.log("Pobieranie szczegółów zespołu o ID:", teamId);
      loading.value = true;
      error.value = null;

      try {
        // Pobierz dane referencyjne równolegle
        await fetchReferenceData();

        // Pobierz szczegóły zespołu
        currentTeam.value = await apiService.get(`/database/teams/${teamId}`);
        console.log("Pobrano szczegóły zespołu:", currentTeam.value);

        // Pobierz członków zespołu
        try {
          // Bezpośrednie pobieranie członków zespołu z API
          const members = await apiService.get(`/database/team-members/team/${teamId}`);

          // Sprawdź czy odpowiedź jest tablicą
          if (Array.isArray(members)) {
            console.log("Pobrano członków zespołu:", members);
            teamMembers.value = members;
          } else {
            console.warn("Odpowiedź API nie zawiera poprawnych danych członków zespołu:", members);
            teamMembers.value = generateDemoMembers(teamId);
          }

          // Dodaj kierownika do listy członków jeśli jest przypisany
          if (currentTeam.value.managerId) {
            try {
              const manager = await apiService.get(`/database/users/${currentTeam.value.managerId}`);
              const managerMember = {
                id: 'manager-' + currentTeam.value.managerId,
                teamId: teamId,
                userId: currentTeam.value.managerId,
                joinedAt: currentTeam.value.createdAt || new Date().toISOString(),
                isActive: true,
                teamName: currentTeam.value.name,
                username: manager.username,
                userFullName: `${manager.firstName} ${manager.lastName}`,
                isManager: true // Flaga do oznaczenia kierownika
              };

              // Dodaj kierownika na początek listy
              teamMembers.value = [managerMember, ...teamMembers.value];
            } catch (err) {
              console.error('Error fetching manager details:', err);
            }
          }
        } catch (err) {
          console.error('Error fetching team members:', err);
          teamMembers.value = generateDemoMembers(teamId); // Dane demonstracyjne
        }

        // Pobierz zadania zespołu
        try {
          const tasks = await apiService.get(`/database/tasks/team/${teamId}`);
          teamTasks.value = tasks.map(task => ({
            id: task.id,
            title: task.title,
            assignedTo: task.createdBy ? `${task.createdBy.firstName} ${task.createdBy.lastName}` : 'Nieprzypisane',
            priority: task.priorityId,
            status: task.statusId
          }));
          console.log("Pobrano zadania zespołu:", teamTasks.value);

          // Podziel zadania na nadchodzące i zakończone - używając dynamicznych statusów
          upcomingTasks.value = teamTasks.value.filter(task => !isTaskCompleted(task.status));
          completedTasks.value = teamTasks.value.filter(task => isTaskCompleted(task.status));
        } catch (err) {
          console.error('Error fetching team tasks:', err);
          teamTasks.value = generateDemoTasks(teamId); // Dane demonstracyjne
          // Podziel dane demonstracyjne
          upcomingTasks.value = teamTasks.value.filter(task => !isTaskCompleted(task.status));
          completedTasks.value = teamTasks.value.filter(task => isTaskCompleted(task.status));
        }

        // Pobierz historię zadań jako aktywności
        try {
          const history = await apiService.get(`/database/task-history`);
          // Filtruj tylko historię zadań związanych z tym zespołem
          const teamHistory = history.filter(item => {
            // Sprawdź, czy zadanie należy do tego zespołu
            return item.task && item.task.team && item.task.team.id == teamId;
          });

          teamActivities.value = teamHistory.map(item => ({
            id: item.id,
            title: `Zmiana w ${item.task ? item.task.title : 'zadaniu'}`,
            description: `Zmieniono ${item.fieldName} z ${item.oldValue || 'brak'} na ${item.newValue || 'brak'}`,
            timestamp: formatDate(item.changedAt)
          }));
          console.log("Pobrano aktywności zespołu:", teamActivities.value);
        } catch (err) {
          console.error('Error fetching task history:', err);
          teamActivities.value = generateDemoActivities(teamId); // Dane demonstracyjne
        }
      } catch (err) {
        console.error('Error fetching team details:', err);
        error.value = err.message;

        // Dane awaryjne na wypadek błędu API
        currentTeam.value = {
          id: teamId,
          name: 'Zespół #' + teamId,
        };

        teamMembers.value = generateDemoMembers(teamId);
        teamTasks.value = generateDemoTasks(teamId);
        teamActivities.value = generateDemoActivities(teamId);
        
        // Podziel dane awaryjne
        upcomingTasks.value = teamTasks.value.filter(task => !isTaskCompleted(task.status));
        completedTasks.value = teamTasks.value.filter(task => isTaskCompleted(task.status));
      } finally {
        loading.value = false;
      }
    };

    // Dane demonstracyjne - członkowie zespołu
    const generateDemoMembers = (teamId) => {
      return [
        {
          id: teamId * 10 + 1,
          teamId: teamId,
          userId: 3,
          joinedAt: "2025-04-03T18:02:37",
          isActive: true,
          teamName: "Zespół demonstracyjny",
          username: "user1",
          userFullName: "Jan Kowalski"
        },
        {
          id: teamId * 10 + 2,
          teamId: teamId,
          userId: 4,
          joinedAt: "2025-04-03T18:02:37",
          isActive: true,
          teamName: "Zespół demonstracyjny",
          username: "user2",
          userFullName: "Anna Nowak"
        }
      ];
    };

    // Dane demonstracyjne - zadania zespołu
    const generateDemoTasks = (teamId) => {
      return [
        {
          id: teamId * 100 + 1,
          title: 'Remont mieszkania',
          assignedTo: 'Jan Kowalski',
          priority: 3, // Wysoki priorytet
          status: 1    // Rozpoczęte
        },
        {
          id: teamId * 100 + 2,
          title: 'Budowa werandy',
          assignedTo: 'Anna Nowak',
          priority: 2, // Średni priorytet
          status: 3    // Zakończone
        },
        {
          id: teamId * 100 + 3,
          title: 'Naprawa dachu',
          assignedTo: 'Piotr Zieliński',
          priority: 1, // Niski priorytet
          status: 2    // W toku
        }
      ];
    };

    // Dane demonstracyjne - aktywności zespołu
    const generateDemoActivities = (teamId) => {
      return [
        {
          id: teamId * 1000 + 1,
          title: 'Zmiana w zadaniu "Remont mieszkania"',
          description: 'Zmieniono status z "W toku" na "Zakończone"',
          timestamp: '2025-04-15 14:30'
        },
        {
          id: teamId * 1000 + 2,
          title: 'Zmiana w zadaniu "Budowa werandy"',
          description: 'Zmieniono priorytet z "Niski" na "Średni"',
          timestamp: '2025-04-16 09:15'
        }
      ];
    };

    // Funkcje pomocnicze
    const getTeamShortName = (team) => {
      if (!team.name) return '??';
      const words = team.name.split(' ');
      if (words.length === 1) {
        return words[0].substring(0, 2).toUpperCase();
      }
      return (words[0][0] + (words[1] ? words[1][0] : '')).toUpperCase();
    };

    const getTeamColor = (team) => {
      const colors = ['#D8572A', '#C32F27', '#780116', '#DB7C26', '#F7B538'];
      return team.id ? colors[team.id % colors.length] : colors[0];
    };

    const getTeamMembersCount = () => {
      // Zwróć liczbę członków (już zawiera kierownika)
      return teamMembers.value.length;
    };

    const getMemberInitials = (member) => {
      if (member.userFullName) {
        const nameParts = member.userFullName.split(' ');
        if (nameParts.length >= 2) {
          return `${nameParts[0][0]}${nameParts[1][0]}`;
        }
        return member.userFullName.substring(0, 2).toUpperCase();
      }
      return member.username ? member.username.substring(0, 2).toUpperCase() : '??';
    };

    const getMemberColor = (member) => {
      // Specjalny kolor dla kierownika - złoty/pomarańczowy
      if (member.isManager) {
        return '#f59e0b'; // Złoty dla kierownika (amber-500)
      }
      
      // Kolory dla zwykłych członków
      const colors = ['#780116', '#DB7C26', '#F7B538', '#C32F27', '#D8572A'];
      return member.id ? colors[member.id % colors.length] : colors[0];
    };

    // Sprawdź czy zadanie jest zakończone - używając dynamicznych statusów
    const isTaskCompleted = (statusId) => {
      if (statusId === null || statusId === undefined) return false;
      
      // Sprawdź w dynamicznie pobranych statusach
      const foundStatus = statuses.value.find(s => s.id === Number(statusId));
      if (foundStatus) {
        const statusName = foundStatus.name.toLowerCase();
        return statusName.includes('zakończ') || statusName.includes('completed') || statusName.includes('done');
      }
      
      // Awaryjne sprawdzenie - status 3 to zwykle "Zakończone"
      return statusId === 3;
    };

    // Tekstowa reprezentacja priorytetu - używając dynamicznych priorytetów
    const getPriorityText = (priorityId) => {
      if (priorityId === null || priorityId === undefined) return 'Średni';

      // Znajdź priorytet po ID w dynamicznie pobranych danych
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

      // Znajdź priorytet po ID w dynamicznie pobranych danych
      const foundPriority = priorities.value.find(p => p.id === Number(priorityId));
      
      if (foundPriority) {
        // Mapowanie na podstawie nazwy priorytetu
        const priorityName = foundPriority.name.toLowerCase();
        
        if (priorityName.includes('niski') || priorityName.includes('low')) {
          return 'bg-blue-500';
        } else if (priorityName.includes('średni') || priorityName.includes('medium')) {
          return 'bg-yellow-500';
        } else if (priorityName.includes('wysoki') || priorityName.includes('high')) {
          return 'bg-red-500';
        } else if (priorityName.includes('krytyczny') || priorityName.includes('critical')) {
          return 'bg-purple-500';
        }
      }

      // Awaryjne mapowanie na podstawie ID jeśli nie znaleziono
      const priorityClasses = {
        1: 'bg-blue-500',   // Niski
        2: 'bg-yellow-500', // Średni
        3: 'bg-red-500'     // Wysoki
      };

      return priorityClasses[priorityId] || 'bg-yellow-500';
    };

    // Tekstowa reprezentacja statusu - używając dynamicznych statusów
    const getStatusText = (statusId) => {
      if (statusId === null || statusId === undefined) return 'Nieznany';

      // Znajdź status po ID w dynamicznie pobranych danych
      const foundStatus = statuses.value.find(s => s.id === Number(statusId));
      if (foundStatus) return foundStatus.name;

      // Awaryjne mapowanie jeśli nie znaleziono
      const statusMap = {
        1: 'Rozpoczęte',
        2: 'W toku',
        3: 'Zakończone'
      };

      return statusMap[statusId] || 'Nieznany';
    };

    // Klasa CSS dla statusu
    const getStatusClass = (statusId) => {
      if (statusId === null || statusId === undefined) return 'bg-gray-100 text-gray-800'; // Domyślny

      // Znajdź status po ID w dynamicznie pobranych danych
      const foundStatus = statuses.value.find(s => s.id === Number(statusId));
      
      if (foundStatus) {
        // Mapowanie na podstawie nazwy statusu
        const statusName = foundStatus.name.toLowerCase();
        
        if (statusName.includes('rozpoczęt') || statusName.includes('start')) {
          return 'bg-blue-100 text-blue-800';
        } else if (statusName.includes('toku') || statusName.includes('progress') || statusName.includes('w realizacji')) {
          return 'bg-yellow-100 text-yellow-800';
        } else if (statusName.includes('zakończ') || statusName.includes('completed') || statusName.includes('done')) {
          return 'bg-green-100 text-green-800';
        } else if (statusName.includes('wstrzyman') || statusName.includes('pause') || statusName.includes('hold')) {
          return 'bg-orange-100 text-orange-800';
        } else if (statusName.includes('anulowa') || statusName.includes('cancel')) {
          return 'bg-red-100 text-red-800';
        }
      }

      // Awaryjne mapowanie na podstawie ID jeśli nie znaleziono
      const statusClasses = {
        1: 'bg-blue-100 text-blue-800',      // Rozpoczęte
        2: 'bg-yellow-100 text-yellow-800',  // W toku
        3: 'bg-green-100 text-green-800'     // Zakończone
      };

      return statusClasses[statusId] || 'bg-gray-100 text-gray-800';
    };

    const formatDate = (dateString) => {
      if (!dateString) return 'Nieznana data';

      try {
        const date = new Date(dateString);
        return date.toLocaleString();
      } catch (err) {
        return dateString;
      }
    };

    // Sprawdzenie czy użytkownik może zarządzać członkami zespołu
    const canManageMembers = computed(() => {
      const user = authState.user;
      if (!user) return false;
      // Admin ma zawsze dostęp
      if (user.role === 'administrator') return true;
      // Kierownik tylko jeśli jest kierownikiem tego zespołu
      if (user.role === 'kierownik' && currentTeam.value.managerId && user.id === currentTeam.value.managerId) {
        return true;
      }
      return false;
    });

    // Monitorowanie parametru ID
    onMounted(() => {
      console.log("TeamDetails component mounted, pobieranie danych...");
      fetchTeamDetails();
    });

    // Obserwuj zmiany route.params.id
    watch(() => route.params.id, (newId, oldId) => {
      console.log("Zmiana parametru ID z", oldId, "na", newId);
      if (newId !== oldId) {
        fetchTeamDetails();
      }
    }, { immediate: true });

    return {
      currentTeam,
      teamMembers,
      teamActivities,
      teamTasks,
      upcomingTasks,
      completedTasks,
      loading,
      error,
      fetchTeamDetails,
      getTeamShortName,
      getTeamColor,
      getTeamMembersCount,
      getMemberInitials,
      getMemberColor,
      navigateToManageMembers,  
      navigateToTaskDetails,
      getPriorityText,
      getPriorityClass,
      getStatusText,
      getStatusClass,
      canManageMembers,
      navigateToAddTask
    };
  }
};
</script>