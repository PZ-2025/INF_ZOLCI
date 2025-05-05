<template>
    <div class="min-h-screen flex items-center justify-center bg-background text-text px-4">
      <form @submit.prevent="updateTask" class="bg-surface p-6 rounded-lg shadow-md border border-gray-200 space-y-6 w-full max-w-xl">
        <h1 class="text-2xl font-bold text-primary mb-2">Edytuj Zadanie</h1>
  
        <div class="flex flex-col">
          <label for="title" class="text-lg font-medium mb-2">Tytuł zadania:</label>
          <input v-model="task.title" id="title" type="text" required
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary" />
        </div>
  
        <div class="flex flex-col">
          <label for="description" class="text-lg font-medium mb-2">Opis:</label>
          <textarea v-model="task.description" id="description" rows="3"
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary" />
        </div>
  
        <div class="flex flex-col">
          <label for="team" class="text-lg font-medium mb-2">Zespół:</label>
          <select v-model="task.team_id" id="team" required
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary">
            <option disabled value="">Wybierz zespół</option>
            <option v-for="team in teams" :key="team.id" :value="team.id">{{ team.name }}</option>
          </select>
        </div>
  
        <div class="flex flex-col">
          <label for="priority" class="text-lg font-medium mb-2">Priorytet:</label>
          <select v-model="task.priority_id" id="priority" required
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary">
            <option disabled value="">Wybierz priorytet</option>
            <option v-for="priority in priorities" :key="priority.id" :value="priority.id">{{ priority.name }}</option>
          </select>
        </div>
  
        <div class="flex flex-col">
          <label for="status" class="text-lg font-medium mb-2">Status:</label>
          <select v-model="task.status_id" id="status" required
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary">
            <option disabled value="">Wybierz status</option>
            <option v-for="status in statuses" :key="status.id" :value="status.id">{{ status.name }}</option>
          </select>
        </div>
  
        <div class="flex flex-col md:flex-row gap-4">
          <div class="flex flex-col flex-1">
            <label class="text-lg font-medium mb-2">Data rozpoczęcia:</label>
            <input type="date" v-model="task.start_date"
              class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary" />
          </div>
          <div class="flex flex-col flex-1">
            <label class="text-lg font-medium mb-2">Deadline:</label>
            <input type="date" v-model="task.deadline"
              class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary" />
          </div>
        </div>
  
        <div>
          <button type="submit"
            class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-md transition w-full">
            Zapisz zmiany
          </button>
        </div>
      </form>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  
  const route = useRoute()
  const router = useRouter()
  
  const taskId = route.params.id
  
  const task = ref({
    id: taskId,
    title: '',
    description: '',
    team_id: '',
    priority_id: '',
    status_id: '',
    start_date: '',
    deadline: ''
  })
  
  const teams = ref([])
  const priorities = ref([])
  const statuses = ref([])
  
  onMounted(() => {
    // Przykladowe dane - docelowo z API
    task.value = {
      id: taskId,
      title: 'Remont mieszkania',
      description: 'Opis zadania...',
      team_id: 1,
      priority_id: 3,
      status_id: 2,
      start_date: '2025-03-26',
      deadline: '2025-04-02'
    }
  
    teams.value = [
      { id: 1, name: 'Zespół A' },
      { id: 2, name: 'Zespół B' }
    ]
  
    priorities.value = [
      { id: 1, name: 'Niski' },
      { id: 2, name: 'Średni' },
      { id: 3, name: 'Wysoki' }
    ]
  
    statuses.value = [
      { id: 1, name: 'Rozpoczęte' },
      { id: 2, name: 'W toku' },
      { id: 3, name: 'Zakończone' }
    ]
  })
  
  const updateTask = () => {
    console.log('Zaktualizowane zadanie:', task.value)
    router.push('/taskshistory') // Po zapisaniu przekieruj z powrotem
  }
  </script>
  