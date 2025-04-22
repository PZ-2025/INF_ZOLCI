<template>
  <div class="h-full flex flex-col p-6 bg-background text-text">
    <h1 class="text-3xl font-bold text-primary mb-6">Generowanie Raportów</h1>

    <div class="mb-4">
      <label for="reportType" class="block text-lg font-medium mb-2">Wybierz typ raportu:</label>
      <select v-model="reportType" @change="handleReportTypeChange"
        class="p-2 border border-gray-300 rounded-md w-full bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
      >
        <option value="workload">Raport obciążenia pracownika</option>
        <option value="progress">Raport postępu prac na budowie</option>
        <option value="teamEffectiveness">Raport efektywności zespołu</option>
      </select>
    </div>

    <div class="mb-4 flex gap-4">
      <div class="flex flex-col flex-1">
        <label class="block text-lg font-medium mb-2">Data początkowa:</label>
        <input type="date" v-model="startDate"
          class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
        />
      </div>
      <div class="flex flex-col flex-1">
        <label class="block text-lg font-medium mb-2">Data końcowa:</label>
        <input type="date" v-model="endDate"
          class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
        />
      </div>
    </div>

    <div class="mt-6">
      <button @click="generateReport"
        class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-md transition w-full"
      >
        Generuj Raport
      </button>
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
  