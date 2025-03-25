<template>
  <div class="h-screen w-64 bg-navbar text-white flex flex-col">
    <div class="flex-grow p-4 overflow-auto">
      <router-link to="/" class="text-white block bg-warning hover:bg-danger p-3 rounded mb-3 transition">Wstecz</router-link>
      <router-link to="/teams" class="text-white block bg-secondary hover:bg-accent p-3 rounded mb-3 transition">Zespoły</router-link>
      <router-link to="/raportgenerate" class="text-white block bg-secondary hover:bg-accent p-3 rounded mb-3 transition">Zarządzanie Raportami</router-link>
      <router-link to="/taskshistory" class="text-white block bg-secondary hover:bg-accent p-3 rounded mb-3 transition">Zarządzanie Zadaniami</router-link>
      <router-link to="/allemployees" class="text-white block bg-secondary hover:bg-accent p-3 rounded mb-3 transition">Zarządzanie Pracownikami</router-link>
      <router-link to="/allusers" class="text-white block bg-secondary hover:bg-accent p-3 rounded mb-3 transition">Zarządzanie Użytkownikami</router-link>
      <router-link to="/settings" class="text-white block bg-secondary hover:bg-accent p-3 rounded mb-3 transition">Ustawienia</router-link>
    </div>

    <div class="p-4">
      <button class="w-full bg-warning hover:bg-danger text-white px-4 py-2 rounded transition mb-2">Wyloguj</button>
      <button class="w-full bg-danger hover:bg-primary text-white px-4 py-2 rounded transition">Ustawienia</button>
    </div>
  </div>
</template>
