<template>
  <div
      v-if="visible"
      :class="[
      'api-error-container',
      { 'api-error-modal': mode === 'modal', 'api-error-inline': mode === 'inline' }
    ]"
  >
    <div class="api-error-box">
      <div class="api-error-icon" :class="errorTypeClass">
        <i :class="errorTypeIcon"></i>
      </div>

      <h3 class="api-error-title">
        {{ customTitle || errorTypeTitle }}
      </h3>

      <p class="api-error-message">
        {{ customMessage || errorMessage }}
      </p>

      <div v-if="isDatabaseError" class="api-error-database-hint">
        <p>Serwer nie może połączyć się z bazą danych. Ta sytuacja jest tymczasowa i zostanie rozwiązana wkrótce.</p>
      </div>

      <button
          v-if="showRetry"
          class="api-error-retry-button"
          @click="handleRetry"
          :disabled="isRetrying"
      >
        <span v-if="isRetrying">
          <i class="fa fa-spinner fa-spin"></i> {{ retryingText }}
        </span>
        <span v-else>
          <i class="fa fa-refresh"></i> {{ retryText }}
        </span>
      </button>

      <button
          v-if="showDismiss && mode === 'inline'"
          class="api-error-dismiss-button"
          @click="handleDismiss"
      >
        <i class="fa fa-times"></i> {{ dismissText }}
      </button>

      <div v-if="showDetails && hasErrorDetails" class="api-error-details">
        <div class="api-error-details-header" @click="toggleErrorDetails">
          <span>{{ detailsExpanded ? 'Ukryj' : 'Pokaż' }} szczegóły techniczne</span>
          <i :class="detailsExpanded ? 'fa fa-chevron-up' : 'fa fa-chevron-down'"></i>
        </div>
        <div v-if="detailsExpanded" class="api-error-details-content">
          <div v-if="errorData.status" class="api-error-detail-item">
            <strong>Status:</strong> {{ errorData.status }} {{ errorData.statusText || '' }}
          </div>
          <div v-if="errorData.type" class="api-error-detail-item">
            <strong>Typ błędu:</strong> {{ errorData.type }}
          </div>
          <div v-if="errorData.message" class="api-error-detail-item">
            <strong>Komunikat:</strong> {{ errorData.message }}
          </div>
          <div v-if="errorData.timestamp" class="api-error-detail-item">
            <strong>Czas:</strong> {{ formatTimestamp(errorData.timestamp) }}
          </div>
          <div v-if="errorData.data" class="api-error-detail-item">
            <strong>Dane odpowiedzi:</strong>
            <pre>{{ JSON.stringify(errorData.data, null, 2) }}</pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch } from 'vue';
import { API_ERROR_TYPES } from '../services/apiService';

export default {
  name: 'ApiErrorDisplay',
  props: {
    // Kontrola widoczności
    visible: {
      type: Boolean,
      default: true
    },

    // Tryb wyświetlania: 'modal' lub 'inline'
    mode: {
      type: String,
      default: 'modal',
      validator: value => ['modal', 'inline'].includes(value)
    },

    // Dane o błędzie
    errorType: {
      type: String,
      default: API_ERROR_TYPES.UNKNOWN
    },
    errorData: {
      type: Object,
      default: () => ({})
    },

    // Niestandardowe teksty
    customTitle: {
      type: String,
      default: ''
    },
    customMessage: {
      type: String,
      default: ''
    },
    retryText: {
      type: String,
      default: 'Spróbuj ponownie'
    },
    retryingText: {
      type: String,
      default: 'Próbuję...'
    },
    dismissText: {
      type: String,
      default: 'Zamknij'
    },

    // Opcje komponentu
    showRetry: {
      type: Boolean,
      default: true
    },
    showDismiss: {
      type: Boolean,
      default: true
    },
    showDetails: {
      type: Boolean,
      default: true
    },

    // Callback dla ponownej próby
    onRetry: {
      type: Function,
      default: () => Promise.resolve(true)
    },

    // Callback dla zamknięcia
    onDismiss: {
      type: Function,
      default: () => {}
    }
  },

  setup(props, { emit }) {
    const isRetrying = ref(false);
    const detailsExpanded = ref(false);

    // Określenie czy to jest błąd bazy danych (503)
    const isDatabaseError = computed(() =>
        props.errorType === API_ERROR_TYPES.DATABASE || props.errorData?.status === 503
    );

    // Sprawdzenie czy mamy szczegóły błędu
    const hasErrorDetails = computed(() => {
      return props.errorData && Object.keys(props.errorData).length > 0;
    });

    // Dynamiczna klasa dla ikony błędu
    const errorTypeClass = computed(() => {
      switch(props.errorType) {
        case API_ERROR_TYPES.DATABASE:
          return 'error-database';
        case API_ERROR_TYPES.NETWORK:
        case API_ERROR_TYPES.TIMEOUT:
          return 'error-network';
        case API_ERROR_TYPES.AUTH:
          return 'error-auth';
        case API_ERROR_TYPES.SERVER:
          return 'error-server';
        default:
          return 'error-unknown';
      }
    });

    // Dynamiczna ikona dla błędu
    const errorTypeIcon = computed(() => {
      switch(props.errorType) {
        case API_ERROR_TYPES.DATABASE:
          return 'fa fa-database';
        case API_ERROR_TYPES.NETWORK:
          return 'fa fa-wifi';
        case API_ERROR_TYPES.TIMEOUT:
          return 'fa fa-clock-o';
        case API_ERROR_TYPES.AUTH:
          return 'fa fa-lock';
        case API_ERROR_TYPES.SERVER:
          return 'fa fa-server';
        default:
          return 'fa fa-exclamation-triangle';
      }
    });

    // Dynamiczny tytuł dla błędu
    const errorTypeTitle = computed(() => {
      switch(props.errorType) {
        case API_ERROR_TYPES.DATABASE:
          return 'Problem z bazą danych';
        case API_ERROR_TYPES.NETWORK:
          return 'Problem z połączeniem sieciowym';
        case API_ERROR_TYPES.TIMEOUT:
          return 'Przekroczono limit czasu';
        case API_ERROR_TYPES.AUTH:
          return 'Problem z autoryzacją';
        case API_ERROR_TYPES.SERVER:
          return 'Problem z serwerem';
        case API_ERROR_TYPES.CLIENT:
          return 'Problem z żądaniem';
        default:
          return 'Wystąpił problem';
      }
    });

    // Domyślny komunikat błędu
    const errorMessage = computed(() => {
      switch(props.errorType) {
        case API_ERROR_TYPES.DATABASE:
          return 'Serwer nie może połączyć się z bazą danych. Prosimy spróbować ponownie za kilka minut.';
        case API_ERROR_TYPES.NETWORK:
          return 'Nie można połączyć się z serwerem. Sprawdź swoje połączenie z internetem.';
        case API_ERROR_TYPES.TIMEOUT:
          return 'Upłynął limit czasu oczekiwania na odpowiedź serwera. Spróbuj ponownie.';
        case API_ERROR_TYPES.AUTH:
          return 'Brak uprawnień lub sesja wygasła. Proszę zalogować się ponownie.';
        case API_ERROR_TYPES.SERVER:
          return 'Wystąpił błąd po stronie serwera. Prosimy spróbować ponownie później.';
        case API_ERROR_TYPES.CLIENT:
          return 'Wystąpił błąd w żądaniu. Proszę sprawdzić dane i spróbować ponownie.';
        default:
          return 'Wystąpił nieoczekiwany błąd. Prosimy spróbować ponownie później.';
      }
    });

    // Obsługa przycisku "Spróbuj ponownie"
    const handleRetry = async () => {
      if (isRetrying.value) return;

      isRetrying.value = true;
      emit('retry-started');

      try {
        const success = await props.onRetry();
        emit('retry-completed', success);
      } catch (error) {
        console.error('Error during retry:', error);
        emit('retry-error', error);
      } finally {
        isRetrying.value = false;
      }
    };

    // Obsługa przycisku "Zamknij"
    const handleDismiss = () => {
      emit('dismiss');
      props.onDismiss();
    };

    // Przełączanie sekcji ze szczegółami
    const toggleErrorDetails = () => {
      detailsExpanded.value = !detailsExpanded.value;
    };

    // Formatowanie timestampu
    const formatTimestamp = (timestamp) => {
      if (!timestamp) return '';

      try {
        const date = new Date(timestamp);
        return date.toLocaleString();
      } catch (e) {
        return timestamp;
      }
    };

    // Reset stanu przy zmianie błędu
    watch(() => props.errorType, () => {
      detailsExpanded.value = false;
    });

    return {
      isRetrying,
      detailsExpanded,
      isDatabaseError,
      hasErrorDetails,
      errorTypeClass,
      errorTypeIcon,
      errorTypeTitle,
      errorMessage,
      handleRetry,
      handleDismiss,
      toggleErrorDetails,
      formatTimestamp
    };
  }
};
</script>

<style scoped>
/* Podstawowe style kontenera */
.api-error-container {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

/* Modal - pełnoekranowy */
.api-error-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

/* Inline - w kontenerze */
.api-error-inline {
  margin: 16px 0;
}

/* Główny box */
.api-error-box {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  padding: 24px;
  max-width: 500px;
  width: 100%;
  text-align: center;
}

.api-error-inline .api-error-box {
  border: 1px solid #f5c6cb;
  box-shadow: none;
  background-color: #f8d7da;
}

/* Ikona błędu */
.api-error-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.error-database { color: #e67e22; }  /* Pomarańczowy dla błędów bazy danych */
.error-network { color: #3498db; }   /* Niebieski dla sieci */
.error-auth { color: #8e44ad; }      /* Fioletowy dla autoryzacji */
.error-server { color: #e74c3c; }    /* Czerwony dla serwera */
.error-unknown { color: #95a5a6; }   /* Szary dla nieznanych */

/* Tytuł błędu */
.api-error-title {
  font-size: 24px;
  margin-bottom: 16px;
  color: #333;
}

/* Komunikat błędu */
.api-error-message {
  font-size: 16px;
  margin-bottom: 24px;
  color: #555;
  line-height: 1.5;
}

/* Dodatkowe wskazówki dla błędu bazy danych */
.api-error-database-hint {
  background-color: #fff3cd;
  border-left: 4px solid #e67e22;
  padding: 12px;
  margin-bottom: 20px;
  text-align: left;
  border-radius: 4px;
}

.api-error-database-hint p {
  margin: 0;
  color: #664d03;
  font-size: 14px;
}

/* Przyciski */
.api-error-retry-button,
.api-error-dismiss-button {
  padding: 10px 20px;
  font-size: 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  margin: 0 8px;
}

.api-error-retry-button {
  background-color: #3498db;
  color: white;
}

.api-error-retry-button:hover:not(:disabled) {
  background-color: #2980b9;
}

.api-error-retry-button:disabled {
  background-color: #95a5a6;
  cursor: not-allowed;
}

.api-error-dismiss-button {
  background-color: #f8f9fa;
  color: #6c757d;
  border: 1px solid #6c757d;
}

.api-error-dismiss-button:hover {
  background-color: #e9ecef;
}

/* Sekcja szczegółów technicznych */
.api-error-details {
  margin-top: 24px;
  text-align: left;
  border-top: 1px solid #e9ecef;
  padding-top: 16px;
}

.api-error-details-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  color: #6c757d;
  font-size: 14px;
}

.api-error-details-content {
  margin-top: 12px;
  background-color: #f8f9fa;
  padding: 12px;
  border-radius: 4px;
  font-size: 14px;
  max-height: 200px;
  overflow-y: auto;
}

.api-error-detail-item {
  margin-bottom: 8px;
}

.api-error-detail-item pre {
  margin: 8px 0;
  white-space: pre-wrap;
  word-break: break-word;
  background-color: #e9ecef;
  padding: 8px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 12px;
}
</style>