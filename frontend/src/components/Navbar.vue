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
          :class="navLinkClass('teams')"
          @click.native="setActiveTab('teams')">
        Zespoły
      </router-link>
      <router-link
          v-if="canAccessManagerFeatures"
          to="/raport"
          :class="navLinkClass('raport')"
          @click.native="setActiveTab('raport')">
        Raporty
      </router-link>
      <router-link
          v-if="canAccessManagerFeatures"
          to="/tasks"
          :class="navLinkClass('tasks')"
          @click.native="setActiveTab('tasks')">
        Zadania
      </router-link>
      <router-link
          v-if="isAdmin"
          to="/adminpanel"
          :class="navLinkClass('adminpanel')"
          @click.native="setActiveTab('adminpanel')">
        Panel administratora
      </router-link>
    </div>

    <div class="p-4">
      <div v-if="userFullName" class="mb-2 font-semibold text-center">
        Użytkownik: {{ userFullName }}
      </div>
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
  data() {
    return {
      activeTab: null
    };
  },
  computed: {
    canAccessManagerFeatures() {
      return authService.hasRoleAtLeast('kierownik');
    },
    isAdmin() {
      return authState.user?.role === 'administrator';
    },
    userFullName() {
      const user = authState.user;
      if (user && user.firstName && user.lastName) {
        return `${user.firstName} ${user.lastName}`;
      }
      return '';
    }
  },
  methods: {
    setActiveTab(tab) {
      this.activeTab = tab;
    },
    navLinkClass(tab) {
      return [
        'block bg-secondary hover:bg-accent p-3 rounded mb-3 transition !text-white',
        this.activeTab === tab ? 'ring-2 ring-accent font-bold' : ''
      ];
    },
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