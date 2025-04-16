<script setup>
import { ref, onMounted } from 'vue';
import { authState } from '../router/router.js';
import Navbar from './components/Navbar.vue';
import LoginForm from './components/LoginForm.vue';
import apiService from './services/api.js';

const currentView = ref('home');
// variable for response from backend
const responseText = ref('');

onMounted(async () => {
  console.log("Vue app loaded");
  // Initialize the API service
  await apiService.init();
});
</script>

<template>
  <div class="flex h-screen w-screen">
    <!-- Show LoginForm if the user is not authenticated -->
    <LoginForm v-if="!authState.isAuthenticated" />

    <!-- Show the rest of the application if the user is authenticated -->
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