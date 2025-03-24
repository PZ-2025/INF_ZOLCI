<script setup>
import Navbar from './components/Navbar.vue';
import Teams from './components/Teams.vue';
</script>

<template>
  <div class="flex h-screen w-screen">
    <Navbar class="w-64 bg-gray-800 text-white h-full flex-shrink-0" />

    <div class="flex-1 overflow-auto bg-gray-100">
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
