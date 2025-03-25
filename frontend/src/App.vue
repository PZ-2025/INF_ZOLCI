<script setup>
import { ref, onMounted } from 'vue';
import Navbar from './components/Navbar.vue';
import Teams from './components/TeamsTasks.vue';

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

      
      <!-- Container for backend data fetch -->
      <!-- 
      <div class="p-4">
        <button 
          @click="fetchData" 
          class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
        >
          Fetch Data
        </button>
        <p class="mt-4 text-gray-700">{{ responseText }}</p>
      </div>
      -->
      <Teams />
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