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

      <!-- Debugowanie struktury zadania -->
      <div v-if="debugMode" class="bg-gray-100 p-4 rounded-lg mb-4 text-xs overflow-auto max-h-40">
        <pre>{{ JSON.stringify(task, null, 2) }}</pre>
      </div>

      <!-- Lista komentarzy -->
      <div class="space-y-4">
        <div class="flex justify-between items-center">
          <h3 class="text-xl font-semibold text-primary">Komentarze</h3>
          <span class="text-sm text-gray-500">{{ taskComments.length }} {{ commentCountText }}</span>
        </div>
        
        <div v-if="!taskComments || taskComments.length === 0" class="text-center text-gray-500">
          Brak komentarzy
        </div>
        
        <div v-else 
          class="space-y-4 overflow-y-auto transition-all" 
          :class="{'max-h-60': taskComments.length > 3 && !showAllComments, 'scrollbar-custom': taskComments.length > 3}">
          <ul class="space-y-4">
            <li v-for="comment in taskComments" :key="comment.id" class="bg-gray-100 p-4 rounded-lg">
              <div class="flex justify-between">
                <div class="text-sm text-gray-600 mb-1">
                  {{ comment.userFullName || comment.username || 'Użytkownik' }} — {{ formatDate(comment.createdAt) }}
                </div>
                <!-- Przycisk usuwania komentarza - widoczny tylko dla managera lub admina -->
                <button 
                  v-if="canDeleteComments" 
                  @click="deleteComment(comment.id)" 
                  class="bg-red-600 hover:bg-red-700 text-white p-2 transition-colors duration-200"
                  :disabled="isDeletingComment"
                  title="Usuń komentarz"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>
              <p class="text-black">{{ comment.content || comment.comment }}</p>
            </li>
          </ul>
        </div>
        
        <!-- Przycisk "Pokaż więcej" gdy liczba komentarzy > 3 -->
        <div v-if="taskComments.length > 3" class="text-center pt-2">
          <button @click="toggleComments" class="text-primary hover:text-secondary text-sm">
            {{ showAllComments ? 'Pokaż mniej' : 'Pokaż wszystkie' }}
          </button>
        </div>
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

      <div class="flex justify-between">
        <button @click="toggleDebug" 
          class="px-4 py-2 bg-gray-200 hover:bg-gray-300 text-gray-700 rounded-lg text-sm transition">
          {{ debugMode ? 'Ukryj debug' : 'Debug' }}
        </button>
        
        <div class="space-x-4">
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
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import taskService from '../services/taskService';
import teamService from '../services/teamService';
import authService from '../services/authService';
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
    const isDeletingComment = ref(false);
    const debugMode = ref(false);
    const showAllComments = ref(false);

    // Sprawdzenie czy użytkownik może usuwać komentarze (manager lub admin)
    const canDeleteComments = computed(() => {
      return authService.hasRoleAtLeast('manager');
    });

    // Computed property do obsługi komentarzy
    const taskComments = computed(() => {
      // Sprawdza różne możliwe struktury komentarzy
      if (task.value?.comments && Array.isArray(task.value.comments)) {
        return task.value.comments;
      } 
      if (task.value?.taskComments && Array.isArray(task.value.taskComments)) {
        return task.value.taskComments;
      }
      // Sprawdza, czy komentarze są w innym formacie lub zagnieżdżeniu
      if (task.value?.task_comments && Array.isArray(task.value.task_comments)) {
        return task.value.task_comments;
      }
      return [];
    });

    const toggleDebug = () => {
      debugMode.value = !debugMode.value;
    };

    const fetchTaskDetails = async () => {
      loading.value = true;
      error.value = null;

      try {
        const taskId = parseInt(route.params.id);
        const response = await taskService.getTaskById(taskId);
        console.log('Odpowiedź z backendu:', response);
        
        // Zapisz odpowiedź i pokaż w konsoli strukturę
        task.value = response;
        console.log('Struktura task:', task.value);
        
        // Sprawdź, czy są komentarze i wyświetl ich strukturę
        if (task.value.comments) {
          console.log('Struktura komentarzy:', task.value.comments);
        } else if (task.value.taskComments) {
          console.log('Struktura taskComments:', task.value.taskComments);
        } else if (task.value.task_comments) {
          console.log('Struktura task_comments:', task.value.task_comments);
        } else {
          console.log('Brak komentarzy w odpowiedzi API');
          
          // Spróbuj pobrać komentarze osobno, jeśli nie ma ich w głównej odpowiedzi
          try {
            const commentsResponse = await taskService.getTaskComments(taskId);
            console.log('Pobrane komentarze:', commentsResponse);
            
            // Dodaj komentarze do obiektu zadania
            if (Array.isArray(commentsResponse)) {
              task.value.comments = commentsResponse;
            }
          } catch (commentsErr) {
            console.error('Błąd podczas pobierania komentarzy:', commentsErr);
          }
        }
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

    // Funkcja do usuwania komentarza
    const deleteComment = async (commentId) => {
      if (!commentId) {
        console.error('Brak ID komentarza');
        return;
      }

      // Potwierdzenie usunięcia
      if (!confirm('Czy na pewno chcesz usunąć ten komentarz?')) {
        return;
      }

      isDeletingComment.value = true;
      try {
        // Wywołaj usługę usuwania komentarza
        await taskService.deleteComment(commentId);
        
        // Odśwież zadanie, aby zaktualizować listę komentarzy
        await fetchTaskDetails();
        
      } catch (err) {
        console.error('Błąd podczas usuwania komentarza:', err);
        alert(err.message || 'Nie udało się usunąć komentarza. Spróbuj ponownie później.');
      } finally {
        isDeletingComment.value = false;
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

    // Przełącznik wyświetlania wszystkich komentarzy
    const toggleComments = () => {
      showAllComments.value = !showAllComments.value;
    };
    
    // Oblicza tekst z liczbą komentarzy
    const commentCountText = computed(() => {
      const count = taskComments.value.length;
      if (count === 1) return 'komentarz';
      else if (count > 1 && count < 5) return 'komentarze';
      else return 'komentarzy';
    });

    onMounted(() => {
      fetchTaskDetails();
      
      // Dodanie stylów dla niestandardowego scrollbara
      const style = document.createElement('style');
      style.textContent = `
        .scrollbar-custom::-webkit-scrollbar {
          width: 6px;
        }
        .scrollbar-custom::-webkit-scrollbar-track {
          background: #f1f1f1;
          border-radius: 10px;
        }
        .scrollbar-custom::-webkit-scrollbar-thumb {
          background: #888;
          border-radius: 10px;
        }
        .scrollbar-custom::-webkit-scrollbar-thumb:hover {
          background: #555;
        }
      `;
      document.head.appendChild(style);
    });

    return {
      task,
      loading,
      error,
      newComment,
      isAddingComment,
      isDeletingComment,
      teamName,
      statusColor,
      priorityColor,
      taskComments,
      commentCountText,
      showAllComments,
      toggleComments,
      debugMode,
      toggleDebug,
      canDeleteComments,
      addComment,
      deleteComment,
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