<template>
  <div class="min-h-screen bg-background text-text flex justify-center items-start px-4 py-10">
    <div class="bg-surface rounded-lg shadow-md border border-gray-200 p-6 w-full space-y-8">

      <h1 class="text-3xl font-bold text-primary">Historia Zadań</h1>

      <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="flex flex-col">
          <label class="mb-1 font-medium text-sm">Zespół:</label>
          <select v-model="filters.team"
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="">Wszystkie</option>
            <option value="frontend">Frontend Devs</option>
            <option value="backend">Backend Engineers</option>
            <option value="qa">QA Team</option>
            <option value="design">Design Team</option>
          </select>
        </div>

        <div class="flex flex-col">
          <label class="mb-1 font-medium text-sm">Priorytet:</label>
          <select v-model="filters.priority"
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="">Wszystkie</option>
            <option value="high">Wysoki</option>
            <option value="medium">Średni</option>
            <option value="low">Niski</option>
          </select>
        </div>

        <div class="flex flex-col">
          <label class="mb-1 font-medium text-sm">Status:</label>
          <select v-model="filters.status"
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="">Wszystkie</option>
            <option value="open">Rozpoczęte</option>
            <option value="in_progress">W toku</option>
            <option value="completed">Zakończony</option>
          </select>
        </div>

        <div class="flex flex-col">
          <label class="mb-1 font-medium text-sm">Deadline:</label>
          <input type="date" v-model="filters.deadline"
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
      </div>

      <div class="flex justify-end">
        <button @click="filterTasks"
          class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-md transition"
        >
          Filtruj
        </button>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="task in filteredTasks"
          :key="task.id"
          class="bg-white border border-gray-200 p-4 rounded-lg shadow-sm transition hover:shadow-lg"
        >
          <div>
            <h3 class="text-xl font-semibold text-primary">{{ task.name }}</h3>
            <p class="text-sm text-muted">{{ task.description }}</p>
          </div>
          <button @click="openTaskDetails(task)"
            class="mt-4 bg-primary hover:bg-secondary text-white px-4 py-2 rounded-md transition w-full"
          >
            Szczegóły
          </button>
        </div>
      </div>
      
      <div class="flex justify-end pt-6">
        <router-link
          to="/addtask"
          class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-md transition"
        >
          Dodaj Zadanie
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const tasks = ref([
  { id: 1, name: "Zadanie 1", description: "Praca nad frontendem aplikacji.", team: "frontend", priority: "high", status: "open", deadline: "2025-03-20" },
  { id: 2, name: "Zadanie 2", description: "Rozwój backendu aplikacji.", team: "backend", priority: "medium", status: "in_progress", deadline: "2025-03-25" },
  { id: 3, name: "Zadanie 3", description: "Testowanie aplikacji.", team: "qa", priority: "low", status: "completed", deadline: "2025-03-15" }
])

const filters = ref({
  team: "",
  priority: "",
  status: "",
  deadline: ""
})

const filteredTasks = computed(() => {
  return tasks.value.filter(task => {
    return (
      (!filters.value.team || task.team === filters.value.team) &&
      (!filters.value.priority || task.priority === filters.value.priority) &&
      (!filters.value.status || task.status === filters.value.status) &&
      (!filters.value.deadline || task.deadline === filters.value.deadline)
    )
  })
})

const filterTasks = () => {
  console.log("Zastosowano filtry:", filters.value)
}

const openTaskDetails = (task) => {
  alert(`Otwieram szczegóły zadania: ${task.name}`)
}
</script>
