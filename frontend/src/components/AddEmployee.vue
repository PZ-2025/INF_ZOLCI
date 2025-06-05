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
        <input
            type="text"
            id="username"
            v-model="user.username"
            class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            placeholder="jan.kowalski"
            required
        />
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
        <select
            id="role"
            v-model="user.role"
            class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            required
        >
          <option value="pracownik">Pracownik</option>
          <option value="kierownik">Kierownik</option>
          <option value="administrator">Administrator</option>
        </select>
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
            :disabled="loading"
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

    // Użycie composable do obsługi modalu
    const { showModal, modalConfig, showStatus, hideModal } = useStatusModal();

    // Walidacja hasła
    const validatePassword = () => {
      if (user.value.password.length < 6) {
        passwordError.value = 'Hasło musi mieć co najmniej 6 znaków';
        return false;
      }
      passwordError.value = '';
      return true;
    };

    // Reset formularza
    const resetForm = () => {
      error.value = '';
      passwordError.value = '';
    };

    // Dodawanie pracownika
    const addEmployee = async () => {
      // Walidacja formularza
      if (!validatePassword()) {
        return;
      }

      loading.value = true;
      error.value = '';
      successMessage.value = '';

      try {
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

        // Wyświetl komunikat sukcesu
        showStatus({
          type: 'success',
          title: 'Sukces',
          message: 'Pracownik został pomyślnie dodany!',
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
          phone: '', // reset phone
          role: 'pracownik',
          isActive: true
        };

      } catch (err) {
        console.error('Błąd podczas dodawania pracownika:', err);
        showStatus({
          type: 'error',
          title: 'Błąd',
          message: `Nie udało się dodać pracownika: ${err.message}`,
          buttonText: 'Zamknij'
        });
      } finally {
        loading.value = false;
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
      addEmployee,
      goBack,
      resetForm,
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