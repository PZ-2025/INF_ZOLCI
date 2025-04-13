<template>
  <div class="p-6 bg-background min-h-screen text-text overflow-auto">
    <div class="bg-surface rounded-xl p-4 flex justify-between items-center mb-6 shadow border border-gray-200">
      <div class="flex items-center">
        <div class="w-12 h-12 rounded-xl mr-4 flex items-center justify-center text-white font-bold" :style="{ backgroundColor: currentTeam.color }">
          {{ currentTeam.shortName }}
        </div>
        <div>
          <h1 class="text-2xl font-bold text-primary">{{ currentTeam.name }}</h1>
          <p class="text-muted text-sm">{{ currentTeam.membersCount }} członków</p>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <!-- Członkowie Zespołu -->
      <div class="bg-surface rounded-xl p-6 shadow border border-gray-200">
        <h2 class="text-xl font-bold text-primary mb-4">Członkowie Zespołu</h2>
        <div class="space-y-4">
          <div 
            v-for="member in currentTeam.members" 
            :key="member.id" 
            class="flex items-center bg-gray-50 p-3 rounded-lg"
          >
            <div class="w-10 h-10 rounded-xl mr-3 flex items-center justify-center text-white font-bold" :style="{ backgroundColor: member.color }">
              {{ member.initials }}
            </div>
            <div>
              <h3 class="font-semibold text-text">{{ member.name }}</h3>
              <p class="text-sm text-muted">{{ member.role }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Ostatnie Aktywności -->
      <div class="bg-surface rounded-xl p-6 shadow border border-gray-200">
        <h2 class="text-xl font-bold text-primary mb-4">Ostatnie Aktywności</h2>
        <div class="space-y-4">
          <div 
            v-for="activity in currentTeam.recentActivities" 
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
        <div class="space-y-4">
          <div 
            v-for="task in currentTeam.upcomingTasks" 
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
              {{ task.priority.toUpperCase() }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
  
  <script>
  export default {
    name: 'TeamDetails',
    props: {
      selectedTeam: {
        type: Object,
        default: null
      }
    },
    data() {
      return {
        defaultTeam: {
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
        }
      };
    },
    computed: {
      currentTeam() {
        return this.selectedTeam || this.defaultTeam;
      }
    }

  }
  </script>