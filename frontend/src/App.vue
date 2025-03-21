<script setup>
import { ref } from 'vue';
import Navbar from './components/Navbar.vue';
import RaportHistory from './views/RaportHistory.vue';

const currentView = ref('home');
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
</template>
