<template>
  <div class="min-h-screen bg-background flex flex-col items-start justify-start px-10 py-10 space-y-8">
    <!-- Nagłówek -->
    <h1 class="text-3xl font-bold text-primary">Edytuj zespół</h1>

    <!-- Loader -->
    <div v-if="loading" class="text-primary text-xl">Ładowanie zespołu...</div>

    <!-- Błąd -->
    <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 w-full max-w-5xl">
      <p>{{ error }}</p>
      <button @click="fetchTeamDetails" class="mt-2 bg-primary text-white px-4 py-1 rounded-md">
        Spróbuj ponownie
      </button>
    </div>

    <!-- Formularz -->
    <form
      v-else
      @submit.prevent="updateTeam"
      class="space-y-8 w-full max-w-5xl"
    >
      <!-- Sekcja 1: Podstawowe dane zespołu -->
      <div class="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
        <h2 class="text-xl font-semibold text-gray-800 mb-4">Podstawowe informacje</h2>
        
        <div class="space-y-5">
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

          <!-- Data utworzenia -->
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
        </div>
      </div>

      <!-- Sekcja 2: Członkowie zespołu -->
      <div class="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
        <h2 class="text-xl font-semibold text-gray-800 mb-4">Członkowie zespołu</h2>
        
        <!-- Wyszukiwanie -->
        <div class="mb-4">
          <input
            v-model="search"
            type="text"
            placeholder="Szukaj pracownika..."
            class="w-full p-3 border border-gray-300 rounded-md bg-white text-sm text-black placeholder-gray-400 focus:ring-2 focus:ring-primary focus:outline-none"
          />
        </div>

        <!-- Zaznacz wszystkich -->
        <div class="mb-4">
          <label class="inline-flex items-center space-x-2 cursor-pointer text-sm">
            <input
              type="checkbox"
              @change="toggleAll"
              :checked="selectedMembers.length === filteredEmployees.length && filteredEmployees.length > 0"
              class="w-4 h-4 text-primary border-gray-300 rounded focus:ring-primary"
            />
            <span class="text-gray-700">Zaznacz / odznacz wszystkich widocznych</span>
          </label>
        </div>

        <!-- Lista pracowników -->
        <div class="space-y-2 max-h-80 overflow-y-auto border border-gray-300 rounded-md p-3 bg-gray-50">
          <div
            v-for="employee in filteredEmployees"
            :key="employee.id"
            class="flex items-center justify-between bg-white hover:bg-gray-50 p-3 rounded-md transition border border-gray-100"
          >
            <div class="flex items-center space-x-3">
              <input
                type="checkbox"
                v-model="selectedMembers"
                :value="employee.id"
                class="w-4 h-4 text-primary border-gray-300 rounded focus:ring-primary"
              />
              <span class="text-black font-medium">{{ employee.name }}</span>
            </div>
            <span class="text-sm text-gray-500">{{ employee.email }}</span>
          </div>

          <div v-if="filteredEmployees.length === 0" class="text-center text-gray-400 py-8">
            Brak pasujących pracowników.
          </div>
        </div>

        <!-- Podsumowanie wybranych -->
        <div class="mt-4 text-sm text-gray-600">
          Wybrano: <span class="font-semibold">{{ selectedMembers.length }}</span> 
          z {{ employees.length }} pracowników
        </div>
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
          <span v-else>Zapisz wszystkie zmiany</span>
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
      return id ? parseInt(id) : null;
    });

    // Stan komponentu
    const team = ref({
      id: null,
      name: '',
      managerId: null,
      createdAt: null,
      isActive: true,
      tasks: [] // Zachowaj zadania przypisane do zespołu
    });

    // Stan dla członków zespołu
    const employees = ref([]);
    const selectedMembers = ref([]);
    const search = ref('');

    const users = ref([]);
    const loading = ref(true);
    const error = ref(null);
    const isSaving = ref(false);

    // Status modal
    const { showModal, modalConfig, showStatus, hideModal } = useStatusModal();

    // Filtrowanie pracowników według wyszukiwarki
    const filteredEmployees = computed(() =>
      employees.value.filter(e =>
        e.name.toLowerCase().includes(search.value.toLowerCase()) ||
        e.email.toLowerCase().includes(search.value.toLowerCase())
      )
    );

    // Zaznacz / Odznacz wszystkich widocznych
    const toggleAll = (event) => {
      if (event.target.checked) {
        // Dodaj wszystkich widocznych pracowników do zaznaczonych
        const visibleIds = filteredEmployees.value.map(e => e.id);
        selectedMembers.value = [...new Set([...selectedMembers.value, ...visibleIds])];
      } else {
        // Usuń wszystkich widocznych pracowników z zaznaczonych
        const visibleIds = filteredEmployees.value.map(e => e.id);
        selectedMembers.value = selectedMembers.value.filter(id => !visibleIds.includes(id));
      }
    };

    // Pobieranie użytkowników dla wyboru managera
    const fetchUsers = async () => {
      try {
        const allUsers = await userService.getAllUsers();
        users.value = allUsers;
        
        // Filtruj pracowników dla sekcji członków zespołu
        employees.value = allUsers
          .filter(user => user.role === 'pracownik')
          .map(user => ({
            id: user.id,
            name: `${user.firstName} ${user.lastName}`,
            email: user.email
          }));

        console.log('Pobrani użytkownicy:', users.value);
        console.log('Pracownicy:', employees.value);
      } catch (err) {
        console.error('Błąd podczas pobierania użytkowników:', err);
        // Fallback - przykładowe dane
        users.value = [
          { id: 1, firstName: 'Jan', lastName: 'Kowalski', email: 'jan.kowalski@example.com' },
          { id: 2, firstName: 'Anna', lastName: 'Nowak', email: 'anna.nowak@example.com' }
        ];
        employees.value = users.value.map(user => ({
          id: user.id,
          name: `${user.firstName} ${user.lastName}`,
          email: user.email
        }));
      }
    };

    // Pobieranie obecnych członków zespołu
    const fetchTeamMembers = async () => {
      try {
        if (!teamId.value) return;
        
        const teamMembers = await teamService.getTeamMembers(teamId.value);
        selectedMembers.value = teamMembers.map(member => member.userId);
        console.log('Obecni członkowie zespołu:', selectedMembers.value);
      } catch (err) {
        console.error('Błąd podczas pobierania członków zespołu:', err);
        selectedMembers.value = [];
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

        // Pobierz dane zespołu, użytkowników i członków równolegle
        const [teamData] = await Promise.all([
          teamService.getTeamById(teamId.value),
          fetchUsers(),
          fetchTeamMembers()
        ]);

        console.log('Pobrane dane zespołu:', teamData);

        // Mapuj dane zespołu do formularza
        team.value = {
          id: teamData.id,
          name: teamData.name || '',
          managerId: teamData.managerId || (teamData.manager?.id) || null,
          createdAt: teamData.createdAt ? new Date(teamData.createdAt) : null,
          isActive: teamData.isActive !== undefined ? teamData.isActive : true,
          tasks: teamData.tasks || [] // Zachowaj przypisane zadania
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

    // Aktualizacja członków zespołu
    const updateTeamMembers = async () => {
      try {
        // Pobierz obecnych członków
        const currentMembers = await teamService.getTeamMembers(teamId.value);
        
        // Usuń członków, którzy nie są już zaznaczeni
        for (const member of currentMembers) {
          if (!selectedMembers.value.includes(member.userId)) {
            await teamService.removeTeamMember(member.id);
          }
        }
        
        // Dodaj nowych członków
        for (const userId of selectedMembers.value) {
          if (!currentMembers.some(m => m.userId === userId)) {
            await teamService.addTeamMember(teamId.value, userId);
          }
        }
        
        console.log('Członkowie zespołu zaktualizowani');
      } catch (err) {
        console.error('Błąd podczas aktualizacji członków zespołu:', err);
        throw new Error(`Nie udało się zaktualizować członków zespołu: ${err.message}`);
      }
    };

    // Aktualizacja zespołu (metadane + członkowie)
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

        // 1. Aktualizuj metadane zespołu - zachowaj zadania jeśli backend ich wymaga
        const teamData = {
          name: team.value.name.trim(),
          managerId: team.value.managerId ? parseInt(team.value.managerId) : null,
          isActive: team.value.isActive,
          // Dodaj zadania tylko jeśli backend używa PUT i ich wymaga
          ...(team.value.tasks && team.value.tasks.length > 0 ? { tasks: team.value.tasks } : {})
        };

        console.log('Wysyłanie aktualizacji zespołu (BEZ zadań):', teamData);
        console.log('Oryginalne dane zespołu z zadaniami:', team.value);
        await teamService.updateTeam(teamId.value, teamData);

        // 2. Aktualizuj członków zespołu
        await updateTeamMembers();

        console.log('Zespół zaktualizowany pomyślnie');

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
      employees,
      selectedMembers,
      search,
      filteredEmployees,
      loading,
      error,
      isSaving,
      fetchTeamDetails,
      updateTeam,
      toggleAll,
      formatDate,
      goBack,
      showModal,
      modalConfig,
      hideModal
    };
  }
};
</script>