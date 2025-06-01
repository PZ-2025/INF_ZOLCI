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
      <!-- Sekcja filtrów -->
      <div class="bg-surface p-4 rounded-lg shadow-md border border-gray-200 mb-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-semibold">Filtrowanie raportów</h2>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 items-end">
          <div>
            <label for="reportDate" class="block text-sm font-medium mb-1">Data raportu:</label>
            <div class="datepicker-container">
              <Datepicker
                v-model="selectedDate"
                :inputClass="datepickerInputClass"
                :format="'yyyy-MM-dd'"
                :id="'reportDate'"
                placeholder="Wybierz datę..."
                :clear-button="true"
                :close-on-select="true"
              />
            </div>
          </div>

          <div>
            <label for="reportType" class="block text-sm font-medium mb-1">Typ raportu:</label>
            <select 
              id="reportType"
              v-model="selectedType" 
              class="w-full p-2 border border-gray-300 rounded-md bg-white text-text focus:ring-2 focus:ring-primary focus:border-primary"
            >
              <option value="">Wszystkie typy</option>
              <option 
                v-for="typeOption in availableTypes" 
                :key="typeOption.value" 
                :value="typeOption.value"
              >
                {{ typeOption.label }}
              </option>
            </select>
          </div>

          <div>
            <label for="searchText" class="block text-sm font-medium mb-1">Wyszukiwanie:</label>
            <input 
              type="text" 
              id="searchText"
              v-model="searchText"
              placeholder="Nazwa pliku, ID, typ..."
              class="w-full p-2 border border-gray-300 rounded-md bg-white text-text focus:ring-2 focus:ring-primary focus:border-primary" 
            />
          </div>
        </div>

        <div class="mt-4 flex justify-end">
          <button 
            @click="clearFilters" 
            class="bg-gray-500 text-white px-6 py-2 rounded-md hover:bg-gray-600 transition"
            :disabled="!hasActiveFilters"
            :class="{ 'opacity-50 cursor-not-allowed': !hasActiveFilters }"
          >
            Wyczyść filtry
          </button>
        </div>

        <!-- Podsumowanie filtrów -->
        <div v-if="hasActiveFilters" class="mt-4 p-3 bg-blue-50 border-l-4 border-blue-400 text-blue-700">
          <p class="text-sm">
            <strong>Aktywne filtry:</strong>
            <span v-if="selectedDate" class="ml-2">Data: {{ formatFilterDate(selectedDate) }}</span>
            <span v-if="selectedType" class="ml-2">Typ: {{ selectedType }}</span>
            <span v-if="searchText" class="ml-2">Wyszukiwanie: "{{ searchText }}"</span>
          </p>
          <p class="text-sm mt-1">Znaleziono {{ filteredReports.length }} z {{ reports.length }} raportów</p>
        </div>
      </div>

      <div v-if="filteredReports.length === 0" class="text-center py-8 text-muted">
        <div class="bg-gray-100 p-6 rounded-lg">
          <p class="text-lg mb-2">Brak raportów spełniających kryteria</p>
          <p v-if="hasActiveFilters" class="text-sm">Spróbuj zmienić filtry lub wyczyść je aby zobaczyć wszystkie raporty</p>
          <p v-else class="text-sm">Nie znaleziono żadnych raportów w systemie</p>
        </div>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div 
          v-for="report in filteredReports" 
          :key="report.id" 
          class="bg-surface border border-gray-200 p-4 rounded-lg shadow hover:scale-105 transition cursor-pointer flex flex-col h-full"
          @click="openReportDetails(report)"
        >
          <!-- Górna część karty -->
          <div class="flex-grow">
            <h3 class="text-xl font-semibold text-primary">{{ getReportName(report) }}</h3>
            <p class="text-muted text-sm mt-1">{{ getReportDescription(report) }}</p>
            <div class="mt-2 space-y-1 text-xs text-muted">
              <p><strong>Utworzony:</strong> {{ formatDate(report.createdAt) }}</p>
              <p><strong>Plik:</strong> {{ report.fileName || "Raport PDF" }}</p>
              <p><strong>ID:</strong> {{ report.id }}</p>
            </div>
          </div>
          
          <!-- Dolna część - przyciski zawsze na dole -->
          <div class="mt-4 flex space-x-3 flex-shrink-0">
            <button 
              @click.stop="openReportDetails(report)" 
              class="bg-primary text-white px-4 py-2 rounded-md hover:bg-secondary transition flex-1"
            >
              Otwórz raport
            </button>
            <button 
              @click.stop="downloadReport(report)" 
              class="bg-gray-500 text-white px-3 py-2 rounded-md hover:bg-gray-600 transition"
              title="Pobierz PDF"
            >
              📥
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
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import reportService from '../services/reportService';
import pdfReportService from '../services/pdfReportService';
import StatusModal from './StatusModal.vue';
import { useStatusModal } from '../composables/useStatusModal';
import Datepicker from 'vue3-datepicker';

export default {
  components: {
    StatusModal,
    Datepicker
  },
  setup() {
    const router = useRouter();
    const { showModal, modalConfig, showStatus, hideModal } = useStatusModal();

    const reports = ref([]);
    const loading = ref(true);
    const error = ref(null);
    const availableTypes = ref([]);
    
    // Filtry - automatyczne działanie
    const selectedDate = ref(null);
    const selectedType = ref('');
    const searchText = ref('');
    
    // Klasa CSS dla inputów datepicker
    const datepickerInputClass = computed(() => 
      'w-full p-2 border border-gray-300 rounded-md bg-white text-sm text-text focus:ring-2 focus:ring-primary focus:border-primary datepicker-input-custom'
    );

    // Sprawdź czy są aktywne filtry
    const hasActiveFilters = computed(() => {
      return selectedDate.value || selectedType.value || searchText.value;
    });

    // Formatuj datę z datepicker do formatu YYYY-MM-DD dla porównań
    const formatDateForComparison = (date) => {
      if (!date) return '';
      
      try {
        if (date instanceof Date) {
          const year = date.getFullYear();
          const month = String(date.getMonth() + 1).padStart(2, '0');
          const day = String(date.getDate()).padStart(2, '0');
          return `${year}-${month}-${day}`;
        }
        return '';
      } catch (error) {
        console.error('Błąd formatowania daty:', error);
        return '';
      }
    };

    // Funkcja pobierająca raporty z API
    const fetchReports = async () => {
      loading.value = true;
      error.value = null;

      try {
        console.log('🔄 Pobieranie raportów z API...');
        const response = await reportService.getAllReports();
        console.log('✅ Pobrane raporty:', response);
        
        // Debug: pokaż wszystkie unikalne typy raportów z kolumny 'name'
        const uniqueTypes = [...new Set(response.map(report => report.name).filter(Boolean))];
        console.log('🔍 Unikalne typy raportów w bazie (kolumna name):', uniqueTypes);
        
        // Sprawdź też kolumnę 'type' dla pewności
        const uniqueTypeColumn = [...new Set(response.map(report => report.type).filter(Boolean))];
        console.log('🔍 Unikalne typy raportów w bazie (kolumna type):', uniqueTypeColumn);
        
        // Ustaw dostępne typy dla selecta na podstawie kolumny 'name'
        availableTypes.value = uniqueTypes.map(type => ({
          value: type,
          label: type
        }));
        
        console.log('📋 Dostępne typy dla selecta:', availableTypes.value);
        
        reports.value = response;
      } catch (err) {
        console.error('❌ Błąd podczas pobierania raportów:', err);
        error.value = `Nie udało się pobrać raportów: ${err.message || 'Nieznany błąd'}`;

        // Przykładowe dane, jeśli API zwróci błąd
        console.log('📝 Używanie przykładowych danych...');
        reports.value = [
          {
            id: 1,
            name: "Raport obciążenia pracownika",
            fileName: "employee_load_report_2025-05-01.pdf",
            filePath: "/reports/employee_load_report_2025-05-01.pdf",
            createdAt: "2025-05-01T10:30:00",
            type: "employee_load"
          },
          {
            id: 2,
            name: "Raport postępu prac na budowie",
            fileName: "construction_progress_report_2025-04-28.pdf",
            filePath: "/reports/construction_progress_report_2025-04-28.pdf",
            createdAt: "2025-04-28T15:45:00",
            type: "construction_progress"
          },
          {
            id: 3,
            name: "Raport efektywności zespołu",
            fileName: "team_efficiency_report_2025-04-15.pdf",
            filePath: "/reports/team_efficiency_report_2025-04-15.pdf",
            createdAt: "2025-04-15T09:15:00",
            type: "team_efficiency"
          }
        ];
        
        // Ustaw przykładowe typy
        availableTypes.value = [
          { value: "Raport obciążenia pracownika", label: "Raport obciążenia pracownika" },
          { value: "Raport postępu prac na budowie", label: "Raport postępu prac na budowie" },
          { value: "Raport efektywności zespołu", label: "Raport efektywności zespołu" }
        ];
      } finally {
        loading.value = false;
      }
    };

    // Raporty przefiltrowane na podstawie bieżących filtrów (automatycznie)
    const filteredReports = computed(() => {
      let filtered = [...reports.value];

      console.log(`🔍 Automatyczne filtrowanie ${filtered.length} raportów...`);
      console.log('📋 Bieżące filtry:', { 
        date: selectedDate.value, 
        type: selectedType.value, 
        search: searchText.value 
      });

      // Filtrowanie po typie raportu (kolumna 'name')
      if (selectedType.value) {
        filtered = filtered.filter(report => {
          const match = report.name === selectedType.value;
          if (!match) {
            console.log(`❌ Raport ${report.id} odfiltrowany przez typ:`);
            console.log(`   - Nazwa raportu: "${report.name}"`);
            console.log(`   - Szukany typ: "${selectedType.value}"`);
          }
          return match;
        });
        console.log(`📊 Po filtrze typu: ${filtered.length} raportów`);
      }

      // Filtrowanie po dacie
      if (selectedDate.value) {
        const selectedDateStr = formatDateForComparison(selectedDate.value);
        if (selectedDateStr) {
          filtered = filtered.filter(report => {
            if (!report.createdAt) return false;
            
            const reportDate = new Date(report.createdAt).toISOString().split('T')[0];
            const match = reportDate === selectedDateStr;
            if (!match) console.log(`❌ Raport ${report.id} odfiltrowany przez datę: ${reportDate} !== ${selectedDateStr}`);
            return match;
          });
          console.log(`📅 Po filtrze daty: ${filtered.length} raportów`);
        }
      }

      // Filtrowanie po tekście wyszukiwania
      if (searchText.value) {
        const searchLower = searchText.value.toLowerCase();
        filtered = filtered.filter(report => {
          const fileName = (report.fileName || '').toLowerCase();
          const reportId = report.id.toString();
          const reportName = getReportName(report).toLowerCase();
          const reportType = (report.name || '').toLowerCase();
          
          const match = fileName.includes(searchLower) || 
                       reportId.includes(searchLower) || 
                       reportName.includes(searchLower) ||
                       reportType.includes(searchLower);
          
          if (!match) console.log(`❌ Raport ${report.id} odfiltrowany przez wyszukiwanie: "${searchLower}" nie znalezione`);
          return match;
        });
        console.log(`🔍 Po filtrze wyszukiwania: ${filtered.length} raportów`);
      }

      console.log(`✅ Końcowy wynik automatycznego filtrowania: ${filtered.length} raportów`);
      return filtered;
    });

    // Czyszczenie filtrów
    const clearFilters = () => {
      console.log('🧹 Czyszczenie filtrów...');
      
      selectedDate.value = null;
      selectedType.value = '';
      searchText.value = '';

      console.log('✅ Filtry wyczyszczone - automatyczne filtrowanie zadziała');
    };

    // Pobranie raportu
    const downloadReport = async (report) => {
      try {
        console.log(`📥 Pobieranie raportu ID: ${report.id}`);
        
        const fileName = report.fileName || `raport_${report.id}.pdf`;
        await pdfReportService.downloadAndSaveReport(report.id, fileName);
        
        console.log(`✅ Raport ${fileName} został pobrany`);
      } catch (err) {
        console.error('❌ Błąd podczas pobierania raportu:', err);
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
      console.log(`🔍 Otwieranie szczegółów raportu ID: ${report.id}`);
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

    // Formatowanie daty dla filtra (wyświetlanie)
    const formatFilterDate = (date) => {
      if (!date) return '';
      
      try {
        if (date instanceof Date) {
          return date.toLocaleDateString('pl-PL', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
          });
        }
        // Fallback dla string daty
        const dateObj = new Date(date + 'T00:00:00');
        return dateObj.toLocaleDateString('pl-PL', {
          year: 'numeric',
          month: 'long',
          day: 'numeric'
        });
      } catch (error) {
        return date.toString();
      }
    };

    // Pobierz nazwę raportu
    const getReportName = (report) => {
      if (report.name) {
        // Jeśli 'name' zawiera typ raportu, użyj go + data
        const date = formatDate(report.createdAt).split(',')[0];
        return `${report.name} - ${date}`;
      }

      // Fallback - użyj fileName lub ID
      return report.fileName || `Raport #${report.id}`;
    };

    // Pobierz nazwę typu raportu
    const getReportTypeName = (type) => {
      if (!type) return 'Raport';
      
      // Jeśli już jest po polsku, zwróć bez zmian
      if (type.includes('Raport')) {
        return type;
      }
      
      // Mapowanie z angielskich kluczy na polskie nazwy
      const reportTypeNames = {
        'employee_load': 'Raport obciążenia pracownika',
        'construction_progress': 'Raport postępu prac na budowie',
        'team_efficiency': 'Raport efektywności zespołu'
      };

      return reportTypeNames[type] || type;
    };

    // Pobierz opis raportu
    const getReportDescription = (report) => {
      if (report.description) return report.description;

      // Generuj opis na podstawie kolumny 'name'
      const reportDescriptions = {
        'Raport obciążenia pracownika': 'Podsumowanie obciążenia pracowników i przypisanych zadań',
        'Raport postępu prac na budowie': 'Analiza postępu prac budowlanych i terminów realizacji',
        'Raport efektywności zespołu': 'Ocena efektywności zespołów i ich wydajności',
        // Dodaj też wersje z angielskimi kluczami na wypadek gdyby były potrzebne
        'employee_load': 'Podsumowanie obciążenia pracowników i przypisanych zadań',
        'construction_progress': 'Analiza postępu prac budowlanych i terminów realizacji',
        'team_efficiency': 'Ocena efektywności zespołów i ich wydajności'
      };

      return reportDescriptions[report.name] || reportDescriptions[report.type] || 'Szczegóły w raporcie';
    };

    // Watchers dla automatycznego filtrowania
    watch([selectedDate, selectedType, searchText], ([newDate, newType, newSearch], [oldDate, oldType, oldSearch]) => {
      console.log('🔄 Zmiana filtrów - automatyczne przefiltrowanie:', {
        date: { old: oldDate, new: newDate },
        type: { old: oldType, new: newType },
        search: { old: oldSearch, new: newSearch }
      });
    });

    // Inicjalizacja
    onMounted(async () => {
      await fetchReports();
      console.log('✅ Komponent RaportHistory załadowany - automatyczne filtrowanie aktywne');
    });

    return {
      reports,
      loading,
      error,
      availableTypes,
      selectedDate,
      selectedType,
      searchText,
      datepickerInputClass,
      hasActiveFilters,
      filteredReports,
      fetchReports,
      clearFilters,
      downloadReport,
      openReportDetails,
      formatDate,
      formatFilterDate,
      getReportName,
      getReportTypeName,
      getReportDescription,
      showModal,
      modalConfig,
      hideModal
    };
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