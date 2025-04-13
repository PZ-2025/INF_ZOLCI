<template>
  <div class="h-full flex flex-col p-6 bg-background text-text">
    <h1 class="text-3xl font-bold text-primary mb-6">Pracownicy</h1>
    
    <div class="flex-grow bg-surface p-6 rounded-lg shadow-md mb-6 overflow-auto border border-gray-200">
      <div v-if="employees.length" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div 
          v-for="employee in employees" 
          :key="employee.id" 
          class="bg-surface border border-gray-200 p-4 rounded-lg shadow-md relative"
        >
          <p class="font-medium text-lg text-text">{{ employee.name }}</p>
          <button 
            @click="removeEmployee(employee.id)" 
            class="absolute top-2 right-2 bg-danger text-white px-2 py-1 rounded-md hover:bg-red-600"
          >
            Usuń
          </button>
        </div>
      </div>
      <p v-else class="text-muted text-center">Brak pracowników.</p>
    </div>
    
    <div class="flex justify-between">
      <button 
        @click="addEmployee" 
        class="bg-primary hover:bg-secondary text-white px-4 py-2 rounded-md transition"
      >
        Dodaj Pracownika
      </button>
      <button 
        @click="removeAllEmployees" 
        class="bg-danger hover:bg-red-600 text-white px-4 py-2 rounded-md transition"
      >
        Usuń Wszystkich
      </button>
    </div>
  </div>
</template>
  
<script setup>
import { ref } from 'vue';
  
// Przykładowa lista pracowników
const employees = ref([
  { id: 1, name: "Jan Kowalski" },
  { id: 2, name: "Anna Nowak" },
  { id: 3, name: "Piotr Zielinski" }
]);
  
const addEmployee = () => {
  const newName = prompt("Podaj imię i nazwisko pracownika:");
  if (newName) {
    employees.value.push({ 
      id: Date.now(), 
      name: newName.trim() 
    });
  }
};
  
const removeEmployee = (id) => {
  employees.value = employees.value.filter(employee => employee.id !== id);
};
  
const removeAllEmployees = () => {
  if (confirm("Czy na pewno chcesz usunąć wszystkich pracowników?")) {
    employees.value = [];
  }
};
</script>