<template>
  <div class="p-4">
    <div class="flex justify-between items-center mb-4">
      <h1 class="text-3xl text-left font-bold text-primary mb-6">
        Raporty
      </h1>
      <div>
        <button
          :class="activeTab === 'generate' ? 'bg-accent' : 'bg-secondary'"
          class="text-white px-4 py-2 rounded mr-2 bg-primary hover:bg-secondary"
          @click="activeTab = 'generate'"
        >
          Generowanie raportów
        </button>
        <button
          :class="activeTab === 'history' ? 'bg-accent' : 'bg-secondary'"
          class="text-white px-4 py-2 rounded bg-primary hover:bg-secondary"
          @click="activeTab = 'history'"
        >
          Historia raportów
        </button>
      </div>
    </div>
    <div>
      <RaportGenerate v-if="activeTab === 'generate'" />
      <RaportHistory v-else />
    </div>
  </div>
</template>

<script>
import RaportGenerate from '../components/RaportGenerate.vue';
import RaportHistory from '../components/RaportHistory.vue';

export default {
  components: {
    RaportGenerate,
    RaportHistory,
  },
  data() {
    return {
      activeTab: 'generate',
    };
  },
};
</script>
