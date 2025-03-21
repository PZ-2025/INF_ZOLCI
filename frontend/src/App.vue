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
  <div class="app-container">
    <Navbar @navigate="currentView = $event" />
    <div class="content">
      <div v-if="currentView === 'home'">
        <h1>Strona Główna</h1>
        <p>Witaj w aplikacji!</p>
      </div>
      <ReportsView v-if="currentView === 'reports'" />
    </div>
  </div>

  <!-- Button to fetch data from the backend -->
  <button @click="fetchData">Fetch Data</button>

  <!-- Placeholder for displaying the server response -->
  <p>{{ responseText }}</p>
</template>
