<template>
  <div class="h-screen w-64 bg-navbar text-white flex flex-col">
    <div class="flex-grow p-4 overflow-auto">
      <button 
        @click="goBack" 
        class="w-full bg-warning hover:bg-warningHover !text-white px-4 py-2 rounded transition mb-2">
        Wstecz
      </button>
      <router-link 
        to="/teams" 
        class="text-white block bg-secondary hover:bg-accent p-3 rounded mb-3 transition">
        Zespoły
      </router-link>
      <router-link 
        v-if="canAccessManagerFeatures" 
        to="/raportgenerate" 
        class="!text-white block bg-secondary hover:bg-accent p-3 rounded mb-3 transition">
        Zarządzanie Raportami
      </router-link>
      <router-link 
        to="/taskshistory" 
        class="!text-white block bg-secondary hover:bg-accent p-3 rounded mb-3 transition">
        Zarządzanie Zadaniami
      </router-link>
      <router-link 
        v-if="canAccessManagerFeatures" 
        to="/allusers" 
        class="!text-white block bg-secondary hover:bg-accent p-3 rounded mb-3 transition">
        Zarządzanie Użytkownikami
      </router-link>
      <router-link 
        to="/settings" 
        class="text-white block bg-secondary hover:bg-accent p-3 rounded mb-3 transition">
        Ustawienia użytkownika
      </router-link>
      <router-link 
        v-if="isAdmin" 
        to="/systemconf" 
        class="!text-white block bg-secondary hover:bg-accent p-3 rounded mb-3 transition">
        Ustawienia systemu
      </router-link>
    </div>

    <div class="p-4">
      <button 
        @click="logout" 
        class="w-full bg-warning hover:bg-warningHover text-white px-4 py-2 rounded transition mb-2">
        Wyloguj
      </button>
    </div>
  </div>
</template>

<script>
import { authState } from '../../router/router.js';
import authService from '../services/authService';

export default {
  computed: {
    canAccessManagerFeatures() {
      return authService.hasRoleAtLeast('manager');
    },
    isAdmin() {
      return authState.user?.role === 'ADMIN';
    }
  },
  methods: {
    goBack() {
      if (authState.isAuthenticated) {
        this.$router.go(-1);
      } else {
        this.$router.push('/');
      }
    },
    logout() {
      authService.logout();
      this.$router.push('/');
    }
  },
};
</script>
