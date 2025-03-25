import { createRouter, createWebHistory } from 'vue-router';
import { reactive } from 'vue';
import Teams from '../src/components/Teams.vue';
import AllEmployees from '../src/components/AllEmployees.vue';
import LoginForm from '../src/components/LoginForm.vue';
import RaportGenerate from '../src/components/RaportGenerate.vue';
import SystemConf from '../src/components/SystemConf.vue';
import TasksHistory from '../src/components/TasksHistory.vue';
import TeamDetails from '../src/components/TeamDetails.vue';
import TeamsTasks from '../src/components/TeamsTasks.vue';
import UserSettings from '../src/components/UserSettings.vue';
import AllUsers from '../src/components/AllUsers.vue';

export const authState = reactive({
  isAuthenticated: false,
  user: null, 
});

const routes = [
  { path: "/", component: LoginForm }, 
  { path: "/teams", component: Teams, meta: { requiresAuth: true, roles: ['employee', 'manager'] } },
  { path: '/allemployees', component: AllEmployees, meta: { requiresAuth: true, roles: ['manager'] } },
  { path: '/raportgenerate', component: RaportGenerate, meta: { requiresAuth: true, roles: ['manager'] } },
  { path: '/systemconf', component: SystemConf, meta: { requiresAuth: true, roles: ['manager'] } },
  { path: '/taskshistory', component: TasksHistory, meta: { requiresAuth: true, roles: ['manager'] } },
  { path: "/teamdetails", component: TeamDetails, meta: { requiresAuth: true, roles: ['employee', 'manager'] } },
  { path: "/teamtasks", component: TeamsTasks, meta: { requiresAuth: true, roles: ['employee', 'manager'] } },
  { path: '/settings', component: UserSettings, meta: { requiresAuth: true, roles: ['employee', 'manager'] } },
  { path: '/allusers', component: AllUsers, meta: { requiresAuth: true, roles: ['manager'] } },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// Navigation guard
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    if (!authState.isAuthenticated) {
      return next('/');
    }
    if (to.meta.roles && !to.meta.roles.includes(authState.user.role)) {
      alert('Access denied');
      return next(false);
    }
  }
  next();
});

export default router;
