<template>
  <div class="bg-primary p-8 rounded-lg shadow-lg w-96">
    <h2 class="text-2xl font-bold text-center text-white mb-6">BuildTask</h2>

    <form @submit.prevent="handleLogin">
      <div class="mb-4">
        <label for="login" class="block text-white font-semibold mb-2">Login</label>
        <input 
          type="text" 
          id="login" 
          v-model="login" 
          class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-accent" 
          placeholder="Wpisz login"
          required
        >
      </div>

      <div class="mb-4">
        <label for="password" class="block text-white font-semibold mb-2">Hasło</label>
        <input 
          type="password" 
          id="password" 
          v-model="password" 
          class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-accent" 
          placeholder="Wpisz hasło"
          required
        >
      </div>

      <button 
        type="submit" 
        class="w-full bg-warning hover:bg-danger text-white font-bold py-2 rounded-lg transition"
      >
        Zaloguj
      </button>
    </form>
  </div>
</template>

  
  <script>
  export default {
    data() {
      return {
        login: '',
        password: ''
      };
    },
    methods: {
      handleLogin() {
        console.log("Login:", this.login, "Hasło:", this.password);
        // logika uwierzytelnieniania
      }
    }
  };
  </script>
  