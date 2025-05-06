<template>
    <div class="min-h-screen bg-background text-text p-6">
      <div class="max-w-3xl mx-auto bg-white shadow rounded-2xl p-8">
        <h1 class="text-3xl font-bold text-primary mb-6">Zarządzaj członkami zespołu</h1>
        <p class="text-gray-600 mb-4">Zaznacz pracowników, którzy mają należeć do tego zespołu.</p>
  
        <form @submit.prevent="updateTeamMembers" class="space-y-6">
          <!-- Wyszukiwanie -->
          <div>
            <input
              v-model="search"
              type="text"
              placeholder="Szukaj członka zespołu..."
              class="w-full p-3 border border-gray-300 rounded-lg bg-gray-100 text-black"
            />
          </div>
  
          <!-- Zaznacz wszystkich -->
          <div>
            <label class="inline-flex items-center space-x-2 cursor-pointer text-sm">
              <input
                type="checkbox"
                @change="toggleAll"
                :checked="selectedMembers.length === filteredEmployees.length && filteredEmployees.length > 0"
              />
              <span>Zaznacz / odznacz wszystkich widocznych</span>
            </label>
          </div>
  
          <!-- Lista pracowników -->
          <div class="space-y-3 max-h-72 overflow-y-auto border border-gray-300 rounded-lg p-3 bg-white">
            <div
              v-for="employee in filteredEmployees"
              :key="employee.id"
              class="flex items-center justify-between bg-gray-50 hover:bg-gray-100 p-3 rounded-md transition"
            >
              <div class="flex items-center space-x-3">
                <input
                  type="checkbox"
                  v-model="selectedMembers"
                  :value="employee.id"
                  class="form-checkbox h-5 w-5 text-primary focus:ring-primary"
                />
                <span class="text-black">{{ employee.name }}</span>
              </div>
              <span class="text-sm text-gray-500">{{ employee.email }}</span>
            </div>
  
            <div v-if="filteredEmployees.length === 0" class="text-center text-gray-400 py-4">
              Brak pasujących członków.
            </div>
          </div>
  
          <!-- Zapisz -->
          <div>
            <button
              type="submit"
              class="bg-accent hover:bg-secondary text-white px-6 py-3 rounded-xl w-full text-lg font-medium shadow">
              Zapisz zmiany
            </button>
          </div>
        </form>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, computed } from 'vue'
  
  // Przykładowe dane pracowników
  const employees = ref([
    { id: 1, name: 'Jan Kowalski', email: 'jan@example.com' },
    { id: 2, name: 'Anna Nowak', email: 'anna@example.com' },
    { id: 3, name: 'Piotr Wiśniewski', email: 'piotr@example.com' },
    { id: 4, name: 'Katarzyna Zielińska', email: 'kasia@example.com' },
    { id: 5, name: 'Marek Maj', email: 'marek@example.com' }
  ])
  
  const selectedMembers = ref([2, 4]) // przykładowe zaznaczone osoby
  const search = ref('')
  
  // Filtrowanie według wyszukiwarki
  const filteredEmployees = computed(() =>
    employees.value.filter(e =>
      e.name.toLowerCase().includes(search.value.toLowerCase()) ||
      e.email.toLowerCase().includes(search.value.toLowerCase())
    )
  )
  
  // Zaznacz / Odznacz wszystkich
  function toggleAll(event) {
    if (event.target.checked) {
      selectedMembers.value = filteredEmployees.value.map(e => e.id)
    } else {
      selectedMembers.value = selectedMembers.value.filter(
        id => !filteredEmployees.value.some(e => e.id === id)
      )
    }
  }
  
  // Funkcja zapisu (tymczasowo tylko alert)
  function updateTeamMembers() {
    alert('Zapisano członków zespołu: ' + JSON.stringify(selectedMembers.value))
  }
  </script>
  