<template>
  <div class="p-4 bg-background min-h-screen text-text">
    <h2 class="text-2xl font-bold text-primary mb-6">Zespoły</h2>
    
    <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
      <div 
        v-for="team in teams" 
        :key="team.id" 
        @click="selectTeam(team)"
        class="bg-surface shadow-md rounded-xl p-4 cursor-pointer hover:bg-accent/10 transition transform hover:scale-105 flex flex-col items-center justify-center text-center"
      >
        <div 
          class="w-16 h-16 rounded-xl mb-3 flex items-center justify-center text-white font-bold"
          :style="{ backgroundColor: team.color }"
        >
          {{ team.shortName }}
        </div>
        <h3 class="font-semibold text-secondary">{{ team.name }}</h3>
        <p class="text-muted text-sm">{{ team.membersCount }} członków</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TeamSelection',
  data() {
    return {
      teams: [
        {
          id: 1,
          name: 'Frontend Development',
          shortName: 'FE',
          color: '#D8572A',
          membersCount: 5,
          members: [
            { 
              id: 1, 
              name: 'Jan Kowalski', 
              initials: 'JK', 
              role: 'Senior Developer',
              color: '#780116'
            },
            { 
              id: 2, 
              name: 'Anna Nowak', 
              initials: 'AN', 
              role: 'UI/UX Designer',
              color: '#DB7C26'
            },
            { 
              id: 3, 
              name: 'Piotr Wiśniewski', 
              initials: 'PW', 
              role: 'Junior Developer',
              color: '#F7B538'
            }
          ],
          recentActivities: [
            {
              id: 1,
              title: 'Nowy komponent UI',
              description: 'Utworzono nowy komponent nawigacyjny',
              timestamp: '2 godziny temu'
            },
            {
              id: 2,
              title: 'Code Review',
              description: 'Przeprowadzono przegląd kodu dla PR #245',
              timestamp: '5 godzin temu'
            }
          ],
          upcomingTasks: [
            {
              id: 1,
              title: 'Redesign strony głównej',
              assignedTo: 'Jan Kowalski',
              priority: 'high'
            },
            {
              id: 2,
              title: 'Optymalizacja wydajności',
              assignedTo: 'Piotr Wiśniewski',
              priority: 'medium'
            }
          ]
        },
        {
          id: 2,
          name: 'Backend Engineering',
          shortName: 'BE',
          color: '#F7B538',
          membersCount: 4,
          members: [
            { 
              id: 4, 
              name: 'Michał Kowalczyk', 
              initials: 'MK', 
              role: 'Tech Lead',
              color: '#780116'
            },
            { 
              id: 5, 
              name: 'Katarzyna Nowacka', 
              initials: 'KN', 
              role: 'Senior Backend Developer',
              color: '#DB7C26'
            }
          ],
          recentActivities: [
            {
              id: 1,
              title: 'Wdrożenie nowego API',
              description: 'Zakończono implementację nowego endpointu',
              timestamp: '3 godziny temu'
            }
          ],
          upcomingTasks: [
            {
              id: 1,
              title: 'Migracja bazy danych',
              assignedTo: 'Michał Kowalczyk',
              priority: 'high'
            },
            {
              id: 2,
              title: 'Zabezpieczenia systemu',
              assignedTo: 'Katarzyna Nowacka',
              priority: 'medium'
            }
          ]
        }
      ]
    };
  },
  methods: {
    selectTeam(team) {
      this.$router.push('/teamdetails');
    }
  }
}
</script>
