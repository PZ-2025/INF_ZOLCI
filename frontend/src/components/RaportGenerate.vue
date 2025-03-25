<template>
  <div class="h-full flex flex-col p-6 bg-primary">
    <h1 class="text-3xl font-bold text-white mb-6">Generowanie Raportów</h1>

    <div class="mb-4">
      <label for="reportType" class="block text-lg font-medium text-white mb-2">Wybierz typ raportu:</label>
      <select v-model="reportType" class="p-2 border rounded-md w-full bg-warning text-white" @change="handleReportTypeChange">
        <option value="workload">Raport obciążenia pracownika</option>
        <option value="progress">Raport postępu prac na budowie</option>
        <option value="teamEffectiveness">Raport efektywności zespołu</option>
      </select>
    </div>

    <div class="mb-4 flex gap-4">
      <div class="flex flex-col">
        <label class="block text-lg font-medium text-white mb-2">Data początkowa:</label>
        <input type="date" v-model="startDate" class="p-2 border rounded-md bg-warning text-white" />
      </div>
      <div class="flex flex-col">
        <label class="block text-lg font-medium text-white mb-2">Data końcowa:</label>
        <input type="date" v-model="endDate" class="p-2 border rounded-md bg-warning text-white" />
      </div>
    </div>

    <div class="mt-6">
      <button @click="generateReport" class="bg-warning text-white px-6 py-2 rounded-md hover:bg-danger transition w-full">
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
  