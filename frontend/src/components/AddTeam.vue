<template>
    <div class="min-h-screen flex items-center justify-center bg-background text-text px-4">
      <form @submit.prevent="addTeam" class="bg-surface p-6 rounded-lg shadow-md border border-gray-200 space-y-6 w-full max-w-xl">
        <h1 class="text-2xl font-bold text-primary mb-2">Dodaj Zespół</h1>
  
        <div class="flex flex-col">
          <label for="teamName" class="text-lg font-medium mb-2">Nazwa zespołu:</label>
          <input
            v-model="team.name"
            id="teamName"
            type="text"
            placeholder="np. Zespół A"
            required
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
  
        <div class="flex flex-col">
          <label for="manager" class="text-lg font-medium mb-2">Kierownik:</label>
          <select
            v-model="team.manager_id"
            id="manager"
            required
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option disabled selected value="">Wybierz kierownika</option>
            <option v-for="manager in managers" :key="manager.id" :value="manager.id">
              {{ manager.first_name }} {{ manager.last_name }}
            </option>
          </select>
        </div>
  
        <div class="pt-2">
          <button type="submit" class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-md transition w-full">
            Dodaj Zespół
          </button>
        </div>
      </form>
    </div>
  </template>
  
  <script>
  export default {
    data() {
      return {
        team: {
          name: '',
          manager_id: ''
        },
        managers: []
      };
    },
    created() {
      // sample data for managers
      this.managers = [
        { id: 2, first_name: 'Fifonż', last_name: 'Wiśniewski' },
        { id: 3, first_name: 'Anna', last_name: 'Nowak' }
      ];
    },
    methods: {
      addTeam() {
        console.log("✅ Dodano zespół:", this.team);
        // spot for api call to add team
      }
    }
  };
  </script>
  