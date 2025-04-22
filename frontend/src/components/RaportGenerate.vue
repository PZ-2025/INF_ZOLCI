<template>
  <div class="min-h-screen flex items-center justify-center bg-background text-text px-4">
    <form @submit.prevent="generateReport" class="bg-surface p-6 rounded-lg shadow-md border border-gray-200 space-y-6 w-full max-w-xl">
      <h1 class="text-2xl font-bold text-primary mb-2">Generowanie Raportów</h1>

      <div class="flex flex-col">
        <label for="reportName" class="text-lg font-medium mb-2">Nazwa raportu:</label>
        <input
          id="reportName"
          v-model="reportName"
          type="text"
          placeholder="np. Raport marzec 2025"
          class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
        />
      </div>

      <div class="flex flex-col">
        <label for="reportType" class="text-lg font-medium mb-2">Wybierz typ raportu:</label>
        <select
          id="reportType"
          v-model="reportType"
          @change="handleReportTypeChange"
          class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
        >
          <option value="workload">Raport obciążenia pracownika</option>
          <option value="progress">Raport postępu prac na budowie</option>
          <option value="teamEffectiveness">Raport efektywności zespołu</option>
        </select>
      </div>

      <div class="flex flex-col md:flex-row gap-4">
        <div class="flex flex-col flex-1">
          <label for="startDate" class="text-lg font-medium mb-2">Data początkowa:</label>
          <input
            id="startDate"
            type="date"
            v-model="startDate"
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
        <div class="flex flex-col flex-1">
          <label for="endDate" class="text-lg font-medium mb-2">Data końcowa:</label>
          <input
            id="endDate"
            type="date"
            v-model="endDate"
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
      </div>

      <div v-if="reportType === 'teamEffectiveness'" class="flex flex-col">
        <label for="team" class="text-lg font-medium mb-2">Zespół:</label>
        <select
          id="team"
          v-model="selectedTeam"
          class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
        >
          <option disabled selected value="">Wybierz zespół</option>
          <option v-for="team in teams" :key="team.id" :value="team.id">
            {{ team.name }}
          </option>
        </select>
      </div>

      <div v-if="reportType === 'workload'" class="flex flex-col">
        <label for="user" class="text-lg font-medium mb-2">Użytkownik:</label>
        <select
          id="user"
          v-model="selectedUser"
          class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
        >
          <option disabled selected value="">Wybierz użytkownika</option>
          <option v-for="user in users" :key="user.id" :value="user.id">
            {{ user.first_name }} {{ user.last_name }}
          </option>
        </select>
      </div>

      <div class="pt-2">
        <button type="submit" class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-md transition w-full">
          Generuj Raport
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const reportName = ref('')
const reportType = ref('workload')
const startDate = ref('')
const endDate = ref('')
const selectedTeam = ref('')
const selectedUser = ref('')

const teams = ref([])
const users = ref([])

const handleReportTypeChange = () => {
  // reset selected values when report type changes 
  selectedTeam.value = ''
  selectedUser.value = ''
}

onMounted(() => {
  // sample data for teams and users
  teams.value = [
    { id: 1, name: 'Zespół A' },
    { id: 2, name: 'Zespół B' }
  ]
  users.value = [
    { id: 3, first_name: 'Jan', last_name: 'Kowalski' },
    { id: 4, first_name: 'Anna', last_name: 'Nowak' }
  ]
})

const generateReport = () => {
  const parameters = {
    startDate: startDate.value,
    endDate: endDate.value,
    ...(reportType.value === 'teamEffectiveness' && { teamId: selectedTeam.value }),
    ...(reportType.value === 'workload' && { userId: selectedUser.value })
  }

  const payload = {
    name: reportName.value || 'Raport bez nazwy',
    type: reportType.value,
    parameters: JSON.stringify(parameters)
  }

  console.log('➡️ Generowanie raportu:', payload)
  // send payload to API
}
</script>
