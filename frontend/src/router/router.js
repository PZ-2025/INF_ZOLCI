import Vue from 'vue';
import Router from 'vue-router';
import Home from '../components/Home.vue';
import TeamSelection from '../components/Teams.vue';
import TeamDetails from '../components/TeamDetails.vue';

Vue.use(Router);

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/teams',
    name: 'TeamSelection',
    component: TeamSelection
  },
  {
    path: '/teamdetails',
    name: 'TeamDetails',
    component: TeamDetails
  }
];

export default new Router({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
});