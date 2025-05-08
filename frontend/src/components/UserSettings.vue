<template>
  <div class="bg-background min-h-screen p-8 text-text">
    <h1 class="text-3xl font-bold text-primary mb-6">
      {{ userId ? 'Edycja Użytkownika' : 'Ustawienia Użytkownika' }}
    </h1>

    <div v-if="loading" class="flex justify-center items-center h-64">
      <p class="text-primary text-xl">Ładowanie danych użytkownika...</p>
    </div>

    <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6">
      <p>{{ error }}</p>
      <button @click="loadUserData" class="mt-2 bg-primary text-white px-4 py-1 rounded-md">
        Spróbuj ponownie
      </button>
    </div>

    <form v-else @submit.prevent="updateSettings" class="bg-surface p-6 rounded-lg shadow-md border border-gray-200">
      <!-- Komunikat sukcesu -->
      <div v-if="successMessage" class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4">
        {{ successMessage }}
      </div>

      <div class="mb-4">
        <label class="block font-semibold mb-2">Imię</label>
        <input
            type="text"
            v-model="user.firstName"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            placeholder="Wpisz imię"
            required
        />
      </div>

      <div class="mb-4">
        <label class="block font-semibold mb-2">Nazwisko</label>
        <input
            type="text"
            v-model="user.lastName"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            placeholder="Wpisz nazwisko"
            required
        />
      </div>

      <div class="mb-4">
        <label for="email" class="block font-semibold mb-2">Adres e-mail</label>
        <input
            type="email"
            id="email"
            v-model="user.email"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            placeholder="Twój adres e-mail"
            required
        />
      </div>

      <div class="mb-4">
        <label for="phone" class="block font-semibold mb-2">Telefon</label>
        <input
            type="text"
            id="phone"
            v-model="user.phone"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            placeholder="Numer telefonu"
        />
      </div>

      <div class="mb-4">
        <label for="currentPassword" class="block font-semibold mb-2">Aktualne hasło</label>
        <input
            type="password"
            id="currentPassword"
            v-model="passwordData.currentPassword"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            placeholder="Wpisz aktualne hasło (wymagane do zmiany hasła)"
            :required="passwordData.newPassword.length > 0"
        />
      </div>

      <div class="mb-4">
        <label for="newPassword" class="block font-semibold mb-2">Nowe hasło</label>
        <input
            type="password"
            id="newPassword"
            v-model="passwordData.newPassword"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            placeholder="Wpisz nowe hasło (pozostaw puste, aby nie zmieniać)"
        />
      </div>

      <div class="mb-4">
        <label for="confirmPassword" class="block font-semibold mb-2">Potwierdź nowe hasło</label>
        <input
            type="password"
            id="confirmPassword"
            v-model="passwordData.confirmPassword"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            placeholder="Potwierdź nowe hasło"
            :required="passwordData.newPassword.length > 0"
        />
        <p v-if="passwordError" class="text-red-500 mt-1 text-sm">{{ passwordError }}</p>
      </div>

      <div class="mb-6">
        <label for="theme" class="block font-semibold mb-2">Motyw</label>
        <select
            id="theme"
            v-model="user.theme"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
        >
          <option value="light">Jasny</option>
          <option value="dark">Ciemny</option>
        </select>
      </div>

      <button
          type="submit"
          class="w-full bg-primary hover:bg-secondary text-white font-bold py-2 rounded-lg transition"
          :disabled="isSaving"
      >
        <span v-if="isSaving">Zapisywanie zmian...</span>
        <span v-else>Zapisz Zmiany</span>
      </button>
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
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import userService from '../services/userService';
import { authState } from '../../router/router.js';
import StatusModal from './StatusModal.vue';
import { useStatusModal } from '../composables/useStatusModal';

export default {
  components: {
    StatusModal
  },
  props: {
    userId: {
      type: [String, Number],
      default: null
    }
  },
  setup(props) {
    const router = useRouter();
    const { showModal, modalConfig, showStatus, hideModal } = useStatusModal();

    // Oryginalne dane użytkownika (do wykrywania zmian)
    const originalUserData = ref({});

    // Dane użytkownika
    const user = reactive({
      id: null,
      username: '',
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      theme: 'light'
    });

    // Dane dotyczące hasła przechowujemy osobno
    const passwordData = reactive({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    });

    // Stany komponentu
    const loading = ref(true);
    const isSaving = ref(false);
    const error = ref(null);
    const successMessage = ref(null);

    // Błąd walidacji haseł
    const passwordError = computed(() => {
      if (passwordData.newPassword && passwordData.newPassword !== passwordData.confirmPassword) {
        return 'Hasła nie są identyczne';
      }
      if (passwordData.newPassword && passwordData.newPassword.length < 6) {
        return 'Hasło musi mieć co najmniej 6 znaków';
      }
      return null;
    });

    // Pobieranie danych użytkownika
    const loadUserData = async () => {
      loading.value = true;
      error.value = null;
      successMessage.value = null;

      try {
        let userData;
        if (props.userId) {
          // Jeśli przekazano userId, pobierz dane tego użytkownika
          userData = await userService.getUserById(props.userId);
        } else {
          // Jeśli nie, sprawdź czy użytkownik jest zalogowany
          if (!authState.isAuthenticated || !authState.user || !authState.user.id) {
            error.value = 'Nie jesteś zalogowany. Zaloguj się, aby zmienić ustawienia.';
            return;
          }
          userData = await userService.getUserById(authState.user.id);
        }

        // Zapisz oryginalne dane do porównania przy aktualizacji
        originalUserData.value = { ...userData };

        // Aktualizuj dane w komponencie
        user.id = userData.id;
        user.username = userData.username;
        user.firstName = userData.firstName || '';
        user.lastName = userData.lastName || '';
        user.email = userData.email || '';
        user.phone = userData.phone || '';

        // Jeśli mamy zapisane preferencje motywu w localStorage, użyj ich
        const savedTheme = localStorage.getItem('theme');
        user.theme = savedTheme || 'light';

        console.log('Dane użytkownika załadowane:', user);
      } catch (err) {
        console.error('Błąd ładowania danych użytkownika:', err);
        showStatus({
          type: 'error',
          title: 'Błąd',
          message: `Nie udało się załadować danych użytkownika: ${err.message}`,
          buttonText: 'Zamknij'
        });

        // Dane demonstracyjne w przypadku błędu
        user.id = 1;
        user.username = 'user1';
        user.firstName = 'Jan';
        user.lastName = 'Kowalski';
        user.email = 'jan.kowalski@example.com';
        user.phone = '';
        user.theme = localStorage.getItem('theme') || 'light';
      } finally {
        loading.value = false;
      }
    };

    // Aktualizacja ustawień użytkownika z użyciem PATCH
    const updateSettings = async () => {
      // Walidacja formularza
      if (passwordError.value) {
        alert(passwordError.value);
        return;
      }

      isSaving.value = true;
      error.value = null;
      successMessage.value = null;

      try {
        // Identyfikuj zmienione pola
        const changedFields = {};

        // Sprawdź, które pola zostały zmienione
        if (user.firstName !== originalUserData.value.firstName) {
          changedFields.firstName = user.firstName;
        }
        if (user.lastName !== originalUserData.value.lastName) {
          changedFields.lastName = user.lastName;
        }
        if (user.email !== originalUserData.value.email) {
          changedFields.email = user.email;
        }
        if (user.phone !== originalUserData.value.phone) {
          changedFields.phone = user.phone;
        }

        // Dodaj hasło tylko jeśli użytkownik chce je zmienić
        if (passwordData.newPassword) {
          if (!passwordData.currentPassword) {
            throw new Error('Aktualne hasło jest wymagane do zmiany hasła');
          }
          changedFields.password = passwordData.newPassword;
          changedFields.currentPassword = passwordData.currentPassword;

          // Jeśli zmieniamy hasło, użyjmy PUT zamiast PATCH dla bezpieczeństwa
          const fullUpdateData = {
            ...originalUserData.value,
            ...changedFields
          };

          // Pełna aktualizacja z hasłem
          await userService.updateUser(user.id, fullUpdateData);
        } else if (Object.keys(changedFields).length > 0) {
          // Aktualizuj tylko zmienione pola za pomocą PATCH
          await userService.partialUpdateUser(user.id, changedFields);
        } else {
          // Brak zmian, ale zapisz temat
          console.log('Brak zmian w danych użytkownika, aktualizuję tylko preferencje motywu');
        }

        // Zapisz preferencje motywu w localStorage
        localStorage.setItem('theme', user.theme);

        // Aktywuj motyw
        document.documentElement.classList.toggle('dark', user.theme === 'dark');

        // Wyczyść dane hasła
        passwordData.currentPassword = '';
        passwordData.newPassword = '';
        passwordData.confirmPassword = '';

        // Zaktualizuj oryginalne dane
        originalUserData.value = { ...user };

        showStatus({
          type: 'success',
          title: 'Sukces',
          message: 'Ustawienia zostały zaktualizowane pomyślnie!',
          buttonText: 'Zamknij',
          autoClose: true,
          autoCloseDelay: 1500,
          onClose: () => {
            hideModal();
            // Jeśli edytujemy innego użytkownika, wróć do listy
            if (props.userId) {
              router.push('/allusers');
            }
          } 
        });

        // Zaktualizuj dane w stanie autoryzacji tylko jeśli edytujemy własne dane
        if (!props.userId && authState.user) {
          authState.user.firstName = user.firstName;
          authState.user.lastName = user.lastName;
          authState.user.email = user.email;
        }
      } catch (err) {
        console.error('Błąd aktualizacji ustawień:', err);
        showStatus({
          type: 'error',
          title: 'Błąd',
          message: `Nie udało się zaktualizować ustawień: ${err.message}`,
          buttonText: 'Zamknij'
        });
      } finally {
        isSaving.value = false;
      }
    };

    // Inicjalizacja komponentu
    onMounted(() => {
      loadUserData();
    });

    return {
      user,
      passwordData,
      loading,
      isSaving,
      error,
      successMessage,
      passwordError,
      loadUserData,
      updateSettings,
      showModal,
      modalConfig,
      hideModal
    };
  }
};
</script>