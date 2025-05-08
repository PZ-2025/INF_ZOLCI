import { ref } from 'vue';

export function useStatusModal() {
  const showModal = ref(false);
  const modalConfig = ref({
    type: 'success',
    title: '',
    message: '',
    buttonText: 'OK',
    autoClose: true,
    autoCloseDelay: 2000,
    onClose: null
  });

  const showStatus = (config) => {
    modalConfig.value = { ...modalConfig.value, ...config };
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
