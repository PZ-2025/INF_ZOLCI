<template>
  <div class="h-full flex flex-col p-6 bg-primary">
    <h1 class="text-3xl font-bold text-white mb-6">Użytkownicy</h1>
    
    <div class="flex-grow bg-secondary p-6 rounded-lg shadow-md mb-6 overflow-auto">
      <div v-if="users.length" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div 
          v-for="user in users" 
          :key="user.id" 
          class="bg-warning p-4 rounded-lg shadow-md relative"
        >
          <p class="font-medium text-lg text-white">{{ user.name }}</p>
          <p class="text-sm text-accent">{{ user.role }}</p>
          <button 
            @click="removeUser(user.id)" 
            class="absolute top-2 right-2 bg-danger text-white px-2 py-1 rounded-md hover:bg-red-700"
          >
            Usuń
          </button>
        </div>
      </div>
      <p v-else class="text-white text-center">Brak użytkowników.</p>
    </div>
    
    <div class="flex justify-between">
      <button 
        @click="addUser" 
        class="bg-warning text-white px-4 py-2 rounded-md hover:bg-green-700"
      >
        Dodaj Użytkownika
      </button>
      <button 
        @click="removeAllUsers" 
        class="bg-danger text-white px-4 py-2 rounded-md hover:bg-red-700"
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
