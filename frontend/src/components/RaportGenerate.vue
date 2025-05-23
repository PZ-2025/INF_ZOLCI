<template>
  <div class="bg-background min-h-screen p-8 text-text">
    <h1 class="text-3xl text-left font-bold text-primary mb-6">
      Generowanie Raportów
    </h1>

    <form @submit.prevent="generateReport" class="bg-surface p-6 rounded-lg shadow-md border border-gray-200 w-full max-w-3xl mx-auto">
      <div v-if="successMessage" class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4">
        {{ successMessage }}
        <div v-if="generatedReportId" class="mt-2">
          <button @click="downloadLastReport" class="bg-primary text-white px-4 py-1 rounded-md">
            Pobierz raport
          </button>
        </div>
      </div>

      <div class="flex items-center gap-4 mb-4">
        <label for="reportType" class="font-semibold w-32 shrink-0">Wybierz typ raportu:</label>
        <select
            id="reportType"
            v-model="reportType"
            @change="handleReportTypeChange"
            class="flex-1 px-4 py-2 border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-primary"
            required
        >
          <option value="employee_load">Raport obciążenia pracownika</option>
          <option value="construction_progress">Raport postępu prac na budowie</option>
          <option value="team_efficiency">Raport efektywności zespołu</option>
        </select>
      </div>

      <div class="mb-4">
        <div class="flex items-center gap-4 mb-3">
          <label for="dateFrom" class="font-semibold w-32 shrink-0">Data początkowa:</label>
          <div class="datepicker-container flex-1">
            <Datepicker
              v-model="dateFrom"
              :input-class="datepickerInputClass"
              :format="'yyyy-MM-dd'"
              :id="'dateFrom'"
              :calendar-class="'datepicker-calendar'"
              required
            />
          </div>
        </div>
        <div class="flex items-center gap-4">
          <label for="dateTo" class="font-semibold w-32 shrink-0">Data końcowa:</label>
          <div class="datepicker-container flex-1">
            <Datepicker
              v-model="dateTo"
              :input-class="datepickerInputClass"
              :format="'yyyy-MM-dd'"
              :id="'dateTo'"
              :calendar-class="'datepicker-calendar'"
              required
            />
          </div>
        </div>
      </div>

      <div v-if="reportType === 'construction_progress'" class="flex items-center gap-4 mb-4">
        <label for="teamId" class="font-semibold w-32 shrink-0">Zespół:</label>
        <select
            id="teamId"
            v-model="teamId"
            class="flex-1 px-4 py-2 border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-primary"
            required
        >
          <option disabled value="">Wybierz zespół</option>
          <option v-for="team in teams" :key="team.id" :value="team.id">
            {{ team.name }}
          </option>
        </select>
      </div>

      <div v-if="reportType === 'employee_load'" class="flex items-center gap-4 mb-4">
        <label for="targetUserId" class="font-semibold w-32 shrink-0">Użytkownik (opcjonalnie):</label>
        <select
            id="targetUserId"
            v-model="targetUserId"
            class="flex-1 px-4 py-2 border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-primary"
        >
          <option value="">Wszyscy użytkownicy</option>
          <option v-for="user in users" :key="user.id" :value="user.id">
            {{ user.firstName }} {{ user.lastName }}
          </option>
        </select>
      </div>

      <div class="pt-2">
        <button
            type="submit"
            class="w-full bg-primary hover:bg-secondary text-white font-bold py-2 rounded-lg transition"
            :disabled="loading"
        >
          <span v-if="loading">Generowanie...</span>
          <span v-else>Generuj Raport</span>
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

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import teamService from '../services/teamService';
import userService from '../services/userService';
import pdfReportService from '../services/pdfReportService';
import { authState } from '../../router/router.js';
import StatusModal from './StatusModal.vue';
import { useStatusModal } from '../composables/useStatusModal';
import Datepicker from 'vue3-datepicker';

const router = useRouter();
const { showModal, modalConfig, showStatus, hideModal } = useStatusModal();

// Klasa CSS dla inputów datepicker - dopasowana wysokość do innych inputów
const datepickerInputClass = computed(() => 
  'w-full px-4 py-2 border border-gray-300 rounded-lg bg-white text-sm text-black focus:ring-2 focus:ring-primary focus:outline-none datepicker-input-custom'
);

// Zmienione nazwy zmiennych, aby dokładnie pasowały do parametrów API
const reportType = ref('employee_load');
// Zmieniamy typ na Date
const dateFrom = ref(null);
const dateTo = ref(null);
const teamId = ref('');
const targetUserId = ref('');

const teams = ref([]);
const users = ref([]);
const loading = ref(false);
const successMessage = ref('');
const generatedReportId = ref(null);

const handleReportTypeChange = () => {
  // reset selected values when report type changes
  teamId.value = '';
  targetUserId.value = '';
};

onMounted(async () => {
  // Ustaw domyślne daty (ostatni miesiąc)
  const today = new Date();
  dateTo.value = today;

  const lastMonth = new Date();
  lastMonth.setMonth(lastMonth.getMonth() - 1);
  dateFrom.value = lastMonth;

  try {
    // Pobierz zespoły
    const teamsData = await teamService.getAllTeams();
    teams.value = teamsData;

    // Pobierz użytkowników
    const usersData = await userService.getActiveUsers();
    users.value = usersData;
  } catch (error) {
    console.error('Błąd podczas pobierania danych:', error);
    showStatus({
      type: 'error',
      title: 'Błąd',
      message: 'Nie udało się załadować danych. Spróbuj odświeżyć stronę.',
      buttonText: 'Zamknij'
    });
  }
});

const generateReport = async () => {
  // Walidacja
  if (!dateFrom.value || !dateTo.value) {
    showStatus({
      type: 'error',
      title: 'Błąd',
      message: 'Proszę wybrać zakres dat',
      buttonText: 'OK'
    });
    return;
  }

  // Sprawdź czy data końcowa nie jest wcześniejsza niż początkowa
  if (dateTo.value < dateFrom.value) {
    showStatus({
      type: 'error',
      title: 'Błąd',
      message: 'Data końcowa nie może być wcześniejsza niż początkowa',
      buttonText: 'OK'
    });
    return;
  }

  // Dodatkowa walidacja dla typów raportów
  if (reportType.value === 'construction_progress' && !teamId.value) {
    showStatus({
      type: 'error',
      title: 'Błąd',
      message: 'Proszę wybrać zespół dla raportu postępu prac',
      buttonText: 'OK'
    });
    return;
  }

  loading.value = true;
  successMessage.value = '';
  generatedReportId.value = null;

  try {
    // Potrzebujemy ID zalogowanego użytkownika
    const userId = authState.user?.id;
    if (!userId) {
      throw new Error('Brak zalogowanego użytkownika');
    }

    // Formatowanie dat do yyyy-MM-dd
    const formatDate = (date) => {
      if (!date) return '';
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    };

    let response;

    // Wywołaj odpowiednią metodę w zależności od typu raportu
    switch (reportType.value) {
      case 'construction_progress':
        response = await pdfReportService.generateConstructionProgressReport(
            teamId.value,
            formatDate(dateFrom.value),
            formatDate(dateTo.value),
            userId
        );
        break;
      case 'employee_load':
        response = await pdfReportService.generateEmployeeLoadReport(
            targetUserId.value || null,
            formatDate(dateFrom.value),
            formatDate(dateTo.value),
            userId
        );
        break;
      case 'team_efficiency':
        response = await pdfReportService.generateTeamEfficiencyReport(
            formatDate(dateFrom.value),
            formatDate(dateTo.value),
            userId
        );
        break;
    }

    console.log('Raport wygenerowany:', response);

    if (response && response.reportId) {
      generatedReportId.value = response.reportId;
      successMessage.value = `Raport został wygenerowany pomyślnie! Nazwa pliku: ${response.fileName || 'Raport PDF'}`;
    } else {
      successMessage.value = 'Raport został wygenerowany pomyślnie! Możesz go znaleźć w historii raportów.';
    }

  } catch (error) {
    console.error('Błąd podczas generowania raportu:', error);
    showStatus({
      type: 'error',
      title: 'Błąd',
      message: `Nie udało się wygenerować raportu: ${error.message || 'Nieznany błąd'}`,
      buttonText: 'Zamknij'
    });
  } finally {
    loading.value = false;
  }
};

const downloadLastReport = async () => {
  if (!generatedReportId.value) return;

  try {
    const downloadUrl = await pdfReportService.downloadReport(generatedReportId.value);
    window.open(downloadUrl, '_blank');
  } catch (error) {
    console.error('Błąd podczas pobierania raportu:', error);
    showStatus({
      type: 'error',
      title: 'Błąd',
      message: `Nie udało się pobrać raportu: ${error.message || 'Nieznany błąd'}`,
      buttonText: 'Zamknij'
    });
  }
};
</script>