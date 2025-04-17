import { createApp } from 'vue';
import App from './App.vue';
import router from '../router/router.js'; // Import routera
import './style.css';

// Stwórz aplikację Vue
const app = createApp(App);

// Dodaj router do aplikacji
app.use(router);

// Zamontuj aplikację
app.mount('#app');