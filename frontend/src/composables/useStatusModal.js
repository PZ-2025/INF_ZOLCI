import { ref } from 'vue';

export function useStatusModal() {
  const showModal = ref(false);
  const modalConfig = ref({
    type: 'success',
    title: '',
    message: '',
    buttonText: 'OK',
    cancelText: 'Anuluj',
    showCancelButton: true,
    autoClose: true,
    autoCloseDelay: 2000,
    onClose: null,
    onCancel: null
  });

  const showStatus = (config) => {
    modalConfig.value = { 
      ...modalConfig.value, // zachowaj domyślne wartości
      ...config // nadpisz przekazanymi wartościami
    };
    showModal.value = true;
  };

  const hideModal = () => {
    showModal.value = false;
    if (modalConfig.value.onClose) {
      modalConfig.value.onClose();
    }
  };

  return {
    showModal,
    modalConfig,
    showStatus,
    hideModal
  };
}
