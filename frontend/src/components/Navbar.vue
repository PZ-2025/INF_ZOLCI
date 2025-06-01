<template>
  <div class="h-screen w-64 bg-navbar text-white flex flex-col">
    <div class="flex-grow p-4 overflow-auto">
      <button
          @click="goBack"
          class="w-full bg-warning hover:bg-warningHover !text-white px-4 py-2 rounded transition mb-2">
        Wstecz
      </button>
      <router-link
          to="/teams"
          :class="navLinkClass('teams')"
          @click.native="setActiveTab('teams')">
        Zespoły
      </router-link>
      <router-link
          v-if="canAccessManagerFeatures"
          to="/raport"
          :class="navLinkClass('raport')"
          @click.native="setActiveTab('raport')">
        Raporty
      </router-link>
      <router-link
          to="/tasks"
          :class="navLinkClass('tasks')"
          @click.native="setActiveTab('tasks')">
        Zadania
      </router-link>
      <router-link
          v-if="isAdmin"
          to="/adminpanel"
          :class="navLinkClass('adminpanel')"
          @click.native="setActiveTab('adminpanel')">
        Panel administratora
      </router-link>
    </div>

    <div class="p-4">
      <div v-if="userFullName" class="mb-2 font-semibold text-center">
        Użytkownik: {{ userFullName }}
      </div>
      <button
          @click="logout"
          class="w-full bg-warning hover:bg-warningHover text-white px-4 py-2 rounded transition mb-2">
        Wyloguj
      </button>
    </div>
  </div>
</template>

<script>
import { authState } from '../../router/router.js';
import authService from '../services/authService';

export default {
  data() {
    return {
      activeTab: null,
      navigationHistory: [] // Dodana historia nawigacji specyficzna dla zalogowanego użytkownika
    };
  },
  computed: {
    canAccessManagerFeatures() {
      return authService.hasRoleAtLeast('kierownik');
    },
    isAdmin() {
      return authState.user?.role === 'administrator';
    },
    userFullName() {
      const user = authState.user;
      if (user && user.firstName && user.lastName) {
        return `${user.firstName} ${user.lastName}`;
      }
      return '';
    }
  },
  methods: {
    setActiveTab(tab) {
      this.activeTab = tab;
    },
    navLinkClass(tab) {
      return [
        'block bg-secondary hover:bg-accent p-3 rounded mb-3 transition !text-white',
        this.activeTab === tab ? 'ring-2 ring-accent font-bold' : ''
      ];
    },
    goBack() {
      // Sprawdź czy użytkownik jest zalogowany
      if (!authState.isAuthenticated) {
        console.log('Użytkownik niezalogowany - przekierowanie na stronę logowania');
        this.$router.push('/');
        return;
      }

      // Sprawdź czy mamy zapisaną historię nawigacji
      if (this.navigationHistory.length > 1) {
        // Usuń bieżącą stronę z historii
        this.navigationHistory.pop();
        // Przejdź do poprzedniej strony z naszej historii
        const previousRoute = this.navigationHistory[this.navigationHistory.length - 1];
        console.log('Cofanie się do:', previousRoute);
        this.$router.push(previousRoute);
        return;
      }

      // Sprawdź czy możemy bezpiecznie cofnąć się w historii przeglądarki
      const currentRoute = this.$route.path;
      const allowedRoutes = ['/teams', '/tasks', '/raport', '/adminpanel', '/settings'];
      
      // Jeśli jesteśmy na jednej ze stron głównych, nie cofaj się dalej
      if (allowedRoutes.includes(currentRoute)) {
        console.log('Już jesteś na stronie głównej - brak akcji');
        return;
      }

      // Sprawdź czy można bezpiecznie użyć historii przeglądarki
      if (window.history.length > 1) {
        // Sprawdź czy poprzednia strona nie była stroną logowania
        const referrer = document.referrer;
        if (referrer && (referrer.includes('/login') || referrer.endsWith('/'))) {
          console.log('Poprzednia strona to logowanie - przekierowanie na teams');
          this.$router.push('/teams');
          return;
        }

        // Sprawdź czy w sessionStorage mamy informację o poprzedniej stronie
        const previousRoute = sessionStorage.getItem('previousRoute');
        if (previousRoute && previousRoute !== '/' && previousRoute !== '/login') {
          console.log('Cofanie się do zapisanej poprzedniej strony:', previousRoute);
          this.$router.push(previousRoute);
          sessionStorage.removeItem('previousRoute');
          return;
        }

        // Jako ostateczność użyj historii przeglądarki, ale tylko jeśli jesteś głębiej w aplikacji
        console.log('Używanie historii przeglądarki');
        this.$router.go(-1);
      } else {
        // Brak historii - przekieruj na domyślną stronę
        console.log('Brak historii - przekierowanie na teams');
        this.$router.push('/teams');
      }
    },
    logout() {
      // Wyczyść historię nawigacji przy wylogowaniu
      this.navigationHistory = [];
      sessionStorage.removeItem('previousRoute');
      sessionStorage.removeItem('navigationHistory');
      
      authService.logout();
      this.$router.push('/');
    },
    
    // Funkcja do śledzenia nawigacji
    trackNavigation(to, from) {
      // Zapisuj tylko trasy dla zalogowanych użytkowników
      if (!authState.isAuthenticated) return;
      
      // Nie zapisuj strony logowania
      if (from.path === '/' || from.path === '/login') return;
      
      // Dodaj poprzednią trasę do historii (jeśli nie jest już ostatnia)
      if (from.path && from.path !== to.path) {
        const lastRoute = this.navigationHistory[this.navigationHistory.length - 1];
        if (lastRoute !== from.path) {
          this.navigationHistory.push(from.path);
          
          // Ogranicz historię do ostatnich 10 pozycji
          if (this.navigationHistory.length > 10) {
            this.navigationHistory = this.navigationHistory.slice(-10);
          }
          
          // Zapisz w sessionStorage
          sessionStorage.setItem('navigationHistory', JSON.stringify(this.navigationHistory));
        }
      }
    }
  },
  
  // Nasłuchuj zmian route'a
  watch: {
    '$route'(to, from) {
      this.trackNavigation(to, from);
    }
  },
  
  // Przywróć historię nawigacji przy załadowaniu komponentu
  created() {
    try {
      const savedHistory = sessionStorage.getItem('navigationHistory');
      if (savedHistory) {
        this.navigationHistory = JSON.parse(savedHistory);
      }
    } catch (error) {
      console.error('Błąd podczas odczytywania historii nawigacji:', error);
      this.navigationHistory = [];
    }
  }
};
</script>