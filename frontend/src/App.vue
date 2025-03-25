<script setup>
import { ref, onMounted } from 'vue';
import Navbar from './components/Navbar.vue';

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
    <Navbar class="w-64 bg-gray-800 text-white h-full flex-shrink-0" />

    <div class="flex-1 overflow-auto bg-gray-100">
      <Teams />
    </div>
  </div>

  <!-- Button to fetch data from the backend -->
  <button @click="fetchData">Fetch Data</button>

  <!-- Placeholder for displaying the server response -->
  <p>{{ responseText }}</p>
</template>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
}
</style>
