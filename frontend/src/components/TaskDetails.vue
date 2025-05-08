<template>
  <div class="min-h-screen bg-background flex items-center justify-center px-4 py-10">
    <div v-if="loading" class="text-center">
      <p class="text-xl text-primary">Ładowanie szczegółów zadania...</p>
    </div>
    
    <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4">
      <p>{{ error }}</p>
      <button @click="fetchTaskDetails" class="mt-2 bg-primary text-white px-4 py-1 rounded-md">
        Spróbuj ponownie
      </button>
    </div>

    <div v-else class="w-full max-w-2xl bg-surface border border-gray-200 rounded-2xl shadow-xl p-8 space-y-6">
      <h2 class="text-3xl font-bold text-primary text-center">Szczegóły Zadania</h2>

      <div class="space-y-4">
        <p><span class="font-semibold text-black">Tytuł: </span> <span class="text-black">{{ task.title }}</span></p>
        <p><span class="font-semibold text-black">Opis: </span> <span class="text-black">{{ task.description }}</span></p>
        <p>
          <span class="font-semibold text-black">Status: </span> 
          <span :class="statusColor">{{ getStatusText(getStatusName(task.statusId)) }}</span>
        </p>
        <p>
          <span class="font-semibold text-black">Priorytet: </span> 
          <span :class="priorityColor">{{ getPriorityText(getPriorityName(task.priorityId)) }}</span>
        </p>
        <p>
          <span class="font-semibold text-black">Termin: </span> 
          <span class="text-black">{{ formatDate(task.deadline) }}</span>
        </p>
        <p>
          <span class="font-semibold text-black">Data rozpoczęcia: </span> 
          <span class="text-black">{{ formatDate(task.startDate) }}</span>
        </p>
        <p><span class="font-semibold text-black">Zespół: </span> <span class="text-black">{{ teamName }}</span></p>
      </div>

      <!-- Lista komentarzy -->
      <div class="space-y-4">
        <h3 class="text-xl font-semibold text-primary">Komentarze</h3>
        <div v-if="!task.comments || task.comments.length === 0" class="text-center text-gray-500">
          Brak komentarzy
        </div>
        <ul v-else class="space-y-4">
          <li v-for="comment in task.comments" :key="comment.id" class="bg-gray-100 p-4 rounded-lg">
            <div class="text-sm text-gray-600 mb-1">
              {{ comment.userFullName || comment.username || 'Użytkownik' }} — {{ formatDate(comment.createdAt) }}
            </div>
            <p class="text-black">{{ comment.comment }}</p>
          </li>
        </ul>
      </div>

      <!-- Formularz dodawania komentarza -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <h4 class="text-lg font-semibold text-primary mb-3">Dodaj komentarz </h4>
        <form @submit.prevent="addComment" class="space-y-3">
          <textarea
              v-model.trim="newComment"
              class="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary focus:outline-none bg-white text-black placeholder-gray-500"
              rows="3"
              placeholder="Wpisz komentarz..."
              required
          ></textarea>
          <button
              type="submit"
              class="w-full bg-primary hover:bg-secondary text-white px-4 py-2 rounded-md transition"
              :disabled="isAddingComment || !newComment.trim()"
          >
              {{ isAddingComment ? 'Dodawanie...' : 'Dodaj komentarz' }}
          </button>
        </form>
      </div>

      <div class="flex justify-end space-x-4">
        <button @click="editTask"
          class="px-4 py-2 bg-primary hover:bg-secondary text-white rounded-lg text-sm transition">
          Edytuj
        </button>
        <button @click="deleteTask"
          class="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm transition">
          Usuń zadanie
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import taskService from '../services/taskService';
import teamService from '../services/teamService';
import { authState } from '../../router/router';

export default {
  name: 'TaskDetails',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const task = ref({});
    const loading = ref(true);
    const error = ref(null);
    const newComment = ref('');
    const isAddingComment = ref(false);

    const fetchTaskDetails = async () => {
      loading.value = true;
      error.value = null;

      try {
        const taskId = parseInt(route.params.id);
        const response = await taskService.getTaskById(taskId);
        task.value = response;
      } catch (err) {
        console.error('Błąd podczas pobierania szczegółów zadania:', err);
        error.value = 'Nie udało się pobrać szczegółów zadania';
      } finally {
        loading.value = false;
      }
    };

    const teamName = computed(() => {
      if (!task.value) return 'Nieznany zespół';
      if (task.value.team?.name) return task.value.team.name;
      return `Zespół #${task.value.teamId}`;
    });

    const addComment = async () => {
      if (!newComment.value.trim()) return;
      
      isAddingComment.value = true;
      try {
          if (!authState.user?.id) {
              throw new Error('Użytkownik nie jest zalogowany');
          }

          if (!task.value.id) {
              throw new Error('Brak ID zadania');
          }

          const commentData = {
              taskId: task.value.id,
              userId: authState.user.id,
              content: newComment.value.trim()
          };
          
          console.log('Wysyłanie komentarza:', commentData);
          await taskService.addComment(commentData);
          
          // Odśwież zadanie aby pobrać nowe komentarze
          await fetchTaskDetails();
          
          // Wyczyść pole komentarza
          newComment.value = '';
          
      } catch (err) {
          console.error('Błąd podczas dodawania komentarza:', err);
          alert(err.message || 'Nie udało się dodać komentarza. Spróbuj ponownie później.');
      } finally {
          isAddingComment.value = false;
      }
    };

    const getStatusName = (status) => {
      if (status === null || status === undefined) return 'open';
      const statusMap = {
        1: 'open',
        2: 'in_progress',
        3: 'completed'
      };
      return statusMap[status] || 'open';
    };

    const getStatusText = (status) => {
      const statusTexts = {
        'open': 'Rozpoczęte',
        'in_progress': 'W toku',
        'completed': 'Zakończone'
      };
      return statusTexts[status] || 'Rozpoczęte';
    };

    const getPriorityName = (priority) => {
      if (priority === null || priority === undefined) return 'medium';
      const priorityMap = {
        1: 'low',
        2: 'medium',
        3: 'high'
      };
      return priorityMap[priority] || 'medium';
    };

    const getPriorityText = (priority) => {
      const priorityTexts = {
        'low': 'Niski',
        'medium': 'Średni',
        'high': 'Wysoki'
      };
      return priorityTexts[priority] || 'Średni';
    };

    const formatDate = (dateString) => {
      if (!dateString) return 'Nie określono';
      try {
        const date = new Date(dateString);
        return date.toLocaleDateString('pl-PL', {
          year: 'numeric',
          month: 'long',
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        });
      } catch (err) {
        return dateString;
      }
    };

    const statusColor = computed(() => {
      const status = getStatusName(task.value.statusId);
      const colorMap = {
        'open': 'text-blue-500',
        'in_progress': 'text-yellow-500',
        'completed': 'text-green-500'
      };
      return `${colorMap[status] || ''} font-semibold`;
    });

    const priorityColor = computed(() => {
      const priority = getPriorityName(task.value.priorityId);
      const colorMap = {
        'low': 'text-blue-500',
        'medium': 'text-yellow-500',
        'high': 'text-red-500'
      };
      return `${colorMap[priority] || ''} font-semibold`;
    });

    const editTask = () => {
      router.push({ name: 'editTask', params: { id: task.value.id } });
    };

    const deleteTask = async () => {
      if (!confirm('Czy na pewno chcesz usunąć to zadanie?')) return;
      
      try {
        await taskService.deleteTask(task.value.id);
        router.push('/taskshistory');
      } catch (err) {
        console.error('Błąd podczas usuwania zadania:', err);
        alert('Nie udało się usunąć zadania');
      }
    };

    onMounted(fetchTaskDetails);

    return {
      task,
      loading,
      error,
      newComment,
      isAddingComment,
      teamName,
      statusColor,
      priorityColor,
      addComment,
      editTask,
      deleteTask,
      getStatusName,
      getStatusText,
      getPriorityName,
      getPriorityText,
      formatDate,
      fetchTaskDetails
    };
  }
};
</script>
