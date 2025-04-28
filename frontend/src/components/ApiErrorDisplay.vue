
<template>
  <div v-if="!isConnected" class="error-container">
    <div class="error-box">
      <div class="error-icon">
        <i class="fa fa-exclamation-triangle"></i>
      </div>
      <h3 class="error-title">{{ title || 'Problem z połączeniem' }}</h3>
      <p class="error-message">{{ message || 'Nie można połączyć się z serwerem. Sprawdź swoje połączenie sieciowe lub spróbuj ponownie później.' }}</p>
      <button
          class="retry-button"
          @click="handleRetry"
          :disabled="isRetrying">
        <span v-if="isRetrying">
          <i class="fa fa-spinner fa-spin"></i> Próbuję...
        </span>
        <span v-else>
          <i class="fa fa-refresh"></i> {{ retryText || 'Spróbuj ponownie' }}
        </span>
      </button>
      <div v-if="showDetails && lastError" class="error-details">
        <p>Szczegóły błędu:</p>
        <pre>{{ errorDetails }}</pre>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';

export default {
  name: 'ApiErrorDisplay',
  props: {
    isConnected: {
      type: Boolean,
      required: true
    },
    lastError: {
      type: [Object, Error, null],
      default: null
    },
    title: {
      type: String,
      default: ''
    },
    message: {
      type: String,
      default: ''
    },
    retryText: {
      type: String,
      default: ''
    },
    showDetails: {
      type: Boolean,
      default: false
    },
    onRetry: {
      type: Function,
      default: () => Promise.resolve(true)
    }
  },
  setup(props, { emit }) {
    const isRetrying = ref(false);

    // Przetwórz informacje o błędzie na czytelny format
    const errorDetails = computed(() => {
      if (!props.lastError) return '';

      if (props.lastError.response) {
        // Błąd odpowiedzi z API
        return `Status: ${props.lastError.response.status}\nMessage: ${props.lastError.response.data?.message || props.lastError.message}`;
      } else if (props.lastError.request) {
        // Brak odpowiedzi
        return `No response received: ${props.lastError.message}`;
      } else {
        // Ogólny błąd
        return props.lastError.message || 'Unknown error';
      }
    });

    // Obsługa kliknięcia przycisku "Spróbuj ponownie"
    const handleRetry = async () => {
      if (isRetrying.value) return;

      isRetrying.value = true;
      emit('retry-started');

      try {
        const success = await props.onRetry();
        emit('retry-completed', success);
      } catch (error) {
        emit('retry-error', error);
      } finally {
        isRetrying.value = false;
      }
    };

    return {
      isRetrying,
      errorDetails,
      handleRetry
    };
  }
};
</script>

<style scoped>
.error-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 9999;
}

.error-box {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  padding: 24px;
  max-width: 500px;
  width: 90%;
  text-align: center;
}

.error-icon {
  font-size: 48px;
  color: #e74c3c;
  margin-bottom: 16px;
}

.error-title {
  font-size: 24px;
  margin-bottom: 16px;
  color: #333;
}

.error-message {
  font-size: 16px;
  margin-bottom: 24px;
  color: #555;
}

.retry-button {
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 12px 24px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.retry-button:hover:not(:disabled) {
  background-color: #2980b9;
}

.retry-button:disabled {
  background-color: #95a5a6;
  cursor: not-allowed;
}

.error-details {
  margin-top: 16px;
  text-align: left;
  background-color: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  font-size: 14px;
  color: #555;
  max-height: 200px;
  overflow-y: auto;
}

.error-details pre {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>