<template>
  <div class="flex items-center justify-center h-full w-full bg-background">
    <div class="bg-surface p-8 rounded-2xl shadow-xl w-96 border border-gray-200">
      <h2 class="text-2xl font-bold text-center text-primary mb-6">BuildTask</h2>

      <form @submit.prevent="handleLogin">
        <div class="mb-4">
          <label for="login" class="block text-text font-semibold mb-2">Login</label>
          <input
            type="text"
            id="login"
            v-model="login"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white text-text"
            placeholder="Wpisz login"
            required
          >
        </div>

        <div class="mb-4">
          <label for="password" class="block text-text font-semibold mb-2">Hasło</label>
          <input
            type="password"
            id="password"
            v-model="password"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white text-text"
            placeholder="Wpisz hasło"
            required
          >
        </div>

        <button
          type="submit"
          class="w-full bg-primary hover:bg-secondary text-white font-bold py-2 rounded-lg transition"
        >
          Zaloguj
        </button>
      </form>
    </div>
  </div>
</template>


<script>
import { authState } from '../../router/router.js';
import { useRouter } from 'vue-router';

export default {
  data() {
    return {
      login: '',
      password: '',
    };
  },
  methods: {
    // przykładowi użytkownicy
    handleLogin() {
      const users = [
        { name: 'Jan Kowalski', login: 'jan', password: '1234', role: 'employee' },
        { name: 'Kierownik', login: 'manager', password: 'admin', role: 'manager' },
      ];

      const user = users.find(u => u.login === this.login && u.password === this.password);
      if (user) {
        authState.isAuthenticated = true;
        authState.user = user;
        this.$router.push('/teams');
      } else {
        alert('Invalid login or password');
      }
    },
  },
};
</script>
