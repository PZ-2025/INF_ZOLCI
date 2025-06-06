<template>
  <div class="bg-background min-h-screen p-8 text-text">
    <h1 class="text-3xl text-left font-bold text-primary mb-6">Dodaj Pracownika</h1>

    <div v-if="loading" class="flex justify-center items-center h-64">
      <p class="text-primary text-xl">Dodawanie pracownika...</p>
    </div>

    <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6">
      <p>{{ error }}</p>
      <button @click="resetForm" class="mt-2 bg-primary text-white px-4 py-1 rounded-md">
        Spróbuj ponownie
      </button>
    </div>

    <form @submit.prevent="addEmployee" class="bg-surface p-6 rounded-lg shadow-md border border-gray-200">
      <!-- Komunikat sukcesu -->
      <div v-if="successMessage" class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4">
        {{ successMessage }}
      </div>

      <div class="mb-4 flex items-center">
        <label for="firstName" class="block font-semibold mr-4 w-40 text-right">Imię</label>
        <input
            type="text"
            id="firstName"
            v-model="user.firstName"
            class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            placeholder="Wpisz imię"
            required
        />
      </div>

      <div class="mb-4 flex items-center">
        <label for="lastName" class="block font-semibold mr-4 w-40 text-right">Nazwisko</label>
        <input
            type="text"
            id="lastName"
            v-model="user.lastName"
            class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            placeholder="Wpisz nazwisko"
            required
        />
      </div>

      <div class="mb-4 flex items-center">
        <label for="email" class="block font-semibold mr-4 w-40 text-right">Adres e-mail</label>
        <input
            type="email"
            id="email"
            v-model="user.email"
            class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            placeholder="jan.kowalski@example.com"
            required
        />
      </div>

      <div class="mb-4 flex items-center">
        <label for="phone" class="block font-semibold mr-4 w-40 text-right">Telefon</label>
        <input
            type="text"
            id="phone"
            v-model="user.phone"
            class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            placeholder="Numer telefonu"
        />
      </div>

      <div class="mb-4 flex items-center">
        <label for="username" class="block font-semibold mr-4 w-40 text-right">Nazwa użytkownika</label>
        <div class="flex-1">
          <input
              type="text"
              id="username"
              v-model="user.username"
              @blur="validateUsername"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
              placeholder="jan.kowalski"
              required
          />
          <!-- Ostrzeżenie dla nazwy "admin" -->
          <div v-if="usernameWarning" class="mt-2 p-3 bg-yellow-50 border-l-4 border-yellow-400 text-yellow-800">
            <div class="flex">
              <div class="flex-shrink-0">
                <svg class="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
                </svg>
              </div>
              <div class="ml-3">
                <p class="text-sm">
                  <strong>Uwaga:</strong> {{ usernameWarning }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="mb-4 flex items-center">
        <label for="password" class="block font-semibold mr-4 w-40 text-right">Hasło</label>
        <div class="flex-1 relative">
          <input
              :type="showPassword ? 'text' : 'password'"
              id="password"
              v-model="user.password"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
              placeholder="Minimum 6 znaków"
              minlength="6"
              required
          />
          <button
            type="button"
            @click="showPassword = !showPassword"
            class="absolute right-2 top-1/2 -translate-y-1/2 bg-transparent p-0 m-0 text-gray-500 text-base focus:outline-none"
            tabindex="-1"
            aria-label="Pokaż/Ukryj hasło"
          >
            <span v-if="showPassword">🙈</span>
            <span v-else>👁️</span>
          </button>
          <p v-if="passwordError" class="text-red-500 mt-1 text-sm">{{ passwordError }}</p>
        </div>
      </div>

      <div class="mb-6 flex items-center">
        <label for="role" class="block font-semibold mr-4 w-40 text-right">Rola</label>
        <div class="flex-1">
          <select
              id="role"
              v-model="user.role"
              @change="onRoleChange"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
              required
          >
            <option value="pracownik">Pracownik</option>
            <option value="kierownik">Kierownik</option>
            <option value="administrator">Administrator</option>
          </select>
          <!-- Informacja o roli administratora -->
          <div v-if="user.role === 'administrator'" class="mt-2 p-3 bg-blue-50 border-l-4 border-blue-400 text-blue-800">
            <p class="text-sm">
              <strong>ℹ️ Informacja:</strong> Administrator ma pełny dostęp do systemu. Nazwa użytkownika "admin" jest zarezerwowana dla głównego administratora.
            </p>
          </div>
        </div>
      </div>

      <div class="flex justify-between mt-6">
        <button
            type="button"
            @click="goBack"
            class="bg-gray-500 hover:bg-gray-600 text-white px-6 py-2 rounded-lg transition"
        >
          Anuluj
        </button>
        <button
            type="submit"
            class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-lg transition"
            :disabled="loading || hasUsernameError"
        >
          <span v-if="loading">Dodawanie...</span>
          <span v-else>Zapisz</span>
        </button>
      </div>
    </form>

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
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import userService from '../services/userService';
import StatusModal from './StatusModal.vue';
import { useStatusModal } from '../composables/useStatusModal';
import { useValidation } from '../composables/useValidation';

export default {
  setup() {
    const router = useRouter();

    // Dane użytkownika
    const user = ref({
      firstName: '',
      lastName: '',
      email: '',
      username: '',
      password: '',
      phone: '', 
      role: 'pracownik',
      isActive: true
    });

    // Stany komponentu
    const loading = ref(false);
    const error = ref('');
    const successMessage = ref('');
    const passwordError = ref('');
    const showPassword = ref(false);
    
    // NOWE: Stany dla walidacji nazwy użytkownika
    const usernameWarning = ref('');
    const hasUsernameError = ref(false);

    // Użycie composable do obsługi modalu
    const { showModal, modalConfig, showStatus, hideModal } = useStatusModal();

    // Użycie composable do walidacji
    const { validateUser } = useValidation();

    // NOWE: Sprawdzenie czy nazwa użytkownika "admin" już istnieje
    const checkAdminExists = async () => {
      try {
        const users = await userService.getAllUsers();
        return users.some(user => user.username.toLowerCase() === 'admin');
      } catch (err) {
        console.error('Błąd podczas sprawdzania istnienia admina:', err);
        return false;
      }
    };

    // NOWE: Walidacja nazwy użytkownika
    const validateUsername = async () => {
      const username = user.value.username.toLowerCase();
      usernameWarning.value = '';
      hasUsernameError.value = false;

      if (username === 'admin') {
        const adminExists = await checkAdminExists();
        if (adminExists) {
          usernameWarning.value = 'Nazwa użytkownika "admin" jest już zajęta przez głównego administratora.';
          hasUsernameError.value = true;
        } else if (user.value.role !== 'administrator') {
          usernameWarning.value = 'Nazwa "admin" może być używana tylko przez administratorów.';
          hasUsernameError.value = true;
        } else {
          usernameWarning.value = 'Ta nazwa użytkownika utworzy głównego administratora systemu.';
          hasUsernameError.value = false;
        }
      }
    };

    // NOWE: Obsługa zmiany roli
    const onRoleChange = () => {
      // Ponownie sprawdź nazwę użytkownika gdy zmieni się rola
      if (user.value.username) {
        validateUsername();
      }
    };

    // Reset formularza
    const resetForm = () => {
      error.value = '';
      passwordError.value = '';
      usernameWarning.value = '';
      hasUsernameError.value = false;
    };

    // Dodawanie pracownika
    const addEmployee = async () => {
      // Sprawdź ponownie nazwę użytkownika przed zapisem
      await validateUsername();
      
      if (hasUsernameError.value) {
        showStatus({
          type: 'error',
          title: 'Błąd',
          message: 'Nazwa użytkownika nie jest dostępna. Wybierz inną nazwę.',
          buttonText: 'Zamknij'
        });
        return;
      }

      loading.value = true;
      error.value = '';
      successMessage.value = '';

      try {
        // Walidacja z użyciem composable
        const validationErrors = validateUser(user.value, false);
        
        if (validationErrors.length > 0) {
          showStatus({
            type: 'error',
            title: 'Błędy walidacji',
            message: validationErrors.join('\n'),
            buttonText: 'Zamknij'
          });
          loading.value = false;
          return;
        }

        // Sprawdź dostępność nazwy użytkownika (dodatkowa walidacja przez API)
        try {
          const usernameCheck = await userService.checkUsernameAvailability(user.value.username);
          if (!usernameCheck.available) {
            showStatus({
              type: 'error',
              title: 'Nazwa użytkownika zajęta',
              message: 'Ta nazwa użytkownika jest już używana. Wybierz inną.',
              buttonText: 'Zamknij'
            });
            loading.value = false;
            return;
          }
        } catch (usernameError) {
          console.warn('Nie można sprawdzić dostępności nazwy użytkownika przez API:', usernameError);
          // Kontynuuj - serwer i tak zwaliduje przy tworzeniu
        }

        // Sprawdź dostępność adresu email
        try {
          const emailCheck = await userService.checkEmailAvailability(user.value.email);
          if (!emailCheck.available) {
            showStatus({
              type: 'error',
              title: 'Email zajęty',
              message: 'Ten adres email jest już używany przez innego użytkownika.',
              buttonText: 'Zamknij'
            });
            loading.value = false;
            return;
          }
        } catch (emailError) {
          console.warn('Nie można sprawdzić dostępności email przez API:', emailError);
          // Kontynuuj - serwer i tak zwaliduje przy tworzeniu
        }

        // Przekształcenie danych do formatu API
        const userData = {
          firstName: user.value.firstName,
          lastName: user.value.lastName,
          email: user.value.email,
          username: user.value.username,
          password: user.value.password,
          phone: user.value.phone, 
          role: user.value.role,
          isActive: user.value.isActive
        };

        // Utworzenie użytkownika przez API
        const createdUser = await userService.createUser(userData);
        console.log('Pracownik został dodany:', createdUser);

        // Specjalny komunikat dla głównego administratora
        let successMsg = 'Pracownik został pomyślnie dodany!';
        if (user.value.username.toLowerCase() === 'admin' && user.value.role === 'administrator') {
          successMsg = '👑 Główny administrator został pomyślnie utworzony!';
        }

        // Wyświetl komunikat sukcesu
        showStatus({
          type: 'success',
          title: 'Sukces',
          message: successMsg,
          buttonText: 'OK',
          autoClose: true,
          autoCloseDelay: 2000,
          onClose: () => router.push('/adminpanel')
        });

        // Wyczyść formularz
        user.value = {
          firstName: '',
          lastName: '',
          email: '',
          username: '',
          password: '',
          phone: '', 
          role: 'pracownik',
          isActive: true
        };
        usernameWarning.value = '';
        hasUsernameError.value = false;

      } catch (err) {
        console.error('Błąd podczas dodawania pracownika:', err);
        
        // NOWE: Lepsza obsługa błędów z serwera
        let errorMessage = 'Nie udało się dodać pracownika.';
        
        if (err.message.includes('email') && err.message.includes('używany')) {
          errorMessage = 'Ten adres email jest już używany przez innego użytkownika.';
        } else if (err.message.includes('username') && err.message.includes('zajęta')) {
          errorMessage = 'Ta nazwa użytkownika jest już zajęta. Wybierz inną.';
        } else if (err.message.includes('Hasło')) {
          errorMessage = 'Hasło musi mieć co najmniej 6 znaków.';
        } else if (err.response && err.response.data && err.response.data.error) {
          errorMessage = err.response.data.error;
        } else {
          errorMessage = `Błąd serwera: ${err.message}`;
        }
        
        showStatus({
          type: 'error',
          title: 'Błąd',
          message: errorMessage,
          buttonText: 'Zamknij'
        });
      } finally {
        loading.value = false;
      }
    };

    const validateEmailUniqueness = async () => {
      if (!user.value.email) return true;
      
      try {
        const isAvailable = await userService.checkEmailAvailability(user.value.email);
        if (!isAvailable) {
          showStatus({
            type: 'error',
            title: 'Email zajęty',
            message: 'Ten adres email jest już używany przez innego użytkownika.',
            buttonText: 'Zamknij'
          });
          return false;
        }
        return true;
      } catch (error) {
        console.error('Błąd walidacji email:', error);
        return true; // Pozwól kontynuować, serwer i tak zwaliduje
      }
    };

    // Powrót do poprzedniej strony
    const goBack = () => {
      router.back();
    };

    return {
      user,
      loading,
      error,
      successMessage,
      passwordError,
      showPassword,
      usernameWarning,
      hasUsernameError,
      addEmployee,
      goBack,
      resetForm,
      validateUsername,
      onRoleChange,
      showModal,
      modalConfig,
      hideModal
    };
  },
  components: {
    StatusModal
  }
};
</script>