<template>
    <div class="bg-gray-100 min-h-screen p-8">
      <h1 class="text-3xl font-bold text-gray-800 mb-6">Konfiguracja Systemu</h1>
  
      <!-- Statusy -->
      <div class="bg-white p-6 rounded-lg shadow-md mb-6">
        <h2 class="text-xl font-semibold mb-4">Kryteria Statusów (1-100)</h2>
        <input
          type="range"
          min="1"
          max="100"
          v-model="statusValue"
          class="w-full"
        />
        <p class="mt-2">Wybrany poziom: <span>{{ statusValue }}</span></p>
      </div>
  
      <!-- Priorytety -->
      <div class="bg-white p-6 rounded-lg shadow-md mb-6">
        <h2 class="text-xl font-semibold mb-4">Globalne Kryteria Priorytetów</h2>
        <select v-model="selectedPriority" class="w-full p-2 border rounded">
          <option v-for="priority in priorities" :key="priority" :value="priority">
            {{ priority }}
          </option>
        </select>
      </div>
  
      <!-- Inne ustawienia administratora -->
      <div class="bg-white p-6 rounded-lg shadow-md">
        <h2 class="text-xl font-semibold mb-4">Inne Ustawienia</h2>
        <label class="block mb-2">Automatyczna archiwizacja zadań</label>
        <input type="checkbox" v-model="autoArchive" class="mr-2"> Włącz
      </div>
    </div>
  </template>
  
  <script>
  export default {
    data() {
      return {
        statusValue: 50,
        selectedPriority: "Średni",
        priorities: ["Niski", "Średni", "Wysoki", "Krytyczny"],
        autoArchive: false
      };
    }
  };
  </script>
  
  <style scoped>
  /* dodatkowe style */
  </style>
  