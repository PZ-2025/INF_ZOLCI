<template>
  <div class="min-h-screen bg-background flex items-center justify-center px-4 py-10">
    <div class="w-full max-w-2xl bg-surface border border-gray-200 rounded-2xl shadow-xl p-8 space-y-6">
      <h2 class="text-3xl font-bold text-primary text-center">Szczegóły Zadania</h2>

      <div class="space-y-4">
        <p><span class="font-semibold text-black">Tytuł:</span> <span class="text-black">{{ task.title }}</span></p>
        <p><span class="font-semibold text-black">Opis:</span> <span class="text-black">{{ task.description }}</span></p>
        <p><span class="font-semibold text-black">Status:</span> <span :class="statusColor">{{ task.status }}</span></p>
        <p><span class="font-semibold text-black">Priorytet:</span> <span class="text-black">{{ task.priority }}</span></p>
        <p><span class="font-semibold text-black">Termin:</span> <span class="text-black">{{ task.deadline }}</span></p>
        <p><span class="font-semibold text-black">Zespół:</span> <span class="text-black">{{ task.team.name }}</span></p>
      </div>

      <h3 class="text-xl font-semibold text-primary">Komentarze</h3>
      <ul class="space-y-4">
        <li v-for="comment in task.comments" :key="comment.id" class="bg-gray-100 p-4 rounded-lg">
          <div class="text-sm text-gray-600 mb-1">{{ comment.user }} — {{ comment.date }}</div>
          <p class="text-black">{{ comment.text }}</p>
        </li>
      </ul>

      <div class="flex justify-end space-x-4">
        <button @click="editTask"
          class="px-4 py-2 bg-primary hover:bg-secondary text-white rounded-lg text-sm transition">
          Edytuj
        </button>
        <button @click="deleteTask"
          class="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm transition">
          Usuń zadanie
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TaskDetails',
  data() {
    return {
      task: {
        id: 1,
        title: 'Remont biura klienta',
        description: 'Przeprowadzenie remontu biura na ul. Głównej 12. Zakres prac obejmuje malowanie ścian, wymianę podłóg oraz instalację oświetlenia.',
        status: 'W trakcie',
        priority: 'Wysoki',
        deadline: '2025-05-20',
        team: {
          id: 3,
          name: 'Zespół Budowlany A',
        },
        comments: [
          {
            id: 1,
            user: 'Anna Nowak',
            date: '2025-05-05',
            text: 'Prace malarskie zostały rozpoczęte, zakończenie planowane na jutro.',
          },
          {
            id: 2,
            user: 'Jan Kowalski',
            date: '2025-05-06',
            text: 'Podłogi zostały zamówione, dostawa planowana na 2025-05-08.',
          },
        ],
      },
    };
  },
  computed: {
    statusColor() {
      switch (this.task.status) {
        case 'Nowe':
          return 'text-blue-500 font-semibold';
        case 'W trakcie':
          return 'text-yellow-500 font-semibold';
        case 'Zakończone':
          return 'text-green-500 font-semibold';
        default:
          return '';
      }
    },
  },
  methods: {
    editTask() {
      this.$router.push({ name: 'EditTask', params: { id: this.task.id } });
    },
    deleteTask() {
      if (confirm('Czy na pewno chcesz usunąć to zadanie?')) {
        alert('Zadanie usunięte (symulacja).');
      }
    },
  },
};
</script>

<style scoped>
</style>
