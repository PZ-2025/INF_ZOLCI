<template>
    <div class="bg-white p-6 rounded-lg shadow-md">
      <h1 class="text-3xl font-bold text-gray-800 mb-6">Generowanie Raportów</h1>
  
      <!-- Typ raportu -->
      <div class="mb-4">
        <label for="reportType" class="block text-lg font-medium text-gray-700 mb-2">Wybierz typ raportu:</label>
        <select v-model="reportType" class="p-2 border rounded-md w-full" @change="handleReportTypeChange">
          <option value="workload">Raport obciążenia pracownika</option>
          <option value="progress">Raport postępu prac na budowie</option>
          <option value="teamEffectiveness">Raport efektywności zespołu</option>
        </select>
      </div>
  
      <!-- Okres raportu -->
      <div class="mb-4 flex gap-4">
        <div class="flex flex-col">
          <label class="block text-lg font-medium text-gray-700 mb-2">Data początkowa:</label>
          <input type="date" v-model="startDate" class="p-2 border rounded-md" />
        </div>
        <div class="flex flex-col">
          <label class="block text-lg font-medium text-gray-700 mb-2">Data końcowa:</label>
          <input type="date" v-model="endDate" class="p-2 border rounded-md" />
        </div>
      </div>
  
      <!-- Dynamiczne filtry -->
      <div v-if="reportType === 'workload'" class="mb-4">
        <label class="block text-lg font-medium text-gray-700 mb-2">Wybierz pracownika:</label>
        <select v-model="selectedEmployee" class="p-2 border rounded-md">
          <option value="john">Jan Kowalski</option>
          <option value="anna">Anna Nowak</option>
          <option value="peter">Piotr Zielinski</option>
        </select>
      </div>
  
      <div v-if="reportType === 'progress'" class="mb-4">
        <label class="block text-lg font-medium text-gray-700 mb-2">Wybierz zespół:</label>
        <select v-model="selectedTeam" class="p-2 border rounded-md">
          <option value="frontend">Frontend Devs</option>
          <option value="backend">Backend Engineers</option>
          <option value="qa">QA Team</option>
          <option value="design">Design Team</option>
        </select>
      </div>
  
      <div v-if="reportType === 'teamEffectiveness'" class="mb-4">
        <label class="block text-lg font-medium text-gray-700 mb-2">Wybierz zespół:</label>
        <select v-model="selectedEffectivenessTeam" class="p-2 border rounded-md">
          <option value="frontend">Frontend Devs</option>
          <option value="backend">Backend Engineers</option>
          <option value="qa">QA Team</option>
          <option value="design">Design Team</option>
        </select>
      </div>
  
      <!-- Przycisk generowania raportu -->
      <div class="mt-6">
        <button @click="generateReport" class="bg-blue-500 text-white px-6 py-2 rounded-md hover:bg-blue-700 transition w-full">
          Generuj Raport
        </button>
      </div>
  
      <!-- Wynik raportu -->
      <div v-if="reportGenerated" class="mt-6">
        <h2 class="text-2xl font-semibold text-gray-800 mb-4">Wynik Raportu:</h2>
        <pre class="bg-gray-200 p-4 rounded-md text-sm font-mono">{{ JSON.stringify(reportData, null, 2) }}</pre>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    data() {
      return {
        reportType: "workload",
        startDate: "",
        endDate: "",
        selectedEmployee: "",
        selectedTeam: "",
        selectedEffectivenessTeam: "",
        reportGenerated: false,
        reportData: {},
      };
    },
    methods: {
      handleReportTypeChange() {
        this.selectedEmployee = "";
        this.selectedTeam = "";
        this.selectedEffectivenessTeam = "";
      },
      generateReport() {
        this.reportData = {
          reportType: this.reportType,
          startDate: this.startDate,
          endDate: this.endDate,
        };
  
        if (this.reportType === "workload") {
          this.reportData.employee = this.selectedEmployee;
        } else if (this.reportType === "progress") {
          this.reportData.team = this.selectedTeam;
        } else if (this.reportType === "teamEffectiveness") {
          this.reportData.teamEffectiveness = this.selectedEffectivenessTeam;
        }
        
        this.reportGenerated = true;
      },
    },
  };
  </script>
  