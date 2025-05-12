<template>
  <div class="p-4 bg-background min-h-screen text-text">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-primary">Zespoły</h1>

      <!-- Przycisk do przejścia na stronę dodawania zespołu -->
      <router-link
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
        <p class="text-muted text-sm">{{ teamMemberCounts[team.id] || 0 }} członków</p>
      </div>
    </div>
  </div>
</template>

<script>
// src/components/Teams.vue (script section)
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import teamService from '../services/teamService';

export default {
  name: 'TeamSelection',
  setup() {
    const router = useRouter();
    const teams = ref([]);
    const teamMemberCounts = reactive({});
    const loading = ref(true);
    const error = ref(null);

    // Fetch teams from API
    const fetchTeams = async () => {
      loading.value = true;
      error.value = null;

      try {
        // Get teams from API
        teams.value = await teamService.getAllTeams();

        // Fetch member counts for each team
        await fetchTeamMemberCounts();
      } catch (err) {
        console.error('Error fetching teams:', err);
        error.value = err.message;

        // Fallback data if API fails
        teams.value = [
          {id: 1, name: 'Zespół A', manager: {firstName: 'Jan', lastName: 'Kowalski'}},
          {id: 2, name: 'Zespół B', manager: {firstName: 'Anna', lastName: 'Nowak'}},
          {id: 3, name: 'Zespół C', manager: {firstName: 'Piotr', lastName: 'Zieliński'}}
        ];

        // Set some default member counts for fallback data
        teamMemberCounts[1] = 3;
        teamMemberCounts[2] = 4;
        teamMemberCounts[3] = 2;
      } finally {
        loading.value = false;
      }
    };

    // Fetch member counts for all teams
    const fetchTeamMemberCounts = async () => {
      for (const team of teams.value) {
        try {
          // Fetch team members using teamService
          const members = await teamService.getTeamMembers(team.id);

          // Store the count
          if (Array.isArray(members)) {
            teamMemberCounts[team.id] = members.length;
          } else {
            teamMemberCounts[team.id] = 0;
          }
        } catch (err) {
          console.error(`Error fetching members for team ${team.id}:`, err);
          teamMemberCounts[team.id] = 0; // Default to 0 on error
        }
      }
    };

    // Select a team and navigate to details
    const selectTeam = (team) => {
      if (!team || !team.id) {
        console.error('Invalid team object or missing ID', team);
        return;
      }

      const teamId = team.id.toString();
      console.log('Selected team, ID:', teamId);

      router.push({ name: 'teamDetails', params: { id: teamId } });
    };

    // Helper functions for UI
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

    onMounted(fetchTeams);

    return {
      teams,
      teamMemberCounts,
      loading,
      error,
      fetchTeams,
      selectTeam,
      getTeamShortName,
      getTeamColor
    };
  }
};
</script>