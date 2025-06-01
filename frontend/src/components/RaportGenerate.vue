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
              :inputClass="datepickerInputClass"
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
              :inputClass="datepickerInputClass"
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
            class="w-full bg-primary hover:bg-secondary text-white font-bold py-2 rounded-lg transition disabled:bg-gray-400 disabled:cursor-not-allowed"
            :disabled="loading || isGenerating"
        >
          <span v-if="loading || isGenerating" class="flex items-center justify-center">
            <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            Generowanie...
          </span>
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

const isGenerating = ref(false);
const lastGeneratedParams = ref(null);
const generationId = ref(0); // Unikalny ID dla każdego generowania
const generatedFileName = ref('');

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
  const currentGenerationId = ++generationId.value;
  console.log(`🚀 Rozpoczynanie generowania raportu #${currentGenerationId}`);

  // Walidacja
  if (!dateFrom.value || !dateTo.value) {
    console.log(`❌ Błąd walidacji dat #${currentGenerationId}`);
    showStatus({
      type: 'error',
      title: 'Błąd',
      message: 'Proszę wybrać zakres dat',
      buttonText: 'OK'
    });
    return;
  }

  if (dateTo.value < dateFrom.value) {
    console.log(`❌ Błąd walidacji zakresu dat #${currentGenerationId}`);
    showStatus({
      type: 'error',
      title: 'Błąd',
      message: 'Data końcowa nie może być wcześniejsza niż początkowa',
      buttonText: 'OK'
    });
    return;
  }

  if (reportType.value === 'construction_progress' && !teamId.value) {
    console.log(`❌ Błąd walidacji zespołu #${currentGenerationId}`);
    showStatus({
      type: 'error',
      title: 'Błąd',
      message: 'Proszę wybrać zespół dla raportu postępu prac',
      buttonText: 'OK'
    });
    return;
  }

  // KLUCZOWE ZABEZPIECZENIE
  if (isGenerating.value) {
    console.log(`⏸️ Raport jest już generowany, ignoruję wywołanie #${currentGenerationId}`);
    return;
  }

  console.log(`🔒 Ustawianie flagi isGenerating na true #${currentGenerationId}`);
  isGenerating.value = true;
  loading.value = true;
  successMessage.value = '';
  generatedReportId.value = null;

  // Formatowanie dat
  const formatDate = (date) => {
    if (!date) return '';
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  const currentParams = {
    type: reportType.value,
    dateFrom: formatDate(dateFrom.value),
    dateTo: formatDate(dateTo.value),
    teamId: teamId.value,
    targetUserId: targetUserId.value,
    timestamp: Date.now()
  };

  console.log(`📋 Parametry raportu #${currentGenerationId}:`, currentParams);

  // Sprawdź duplikaty na podstawie parametrów
  if (lastGeneratedParams.value) {
    const timeDiff = currentParams.timestamp - lastGeneratedParams.value.timestamp;
    const paramsEqual = JSON.stringify({...currentParams, timestamp: undefined}) === 
                       JSON.stringify({...lastGeneratedParams.value, timestamp: undefined});
    
    console.log(`🔍 Sprawdzanie duplikatów #${currentGenerationId}: timeDiff=${timeDiff}ms, paramsEqual=${paramsEqual}`);
    
    if (paramsEqual && timeDiff < 10000) {
      console.log(`🚫 Ignoruję duplikat #${currentGenerationId}`);
      isGenerating.value = false;
      loading.value = false;
      return;
    }
  }

  try {
    const userId = authState.user?.id;
    if (!userId) {
      throw new Error('Brak zalogowanego użytkownika');
    }

    console.log(`📞 Wywołanie API dla raportu #${currentGenerationId}, userId: ${userId}`);
    
    let response;

    switch (reportType.value) {
      case 'construction_progress':
        console.log(`🏗️ Generowanie raportu postępu budowy #${currentGenerationId}`);
        response = await pdfReportService.generateConstructionProgressReport(
            teamId.value,
            currentParams.dateFrom,
            currentParams.dateTo,
            userId
        );
        break;
      case 'employee_load':
        console.log(`👥 Generowanie raportu obciążenia pracownika #${currentGenerationId}`);
        response = await pdfReportService.generateEmployeeLoadReport(
            targetUserId.value || null,
            currentParams.dateFrom,
            currentParams.dateTo,
            userId
        );
        break;
      case 'team_efficiency':
        console.log(`📊 Generowanie raportu efektywności zespołu #${currentGenerationId}`);
        response = await pdfReportService.generateTeamEfficiencyReport(
            currentParams.dateFrom,
            currentParams.dateTo,
            userId
        );
        break;
    }

    console.log(`✅ Odpowiedź z API #${currentGenerationId}:`, response);

    if (response && response.reportId) {
      generatedReportId.value = response.reportId;
      generatedFileName.value = response.fileName || `${reportType.value}_${formatDate(dateFrom.value)}_${formatDate(dateTo.value)}.pdf`;
      successMessage.value = `Raport został wygenerowany pomyślnie! Nazwa pliku: ${generatedFileName.value}`;
      
      lastGeneratedParams.value = currentParams;
      console.log(`💾 Zapisano parametry ostatniego raportu #${currentGenerationId}:`, lastGeneratedParams.value);
    } else {
      successMessage.value = 'Raport został wygenerowany pomyślnie! Możesz go znaleźć w historii raportów.';
      console.log(`⚠️ Brak reportId w odpowiedzi #${currentGenerationId}`);
    }

  } catch (error) {
    console.error(`❌ Błąd podczas generowania raportu #${currentGenerationId}:`, error);
    showStatus({
      type: 'error',
      title: 'Błąd',
      message: `Nie udało się wygenerować raportu: ${error.message || 'Nieznany błąd'}`,
      buttonText: 'Zamknij'
    });
  } finally {
    console.log(`🔓 Zwalnianie flagi isGenerating #${currentGenerationId}`);
    loading.value = false;
    isGenerating.value = false;
  }
};

const downloadLastReport = async () => {
  console.log(`📥 Próba pobrania raportu. ID: ${generatedReportId.value}, Nazwa: ${generatedFileName.value}`);
  
  if (!generatedReportId.value) {
    console.log(`❌ Brak ID raportu do pobrania`);
    showStatus({
      type: 'error',
      title: 'Błąd',
      message: 'Brak raportu do pobrania',
      buttonText: 'OK'
    });
    return;
  }

  try {
    console.log(`🔄 Rozpoczynanie pobierania raportu ID: ${generatedReportId.value}`);
    
    // Użyj funkcji z pdfReportService do pobierania i zapisywania
    const fileName = generatedFileName.value || `raport_${generatedReportId.value}.pdf`;
    await pdfReportService.downloadAndSaveReport(generatedReportId.value, fileName);
    
    console.log(`✅ Raport ${fileName} został pobrany pomyślnie`);
    
  } catch (error) {
    console.error('❌ Błąd podczas pobierania raportu:', error);
    showStatus({
      type: 'error',
      title: 'Błąd',
      message: `Nie udało się pobrać raportu: ${error.message || 'Nieznany błąd'}`,
      buttonText: 'Zamknij'
    });
  }
};
</script>

<style scoped>
.transition {
  transition: all 0.2s ease-in-out;
}
/* Podstawowe style dla vue3-datepicker */
:deep(.datepicker input) {
  width: 100% !important;
  background-color: white !important;
  color: black !important;
}
</style>