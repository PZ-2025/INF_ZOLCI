<template>
  <div class="bg-background min-h-screen p-8 text-text">
    <h1 class="text-3xl text-left font-bold text-primary mb-6">
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

    <form v-else @submit.prevent="updateSettings" class="bg-surface p-6 rounded-lg space-y-6">
      <!-- Komunikat sukcesu -->
      <div v-if="successMessage" class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4">
        {{ successMessage }}
      </div>

      <!-- Pole formularza: Username -->
      <div class="flex items-center mb-4">
        <label class="w-40 font-semibold">Nazwa użytkownika</label>
        <div class="flex-1">
          <input
              type="text"
              v-model="user.username"
              @blur="checkAdminAvailability"
              @input="checkAdminAvailability"
              :disabled="isMainAdmin"
              :class="[
                'w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary',
                isMainAdmin ? 'bg-gray-200 cursor-not-allowed text-gray-600' : 'bg-white',
                isChangingToAdmin && !isAdminUsernameAvailable ? 'border-red-500' : ''
              ]"
              placeholder="Wpisz nazwę użytkownika"
              required
          />
          
          <!-- Informacja dla oryginalnego głównego administratora -->
          <div v-if="isMainAdmin" class="mt-1 text-xs text-gray-500 italic">
            Główny administrator - nie można zmienić nazwy użytkownika
          </div>
          
          <!-- Ostrzeżenie gdy próbuje zmienić na "admin" ale nazwa jest zajęta -->
          <div v-else-if="isChangingToAdmin && !isAdminUsernameAvailable" class="mt-2 p-3 bg-red-50 border-l-4 border-red-400 text-red-800">
            <div class="flex">
              <div class="flex-shrink-0">
                <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                </svg>
              </div>
              <div class="ml-3">
                <p class="text-sm">
                  <strong>Błąd:</strong> Nazwa użytkownika "admin" jest już zajęta przez głównego administratora.
                </p>
              </div>
            </div>
          </div>
          
          <!-- Informacja gdy zmienia na "admin" i nazwa jest dostępna -->
          <div v-else-if="isChangingToAdmin && isAdminUsernameAvailable" class="mt-2 p-3 bg-yellow-50 border-l-4 border-yellow-400 text-yellow-800">
            <div class="flex">
              <div class="flex-shrink-0">
                <svg class="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
                </svg>
              </div>
              <div class="ml-3">
                <p class="text-sm">
                  <strong>Uwaga:</strong> Zmiana nazwy użytkownika na "admin" nada temu użytkownikowi uprawnienia głównego administratora.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Pole formularza: Imię -->
      <div class="flex items-center mb-4">
        <label class="w-40 font-semibold">Imię</label>
        <input
            type="text"
            v-model="user.firstName"
            class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            placeholder="Wpisz imię"
            required
        />
      </div>

      <div class="flex items-center mb-4">
        <label class="w-40 font-semibold">Nazwisko</label>
        <input
            type="text"
            v-model="user.lastName"
            class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            placeholder="Wpisz nazwisko"
            required
        />
      </div>

      <div class="flex items-center mb-4">
        <label for="email" class="w-40 font-semibold">Adres e-mail</label>
        <input
            type="email"
            id="email"
            v-model="user.email"
            class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            placeholder="Twój adres e-mail"
            required
        />
      </div>

      <div class="flex items-center mb-4">
        <label for="phone" class="w-40 font-semibold">Telefon</label>
        <input
            type="text"
            id="phone"
            v-model="user.phone"
            class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            placeholder="Numer telefonu"
        />
      </div>

      <div class="flex items-center mb-4">
        <label for="newPassword" class="w-40 font-semibold">Nowe hasło</label>
        <div class="flex-1 relative">
          <input
            :type="showNewPassword ? 'text' : 'password'"
            id="newPassword"
            v-model="passwordData.newPassword"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            placeholder="Wpisz nowe hasło"
          />
          <button
            type="button"
            @click="showNewPassword = !showNewPassword"
            class="absolute right-2 top-1/2 -translate-y-1/2 bg-transparent p-0 m-0 text-gray-500 text-base focus:outline-none"
            tabindex="-1"
            aria-label="Pokaż/Ukryj hasło"
          >
            <span v-if="showNewPassword">🙈</span>
            <span v-else>👁️</span>
          </button>
        </div>
      </div>

      <div class="flex items-center mb-1">
        <label for="confirmPassword" class="w-40 font-semibold">Potwierdź hasło</label>
        <div class="flex-1 relative">
          <input
            :type="showConfirmPassword ? 'text' : 'password'"
            id="confirmPassword"
            v-model="passwordData.confirmPassword"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary bg-white"
            placeholder="Potwierdź nowe hasło"
            :required="passwordData.newPassword.length > 0"
          />
          <button
            type="button"
            @click="showConfirmPassword = !showConfirmPassword"
            class="absolute right-2 top-1/2 -translate-y-1/2 bg-transparent p-0 m-0 text-gray-500 text-base focus:outline-none"
            tabindex="-1"
            aria-label="Pokaż/Ukryj hasło"
          >
            <span v-if="showConfirmPassword">🙈</span>
            <span v-else>👁️</span>
          </button>
        </div>
      </div>
      <p v-if="passwordError" class="text-red-500 ml-40 mt-1 text-sm">{{ passwordError }}</p>

      <div v-if="userId" class="flex items-center mb-6">
        <label for="isActive" class="w-40 font-semibold">Aktywny</label>
        <input
            type="checkbox"
            id="isActive"
            v-model="user.isActive"
            :disabled="isMainAdmin"
            :class="[
              'h-5 w-5',
              isMainAdmin ? 'cursor-not-allowed opacity-50' : ''
            ]"
        />
        <span v-if="isMainAdmin" class="ml-2 text-xs text-gray-500 italic">
          Główny administrator - zawsze aktywny
        </span>
      </div>

      <div class="flex justify-between mt-6">
        <button
            type="button"
            @click="cancelChanges"
            class="bg-gray-500 hover:bg-gray-600 text-white px-6 py-2 rounded-lg transition"
            :disabled="isSaving"
        >
          Anuluj
        </button>
        <button
            type="submit"
            class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-lg transition"
            :disabled="isSaving || (isChangingToAdmin && !isAdminUsernameAvailable)"
        >
          <span v-if="isSaving">Zapisywanie zmian...</span>
          <span v-else>Zapisz Zmiany</span>
        </button>
      </div>
    </form>

    <StatusModal
        :show="showModal"
        :type="modalConfig.type"
        :title="modalConfig.title"
        :message="modalConfig.message"
        :button-text="modalConfig.buttonText"
        :cancel-text="modalConfig.cancelText"
        :show-cancel-button="modalConfig.showCancelButton"
        :auto-close="modalConfig.autoClose"
        :auto-close-delay="modalConfig.autoCloseDelay"
        :on-close="modalConfig.onClose"
        :on-cancel="modalConfig.onCancel"
        @close="hideModal"
        @confirm="handleModalConfirm"
        @cancel="handleModalCancel"
    />
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import userService from '../services/userService';
import { authState } from '../../router/router.js';
import StatusModal from './StatusModal.vue';
import { useStatusModal } from '../composables/useStatusModal';
import { useValidation } from '../composables/useValidation';

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
    const route = useRoute();
    const { showModal, modalConfig, showStatus, hideModal } = useStatusModal();

    const { validateUser, validatePasswordMatch } = useValidation();

    const showNewPassword = ref(false);
    const showConfirmPassword = ref(false);

    // Pobieranie userId z props lub route params
    const currentUserId = computed(() => {
      return props.userId || route.params.id || route.params.userId;
    });

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
      isActive: true
    });

    // POPRAWIONE: Sprawdzenie czy to główny administrator (na podstawie ORYGINALNYCH danych)
    const isMainAdmin = computed(() => {
      return originalUserData.value.username === 'admin';
    });

    // NOWE: Sprawdzenie czy użytkownik próbuje zmienić nazwę na "admin"
    const isChangingToAdmin = computed(() => {
      return user.username === 'admin' && originalUserData.value.username !== 'admin';
    });

    // NOWE: Sprawdzenie czy nazwa "admin" jest dostępna do użycia
    const isAdminUsernameAvailable = ref(true);

    // Dane dotyczące hasła przechowujemy osobno
    const passwordData = reactive({
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

    // NOWE: Funkcja sprawdzania dostępności nazwy "admin"
    const checkAdminAvailability = async () => {
      if (user.username.toLowerCase() !== 'admin') {
        isAdminUsernameAvailable.value = true;
        return;
      }

      // Jeśli to oryginalny admin, zawsze dostępne
      if (originalUserData.value.username === 'admin') {
        isAdminUsernameAvailable.value = true;
        return;
      }

      try {
        const usernameCheck = await userService.checkUsernameAvailability('admin');
        isAdminUsernameAvailable.value = usernameCheck.available;
      } catch (error) {
        console.warn('Nie można sprawdzić dostępności nazwy admin:', error);
        isAdminUsernameAvailable.value = true; // Zakładamy dostępność w przypadku błędu
      }
    };

    // Sprawdzenie czy nazwa użytkownika "admin" już istnieje
    const checkAdminExists = async (username) => {
      if (username.toLowerCase() !== 'admin') return false;
      
      try {
        const users = await userService.getAllUsers();
        return users.some(user => user.username.toLowerCase() === 'admin');
      } catch (err) {
        console.error('Błąd podczas sprawdzania istnienia admina:', err);
        return false;
      }
    };

    const loadUserData = async () => {
      loading.value = true;
      error.value = null;
      successMessage.value = null;

      try {
        let userData;
        if (currentUserId.value) {
          // Jeśli przekazano userId, pobierz dane tego użytkownika
          userData = await userService.getUserById(currentUserId.value);
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
        user.isActive = userData.isActive !== undefined ? userData.isActive : true;

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
        user.isActive = true;
      } finally {
        loading.value = false;
      }
    };

    // Aktualizacja ustawień użytkownika z użyciem PATCH i walidacją dostępności
    const updateSettings = async () => {
      if (passwordError.value) {
        showStatus({
          type: 'error',
          title: 'Błąd walidacji',
          message: passwordError.value,
          buttonText: 'Zamknij'
        });
        return;
      }

      // Przygotuj dane do walidacji
      const dataToValidate = { ...user };
      if (passwordData.newPassword) {
        dataToValidate.password = passwordData.newPassword;
      }

      // Walidacja z użyciem composable
      const validationErrors = validateUser(dataToValidate, true);

      if (validationErrors.length > 0) {
        showStatus({
          type: 'error',
          title: 'Błędy walidacji',
          message: validationErrors.join('\n'),
          buttonText: 'Zamknij'
        });
        return;
      }
      
      isSaving.value = true;
      error.value = null;
      successMessage.value = null;

      try {
        // Identyfikuj zmienione pola
        const changedFields = {};

        // ZMIANA: Sprawdź zmienione pola, ale pomiń username i isActive dla głównego admina
        if (!isMainAdmin.value && user.username !== originalUserData.value.username) {
          changedFields.username = user.username;
        }
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
        if (!isMainAdmin.value && currentUserId.value && user.isActive !== originalUserData.value.isActive) {
          changedFields.isActive = user.isActive;
        }

        // NOWE: Sprawdzenie dostępności username (jeśli został zmieniony)
        if (changedFields.username) {
          // Sprawdź czy próbuje zmienić na "admin" ale nie jest dostępne
          if (changedFields.username.toLowerCase() === 'admin' && !isAdminUsernameAvailable.value) {
            showStatus({
              type: 'error',
              title: 'Nazwa użytkownika niedostępna',
              message: 'Nazwa użytkownika "admin" jest już zajęta przez głównego administratora.',
              buttonText: 'Zamknij'
            });
            isSaving.value = false;
            return;
          }

          // Sprawdzenie dostępności przez API (dla innych nazw)
          if (changedFields.username.toLowerCase() !== 'admin') {
            try {
              const usernameCheck = await userService.checkUsernameAvailability(changedFields.username);
              if (!usernameCheck.available) {
                showStatus({
                  type: 'error',
                  title: 'Nazwa użytkownika zajęta',
                  message: 'Ta nazwa użytkownika jest już używana. Wybierz inną.',
                  buttonText: 'Zamknij'
                });
                isSaving.value = false;
                return;
              }
            } catch (usernameError) {
              console.warn('Nie można sprawdzić dostępności nazwy użytkownika przez API:', usernameError);
              // Kontynuuj - serwer i tak zwaliduje przy aktualizacji
            }
          }
        }

        // NOWE: Sprawdzenie dostępności email (jeśli został zmieniony)
        if (changedFields.email) {
          try {
            const emailCheck = await userService.checkEmailAvailability(changedFields.email);
            if (!emailCheck.available) {
              showStatus({
                type: 'error',
                title: 'Email zajęty',
                message: 'Ten adres email jest już używany przez innego użytkownika.',
                buttonText: 'Zamknij'
              });
              isSaving.value = false;
              return;
            }
          } catch (emailError) {
            console.warn('Nie można sprawdzić dostępności email przez API:', emailError);
            // Kontynuuj - serwer i tak zwaliduje przy aktualizacji
          }
        }

        // Dodaj hasło tylko jeśli użytkownik chce je zmienić
        if (passwordData.newPassword) {
          changedFields.password = passwordData.newPassword;

          // Jeśli zmieniamy hasło, użyjmy PUT zamiast PATCH dla bezpieczeństwa
          const fullUpdateData = {
            ...originalUserData.value,
            ...changedFields
          };

          // ZMIANA: Dla głównego admina usuń pola, których nie może zmieniać
          if (isMainAdmin.value) {
            fullUpdateData.username = originalUserData.value.username;
            fullUpdateData.isActive = originalUserData.value.isActive;
          }

          // Pełna aktualizacja z hasłem
          await userService.updateUser(user.id, fullUpdateData);
        } else if (Object.keys(changedFields).length > 0) {
          // Aktualizuj tylko zmienione pola za pomocą PATCH
          await userService.partialUpdateUser(user.id, changedFields);
        } else {
          console.log('Brak zmian w danych użytkownika');
          
          // Informuj użytkownika, że nie wprowadził żadnych zmian
          showStatus({
            type: 'info',
            title: 'Brak zmian',
            message: 'Nie wprowadzono żadnych zmian w danych użytkownika.',
            buttonText: 'Zamknij',
            autoClose: true,
            autoCloseDelay: 2000
          });
          isSaving.value = false;
          return;
        }

        // Wyczyść dane hasła
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
            router.push('/adminpanel');
          }
        });

        // Zaktualizuj dane w stanie autoryzacji tylko jeśli edytujemy własne dane
        if (!currentUserId.value && authState.user) {
          authState.user.firstName = user.firstName;
          authState.user.lastName = user.lastName;
          authState.user.email = user.email;
        }
      } catch (err) {
        console.error('Błąd aktualizacji ustawień:', err);
        
        // NOWE: Lepsza obsługa błędów z serwera
        let errorMessage = 'Nie udało się zaktualizować ustawień.';
        
        if (err.message.includes('email') && err.message.includes('używany')) {
          errorMessage = 'Ten adres email jest już używany przez innego użytkownika.';
        } else if (err.message.includes('username') && err.message.includes('zajęta')) {
          errorMessage = 'Ta nazwa użytkownika jest już zajęta. Wybierz inną.';
        } else if (err.message.includes('nieprawidłowe')) {
          errorMessage = 'Aktualne hasło jest nieprawidłowe.';
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
        isSaving.value = false;
      }
    };

    // Funkcja anulowania zmian
    const cancelChanges = () => {
      // Sprawdź czy są jakieś niezapisane zmiany
      const hasChanges = 
        user.username !== originalUserData.value.username ||
        user.firstName !== originalUserData.value.firstName ||
        user.lastName !== originalUserData.value.lastName ||
        user.email !== originalUserData.value.email ||
        user.phone !== originalUserData.value.phone ||
        (currentUserId.value && user.isActive !== originalUserData.value.isActive) ||
        passwordData.newPassword.length > 0 ||
        passwordData.confirmPassword.length > 0;

      if (hasChanges) {
        // Jeśli są zmiany, pokaż modal potwierdzenia
        showStatus({
          type: 'warning',
          title: 'Niezapisane zmiany',
          message: 'Masz niezapisane zmiany. Czy na pewno chcesz anulować? Wszystkie zmiany zostaną utracone.',
          buttonText: 'Tak, anuluj edycję',
          cancelText: 'Nie, kontynuuj edycję',
          showCancelButton: true,
          autoClose: false,
          onClose: () => {
            // To się wykona gdy kliknie główny przycisk "Tak, anuluj edycję"
            resetToOriginalData();
            goBack();
          },
          onCancel: () => {
            // To się wykona gdy kliknie przycisk "Nie, kontynuuj edycję"
            console.log('Użytkownik kontynuuje edycję');
          }
        });
      } else {
        // Jeśli nie ma zmian, po prostu wróć
        goBack();
      }
    };

    // Funkcja resetowania danych do oryginalnych wartości
    const resetToOriginalData = () => {
      user.username = originalUserData.value.username || '';
      user.firstName = originalUserData.value.firstName || '';
      user.lastName = originalUserData.value.lastName || '';
      user.email = originalUserData.value.email || '';
      user.phone = originalUserData.value.phone || '';
      user.isActive = originalUserData.value.isActive !== undefined ? originalUserData.value.isActive : true;
      
      // Wyczyść dane hasła
      passwordData.newPassword = '';
      passwordData.confirmPassword = '';
      
      // Wyczyść komunikaty
      error.value = null;
      successMessage.value = null;
    };

    // Funkcja powrotu do poprzedniej strony
    const goBack = () => {
      // if (currentUserId.value) {
        // Jeśli edytujemy konkretnego użytkownika, wróć do panelu admin
        router.push('/adminpanel');
      // } else {
      //   // Jeśli edytujemy własne dane, wróć do poprzedniej strony
      //   router.back();
      // }
    };

    // Przyciski dla modalu
    const handleModalConfirm = () => {
      // Przycisk "Tak, anuluj edycję" - wywołuje onClose
        modalConfig.value.onClose(); // resetToOriginalData() + goBack()
    };

    const handleModalCancel = () => {
      // Przycisk "Nie, kontynuuj edycję" - wywołuje onCancel
        modalConfig.value.onCancel(); // hideModal()
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
      isMainAdmin,
      isChangingToAdmin,
      isAdminUsernameAvailable,
      checkAdminAvailability,
      loadUserData,
      updateSettings,
      cancelChanges,
      handleModalConfirm, 
      handleModalCancel,
      showModal,
      modalConfig,
      hideModal,
      showNewPassword,
      showConfirmPassword
    };
  }
};
</script>