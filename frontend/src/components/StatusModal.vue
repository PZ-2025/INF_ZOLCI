<template>
  <Transition name="modal">
    <div v-if="show" class="fixed inset-0 flex items-center justify-center z-50">
      <div class="fixed inset-0 bg-black opacity-50"></div>
      <div class="bg-white rounded-lg p-8 shadow-xl z-10 max-w-md w-full mx-4">
        <div class="text-center">
          <!-- Icon -->
          <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full mb-4"
               :class="type === 'success' ? 'bg-green-100' : 'bg-red-100'">
            <svg v-if="type === 'success'" class="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
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
          
          <!-- Button -->
          <button
            @click="handleClose"
            class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:text-sm"
            :class="type === 'success' ? 'bg-green-600 hover:bg-green-700 focus:ring-green-500' : 'bg-red-600 hover:bg-red-700 focus:ring-red-500'"
          >
            {{ buttonText }}
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  type: {
    type: String,
    default: 'success',
    validator: (value) => ['success', 'error'].includes(value)
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
  }
});

const emit = defineEmits(['close']);

const handleClose = () => {
  emit('close');
  if (props.onClose) {
    props.onClose();
  }
};

if (props.autoClose && props.show) {
  setTimeout(() => {
    handleClose();
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
