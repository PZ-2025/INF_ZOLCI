<template>
  <Transition name="modal">
    <div v-if="show" class="fixed inset-0 flex items-center justify-center z-50">
      <div class="fixed inset-0 bg-black opacity-50"></div>
      <div class="bg-white rounded-lg p-8 shadow-xl z-10 max-w-md w-full mx-4">
        <div class="text-center">
          <!-- Icon -->
          <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full mb-4"
               :class="iconClass">
            <svg v-if="type === 'success'" class="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
            <svg v-else-if="type === 'warning'" class="h-6 w-6 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <svg v-else-if="type === 'info'" class="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <svg v-else class="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </div>
          
          <!-- Title -->
          <h3 class="text-lg leading-6 font-medium text-gray-900 mb-2">
            {{ title }}
          </h3>
          
          <!-- Message -->
          <div class="text-sm text-gray-500 mb-4">
            {{ message }}
          </div>
          
          <!-- Buttons -->
          <div v-if="showCancelButton" class="flex space-x-3 justify-center">
            <!-- Cancel Button -->
            <button
              @click="handleCancel"
              class="inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 sm:text-sm"
            >
              {{ cancelText }}
            </button>
            
            <!-- Confirm Button -->
            <button
              @click="handleConfirm"
              class="inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:text-sm"
              :class="confirmButtonClass"
            >
              {{ buttonText }}
            </button>
          </div>
          
          <!-- Single Button (original behavior) -->
          <button
            v-else
            @click="handleConfirm"
            class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:text-sm"
            :class="confirmButtonClass"
          >
            {{ buttonText }}
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { defineProps, defineEmits, computed } from 'vue';

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  type: {
    type: String,
    default: 'success',
    validator: (value) => ['success', 'error', 'warning', 'info'].includes(value)
  },
  title: {
    type: String,
    required: true
  },
  message: {
    type: String,
    required: true
  },
  buttonText: {
    type: String,
    default: 'OK'
  },
  cancelText: {
    type: String,
    default: 'Anuluj'
  },
  showCancelButton: {
    type: Boolean,
    default: false
  },
  autoClose: {
    type: Boolean,
    default: true
  },
  autoCloseDelay: {
    type: Number,
    default: 2000
  },
  onClose: {
    type: Function,
    default: null
  },
  onCancel: {
    type: Function,
    default: null
  }
});

const emit = defineEmits(['close', 'confirm', 'cancel']);

// Computed properties for styling
const iconClass = computed(() => {
  switch (props.type) {
    case 'success':
      return 'bg-green-100';
    case 'warning':
      return 'bg-yellow-100';
    case 'info':
      return 'bg-blue-100';
    case 'error':
    default:
      return 'bg-red-100';
  }
});

const confirmButtonClass = computed(() => {
  switch (props.type) {
    case 'success':
      return 'bg-green-600 hover:bg-green-700 focus:ring-green-500';
    case 'warning':
      return 'bg-yellow-600 hover:bg-yellow-700 focus:ring-yellow-500';
    case 'info':
      return 'bg-blue-600 hover:bg-blue-700 focus:ring-blue-500';
    case 'error':
    default:
      return 'bg-red-600 hover:bg-red-700 focus:ring-red-500';
  }
});

const handleConfirm = () => {
  emit('confirm');
  emit('close');
  if (props.onClose) {
    props.onClose();
  }
};

const handleCancel = () => {
  emit('cancel');
  emit('close');
  if (props.onCancel) {
    props.onCancel();
  }
};

// Auto close functionality
if (props.autoClose && props.show && !props.showCancelButton) {
  setTimeout(() => {
    handleConfirm();
  }, props.autoCloseDelay);
}
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
</style>