import { createApp } from 'vue';
import App from './App.vue';
import router from '../router/router.js'; // Import the router
import './assets/datepicker.css' // Import the datepicker CSS

import './style.css';

const app = createApp(App);

app.use(router); // Ensure the router is used
app.mount('#app');
