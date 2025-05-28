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

    <div v-else class="bg-surface p-6 rounded-lg shadow-md border border-gray-200 max-w-4xl mx-auto">
      <div class="flex justify-between items-center mb-6">
        <h1 class="text-3xl font-bold text-primary">{{ getReportName(report) }}</h1>
        <button @click="downloadReport" class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-lg transition">
          Pobierz PDF
        </button>
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

      <div v-if="reportUrl" class="border border-gray-300 rounded-lg overflow-hidden h-96">
        <iframe :src="reportUrl" class="w-full h-full"></iframe>
      </div>
      <div v-else class="bg-gray-100 p-4 rounded-lg text-center">
        <p>Podgląd raportu niedostępny. Kliknij przycisk "Pobierz PDF" aby pobrać raport.</p>
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
import { ref, onMounted } from 'vue';
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
    const reportUrl = ref(null);

    const fetchReportDetails = async () => {
      loading.value = true;
      error.value = null;

      try {
        const reportId = props.id || route.params.id;
        if (!reportId) {
          throw new Error('ID raportu jest wymagane');
        }

        const reportData = await reportService.getReportById(reportId);
        report.value = reportData;

        // Utwórz URL do podglądu raportu
        reportUrl.value = await pdfReportService.downloadReport(reportId);
      } catch (err) {
        console.error('Błąd podczas pobierania szczegółów raportu:', err);
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

    const downloadReport = async () => {
      try {
        const reportId = props.id || route.params.id;
        if (!reportId) {
          throw new Error('ID raportu jest wymagane');
        }

        const downloadUrl = await pdfReportService.downloadReport(reportId);

        // Utwórz ukryty link i kliknij go
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.download = report.value.fileName || `raport_${reportId}.pdf`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
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

    // Pobierz nazwę raportu
    const getReportName = (report) => {
      if (report.name) return report.name;

      // Jeśli brak nazwy, generuj ją na podstawie typu i daty
      const typeName = getReportTypeName(report.type);
      const date = formatDate(report.createdAt).split(',')[0]; // Tylko data bez godziny

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

      // Jeśli brak opisu, generuj go na podstawie typu i daty
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

    onMounted(() => {
      fetchReportDetails();
    });

    return {
      report,
      loading,
      error,
      reportUrl,
      fetchReportDetails,
      downloadReport,
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