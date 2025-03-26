<script setup>
import { ref, onMounted } from 'vue';
import { authState } from '../router/router.js';
import Navbar from './components/Navbar.vue';
import LoginForm from './components/LoginForm.vue';

const currentView = ref('home');
// variable for response from backend
const responseText = ref('');
// variable for API
let apiUrl = '';

onMounted(async () => {
  console.log("Vue app loaded");
  // setting apiURL after mount
  if (window.api?.getApiUrl) {
    apiUrl = await window.api.getApiUrl();
  } else {
    console.error("API bridge not available");
  }
});

// fetching response from backend
async function fetchData() {
  if (!apiUrl) {
    responseText.value = "API bridge not available";
    return;
  }
  responseText.value = "Fetching data...";
  try {
    const response = await fetch(apiUrl + "/home/hello");
    if (!response.ok) throw new Error("Network response was not ok");
    responseText.value = await response.text();
  } catch (error) {
    console.error("Error:", error);
    responseText.value = "Error fetching data: " + error.message;
  }
}
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