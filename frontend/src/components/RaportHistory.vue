<template>
  <div class="bg-primary text-white min-h-screen p-6">
    <h1 class="text-3xl font-bold text-accent mb-6">Historia Raportów</h1>

    <div class="flex justify-between items-center mb-6">
      <div class="flex items-center">
        <label for="reportDate" class="mr-2 font-medium text-white">Data Raportu:</label>
        <input type="date" v-model="selectedDate" class="p-2 border rounded-md bg-secondary text-white">
      </div>

      <div class="flex items-center">
        <label for="reportType" class="mr-2 font-medium text-white">Typ Raportu:</label>
        <select v-model="selectedType" class="p-2 border rounded-md bg-secondary text-white">
          <option value="workload">Raport obciążenia pracownika</option>
          <option value="construction_progress">Raport postępu prac na budowie</option>
          <option value="team_efficiency">Raport efektywności zespołu</option>
        </select>
      </div>

      <button @click="filterReports" class="bg-warning text-white p-2 rounded-md hover:bg-danger transition">Filtruj</button>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="report in filteredReports" :key="report.id" class="bg-secondary p-4 rounded-lg shadow-lg flex justify-between items-center hover:scale-105 transition">
        <div>
          <h3 class="text-xl font-semibold text-accent">{{ report.name }}</h3>
          <p class="text-white">{{ report.description }}</p>
        </div>
        <button @click="openReportDetails(report)" class="bg-warning text-white px-4 py-2 rounded-md hover:bg-danger transition">Szczegóły</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      selectedDate: "",
      selectedType: "",
      reports: [
        { id: 1, name: "Raport obciążenia pracownika", description: "Podsumowanie obciążenia pracownika za miesiąc marzec.", type: "workload" },
        { id: 2, name: "Raport postępu prac na budowie", description: "Raport o stanie zaawansowania prac budowlanych na projekcie XYZ.", type: "construction_progress" },
        { id: 3, name: "Raport efektywności zespołu", description: "Podsumowanie efektywności zespołu IT w ostatnich trzech miesiącach.", type: "team_efficiency" }
      ]
    };
  },
  computed: {
    filteredReports() {
      return this.reports.filter(report => !this.selectedType || report.type === this.selectedType);
    }
  },
  methods: {
    filterReports() {
      console.log("Filtruję raporty dla", this.selectedDate, this.selectedType);
    },
    openReportDetails(report) {
      alert(`Otwieram szczegóły raportu: ${report.name}`);
    }
  }
};
</script>