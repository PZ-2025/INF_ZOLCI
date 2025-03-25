<template>
  <div>
    <h1 class="text-3xl font-bold text-primary mb-6">Pracownicy</h1>
    
    <div class="bg-secondary p-6 rounded-lg shadow-md mb-6">
      <div v-if="employees.length" class="flex flex-col gap-4">
        <div v-for="employee in employees" :key="employee.id" class="bg-gray-700 p-4 rounded-lg shadow-md relative">
          <p class="font-medium text-lg text-white">{{ employee.name }}</p>
          <button @click="removeEmployee(employee.id)" class="absolute top-2 right-2 bg-danger text-white px-2 py-1 rounded-md hover:bg-red-700">
            Usuń
          </button>
        </div>
      </div>
      <p v-else class="text-white">Brak pracowników.</p>
    </div>
    
    <div class="flex justify-between mt-6">
      <button @click="addEmployee" class="bg-success text-white px-4 py-2 rounded-md hover:bg-green-700">
        Dodaj Pracownika
      </button>
      <button @click="removeAllEmployees" class="bg-danger text-white px-4 py-2 rounded-md hover:bg-red-700">
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
      employees.value.push({ id: Date.now(), name: newName });
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
  