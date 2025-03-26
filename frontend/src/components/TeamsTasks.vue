<template>
  <div class="bg-primary text-white min-h-screen p-6">
    <h1 class="text-3xl font-bold text-accent mb-6">Historia Zadań</h1>

    <div class="flex justify-between items-center mb-6">
      <div class="flex items-center">
        <label for="team" class="mr-2 font-medium text-white">Zespół:</label>
        <select v-model="filters.team" class="p-2 border rounded-md bg-secondary text-white">
          <option value="">Wszystkie</option>
          <option value="frontend">Frontend Devs</option>
          <option value="backend">Backend Engineers</option>
          <option value="qa">QA Team</option>
          <option value="design">Design Team</option>
        </select>
      </div>

      <div class="flex items-center">
        <label class="mr-2 font-medium text-white">Priorytet:</label>
        <select v-model="filters.priority" class="p-2 border rounded-md bg-secondary text-white">
          <option value="">Wszystkie</option>
          <option value="high">Wysoki</option>
          <option value="medium">Średni</option>
          <option value="low">Niski</option>
        </select>
      </div>

      <div class="flex items-center">
        <label class="mr-2 font-medium text-white">Status:</label>
        <select v-model="filters.status" class="p-2 border rounded-md bg-secondary text-white">
          <option value="">Wszystkie</option>
          <option value="open">Rozpoczęte</option>
          <option value="in_progress">W toku</option>
          <option value="completed">Zakończony</option>
        </select>
      </div>

      <div class="flex items-center">
        <label class="mr-2 font-medium text-white">Deadline:</label>
        <input type="date" v-model="filters.deadline" class="p-2 border rounded-md bg-secondary text-white">
      </div>

      <button @click="applyFilters" class="bg-warning text-white p-2 rounded-md hover:bg-danger transition">
        Filtruj
      </button>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="task in filteredTasks" :key="task.id" class="bg-secondary p-4 rounded-lg shadow-lg flex justify-between items-center hover:scale-105 transition">
        <div>
          <h3 class="text-xl font-semibold text-accent">{{ task.name }}</h3>
          <p class="text-white">{{ task.description }}</p>
        </div>
        <button @click="openTaskDetails(task)" class="bg-warning text-white px-4 py-2 rounded-md hover:bg-danger transition">
          Szczegóły
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      filters: {
        team: "",
        priority: "",
        status: "",
        deadline: ""
      },
      tasks: [
        { id: 1, name: "Zadanie 1", description: "Praca nad frontendem aplikacji.", team: "frontend", priority: "high", status: "open", deadline: "2025-03-20" },
        { id: 2, name: "Zadanie 2", description: "Rozwój backendu aplikacji.", team: "backend", priority: "medium", status: "in_progress", deadline: "2025-03-25" },
        { id: 3, name: "Zadanie 3", description: "Testowanie aplikacji.", team: "qa", priority: "low", status: "completed", deadline: "2025-03-15" }
      ]
    };
  },
  computed: {
    filteredTasks() {
      return this.tasks.filter(task => {
        return (
          (!this.filters.team || task.team === this.filters.team) &&
          (!this.filters.priority || task.priority === this.filters.priority) &&
          (!this.filters.status || task.status === this.filters.status) &&
          (!this.filters.deadline || task.deadline === this.filters.deadline)
        );
      });
    }
  },
  methods: {
    applyFilters() {
      // filtracja dzieje się automatycznie - computed properties
    },
    openTaskDetails(task) {
      alert(`Otwieram szczegóły zadania: ${task.name}`);
    }
  }
};
</script>