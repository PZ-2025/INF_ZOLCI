import { createRouter, createWebHistory } from 'vue-router';
import { reactive } from 'vue';
import authService from '@/services/authService';

import Teams from '@/components/Teams.vue';
import TeamEdit from '@/components/TeamEdit.vue';
import AllEmployees from '@/components/AllEmployees.vue';
import LoginForm from '@/components/LoginForm.vue';
import RaportGenerate from '@/components/RaportGenerate.vue';
import RaportHistory from '@/components/RaportHistory.vue';
import RaportView from '@/components/RaportView.vue';
import SystemConf from '@/components/SystemConf.vue';
import TasksHistory from '@/components/TasksHistory.vue';
import TeamDetails from '@/components/TeamDetails.vue';
import TeamsTasks from '@/components/TeamsTasks.vue';
import UserSettings from '@/components/UserSettings.vue';
import AllUsers from '@/components/AllUsers.vue';
import AddEmployee from '@/components/AddEmployee.vue';
import AddTeam from '@/components/AddTeam.vue';
import AddTask from '@/components/AddTask.vue';
import EditTask from '@/components/TaskEdit.vue';
import TaskDetails from '@/components/TaskDetails.vue';
import TeamMembersManage from '@/components/TeamMembersManage.vue';
import RaportMenu from '@/components/RaportMenu.vue';
import TaskMenu from '@/components/TaskMenu.vue';
import AdminPanelMenu from '@/components/AdminPanelMenu.vue';

export const authState = reactive({
  isAuthenticated: false,
  user: null,
});

const routes = [
  { path: '/', component: LoginForm },
  { path: '/teams', name: "teams", component: Teams, meta: { requiresAuth: true }, props: true },
  { path: '/addteam', name: "addTeam", component: AddTeam, meta: { requiresAuth: true, minRole: 'kierownik' } },
  { path: '/teamdetails/:id', name: "teamDetails", component: TeamDetails, props: true },
  { path: '/teamtasks/:id', name: "teamTasks", component: TeamsTasks, props: true, meta: { requiresAuth: true } },
  { path: '/allemployees', name: "allEmployees", component: AllEmployees, meta: { requiresAuth: true, minRole: 'kierownik' } },
  { path: '/addemployee', name: "addEmployee", component: AddEmployee, meta: { requiresAuth: true, minRole: 'kierownik' } },
  { path: '/raport', name: "raportMenu", component: RaportMenu, meta: { requiresAuth: true, minRole: 'kierownik' } },
  { path: '/raportgenerate', name: "raportGenerate", component: RaportGenerate, meta: { requiresAuth: true, minRole: 'kierownik' } },
  { path: '/raporthistory', name: "raportHistory", component: RaportHistory, meta: { requiresAuth: true, minRole: 'kierownik' } },
  { path: '/tasks', name: "TaskMenu", component: TaskMenu, meta: { requiresAuth: true } },
  { path: '/addtask', name: "addTask", component: AddTask, meta: { requiresAuth: true, minRole: 'kierownik' } },
  { path: '/taskshistory', name: "tasksHistory", component: TasksHistory, meta: { requiresAuth: true } },
  { path: '/adminpanel', name: "adminPanelMenu", component: AdminPanelMenu, meta: { requiresAuth: true, requiredRole: 'administrator' } },
  { path: '/settings', name: "userSettings", component: UserSettings, meta: { requiresAuth: true } },
  { path: '/allusers', name: "allUsers", component: AllUsers, meta: { requiresAuth: true, minRole: 'kierownik' } },
  { path: '/systemconf', name: "systemConf", component: SystemConf, meta: { requiresAuth: true, requiredRole: 'administrator' } },
  { path: '/edittask/:id', name: "editTask", component: EditTask, meta: { requiresAuth: true, minRole: 'kierownik' }, props: true },
  { path: '/taskdetails/:id', name: "taskDetails", component: TaskDetails, meta: { requiresAuth: true } },
  {
    path: '/teammembers/:id',
    name: "teamMembers",
    component: TeamMembersManage,
    meta: { requiresAuth: true, minRole: 'kierownik' },
    props: route => ({ id: parseInt(route.params.id) || null })
  },
  {
    path: '/users/:id/edit',
    name: "editUser",
    component: UserSettings,
    meta: { requiresAuth: true, minRole: 'kierownik' },
    props: route => ({ userId: parseInt(route.params.id) || null })
  },
  {
    path: '/raportview/:id',
    name: "raportView",
    component: RaportView,
    meta: { requiresAuth: true, minRole: 'kierownik' },
    props: route => ({ id: parseInt(route.params.id) || null })
  },
    {
    path: '/teamedit/:id',
    name: "teamEdit",
    component: TeamEdit,
    meta: { requiresAuth: true, minRole: 'kierownik' },
    props: route => ({ id: parseInt(route.params.id) || null })
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    if (!authState.isAuthenticated) {
      return next('/');
    }

    if (to.meta.requiredRole && authState.user.role !== to.meta.requiredRole) {
      alert('Access denied - specific role required');
      return next(false);
    }

    if (to.meta.minRole && !authService.hasRoleAtLeast(to.meta.minRole)) {
      alert('Access denied - insufficient permissions');
      return next(false);
    }
  }
  next();
});

export default router;