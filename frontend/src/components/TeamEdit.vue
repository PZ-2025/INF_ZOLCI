<template>
  <div class="min-h-screen bg-background flex flex-col items-start justify-start px-10 py-10 space-y-8">
    <!-- Nagłówek -->
    <h1 class="text-3xl font-bold text-primary">Edytuj zespół</h1>

    <!-- Loader -->
    <div v-if="loading" class="text-primary text-xl">Ładowanie zespołu...</div>

    <!-- Błąd -->
    <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 w-full max-w-3xl">
      <p>{{ error }}</p>
      <button @click="fetchTeamDetails" class="mt-2 bg-primary text-white px-4 py-1 rounded-md">
        Spróbuj ponownie
      </button>
    </div>

    <!-- Formularz -->
    <form
      v-else
      @submit.prevent="updateTeam"
      class="space-y-5 w-full max-w-3xl"
    >
      <!-- Nazwa zespołu -->
      <div class="flex items-center space-x-4">
        <label for="name" class="w-48 text-black text-lg font-medium">Nazwa zespołu</label>
        <input
          v-model="team.name"
          id="name"
          type="text"
          required
          class="flex-1 p-2.5 border border-gray-300 rounded-md bg-white text-sm text-black placeholder-gray-400 focus:ring-2 focus:ring-primary focus:outline-none"
          placeholder="np. Zespół remontowy"
        />
      </div>

      <!-- Manager -->
      <div class="flex items-center space-x-4">
        <label for="managerId" class="w-48 text-black text-lg font-medium">Manager zespołu</label>
        <select
          v-model.number="team.managerId"
          id="managerId"
          class="flex-1 p-2.5 border border-gray-300 rounded-md bg-white text-sm text-black focus:ring-2 focus:ring-primary focus:outline-none"
        >
          <option disabled value="">Wybierz managera</option>
          <option v-for="user in users" :key="user.id" :value="user.id">
            {{ user.firstName }} {{ user.lastName }} ({{ user.email }})
          </option>
        </select>
      </div>

      <!-- Status aktywności -->
      <div class="flex items-center space-x-4">
        <input
          id="isActiveCheckbox"
          type="checkbox"
          v-model="team.isActive"
          class="w-5 h-5 text-primary border-gray-300 rounded focus:ring-primary"
        />
        <label for="isActiveCheckbox" class="text-black text-lg font-medium">Zespół aktywny</label>
      </div>

      <!-- Data utworzenia (tylko do odczytu) -->
      <div class="flex items-center space-x-4">
        <label for="createdAt" class="w-48 text-black text-lg font-medium">Data utworzenia</label>
        <input
          :value="formatDate(team.createdAt)"
          id="createdAt"
          type="text"
          readonly
          class="flex-1 p-2.5 border border-gray-300 rounded-md bg-gray-100 text-sm text-gray-600 cursor-not-allowed"
          placeholder="Automatycznie ustawiona"
        />
      </div>

      <!-- Przyciski -->
      <div class="flex justify-end gap-4 pt-4">
        <button
          type="button"
          @click="goBack"
          class="px-5 py-2 rounded-md bg-gray-500 text-white text-sm hover:bg-gray-600 transition"
        >
          Anuluj
        </button>
        <button
          type="submit"
          class="px-6 py-2 rounded-md bg-primary hover:bg-secondary text-white text-sm font-semibold transition"
          :disabled="isSaving"
        >
          <span v-if="isSaving">Zapisywanie...</span>
          <span v-else>Zapisz zmiany</span>
        </button>
      </div>
    </form>

    <!-- Status Modal -->
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
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import teamService from '../services/teamService';
import userService from '../services/userService'; 
import { authState } from '../../router/router';
import StatusModal from './StatusModal.vue';
import { useStatusModal } from '../composables/useStatusModal';

export default {
  name: 'TeamEdit',
  components: {
    StatusModal
  },
  props: {
    id: {
      type: [String, Number],
      default: null
    }
  },
  setup(props) {
    const route = useRoute();
    const router = useRouter();

    // Pobierz ID zespołu z parametrów URL lub props
    const teamId = computed(() => {
      const id = props.id || route.params.id || route.query.id;
      console.log('TeamEdit - ID zespołu (props, params, query):', props.id, route.params.id, route.query.id);
      return id;
    });

    // Stan komponentu
    const team = ref({
      id: null,
      name: '',
      managerId: null,
      createdAt: null,
      isActive: true
    });

    const users = ref([]);
    const loading = ref(true);
    const error = ref(null);
    const isSaving = ref(false);

    // Status modal
    const { showModal, modalConfig, showStatus, hideModal } = useStatusModal();

    // Pobieranie użytkowników dla wyboru managera
    const fetchUsers = async () => {
      try {
        users.value = await userService.getAllUsers();
        console.log('Pobrani użytkownicy:', users.value);
      } catch (err) {
        console.error('Błąd podczas pobierania użytkowników:', err);
        // Fallback - pusta lista lub przykładowe dane
        users.value = [
          { id: 1, firstName: 'Jan', lastName: 'Kowalski', email: 'jan.kowalski@example.com' },
          { id: 2, firstName: 'Anna', lastName: 'Nowak', email: 'anna.nowak@example.com' }
        ];
      }
    };

    // Pobieranie szczegółów zespołu
    const fetchTeamDetails = async () => {
      loading.value = true;
      error.value = null;

      try {
        // Sprawdź czy mamy ID zespołu
        if (!teamId.value) {
          console.error('Brak ID zespołu w parametrach URL:', route.params, route.query);
          throw new Error('ID zespołu jest wymagane');
        }

        console.log('Pobieranie zespołu o ID:', teamId.value);

        // Pobierz dane zespołu i użytkowników równolegle
        const [teamData] = await Promise.all([
          teamService.getTeamById(teamId.value),
          fetchUsers()
        ]);

        console.log('Pobrane dane zespołu:', teamData);

        // Mapuj dane zespołu do formularza
        team.value = {
          id: teamData.id,
          name: teamData.name || '',
          managerId: teamData.managerId || (teamData.manager?.id) || null,
          createdAt: teamData.createdAt ? new Date(teamData.createdAt) : null,
          isActive: teamData.isActive !== undefined ? teamData.isActive : true
        };
      } catch (err) {
        console.error('Błąd podczas ładowania zespołu:', err);
        error.value = `Nie udało się załadować zespołu: ${err.message}`;

        // Pobierz użytkowników nawet w przypadku błędu
        await fetchUsers();
      } finally {
        loading.value = false;
      }
    };

    // Aktualizacja zespołu
    const updateTeam = async () => {
      isSaving.value = true;

      try {
        if (!teamId.value) {
          throw new Error('ID zespołu jest wymagane');
        }

        // Walidacja nazwy zespołu
        if (!team.value.name || team.value.name.trim().length < 2) {
          showStatus({
            type: 'error',
            title: 'Błąd',
            message: 'Nazwa zespołu musi mieć co najmniej 2 znaki.',
            buttonText: 'Zamknij'
          });
          isSaving.value = false;
          return;
        }

        // Przygotuj dane zespołu do wysłania
        const teamData = {
          name: team.value.name.trim(),
          managerId: team.value.managerId ? parseInt(team.value.managerId) : null,
          isActive: team.value.isActive
        };

        console.log('Wysyłanie aktualizacji zespołu:', teamData);

        const updatedTeam = await teamService.updateTeam(teamId.value, teamData);
        console.log('Zaktualizowany zespół:', updatedTeam);

        // Pokaż komunikat powodzenia
        showStatus({
          type: 'success',
          title: 'Sukces',
          message: 'Zespół został pomyślnie zaktualizowany!',
          buttonText: 'OK',
          autoClose: true,
          autoCloseDelay: 2000,
          onClose: () => router.push(`/teamdetails/${teamId.value}`)
        });
      } catch (err) {
        console.error('Błąd podczas aktualizacji zespołu:', err);

        showStatus({
          type: 'error',
          title: 'Błąd',
          message: `Nie udało się zaktualizować zespołu: ${err.message}`,
          buttonText: 'Zamknij'
        });
      } finally {
        isSaving.value = false;
      }
    };

    // Formatowanie daty do wyświetlenia
    const formatDate = (date) => {
      if (!date) return '';
      try {
        const dateObj = new Date(date);
        return dateObj.toLocaleDateString('pl-PL', {
          year: 'numeric',
          month: 'long',
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        });
      } catch (err) {
        return '';
      }
    };

    // Powrót do poprzedniej strony
    const goBack = () => {
      router.back();
    };

    // Inicjalizacja komponentu
    onMounted(() => {
      console.log('TeamEdit component mounted, route params:', route.params);
      fetchTeamDetails();
    });

    return {
      team,
      users,
      loading,
      error,
      isSaving,
      fetchTeamDetails,
      updateTeam,
      formatDate,
      goBack,
      showModal,
      modalConfig,
      hideModal
    };
  }
};
</script>