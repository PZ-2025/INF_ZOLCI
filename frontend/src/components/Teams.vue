<template>
  <div class="p-4 bg-background min-h-screen text-text">
    <h2 class="text-2xl font-bold text-primary mb-6">Zespoły</h2>

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
        <p class="text-muted text-sm">{{ getTeamMembersCount(team) }} członków</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import apiService from '../services/api.js';
import { useRouter } from 'vue-router';

export default {
  name: 'TeamSelection',
  setup() {
    const router = useRouter();
    const teams = ref([]);
    const loading = ref(true);
    const error = ref(null);

    // Funkcja pobierająca zespoły z API
    const fetchTeams = async () => {
      loading.value = true;
      error.value = null;

      try {
        // Pobierz zespoły z API
        const response = await apiService.get('/database/teams');
        teams.value = response;
      } catch (err) {
        console.error('Error fetching teams:', err);
        error.value = err.message;

        // Dane awaryjne na wypadek błędu API
        teams.value = [
          {id: 1, name: 'Zespół A', manager: {firstName: 'Jan', lastName: 'Kowalski'}},
          {id: 2, name: 'Zespół B', manager: {firstName: 'Anna', lastName: 'Nowak'}},
          {id: 3, name: 'Zespół C', manager: {firstName: 'Piotr', lastName: 'Zieliński'}}
        ];
      } finally {
        loading.value = false;
      }
    };

    // NAJWAŻNIEJSZA ZMIANA - niezawodna metoda nawigacji
    const selectTeam = (team) => {
      if (!team || !team.id) {
        console.error('Nieprawidłowy obiekt zespołu lub brak ID', team);
        return;
      }

      // Zapewniamy, że ID jest stringiem
      const teamId = team.id.toString();
      console.log('Wybrano zespół, ID:', teamId);


      // Metoda 2: Używamy URL z query
      router.push({ name: 'teamDetails', params: { id: teamId } });

      // Log potwierdzający
      console.log('Rozpoczęto nawigację do:', `/teamdetails/${teamId}`);
    };

    // Funkcje pomocnicze pozostają bez zmian
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

    const getTeamMembersCount = (team) => {
      if (team.members && Array.isArray(team.members)) {
        return team.members.length;
      }
      return team.id ? (team.id % 5) + 1 : 0;
    };

    onMounted(fetchTeams);

    return {
      teams,
      loading,
      error,
      fetchTeams,
      selectTeam,
      getTeamShortName,
      getTeamColor,
      getTeamMembersCount
    };
  }
}
</script>