<script setup>
import { ref, onMounted } from 'vue';
import { authState } from '../router/router.js';
import Navbar from './components/Navbar.vue';
import LoginForm from './components/LoginForm.vue';
import apiService from './services/api.js';

const backendStatus = ref('checking');

onMounted(async () => {
  console.log("Vue app loaded");

  try {
    await apiService.init();

    try {
      await apiService.get('/database/users');
      backendStatus.value = 'connected';
      console.log('✅ Połączono z backendem Spring Boot');
    } catch (error) {
      backendStatus.value = 'disconnected';
      console.error('❌ Brak połączenia z backendem:', error);
    }
  } catch (error) {
    console.error('Error initializing API service:', error);
    backendStatus.value = 'error';
  }
});
</script>

<template>
  <div class="flex h-screen w-screen">
    <div v-if="backendStatus === 'disconnected'" class="fixed  top-0 left-0 right-0 bg-warning text-red-800 p-1 text-center text-sm z-50">
      ⚠️ Brak połączenia z backendem. Aplikacja działa w trybie demonstracyjnym z przykładowymi danymi.
    </div>

    <LoginForm v-if="!authState.isAuthenticated" />

    <div v-else class="flex h-screen w-screen">
      <Navbar class="w-64 bg-gray-800 text-white h-full flex-shrink-0" />
      <div class="flex-1 overflow-auto bg-gray-100">
        <router-view />
      </div>
    </div>
  </div>
</template>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
}
</style>