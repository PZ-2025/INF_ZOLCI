<template>
  <div class="p-4">
    <div class="flex justify-between items-center mb-4">
      <h1 class="text-3xl text-left font-bold text-primary mb-6">
          Zadania
        </h1>
        <div>
          <button
            :class="activeTab === 'history' ? 'bg-accent' : 'bg-secondary'"
            class="text-white px-4 py-2 rounded mr-2 bg-primary hover:bg-secondary"
            @click="activeTab = 'history'"
          >
            Historia zadań
          </button>
          <button
            v-if="canAddTask"
            :class="activeTab === 'add' ? 'bg-accent' : 'bg-secondary'"
            class="text-white px-4 py-2 rounded bg-primary hover:bg-secondary"
            @click="activeTab = 'add'"
          >
            Dodaj zadanie
          </button>
        </div>
      </div>
      <div>
        <TasksHistory v-if="activeTab === 'history'" />
        <AddTask v-else />
      </div>
    </div>
</template>

<script>
import TasksHistory from './TasksHistory.vue';
import AddTask from './AddTask.vue';
import { authState } from '../../router/router.js';

export default {
  components: { TasksHistory, AddTask },
  data() {
    return {
      activeTab: 'history'
    };
  },
  computed: {
    canAddTask() {
      const role = authState.user?.role;
      return role === 'kierownik' || role === 'administrator';
    }
  }
};
</script>
