<template>
  <div class="p-4">
    <div class="flex justify-between items-center mb-4">
      <h1 class="text-3xl text-left font-bold text-primary mb-6">
          Panel administratora
        </h1>
        <div>
          <button
            :class="activeTab === 'users' ? 'bg-accent' : 'bg-secondary'"
            class="text-white px-4 py-2 rounded mr-2 bg-primary hover:bg-secondary"
            @click="activeTab = 'users'"
          >
            Ustawienia użytkowników
          </button>
          <button
            :class="activeTab === 'system' ? 'bg-accent' : 'bg-secondary'"
            class="text-white px-4 py-2 rounded bg-primary hover:bg-secondary"
            @click="activeTab = 'system'"
          >
            Ustawienia systemu
          </button>
        </div>
      </div>
      <div>
        <AllUsers v-if="activeTab === 'users'" />
        <SystemConf v-else />
      </div>
    </div>
</template>

<script>
import AllUsers from './AllUsers.vue';
import SystemConf from './SystemConf.vue';

export default {
  components: { AllUsers, SystemConf },
  data() {
    return {
      activeTab: 'users'
    };
  }
};
</script>
