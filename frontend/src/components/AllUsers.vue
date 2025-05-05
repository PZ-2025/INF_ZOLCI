<template>
  <div class="h-full flex flex-col p-6 bg-background text-text">
    <h1 class="text-3xl font-bold text-primary mb-6">Pracownicy</h1>

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
            <p class="font-medium text-lg text-text">{{ employee.firstName }} {{ employee.lastName }}</p>
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
                class="text-xs px-2 py-1 rounded-full ml-1 inline-block bg-blue-100 text-blue-800"
            >
              {{ getRoleName(employee.role) }}
            </span>
          </div>

          <div class="flex space-x-2">
            <button
                @click="editEmployee(employee.id)"
                class="text-xs bg-primary text-white px-2 py-1 rounded-md hover:bg-secondary transition"
            >
              Edytuj
            </button>
            <button
                v-if="employee.isActive"
                @click="deactivateEmployee(employee.id)"
                class="text-xs bg-yellow-500 text-white px-2 py-1 rounded-md hover:bg-yellow-600 transition"
            >
              Dezaktywuj
            </button>
            <button
                v-else
                @click="activateEmployee(employee.id)"
                class="text-xs bg-green-500 text-white px-2 py-1 rounded-md hover:bg-green-600 transition"
            >
              Aktywuj
            </button>
            <button
                @click="removeEmployee(employee.id, employee.firstName, employee.lastName)"
                class="text-xs bg-danger text-white px-2 py-1 rounded-md hover:bg-red-600 transition"
            >
              Usuń
            </button>
          </div>
        </div>
      </div>
      <p v-else class="text-muted text-center py-8">Brak pracowników. Dodaj pierwszego pracownika używając przycisku poniżej.</p>
    </div>

    <!-- Przyciski akcji -->
    <div class="flex justify-between">
      <router-link
          to="/addemployee"
          class="bg-primary hover:bg-secondary text-white px-4 py-2 rounded-md transition text-center"
      >
        Dodaj Pracownika
      </router-link>
      <button
          @click="removeAllEmployees"
          class="bg-danger hover:bg-red-600 text-white px-4 py-2 rounded-md transition"
          :disabled="!employees.length"
      >
        Usuń Wszystkich
      </button>
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
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import userService from '../services/userService';

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
          { id: 3, firstName: 'Piotr', lastName: 'Zieliński', email: 'piotr.zielinski@example.com', username: 'pzielinski', role: 'employee', isActive: false }
        ];
      } finally {
        loading.value = false;
      }
    };

    // Edycja pracownika
    const editEmployee = (id) => {
      router.push(`/employees/${id}/edit`);
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

    // Usuwanie wszystkich pracowników z potwierdzeniem
    const removeAllEmployees = () => {
      confirmMessage.value = `Czy na pewno chcesz usunąć wszystkich pracowników? Ta operacja jest nieodwracalna!`;
      pendingAction.value = async () => {
        try {
          // Usuwamy pracowników jeden po drugim
          for (const employee of employees.value) {
            await userService.deleteUser(employee.id);
          }
          // Odśwież listę
          fetchEmployees();
        } catch (err) {
          console.error('Błąd podczas usuwania wszystkich pracowników:', err);
          error.value = `Nie udało się usunąć wszystkich pracowników: ${err.message}`;
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
        'admin': 'Administrator'
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
      fetchEmployees,
      editEmployee,
      deactivateEmployee,
      activateEmployee,
      removeEmployee,
      removeAllEmployees,
      confirmAction,
      getRoleName
    };
  }
};
</script>