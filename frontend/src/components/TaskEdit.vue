<template>
  <div class="h-full flex flex-col p-6 bg-background text-text">
    <h1 class="text-3xl font-bold text-primary mb-6">Edytuj Zadanie</h1>
    <div class="p-6 border border-gray-300 rounded-md bg-white shadow-md">
      <form @submit.prevent="updateTask" class="space-y-4">
        <div>
          <label class="block text-lg font-medium mb-2">Tytuł</label>
          <input
            v-model="task.title"
            type="text"
            placeholder="Wprowadź tytuł zadania"
            class="p-2 border border-gray-300 rounded-md w-full bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
            required
          />
        </div>

        <div>
          <label class="block text-lg font-medium mb-2">Opis</label>
          <textarea
            v-model="task.description"
            placeholder="Wprowadź opis zadania"
            class="p-2 border border-gray-300 rounded-md w-full bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          ></textarea>
        </div>

        <div>
          <label class="block text-lg font-medium mb-2">Zespół</label>
          <select
            v-model="task.team_id"
            required
            class="p-2 border border-gray-300 rounded-md w-full bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option disabled value="">Wybierz zespół</option>
            <option v-for="team in teams" :key="team.id" :value="team.id">{{ team.name }}</option>
          </select>
        </div>

        <div>
          <label class="block text-lg font-medium mb-2">Priorytet</label>
          <select
            v-model="task.priority_id"
            required
            class="p-2 border border-gray-300 rounded-md w-full bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option disabled value="">Wybierz priorytet</option>
            <option v-for="priority in priorities" :key="priority.id" :value="priority.id">{{ priority.name }}</option>
          </select>
        </div>

        <div>
          <label class="block text-lg font-medium mb-2">Status</label>
          <select
            v-model="task.status_id"
            required
            class="p-2 border border-gray-300 rounded-md w-full bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option disabled value="">Wybierz status</option>
            <option v-for="status in statuses" :key="status.id" :value="status.id">{{ status.name }}</option>
          </select>
        </div>

        <div class="flex flex-col md:flex-row gap-4">
          <div class="flex-1">
            <label class="block text-lg font-medium mb-2">Data rozpoczęcia</label>
            <input
              type="date"
              v-model="task.start_date"
              class="p-2 border border-gray-300 rounded-md w-full bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
          <div class="flex-1">
            <label class="block text-lg font-medium mb-2">Deadline</label>
            <input
              type="date"
              v-model="task.deadline"
              class="p-2 border border-gray-300 rounded-md w-full bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
        </div>

        <div class="mt-6">
          <button
            type="submit"
            class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-md transition w-full"
          >
            Zapisz zmiany
          </button>
        </div>
      </form>
    </div>
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
  // Przykladowe dane — zamień na fetch z backendu
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

// const updateTask = async () => {
//   try {
//     const response = await fetch(`/api/tasks/${taskId}`, {
//       method: 'PUT',
//       headers: {
//         'Content-Type': 'application/json'
//       },
//       body: JSON.stringify(task.value)
//     })

//     if (!response.ok) throw new Error('Błąd podczas aktualizacji zadania')

//     alert('Zadanie zaktualizowane pomyślnie')
//     router.push('/taskshistory')
//   } catch (error) {
//     alert('Wystąpił błąd: ' + error.message)
//   }
// }
</script>
