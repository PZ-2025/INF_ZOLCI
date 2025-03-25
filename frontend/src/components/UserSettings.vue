<template>
  <div class="bg-primary min-h-screen p-8">
    <h1 class="text-3xl font-bold text-white mb-6">Ustawienia Użytkownika</h1>

    <form @submit.prevent="updateSettings" class="bg-secondary p-6 rounded-lg shadow-md">
      <div class="mb-4">
        <label for="name" class="block text-white font-semibold mb-2">Imię i Nazwisko</label>
        <input
          type="text"
          id="name"
          v-model="user.name"
          class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-accent"
          placeholder="Wpisz imię i nazwisko"
          required
        />
      </div>

      <div class="mb-4">
        <label for="password" class="block text-white font-semibold mb-2">Nowe Hasło</label>
        <input
          type="password"
          id="password"
          v-model="user.password"
          class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-accent"
          placeholder="Wpisz nowe hasło"
        />
      </div>

      <button
        type="submit"
        class="w-full bg-warning hover:bg-danger text-white font-bold py-2 rounded-lg transition"
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
        password: "",
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
