<template>
  <div class="h-full flex flex-col p-6 bg-background text-text">
    <h1 class="text-3xl font-bold text-primary mb-6">Użytkownicy</h1>
    
    <div class="flex-grow bg-surface p-6 rounded-lg shadow-md mb-6 overflow-auto border border-gray-200">
      <div v-if="users.length" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div 
          v-for="user in users" 
          :key="user.id" 
          class="bg-surface border border-gray-200 p-4 rounded-lg shadow-md relative"
        >
          <p class="font-medium text-lg text-text">{{ user.name }}</p>
          <p class="text-sm text-muted">{{ user.role }}</p>
          <button 
            @click="removeUser(user.id)" 
            class="absolute top-2 right-2 bg-danger text-white px-2 py-1 rounded-md hover:bg-red-600"
          >
            Usuń
          </button>
        </div>
      </div>
      <p v-else class="text-muted text-center">Brak użytkowników.</p>
    </div>
    
    <div class="flex justify-between">
      <router-link
        to="/addemployee"
        class="bg-primary hover:bg-secondary text-white px-4 py-2 rounded-md transition text-center"
      >
        Dodaj Użytkownika
      </router-link>
      <button 
        @click="removeAllUsers" 
        class="bg-danger hover:bg-red-600 text-white px-4 py-2 rounded-md transition"
      >
        Usuń Wszystkich
      </button>
    </div>
  </div>
</template>
  
<script setup>
import { ref } from 'vue';
  
// Example list of users
const users = ref([
  { id: 1, name: "Jan Kowalski", role: "Administrator" },
  { id: 2, name: "Anna Nowak", role: "Użytkownik" },
  { id: 3, name: "Piotr Zielinski", role: "Moderator" }
]);
  
const addUser = () => {
  const newName = prompt("Podaj imię i nazwisko użytkownika:");
  const newRole = prompt("Podaj rolę użytkownika:");
  if (newName && newRole) {
    users.value.push({ 
      id: Date.now(), 
      name: newName.trim(),
      role: newRole.trim()
    });
  }
};
  
const removeUser = (id) => {
  users.value = users.value.filter(user => user.id !== id);
};
  
const removeAllUsers = () => {
  if (confirm("Czy na pewno chcesz usunąć wszystkich użytkowników?")) {
    users.value = [];
  }
};
</script>
