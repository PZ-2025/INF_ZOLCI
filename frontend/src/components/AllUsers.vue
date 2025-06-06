<template>
  <div class="h-full flex flex-col p-6 bg-background text-text">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-3xl font-bold text-primary">Pracownicy</h1>
      <router-link
          to="/addemployee"
          class="bg-primary hover:bg-secondary text-white px-4 py-2 rounded-md transition text-center"
      >
        Dodaj Pracownika
      </router-link>
    </div>

    <!-- Stan ładowania -->
    <div v-if="loading" class="flex-grow flex justify-center items-center">
      <p class="text-primary text-xl">Ładowanie pracowników...</p>
    </div>

    <!-- Komunikat o błędzie -->
    <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6">
      <p>{{ error }}</p>
      <button @click="fetchEmployees" class="mt-2 bg-primary text-white px-4 py-1 rounded-md">
        Spróbuj ponownie
      </button>
    </div>

    <!-- Lista pracowników -->
    <div v-else class="flex-grow bg-surface p-6 rounded-lg shadow-md mb-6 overflow-auto border border-gray-200">
      <div v-if="employees.length" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
            v-for="employee in employees"
            :key="employee.id"
            class="bg-surface border border-gray-200 p-4 rounded-lg shadow-md relative hover:shadow-lg transition"
        >
          <div class="mb-4">
            <div class="flex items-center gap-2">
              <p class="font-medium text-lg text-text">{{ employee.firstName }} {{ employee.lastName }}</p>
              <!-- Specjalna ikona dla głównego administratora -->
              <span v-if="isMainAdmin(employee)" class="text-yellow-500 text-lg" title="Główny Administrator">
                👑
              </span>
            </div>
            <p class="text-sm text-muted">{{ employee.email }}</p>
            <p class="text-sm text-muted">Nazwa użytkownika: {{ employee.username }}</p>
            <span
                class="text-xs px-2 py-1 rounded-full mt-2 inline-block"
                :class="{
                'bg-green-100 text-green-800': employee.isActive,
                'bg-red-100 text-red-800': !employee.isActive
              }"
            >
              {{ employee.isActive ? 'Aktywny' : 'Nieaktywny' }}
            </span>
            <span
                class="text-xs px-2 py-1 rounded-full ml-1 inline-block"
                :class="{
                  'bg-yellow-100 text-yellow-800 font-semibold': isMainAdmin(employee),
                  'bg-blue-100 text-blue-800': !isMainAdmin(employee)
                }"
            >
              {{ isMainAdmin(employee) ? '👑 Główny Administrator' : getRoleName(employee.role) }}
            </span>
          </div>

          <div class="flex space-x-2">
            <!-- Przycisk edycji -->
            <button
                v-if="canEditUser(employee)"
                :disabled="isMainAdmin(employee) && !isCurrentUserMainAdmin"
                @click="editEmployee(employee.id)"
                class="text-xs bg-primary text-white px-2 py-1 rounded-md hover:bg-secondary transition"
                :class="{ 'opacity-50 cursor-not-allowed': isMainAdmin(employee) && !isCurrentUserMainAdmin }"
            >
              Edytuj
            </button>
            <!-- Przycisk dezaktywacji - admin może dezaktywować adminów poza głównym adminem -->
            <button
                v-if="canDeactivateUser(employee)"
                @click="deactivateEmployee(employee.id)"
                class="text-xs bg-yellow-500 text-white px-2 py-1 rounded-md hover:bg-yellow-600 transition"
            >
              Dezaktywuj
            </button>
            <!-- Informacja o braku możliwości dezaktywacji głównego admina -->
            <span
                v-if="isMainAdmin(employee) && isAdmin"
                class="text-xs text-gray-500 italic"
            >
              Nie można dezaktywować głównego administratora
            </span>
            <!-- Przycisk aktywacji -->
            <button
                v-if="canEditUser(employee) && !employee.isActive"
                @click="activateEmployee(employee.id)"
                class="text-xs bg-green-500 text-white px-2 py-1 rounded-md hover:bg-green-600 transition"
            >
              Aktywuj
            </button>
            <!-- Przycisk usuwania - admin może usuwać adminów poza głównym adminem -->
            <button
                v-if="canDeleteUser(employee)"
                @click="removeEmployee(employee.id, employee.firstName, employee.lastName)"
                class="text-xs bg-danger text-white px-2 py-1 rounded-md hover:bg-red-600 transition"
            >
              Usuń
            </button>
            <!-- Informacja o braku możliwości usunięcia głównego admina -->
            <span
                v-if="isMainAdmin(employee) && isAdmin"
                class="text-xs text-gray-500 italic"
            >
              Nie można usunąć głównego administratora
            </span>
          </div>
        </div>
      </div>
      <p v-else class="text-muted text-center py-8">Brak pracowników. Dodaj pierwszego pracownika używając przycisku poniżej.</p>
    </div>

    <!-- Modal potwierdzenia -->
    <div v-if="showConfirmModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white p-6 rounded-lg shadow-lg max-w-md w-full">
        <h2 class="text-xl font-bold mb-4">Potwierdź operację</h2>
        <p class="mb-6">{{ confirmMessage }}</p>
        <div class="flex justify-end space-x-4">
          <button
              @click="showConfirmModal = false"
              class="px-4 py-2 bg-gray-200 rounded-md hover:bg-gray-300"
          >
            Anuluj
          </button>
          <button
              @click="confirmAction"
              class="px-4 py-2 bg-danger text-white rounded-md hover:bg-red-600"
          >
            Potwierdź
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import userService from '../services/userService';
import authService from '../services/authService';
import { authState } from '../../router/router';

export default {
  setup() {
    const router = useRouter();
    const employees = ref([]);
    const loading = ref(true);
    const error = ref(null);

    // Modal potwierdzenia
    const showConfirmModal = ref(false);
    const confirmMessage = ref('');
    const pendingAction = ref(null);

    // Określ rolę bieżącego użytkownika
    const currentUserRole = computed(() => authState.user?.role || '');

    // Czy bieżący użytkownik jest administratorem
    const isAdmin = computed(() => {
      return authService.hasRoleAtLeast('admin') ||
          currentUserRole.value === 'administrator' ||
          currentUserRole.value === 'ADMIN';
    });

    // Czy bieżący użytkownik jest kierownikiem
    const isManager = computed(() => {
      return authService.hasRoleAtLeast('manager') && !isAdmin.value;
    });

    // NOWE: Czy bieżący użytkownik to główny administrator
    const isCurrentUserMainAdmin = computed(() => {
      return authState.user?.username === 'admin';
    });

    // Czy użytkownik jest administratorem (na podstawie jego roli)
    const isUserAdmin = (user) => {
      return user.role === 'admin' ||
          user.role === 'administrator';
    };

    // Czy użytkownik jest pracownikiem (na podstawie jego roli)
    const isUserEmployee = (user) => {
      return user.role === 'employee' ||
          user.role === 'pracownik';
    };

    // Czy użytkownik to główny administrator
    const isMainAdmin = (user) => {
      return isUserAdmin(user) && user.username === 'admin';
    };

    // ZMIENIONE: Czy bieżący użytkownik może edytować wskazanego użytkownika
    const canEditUser = (user) => {
      // Główny admin może edytować tylko siebie
      if (isMainAdmin(user)) {
        return isCurrentUserMainAdmin.value;
      }

      // Administrator może edytować wszystkich (oprócz głównego admina, który jest obsłużony wyżej)
      if (isAdmin.value) {
        return true;
      }

      // Kierownik może edytować tylko pracowników
      if (isManager.value) {
        return isUserEmployee(user);
      }

      // Domyślnie - brak uprawnień
      return false;
    };

    // Czy bieżący użytkownik może dezaktywować wskazanego użytkownika
    const canDeactivateUser = (user) => {
      // Nie można dezaktywować głównego admina
      if (isMainAdmin(user)) return false;
      // Tylko aktywnych można dezaktywować
      if (!user.isActive) return false;
      // Admin może dezaktywować innych adminów (poza głównym)
      if (isAdmin.value && isUserAdmin(user)) return true;
      // Admin może dezaktywować pracowników i kierowników
      if (isAdmin.value && !isUserAdmin(user)) return true;
      // Kierownik może dezaktywować tylko pracowników
      if (isManager.value) return isUserEmployee(user);
      return false;
    };

    // Czy bieżący użytkownik może usunąć wskazanego użytkownika
    const canDeleteUser = (user) => {
      // Nie można usunąć głównego admina
      if (isMainAdmin(user)) return false;
      // Admin może usuwać innych adminów (poza głównym) oraz pozostałych
      if (isAdmin.value && isUserAdmin(user)) return true;
      if (isAdmin.value && !isUserAdmin(user)) return true;
      // Kierownik może usuwać tylko pracowników
      if (isManager.value) return isUserEmployee(user);
      return false;
    };

    // Pobieranie pracowników
    const fetchEmployees = async () => {
      loading.value = true;
      error.value = null;

      try {
        const users = await userService.getAllUsers();
        employees.value = users;
        console.log('Pobrano pracowników:', employees.value);
      } catch (err) {
        console.error('Błąd podczas pobierania pracowników:', err);
        error.value = `Nie udało się pobrać pracowników: ${err.message}`;

        // Dane demonstracyjne w przypadku błędu
        employees.value = [
          { id: 1, firstName: 'Jan', lastName: 'Kowalski', email: 'jan.kowalski@example.com', username: 'jkowalski', role: 'employee', isActive: true },
          { id: 2, firstName: 'Anna', lastName: 'Nowak', email: 'anna.nowak@example.com', username: 'anowak', role: 'manager', isActive: true },
          { id: 3, firstName: 'Piotr', lastName: 'Zieliński', email: 'piotr.zielinski@example.com', username: 'pzielinski', role: 'employee', isActive: false },
          { id: 4, firstName: 'Adam', lastName: 'Wiśniewski', email: 'adam.wisniewski@example.com', username: 'admin', role: 'administrator', isActive: true }
        ];
      } finally {
        loading.value = false;
      }
    };

    // Edycja pracownika
    const editEmployee = (id) => {
      router.push(`/users/${id}/edit`);
    };

    // Dezaktywacja pracownika
    const deactivateEmployee = async (id) => {
      try {
        await userService.deactivateUser(id);
        // Odśwież listę
        fetchEmployees();
      } catch (err) {
        console.error('Błąd podczas dezaktywacji pracownika:', err);
        error.value = `Nie udało się dezaktywować pracownika: ${err.message}`;
      }
    };

    // Aktywacja pracownika
    const activateEmployee = async (id) => {
      try {
        // Zakładamy, że możemy użyć partialUpdateUser do aktywacji
        await userService.partialUpdateUser(id, { isActive: true });
        // Odśwież listę
        fetchEmployees();
      } catch (err) {
        console.error('Błąd podczas aktywacji pracownika:', err);
        error.value = `Nie udało się aktywować pracownika: ${err.message}`;
      }
    };

    // Usuwanie pracownika z potwierdzeniem
    const removeEmployee = (id, firstName, lastName) => {
      confirmMessage.value = `Czy na pewno chcesz usunąć pracownika ${firstName} ${lastName}?`;
      pendingAction.value = async () => {
        try {
          await userService.deleteUser(id);
          // Odśwież listę
          fetchEmployees();
        } catch (err) {
          console.error('Błąd podczas usuwania pracownika:', err);
          error.value = `Nie udało się usunąć pracownika: ${err.message}`;
        }
      };
      showConfirmModal.value = true;
    };

    // Funkcja potwierdzająca akcję z modala
    const confirmAction = async () => {
      if (pendingAction.value) {
        await pendingAction.value();
        pendingAction.value = null;
      }
      showConfirmModal.value = false;
    };

    // Konwersja roli na czytelną nazwę
    const getRoleName = (role) => {
      const roleMap = {
        'employee': 'Pracownik',
        'manager': 'Kierownik',
        'admin': 'Administrator',
        'administrator': 'Administrator',
        'ADMIN': 'Administrator',
        'pracownik': 'Pracownik',
        'kierownik': 'Kierownik'
      };
      return roleMap[role] || role;
    };

    // Inicjalizacja komponentu
    onMounted(() => {
      fetchEmployees();
    });

    return {
      employees,
      loading,
      error,
      showConfirmModal,
      confirmMessage,
      isAdmin,
      isManager,
      isCurrentUserMainAdmin,
      isMainAdmin,
      canEditUser,
      canDeleteUser,
      canDeactivateUser,
      fetchEmployees,
      editEmployee,
      deactivateEmployee,
      activateEmployee,
      removeEmployee,
      confirmAction,
      getRoleName,
      isUserAdmin
    };
  }
};
</script>