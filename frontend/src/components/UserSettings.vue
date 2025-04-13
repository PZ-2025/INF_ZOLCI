<template>
  <div class="bg-background min-h-screen p-8 text-text">
    <h1 class="text-3xl font-bold text-primary mb-6">Ustawienia Użytkownika</h1>

    <form @submit.prevent="updateSettings" class="bg-surface p-6 rounded-lg shadow-md border border-gray-200">
      <div class="mb-4">
        <label for="name" class="block font-semibold mb-2">Imię i Nazwisko</label>
        <input
          type="text"
          id="name"
          v-model="user.name"
          class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
          placeholder="Wpisz imię i nazwisko"
          required
        />
      </div>

      <div class="mb-4">
        <label for="email" class="block font-semibold mb-2">Adres e-mail</label>
        <input
          type="email"
          id="email"
          v-model="user.email"
          class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
          placeholder="Twój adres e-mail"
        />
      </div>

      <div class="mb-4">
        <label for="currentPassword" class="block font-semibold mb-2">Aktualne hasło</label>
        <input
          type="password"
          id="currentPassword"
          v-model="user.currentPassword"
          class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
          placeholder="Wpisz aktualne hasło"
        />
      </div>

      <div class="mb-4">
        <label for="password" class="block font-semibold mb-2">Nowe hasło</label>
        <input
          type="password"
          id="password"
          v-model="user.password"
          class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
          placeholder="Wpisz nowe hasło"
        />
      </div>

      <div class="mb-6">
        <label for="theme" class="block font-semibold mb-2">Motyw</label>
        <select
          id="theme"
          v-model="user.theme"
          class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
        >
          <option value="light">Jasny</option>
          <option value="dark">Ciemny</option>
        </select>
      </div>

      <button
        type="submit"
        class="w-full bg-primary hover:bg-secondary text-white font-bold py-2 rounded-lg transition"
      >
        Zapisz Zmiany
      </button>
    </form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      user: {
        name: "Jan Kowalski",
        email: "jan.kowalski@example.com",
        currentPassword: "",
        password: "",
        theme: "light",
      },
    };
  },
  methods: {
    updateSettings() {
      console.log("Zaktualizowano ustawienia:", this.user);
      alert("Ustawienia zostały zaktualizowane!");
    },
  },
};
</script>
