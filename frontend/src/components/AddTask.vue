<template>
    <div class="min-h-screen flex items-center justify-center bg-background text-text px-4">
      <form @submit.prevent="addTask" class="bg-surface p-6 rounded-lg shadow-md border border-gray-200 space-y-4 w-full">
        <h1 class="text-2xl font-bold text-primary mb-4">Dodaj Zadanie</h1>
  
        <div class="flex flex-col">
          <label for="title" class="block text-lg font-medium mb-2">Tytuł zadania</label>
          <input
            v-model="task.title"
            id="title"
            type="text"
            required
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
  
        <div class="flex flex-col">
          <label for="description" class="block text-lg font-medium mb-2">Opis zadania</label>
          <textarea
            v-model="task.description"
            id="description"
            rows="3"
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          ></textarea>
        </div>
  
        <div class="flex flex-col">
          <label for="team_id" class="block text-lg font-medium mb-2">Zespół</label>
          <select
            v-model="task.team_id"
            id="team_id"
            required
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option disabled selected value="">Wybierz zespół</option>
            <option v-for="team in teams" :key="team.id" :value="team.id">{{ team.name }}</option>
          </select>
        </div>
  
        <div class="flex flex-col">
          <label for="priority_id" class="block text-lg font-medium mb-2">Priorytet</label>
          <select
            v-model="task.priority_id"
            id="priority_id"
            required
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option disabled selected value="">Wybierz priorytet</option>
            <option v-for="priority in priorities" :key="priority.id" :value="priority.id">{{ priority.name }}</option>
          </select>
        </div>
  
        <div class="flex flex-col">
          <label for="start_date" class="block text-lg font-medium mb-2">Data rozpoczęcia</label>
          <input
            v-model="task.start_date"
            id="start_date"
            type="date"
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
  
        <div class="flex flex-col">
          <label for="deadline" class="block text-lg font-medium mb-2">Deadline</label>
          <input
            v-model="task.deadline"
            id="deadline"
            type="date"
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
  
        <div class="mt-6">
          <button type="submit" class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-md transition w-full">
            Dodaj Zadanie
          </button>
        </div>
      </form>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  
  const task = ref({
    title: '',
    description: '',
    team_id: '',
    priority_id: '',
    start_date: '',
    deadline: ''
  })
  
  const teams = ref([])
  const priorities = ref([])
  
  onMounted(() => {
    // sample data for teams and priorities
    teams.value = [
      { id: 1, name: 'Zespół A' },
      { id: 2, name: 'Zespół B' }
    ]
    priorities.value = [
      { id: 1, name: 'Niski' },
      { id: 2, name: 'Średni' },
      { id: 3, name: 'Wysoki' }
    ]
  })
  
  const addTask = () => {
    console.log('Dodano zadanie:', task.value)
    // spot for api call to add task
  }
  </script>
  