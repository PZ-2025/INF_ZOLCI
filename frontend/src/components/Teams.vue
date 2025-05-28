<template>
  <div class="p-4 bg-background min-h-screen text-text">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-3xl font-bold text-primary">Zespoły</h1>

      <router-link
          v-if="canAddTeam"
          to="/addteam"
          class="bg-primary hover:bg-secondary text-white px-4 py-2 rounded-md transition flex items-center"
      >
        <span class="mr-1">+</span> Dodaj Zespół
      </router-link>
    </div>

    <div v-if="loading" class="flex justify-center items-center h-64">
      <p class="text-white text-xl">Ładowanie zespołów...</p>
    </div>

    <div v-else-if="error" class="bg-danger p-4 rounded-xl text-white">
      <p>Błąd podczas ładowania zespołów: {{ error }}</p>
      <button @click="fetchTeams" class="bg-warning mt-2 px-4 py-2 rounded-md">Spróbuj ponownie</button>
    </div>

    <div v-else-if="teams.length === 0" class="flex justify-center items-center h-64">
      <p class="text-primary text-xl">Nie należysz do żadnego zespołu.</p>
    </div>

    <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
      <div
          v-for="team in teams"
          :key="team.id"
          @click="selectTeam(team)"
          class="bg-surface shadow-md rounded-xl p-4 cursor-pointer hover:bg-accent/10 transition transform hover:scale-105 flex flex-col items-center justify-center text-center"
      >
        <div
            class="w-16 h-16 rounded-xl mb-3 flex items-center justify-center text-white font-bold"
            :style="{ backgroundColor: getTeamColor(team) }"
        >
          {{ getTeamShortName(team) }}
        </div>
        <h3 class="font-semibold text-secondary">{{ team.name }}</h3>
        <p class="text-muted text-sm">{{ getTeamManagerName(team) }}</p>
        <p class="text-muted text-sm">{{ teamMemberCounts[team.id] || 0 }} członków</p>
      </div>
    </div>
  </div>
</template>

<script>

import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import teamService from '../services/teamService';
import userService from '../services/userService';
import { authState } from '../../router/router.js';

export default {
  name: 'TeamSelection',
  setup() {
    const router = useRouter();
    const teams = ref([]);
    const teamMemberCounts = reactive({});
    const loading = ref(true);
    const error = ref(null);

    // aktualny użytkownik i rola
    const currentUser = ref(null);
    const userRole = ref('');

    // Filtrowane zespoły do wyświetlenia
    const visibleTeams = computed(() => {
      if (userRole.value === 'admin' || userRole.value === 'administrator' || userRole.value === 'ADMIN') {
        return teams.value;
      }
      // Pracownik/kierownik: tylko zespoły, do których należy
      if (!currentUser.value) return [];
      return teams.value.filter(team =>
          team.managerId === currentUser.value.id ||
          (team.members && team.members.some(m => m.userId === currentUser.value.id))
      );
    });

    // Możliwość dodawania zespołów (administrator, kierownik)
    const canAddTeam = computed(() => {
      const role = authState.user?.role;
      return role === 'administrator' || role === 'admin' || role === 'kierownik' || role === 'manager' || role === 'ADMIN';
    });

    // Pobierz zespoły z API
    const fetchTeams = async () => {
      loading.value = true;
      error.value = null;

      try {
        // Pobierz aktualnego użytkownika i rolę
        if (authState.user && authState.user.id) {
          currentUser.value = authState.user;
          userRole.value = authState.user.role;
        } else {
          // fallback jeśli nie ma w stanie
          const user = await userService.getCurrentUser();
          currentUser.value = user;
          userRole.value = user.role;
        }

        // Weź zespoły z API
        teams.value = await teamService.getAllTeams();

        // Pobierz dane kierownika dla każdego zespołu
        for (const team of teams.value) {
          if (team.managerId) {
            try {
              const manager = await userService.getUserById(team.managerId);
              team.manager = manager;
            } catch (err) {
              console.error(`Error fetching manager for team ${team.id}:`, err);
              team.manager = null;
            }
          }

          // Pobierz członków zespołu (potrzebne do filtracji)
          try {
            const members = await teamService.getTeamMembers(team.id);
            team.members = members || [];
          } catch {
            team.members = [];
          }
        }

        // Pobierz liczbę członków dla każdego zespołu
        await fetchTeamMemberCounts();
      } catch (err) {
        console.error('Error fetching teams:', err);
        error.value = err.message;

        // Fallback jeśli API nie działa
        teams.value = [
          {id: 1, name: 'Zespół A', manager: {firstName: 'Jan', lastName: 'Kowalski'}},
          {id: 2, name: 'Zespół B', manager: {firstName: 'Anna', lastName: 'Nowak'}},
          {id: 3, name: 'Zespół C', manager: {firstName: 'Piotr', lastName: 'Zieliński'}}
        ];

        teamMemberCounts[1] = 3;
        teamMemberCounts[2] = 4;
        teamMemberCounts[3] = 2;
      } finally {
        loading.value = false;
      }
    };

    // Pobierz liczbę członków zespołu
    const fetchTeamMemberCounts = async () => {
      for (const team of teams.value) {
        try {
          const members = await teamService.getTeamMembers(team.id);

          if (Array.isArray(members)) {
            // Liczba członków + kierownik (jeśli jest)
            let count = members.length;
            if (team.managerId) {
              count += 1; // Dodaj kierownika do liczby członków
            }
            teamMemberCounts[team.id] = count;
          } else {
            teamMemberCounts[team.id] = team.managerId ? 1 : 0;
          }
        } catch (err) {
          console.error(`Error fetching members for team ${team.id}:`, err);
          teamMemberCounts[team.id] = team.managerId ? 1 : 0;
        }
      }
    };

    // wybierz zespół i przejdź do szczegółów
    const selectTeam = (team) => {
      if (!team || !team.id) {
        console.error('Invalid team object or missing ID', team);
        return;
      }

      const teamId = team.id.toString();
      console.log('Selected team, ID:', teamId);

      router.push({ name: 'teamDetails', params: { id: teamId } });
    };

    // pomocnicze funkcje do wyświetlania skrótu i koloru zespołu
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
      return colors[team.id % colors.length];
    };

    // Pobierz nazwę kierownika zespołu
    const getTeamManagerName = (team) => {
      if (team.manager) {
        return `Kierownik: ${team.manager.firstName} ${team.manager.lastName}`;
      } else if (team.managerId) {
        return 'Kierownik: Ładowanie...';
      }
      return 'Kierownik: Nieprzypisany';
    };

    onMounted(fetchTeams);

    return {
      teams: visibleTeams,
      teamMemberCounts,
      loading,
      error,
      fetchTeams,
      selectTeam,
      getTeamShortName,
      getTeamColor,
      canAddTeam,
      getTeamManagerName
    };
  }
};
</script>