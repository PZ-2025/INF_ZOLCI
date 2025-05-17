<template>
  <div class="bg-background text-text min-h-screen p-6">
    <h1 class="text-3xl text-left font-bold text-primary mb-6">Historia Raportów</h1>

    <div v-if="loading" class="flex justify-center items-center h-64">
      <p class="text-primary text-xl">Ładowanie raportów...</p>
    </div>

    <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6">
      <p>{{ error }}</p>
      <button @click="fetchReports" class="mt-2 bg-primary text-white px-4 py-1 rounded-md">
        Spróbuj ponownie
      </button>
    </div>

    <div v-else>
      <div class="flex flex-wrap gap-4 justify-between items-center mb-6">
        <div class="flex items-center">
          <label for="reportDate" class="mr-2 font-medium">Data Raportu:</label>
          <input type="date" v-model="selectedDate" class="p-2 border border-gray-300 rounded-md bg-white text-text focus:ring-2 focus:ring-primary" />
        </div>

        <div class="flex items-center">
          <label for="reportType" class="mr-2 font-medium">Typ Raportu:</label>
          <select v-model="selectedType" class="p-2 border border-gray-300 rounded-md bg-white text-text focus:ring-2 focus:ring-primary">
            <option value="">Wszystkie</option>
            <option value="employee_load">Raport obciążenia pracownika</option>
            <option value="construction_progress">Raport postępu prac na budowie</option>
            <option value="team_efficiency">Raport efektywności zespołu</option>
          </select>
        </div>

        <button @click="applyFilters" class="bg-primary text-white p-2 rounded-md hover:bg-secondary transition">Filtruj</button>
      </div>

      <div v-if="filteredReports.length === 0" class="text-center py-8 text-muted">
        <p>Brak raportów spełniających kryteria filtrowania</p>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="report in filteredReports" :key="report.id" class="bg-surface border border-gray-200 p-4 rounded-lg shadow hover:scale-105 transition">
          <div>
            <h3 class="text-xl font-semibold text-primary">{{ getReportName(report) }}</h3>
            <p class="text-muted">{{ getReportDescription(report) }}</p>
            <p class="text-xs text-muted mt-1">Utworzony: {{ formatDate(report.createdAt) }}</p>
            <p class="text-xs text-muted">Plik: {{ report.fileName || "Raport PDF" }}</p>
          </div>
          <div class="mt-4 flex space-x-3">
            <button @click="openReportDetails(report)" class="border border-primary text-primary px-4 py-2 rounded-md hover:bg-primary hover:text-white transition">
              Szczegóły
            </button>
          </div>
        </div>
      </div>
    </div>

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
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import reportService from '../services/reportService';
import pdfReportService from '../services/pdfReportService';
import StatusModal from './StatusModal.vue';
import { useStatusModal } from '../composables/useStatusModal';

export default {
  components: {
    StatusModal
  },
  setup() {
    const router = useRouter();
    const { showModal, modalConfig, showStatus, hideModal } = useStatusModal();

    const reports = ref([]);
    const loading = ref(true);
    const error = ref(null);
    const selectedDate = ref('');
    const selectedType = ref('');

    // Funkcja pobierająca raporty z API
    const fetchReports = async () => {
      loading.value = true;
      error.value = null;

      try {
        const response = await reportService.getAllReports();
        console.log('Pobrane raporty:', response);
        reports.value = response;
      } catch (err) {
        console.error('Błąd podczas pobierania raportów:', err);
        error.value = `Nie udało się pobrać raportów: ${err.message || 'Nieznany błąd'}`;

        // Przykładowe dane, jeśli API zwróci błąd
        reports.value = [
          {
            id: 1,
            fileName: "employee_load_report_2025-05-01.pdf",
            filePath: "/reports/employee_load_report_2025-05-01.pdf",
            createdAt: "2025-05-01T10:30:00",
            type: "employee_load"
          },
          {
            id: 2,
            fileName: "construction_progress_report_2025-04-28.pdf",
            filePath: "/reports/construction_progress_report_2025-04-28.pdf",
            createdAt: "2025-04-28T15:45:00",
            type: "construction_progress"
          },
          {
            id: 3,
            fileName: "team_efficiency_report_2025-04-15.pdf",
            filePath: "/reports/team_efficiency_report_2025-04-15.pdf",
            createdAt: "2025-04-15T09:15:00",
            type: "team_efficiency"
          }
        ];
      } finally {
        loading.value = false;
      }
    };

    // Raporty przefiltrowane
    const filteredReports = computed(() => {
      return reports.value.filter(report => {
        // Filtrowanie po typie raportu
        const typeMatch = !selectedType.value || report.type === selectedType.value;

        // Filtrowanie po dacie (jeśli podana)
        let dateMatch = true;
        if (selectedDate.value && report.createdAt) {
          const reportDate = new Date(report.createdAt).toISOString().split('T')[0];
          dateMatch = reportDate === selectedDate.value;
        }

        return typeMatch && dateMatch;
      });
    });

    // Zastosowanie filtrów
    const applyFilters = () => {
      console.log('Stosowanie filtrów:', { type: selectedType.value, date: selectedDate.value });
      // Filtrowanie odbywa się automatycznie przez computed property
    };

    // Pobranie raportu
    const downloadReport = async (report) => {
      try {
        const downloadUrl = await pdfReportService.downloadReport(report.id);

        // Otwórz link w nowym oknie lub pobierz plik
        window.open(downloadUrl, '_blank');
      } catch (err) {
        console.error('Błąd podczas pobierania raportu:', err);
        showStatus({
          type: 'error',
          title: 'Błąd',
          message: `Nie udało się pobrać raportu: ${err.message || 'Nieznany błąd'}`,
          buttonText: 'Zamknij'
        });
      }
    };

    // Przejście do szczegółów raportu
    const openReportDetails = (report) => {
      router.push({ name: 'raportView', params: { id: report.id } });
    };

    // Formatowanie daty
    const formatDate = (dateString) => {
      if (!dateString) return 'Nieznana data';

      try {
        const date = new Date(dateString);
        return date.toLocaleString('pl-PL', {
          year: 'numeric',
          month: 'long',
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        });
      } catch (error) {
        return dateString;
      }
    };

    // Pobierz nazwę raportu
    const getReportName = (report) => {
      if (report.name) return report.name;

      // Jeśli brak nazwy, generuj ją na podstawie typu i daty
      const reportTypeNames = {
        'employee_load': 'Raport obciążenia pracownika',
        'construction_progress': 'Raport postępu prac',
        'team_efficiency': 'Raport efektywności zespołu'
      };

      const typeName = reportTypeNames[report.type] || 'Raport';
      const date = formatDate(report.createdAt).split(',')[0]; // Tylko data bez godziny

      return `${typeName} - ${date}`;
    };

    // Pobierz opis raportu
    const getReportDescription = (report) => {
      if (report.description) return report.description;

      // Jeśli brak opisu, generuj go na podstawie typu i daty
      const reportDescriptions = {
        'employee_load': 'Podsumowanie obciążenia pracowników i przypisanych zadań',
        'construction_progress': 'Analiza postępu prac budowlanych i terminów realizacji',
        'team_efficiency': 'Ocena efektywności zespołów i ich wydajności'
      };

      return reportDescriptions[report.type] || 'Szczegóły w raporcie';
    };

    // Inicjalizacja
    onMounted(() => {
      fetchReports();
    });

    return {
      reports,
      loading,
      error,
      selectedDate,
      selectedType,
      filteredReports,
      fetchReports,
      applyFilters,
      downloadReport,
      openReportDetails,
      formatDate,
      getReportName,
      getReportDescription,
      showModal,
      modalConfig,
      hideModal
    };
  }
};
</script>