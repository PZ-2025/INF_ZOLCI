import { createRouter, createWebHistory } from 'vue-router';
import { reactive } from 'vue';

import Teams from '@/components/Teams.vue';
import AllEmployees from '@/components/AllEmployees.vue';
import LoginForm from '@/components/LoginForm.vue';
import RaportGenerate from '@/components/RaportGenerate.vue';
import SystemConf from '@/components/SystemConf.vue';
import TasksHistory from '@/components/TasksHistory.vue';
import TeamDetails from '@/components/TeamDetails.vue';
import TeamsTasks from '@/components/TeamsTasks.vue';
import UserSettings from '@/components/UserSettings.vue';
import AllUsers from '@/components/AllUsers.vue';
import AddEmployee from '@/components/AddEmployee.vue';
import AddTeam from '@/components/AddTeam.vue';
import AddTask from '@/components/AddTask.vue';

export const authState = reactive({
  isAuthenticated: false,
  user: null,
});

const routes = [
  { path: '/', component: LoginForm },
  { path: '/teams', name: "teams", component: Teams, meta: { requiresAuth: true }, props: true },
  { path: '/addteam', name: "addTeam", component: AddTeam, meta: { requiresAuth: true, roles: ['manager'] } },
  { path: '/teamdetails/:id', name: "teamDetails", component: TeamDetails, props: true },
  { path: '/teamtasks/:id', name: "teamTasks", component: TeamsTasks, props: true, meta: { requiresAuth: true } },
  { path: '/allemployees', name: "allEmployees", component: AllEmployees, meta: { requiresAuth: true, roles: ['manager'] } },
  { path: '/addemployee', name: "addEmployee", component: AddEmployee, meta: { requiresAuth: true, roles: ['manager'] } },
  { path: '/raportgenerate', name: "raportGenerate", component: RaportGenerate, meta: { requiresAuth: true, roles: ['manager'] } },
  { path: '/systemconf', name: "systemConf", component: SystemConf, meta: { requiresAuth: true, roles: ['manager'] } },
  { path: '/taskshistory', name: "tasksHistory", component: TasksHistory, meta: { requiresAuth: true } },
  { path: '/addtask', name: "addTask", component: AddTask, meta: { requiresAuth: true, roles: ['manager'] } },
  { path: '/settings', name: "userSettings", component: UserSettings, meta: { requiresAuth: true } },
  { path: '/allusers', name: "allUsers", component: AllUsers, meta: { requiresAuth: true, roles: ['manager'] } },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  console.log(`Router: nawigacja z "${from.path}" do "${to.path}"`);
  console.log("Parametry trasy:", to.params);

  if (to.name === 'teamDetails' || to.path.startsWith('/teamdetails/')) {
    console.log("DEBUG - TeamDetails route - ID param:", to.params.id);
  }

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