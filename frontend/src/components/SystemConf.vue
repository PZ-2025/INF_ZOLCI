<template>
  <div class="bg-background min-h-screen p-8 text-text">
    <h1 class="text-3xl font-bold text-primary mb-6">Konfiguracja Systemu</h1>

    <div class="bg-surface p-6 rounded-lg shadow-md mb-6 border border-gray-200">
      <h2 class="text-xl font-semibold mb-4">Kryteria Statusów (1–100)</h2>
      <input
        type="range"
        min="1"
        max="100"
        v-model="statusValue"
        class="w-full"
      />
      <p class="mt-2 text-muted">Wybrany poziom: <span class="font-bold">{{ statusValue }}</span></p>
    </div>

    <div class="bg-surface p-6 rounded-lg shadow-md mb-6 border border-gray-200">
      <h2 class="text-xl font-semibold mb-4">Globalne Kryteria Priorytetów</h2>
      <select v-model="selectedPriority" class="w-full p-2 border border-gray-300 rounded bg-white focus:outline-none focus:ring-2 focus:ring-primary">
        <option v-for="priority in priorities" :key="priority" :value="priority">
          {{ priority }}
        </option>
      </select>
    </div>

    <div class="bg-surface p-6 rounded-lg shadow-md border border-gray-200">
      <h2 class="text-xl font-semibold mb-4">Inne Ustawienia</h2>

      <div class="mb-4 flex items-center">
        <input type="checkbox" v-model="autoArchive" class="mr-2" id="archive">
        <label for="archive">Automatyczna archiwizacja zadań</label>
      </div>

      <div class="mb-4">
        <label class="block font-medium mb-1">Minimalna długość hasła</label>
        <input
          type="number"
          v-model="minPasswordLength"
          min="4"
          max="32"
          class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
        />
      </div>

      <div class="mb-4">
        <label class="block font-medium mb-1">Automatyczne wylogowanie (minuty)</label>
        <input
          type="number"
          v-model="autoLogout"
          min="1"
          max="120"
          class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
        />
      </div>

      <div class="mb-4">
        <label class="block font-medium mb-1">Domyślny język systemu</label>
        <select v-model="language" class="w-full p-2 border border-gray-300 rounded bg-white focus:outline-none focus:ring-2 focus:ring-primary">
          <option value="pl">Polski</option>
          <option value="en">English</option>
        </select>
      </div>

      <div class="mt-6">
        <button class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-md transition w-full">
          Zapisz Ustawienia
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      statusValue: 50,
      selectedPriority: 'medium',
      priorities: ['low', 'medium', 'high'],
      autoArchive: true,
      minPasswordLength: 8,
      autoLogout: 15,
      language: 'pl',
    };
  },
};
</script>
