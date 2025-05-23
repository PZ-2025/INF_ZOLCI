<template>
  <div class="min-h-screen bg-background text-text p-6">
    <div class="max-w-3xl mx-auto bg-white shadow rounded-2xl p-8">
      <h1 class="text-3xl font-bold text-primary mb-6">Zarządzaj członkami zespołu</h1>
      
      <!-- Loading state -->
      <div v-if="loading" class="text-center py-8">
        <p class="text-primary">Ładowanie danych...</p>
      </div>

      <!-- Error state -->
      <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6">
        <p>{{ error }}</p>
        <button @click="fetchData" class="mt-2 bg-primary text-white px-4 py-1 rounded-md">
          Spróbuj ponownie
        </button>
      </div>

      <!-- Main content -->
      <div v-else>
        <p class="text-gray-600 mb-4">Zaznacz pracowników, którzy mają należeć do tego zespołu.</p>

        <form @submit.prevent="updateTeamMembers" class="space-y-6">
          <!-- Wyszukiwanie -->
          <div>
            <input
              v-model="search"
              type="text"
              placeholder="Szukaj członka zespołu..."
              class="w-full p-3 border border-gray-300 rounded-lg bg-gray-100 text-black"
            />
          </div>
  
          <!-- Zaznacz wszystkich -->
          <div>
            <label class="inline-flex items-center space-x-2 cursor-pointer text-sm">
              <input
                type="checkbox"
                @change="toggleAll"
                :checked="selectedMembers.length === filteredEmployees.length && filteredEmployees.length > 0"
              />
              <span>Zaznacz / odznacz wszystkich widocznych</span>
            </label>
          </div>
  
          <!-- Lista pracowników -->
          <div class="space-y-3 max-h-72 overflow-y-auto border border-gray-300 rounded-lg p-3 bg-white">
            <div
              v-for="employee in filteredEmployees"
              :key="employee.id"
              class="flex items-center justify-between bg-gray-50 hover:bg-gray-100 p-3 rounded-md transition"
            >
              <div class="flex items-center space-x-3">
                <input
                  type="checkbox"
                  v-model="selectedMembers"
                  :value="employee.id"
                  class="form-checkbox h-5 w-5 text-primary focus:ring-primary"
                />
                <span class="text-black">{{ employee.name }}</span>
              </div>
              <span class="text-sm text-gray-500">{{ employee.email }}</span>
            </div>
  
            <div v-if="filteredEmployees.length === 0" class="text-center text-gray-400 py-4">
              Brak pasujących członków.
            </div>
          </div>
  
          <!-- Zapisz -->
          <div>
            <button
              type="submit"
              class="bg-accent hover:bg-secondary text-white px-6 py-3 rounded-xl w-full text-lg font-medium shadow">
              Zapisz zmiany
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
  <StatusModal
    :show="showModal"
    :type="modalConfig.type"
    :title="modalConfig.title"
    :message="modalConfig.message"
    :button-text="modalConfig.buttonText"
    :auto-close="modalConfig.autoClose"
    :auto-close-delay="modalConfig.autoCloseDelay"
    @close="hideModal"
  />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import userService from '../services/userService';
import teamService from '../services/teamService';
import StatusModal from './StatusModal.vue';
import { useStatusModal } from '../composables/useStatusModal';

const props = defineProps({
  id: {
    type: [String, Number],
    required: true,
    validator: (value) => {
      const parsed = parseInt(value);
      return !isNaN(parsed) && parsed > 0;
    }
  }
});

const employees = ref([]);
const selectedMembers = ref([]);
const search = ref('');
const loading = ref(true);
const error = ref(null);

const router = useRouter();

// Convert ID to number with validation
const teamId = computed(() => {
  const parsed = parseInt(props.id);
  if (isNaN(parsed) || parsed <= 0) {
    console.error('Invalid team ID:', props.id);
    error.value = 'Nieprawidłowe ID zespołu';
    return null;
  }
  return parsed;
});

// Fetch all active users and current team members
const fetchData = async () => {
  try {
    loading.value = true;
    error.value = null;

    // Early validation of team ID
    if (!teamId.value) {
      throw new Error(`Nieprawidłowe ID zespołu: ${props.id}`);
    }
    
    // Fetch all active users
    const users = await userService.getActiveUsers();
    // Filtrowanie tylko pracowników
    employees.value = users
      .filter(user => user.role === 'pracownik')
      .map(user => ({
        id: user.id,
        name: `${user.firstName} ${user.lastName}`,
        email: user.email
      }));

    // Fetch current team members using validated teamId
    const teamMembers = await teamService.getTeamMembers(teamId.value);
    selectedMembers.value = teamMembers.map(member => member.userId);

  } catch (err) {
    console.error('Error fetching data:', err);
    error.value = 'Nie udało się pobrać danych. ' + (err.message || 'Spróbuj odświeżyć stronę.');
  } finally {
    loading.value = false;
  }
};

// Filtrowanie według wyszukiwarki
const filteredEmployees = computed(() =>
  employees.value.filter(e =>
    e.name.toLowerCase().includes(search.value.toLowerCase()) ||
    e.email.toLowerCase().includes(search.value.toLowerCase())
  )
);

// Zaznacz / Odznacz wszystkich
const toggleAll = (event) => {
  if (event.target.checked) {
    selectedMembers.value = filteredEmployees.value.map(e => e.id);
  } else {
    selectedMembers.value = selectedMembers.value.filter(
      id => !filteredEmployees.value.some(e => e.id === id)
    );
  }
};

const { showModal, modalConfig, showStatus, hideModal } = useStatusModal();

// Zapisz zmiany w składzie zespołu
const updateTeamMembers = async () => {
  try {
    loading.value = true;
    
    if (!teamId.value || isNaN(teamId.value)) {
      throw new Error('Nieprawidłowe ID zespołu');
    }
    
    // Get current team members
    const currentMembers = await teamService.getTeamMembers(teamId.value);
    
    // Remove members that are no longer selected
    for (const member of currentMembers) {
      if (!selectedMembers.value.includes(member.userId)) {
        await teamService.removeTeamMember(member.id);
      }
    }
    
    // Add new members
    for (const userId of selectedMembers.value) {
      if (!currentMembers.some(m => m.userId === userId)) {
        await teamService.addTeamMember(teamId.value, userId);
      }
    }

    showStatus({
      type: 'success',
      title: 'Zapisano zmiany',
      message: 'Skład zespołu został pomyślnie zaktualizowany.',
      buttonText: 'Zamknij',
      autoClose: true,
      autoCloseDelay: 1500,
      onClose: () => router.back()
    });
  } catch (err) {
    console.error('Error updating team members:', err);
    showStatus({
      type: 'error',
      title: 'Błąd',
      message: 'Nie udało się zaktualizować składu zespołu: ' + (err.message || ''),
      buttonText: 'Zamknij',
      autoClose: true,
      autoCloseDelay: 3000
    });
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  console.log("TeamMembersManage mounted, team ID:", teamId.value);
  fetchData();
});
</script>