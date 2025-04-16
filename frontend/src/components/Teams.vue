<template>
  <div class="p-4 bg-primary h-screen">
    <h2 class="text-2xl font-bold text-accent mb-6">Zespoły</h2>

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
          class="bg-secondary rounded-xl p-4 cursor-pointer hover:bg-primary transition transform hover:scale-105 flex flex-col items-center justify-center text-center"
      >
        <div
            class="w-16 h-16 rounded-xl mb-3 flex items-center justify-center text-white font-bold"
            :style="{ backgroundColor: team.color }"
        >
          {{ team.shortName }}
        </div>
        <h3 class="font-semibold text-white">{{ team.name }}</h3>
        <p class="text-accent text-sm">{{ team.membersCount }} członków</p>
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

    const fetchTeams = async () => {
      loading.value = true;
      error.value = null;

      try {
        const response = await apiService.get('/database/teams');
        teams.value = response;
      } catch (err) {
        console.error('Error fetching teams:', err);
        error.value = err.message;
        teams.value = [ /* fallback data */ ];
      } finally {
        loading.value = false;
      }
    };


    // TODO: find a fix to pass team id to other views via router.js
    // const selectTeam = (team) => {
    //   console.log('Selected team:', team.id); // debug
    //   router.push({
    //     name: 'TeamDetails',
    //     params: { id: team.id }
    //   });
    // };

    // TODO: functions for counting team members and shorten names for team icon (team.shortName, team.memberCount)

    onMounted(fetchTeams);

    return {
      teams,
      loading,
      error,
      fetchTeams,
      selectTeam
    };
  }
}
</script>