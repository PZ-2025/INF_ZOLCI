<template>
  <div class="bg-background min-h-screen p-8 text-text">
    <div v-if="loading" class="flex justify-center items-center h-64">
      <p class="text-primary text-xl">Ładowanie raportu...</p>
    </div>

    <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6">
      <p>{{ error }}</p>
      <button @click="fetchReportDetails" class="mt-2 bg-primary text-white px-4 py-1 rounded-md">
        Spróbuj ponownie
      </button>
    </div>

    <div v-else class="bg-surface p-6 rounded-lg shadow-md border border-gray-200 max-w-6xl mx-auto">
      <div class="flex justify-between items-center mb-6">
        <h1 class="text-3xl font-bold text-primary">{{ getReportName(report) }}</h1>
        <div class="flex gap-3">
          <button 
            @click="togglePreview" 
            class="bg-secondary hover:bg-primary text-white px-4 py-2 rounded-lg transition"
          >
            {{ showPreview ? 'Ukryj podgląd' : 'Pokaż podgląd' }}
          </button>
          <button 
            @click="downloadReport" 
            class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-lg transition"
            :disabled="downloading"
          >
            <span v-if="downloading" class="flex items-center">
              <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Pobieranie...
            </span>
            <span v-else>Pobierz PDF</span>
          </button>
        </div>
      </div>

      <div class="mb-6">
        <h2 class="text-xl font-semibold mb-2">Szczegóły raportu</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <p><span class="font-medium">Typ raportu:</span> {{ getReportTypeName(report.type) }}</p>
            <p><span class="font-medium">Data utworzenia:</span> {{ formatDate(report.createdAt) }}</p>
          </div>
          <div>
            <p><span class="font-medium">Nazwa pliku:</span> {{ report.fileName || "Raport PDF" }}</p>
            <p><span class="font-medium">ID raportu:</span> {{ report.id }}</p>
          </div>
        </div>
      </div>

      <div class="mb-6">
        <h2 class="text-xl font-semibold mb-2">Opis raportu</h2>
        <p>{{ getReportDescription(report) }}</p>
      </div>

      <!-- Podgląd raportu -->
      <div v-if="showPreview" class="mb-6">
        <h2 class="text-xl font-semibold mb-2">Podgląd raportu</h2>
        <div v-if="previewLoading" class="bg-gray-100 p-8 rounded-lg text-center">
          <div class="flex items-center justify-center">
            <svg class="animate-spin h-8 w-8 text-primary mr-3" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <p class="text-lg">Ładowanie podglądu raportu...</p>
          </div>
        </div>
        <div v-else-if="previewError" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 rounded-lg">
          <p>{{ previewError }}</p>
          <button @click="loadPreview" class="mt-2 bg-primary text-white px-4 py-1 rounded-md">
            Spróbuj ponownie
          </button>
        </div>
        <div v-else-if="reportBlobUrl" class="border border-gray-300 rounded-lg overflow-hidden" style="height: 600px;">
          <iframe 
            :src="reportBlobUrl" 
            class="w-full h-full"
            @load="onIframeLoad"
            @error="onIframeError"
          ></iframe>
        </div>
        <div v-else class="bg-gray-100 p-4 rounded-lg text-center">
          <p>Kliknij "Pokaż podgląd" aby załadować raport.</p>
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
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute } from 'vue-router';
import reportService from '../services/reportService';
import pdfReportService from '../services/pdfReportService';
import StatusModal from './StatusModal.vue';
import { useStatusModal } from '../composables/useStatusModal';

export default {
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
    const { showModal, modalConfig, showStatus, hideModal } = useStatusModal();

    const report = ref({});
    const loading = ref(true);
    const error = ref(null);
    const downloading = ref(false);
    
    // Podgląd
    const showPreview = ref(true); // Zmiana: domyślnie widoczny
    const previewLoading = ref(false);
    const previewError = ref(null);
    const reportBlobUrl = ref(null);

    const fetchReportDetails = async () => {
      loading.value = true;
      error.value = null;

      try {
        const reportId = props.id || route.params.id;
        if (!reportId) {
          throw new Error('ID raportu jest wymagane');
        }

        console.log(`📋 Pobieranie szczegółów raportu ID: ${reportId}`);
        const reportData = await reportService.getReportById(reportId);
        report.value = reportData;
        
        console.log(`✅ Pobrano szczegóły raportu:`, reportData);

      } catch (err) {
        console.error('❌ Błąd podczas pobierania szczegółów raportu:', err);
        error.value = `Nie udało się pobrać szczegółów raportu: ${err.message || 'Nieznany błąd'}`;

        // Przykładowe dane w przypadku błędu
        report.value = {
          id: props.id || route.params.id,
          fileName: "report.pdf",
          type: "construction_progress",
          createdAt: new Date().toISOString()
        };
      } finally {
        loading.value = false;
      }
    };

    const loadPreview = async () => {
      const reportId = props.id || route.params.id;
      if (!reportId) {
        previewError.value = 'Brak ID raportu';
        return;
      }

      previewLoading.value = true;
      previewError.value = null;

      try {
        console.log(`🔍 Ładowanie podglądu raportu ID: ${reportId}`);
        
        // Pobierz blob raportu
        const blob = await pdfReportService.downloadReportBlob(reportId);
        
        // Zwolnij poprzedni URL jeśli istnieje
        if (reportBlobUrl.value) {
          window.URL.revokeObjectURL(reportBlobUrl.value);
        }
        
        // Utwórz nowy URL dla blob
        reportBlobUrl.value = window.URL.createObjectURL(blob);
        
        console.log(`✅ Podgląd raportu załadowany, URL: ${reportBlobUrl.value}`);
        
      } catch (err) {
        console.error('❌ Błąd podczas ładowania podglądu:', err);
        previewError.value = `Nie udało się załadować podglądu: ${err.message || 'Nieznany błąd'}`;
      } finally {
        previewLoading.value = false;
      }
    };

    const togglePreview = async () => {
      showPreview.value = !showPreview.value;
      
      if (showPreview.value && !reportBlobUrl.value && !previewLoading.value) {
        await loadPreview();
      }
    };

    const downloadReport = async () => {
      downloading.value = true;
      
      try {
        const reportId = props.id || route.params.id;
        if (!reportId) {
          throw new Error('ID raportu jest wymagane');
        }

        console.log(`💾 Pobieranie raportu ID: ${reportId}`);
        
        // Użyj funkcji do pobierania i zapisywania
        const fileName = report.value.fileName || `raport_${reportId}.pdf`;
        await pdfReportService.downloadAndSaveReport(reportId, fileName);
        
        console.log(`✅ Raport ${fileName} został pobrany`);

      } catch (err) {
        console.error('❌ Błąd podczas pobierania raportu:', err);
        showStatus({
          type: 'error',
          title: 'Błąd',
          message: `Nie udało się pobrać raportu: ${err.message || 'Nieznany błąd'}`,
          buttonText: 'Zamknij'
        });
      } finally {
        downloading.value = false;
      }
    };

    const onIframeLoad = () => {
      console.log(`🖼️ Iframe z podglądem PDF załadowany pomyślnie`);
    };

    const onIframeError = () => {
      console.error('❌ Błąd podczas ładowania iframe');
      previewError.value = 'Nie udało się wyświetlić podglądu PDF';
    };

    // Pobierz nazwę raportu
    const getReportName = (report) => {
      if (report.name) return report.name;

      const typeName = getReportTypeName(report.type);
      const date = formatDate(report.createdAt).split(',')[0];

      return `${typeName} - ${date}`;
    };

    // Pobierz nazwę typu raportu
    const getReportTypeName = (type) => {
      const reportTypeNames = {
        'employee_load': 'Raport obciążenia pracownika',
        'construction_progress': 'Raport postępu prac na budowie',
        'team_efficiency': 'Raport efektywności zespołu'
      };

      return reportTypeNames[type] || 'Raport';
    };

    // Pobierz opis raportu
    const getReportDescription = (report) => {
      if (report.description) return report.description;

      const reportDescriptions = {
        'employee_load': 'Ten raport zawiera szczegółowe informacje o obciążeniu pracowników zadaniami w wybranym okresie czasu. Obejmuje liczbę przypisanych zadań, czas spędzony na poszczególnych zadaniach oraz poziom wykorzystania zasobów.',
        'construction_progress': 'Ten raport zawiera informacje o postępie prac budowlanych dla wybranego zespołu. Obejmuje zakończone etapy, procent wykonania, opóźnienia oraz prognozy dotyczące terminów zakończenia.',
        'team_efficiency': 'Ten raport zawiera analizę efektywności zespołów. Obejmuje liczbę zakończonych zadań, średni czas wykonania zadania, wydajność zespołu oraz porównanie z poprzednimi okresami.'
      };

      return reportDescriptions[report.type] || 'Ten raport zawiera szczegółowe informacje dotyczące wybranego zakresu danych i kryteriów.';
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

    // Cleanup function
    onUnmounted(() => {
      if (reportBlobUrl.value) {
        console.log(`🧹 Zwalnianie URL blob: ${reportBlobUrl.value}`);
        window.URL.revokeObjectURL(reportBlobUrl.value);
      }
    });

    onMounted(async () => {
      await fetchReportDetails();
      // Automatycznie załaduj podgląd po pobraniu szczegółów
      await loadPreview();
    });

    return {
      report,
      loading,
      error,
      downloading,
      showPreview,
      previewLoading,
      previewError,
      reportBlobUrl,
      fetchReportDetails,
      loadPreview,
      togglePreview,
      downloadReport,
      onIframeLoad,
      onIframeError,
      getReportName,
      getReportTypeName,
      getReportDescription,
      formatDate,
      showModal,
      modalConfig,
      hideModal
    };
  }
};
</script>

<style scoped>
.disabled\:bg-gray-400:disabled {
  background-color: #9ca3af;
}

.disabled\:cursor-not-allowed:disabled {
  cursor: not-allowed;
}
</style>