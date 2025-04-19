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
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <!-- Członkowie Zespołu -->
        <div class="bg-surface rounded-xl p-6 shadow border border-gray-200">
          <h2 class="text-xl font-bold text-primary mb-4">Członkowie Zespołu</h2>
          <div v-if="teamMembers.length === 0" class="text-muted text-center">
            Brak członków zespołu
          </div>
          <div v-else class="space-y-4">
            <div
                v-for="member in teamMembers"
                :key="member.id"
                class="flex items-center bg-gray-50 p-3 rounded-lg"
            >
              <div class="w-10 h-10 rounded-xl mr-3 flex items-center justify-center text-white font-bold" :style="{ backgroundColor: getMemberColor(member) }">
                {{ getMemberInitials(member) }}
              </div>
              <div>
                <h3 class="font-semibold text-text">{{ getMemberName(member) }}</h3>
                <p class="text-sm text-muted">{{ getMemberRole(member) }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Ostatnie Aktywności -->
        <div class="bg-surface rounded-xl p-6 shadow border border-gray-200">
          <h2 class="text-xl font-bold text-primary mb-4">Ostatnie Aktywności</h2>
          <div v-if="teamActivities.length === 0" class="text-muted text-center">
            Brak aktywności
          </div>
          <div v-else class="space-y-4">
            <div
                v-for="activity in teamActivities"
                :key="activity.id"
                class="bg-gray-50 p-3 rounded-lg"
            >
              <h3 class="font-semibold text-text">{{ activity.title }}</h3>
              <p class="text-sm text-muted">{{ activity.description }}</p>
              <p class="text-xs text-muted">{{ activity.timestamp }}</p>
            </div>
          </div>
        </div>

        <!-- Nadchodzące Zadania -->
        <div class="bg-surface rounded-xl p-6 shadow border border-gray-200">
          <h2 class="text-xl font-bold text-primary mb-4">Nadchodzące Zadania</h2>
          <div v-if="teamTasks.length === 0" class="text-muted text-center">
            Brak nadchodzących zadań
          </div>
          <div v-else class="space-y-4">
            <div
                v-for="task in teamTasks"
                :key="task.id"
                class="bg-gray-50 p-3 rounded-lg flex justify-between items-center"
            >
              <div>
                <h3 class="font-semibold text-text">{{ task.title }}</h3>
                <p class="text-sm text-muted">{{ task.assignedTo }}</p>
              </div>
              <span
                  class="px-2 py-1 rounded-md text-xs font-bold text-white"
                  :class="{
                  'bg-danger': task.priority === 'high',
                  'bg-warning': task.priority === 'medium',
                  'bg-secondary': task.priority === 'low'
                }"
              >
                {{ task.priority ? task.priority.toUpperCase() : 'MEDIUM' }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import apiService from '../services/api.js';

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
    const loading = ref(true);
    const error = ref(null);

// NAJWAŻNIEJSZA ZMIANA - Niezawodna metoda pozyskiwania ID
    const getTeamId = () => {
      console.log("OGOLNIE TUTAJ JEST KONGO");
      // Sprawdź route.query.id (z URL ?id=1)
      let teamId = route.params.id;

      // Log do debugowania
      console.log("TeamDetails - route.query.id:", teamId);

      // Konwersja ID do stringa
      return teamId ? teamId.toString() : null;
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
        // Pobierz szczegóły zespołu
        currentTeam.value = await apiService.get(`/database/teams/${teamId}`);
        console.log("Pobrano szczegóły zespołu:", currentTeam.value);

        // Pobierz członków zespołu
        try {
          const members = await apiService.get(`/database/team-members/team/${teamId}`);
          teamMembers.value = members;
          console.log("Pobrano członków zespołu:", teamMembers.value);
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
            priority: getPriorityName(task.priority)
          }));
          console.log("Pobrano zadania zespołu:", teamTasks.value);
        } catch (err) {
          console.error('Error fetching team tasks:', err);
          teamTasks.value = generateDemoTasks(teamId); // Dane demonstracyjne
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
      } finally {
        loading.value = false;
      }
    };

    // Pozostałe funkcje pozostają bez zmian
    // Dane demonstracyjne - członkowie zespołu
    const generateDemoMembers = (teamId) => {
      return [
        {
          id: teamId * 10 + 1,
          user: {
            firstName: 'Jan',
            lastName: 'Kowalski',
            role: 'Kierownik projektu'
          }
        },
        {
          id: teamId * 10 + 2,
          user: {
            firstName: 'Anna',
            lastName: 'Nowak',
            role: 'Starszy pracownik'
          }
        },
        {
          id: teamId * 10 + 3,
          user: {
            firstName: 'Piotr',
            lastName: 'Zieliński',
            role: 'Specjalista'
          }
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
          priority: 'high'
        },
        {
          id: teamId * 100 + 2,
          title: 'Budowa werandy',
          assignedTo: 'Anna Nowak',
          priority: 'medium'
        },
        {
          id: teamId * 100 + 3,
          title: 'Naprawa dachu',
          assignedTo: 'Piotr Zieliński',
          priority: 'low'
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
      return teamMembers.value.length;
    };

    const getMemberInitials = (member) => {
      if (member.user) {
        return `${member.user.firstName ? member.user.firstName[0] : '?'}${member.user.lastName ? member.user.lastName[0] : '?'}`;
      }
      return '??';
    };

    const getMemberName = (member) => {
      if (member.user) {
        return `${member.user.firstName || ''} ${member.user.lastName || ''}`.trim() || 'Nieznany użytkownik';
      }
      return 'Nieznany użytkownik';
    };

    const getMemberRole = (member) => {
      if (member.user && member.user.role) {
        return member.user.role;
      }
      return 'Członek zespołu';
    };

    const getMemberColor = (member) => {
      const colors = ['#780116', '#DB7C26', '#F7B538', '#C32F27', '#D8572A'];
      return member.id ? colors[member.id % colors.length] : colors[0];
    };

    const getPriorityName = (priority) => {
      if (!priority) return 'low';

      const priorityNames = {
        1: 'low',
        2: 'medium',
        3: 'high'
      };

      if (typeof priority === 'object' && priority.value) {
        return priorityNames[priority.value] || 'medium';
      }

      return 'medium';
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

    // NAJWAŻNIEJSZE ZMIANY - Monitorowanie parametru ID
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
      loading,
      error,
      fetchTeamDetails,
      getTeamShortName,
      getTeamColor,
      getTeamMembersCount,
      getMemberInitials,
      getMemberName,
      getMemberRole,
      getMemberColor
    };
  }
};
</script>