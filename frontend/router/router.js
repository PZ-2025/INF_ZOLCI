import { createRouter, createWebHistory } from 'vue-router';
import { reactive } from 'vue';
import authService from '@/services/authService';

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
import EditTask from '@/components/TaskEdit.vue';
import TaskDetails from '@/components/TaskDetails.vue';
import TeamMembersManage from '@/components/TeamMembersManage.vue';

export const authState = reactive({
  isAuthenticated: false,
  user: null,
});

const routes = [
  { path: '/', component: LoginForm },
  { path: '/teams', name: "teams", component: Teams, meta: { requiresAuth: true }, props: true },
  { path: '/addteam', name: "addTeam", component: AddTeam, meta: { requiresAuth: true, minRole: 'manager' } },
  { path: '/teamdetails/:id', name: "teamDetails", component: TeamDetails, props: true },
  { path: '/teamtasks/:id', name: "teamTasks", component: TeamsTasks, props: true, meta: { requiresAuth: true } },
  { path: '/allemployees', name: "allEmployees", component: AllEmployees, meta: { requiresAuth: true, minRole: 'manager' } },
  { path: '/addemployee', name: "addEmployee", component: AddEmployee, meta: { requiresAuth: true, minRole: 'manager' } },
  { path: '/raportgenerate', name: "raportGenerate", component: RaportGenerate, meta: { requiresAuth: true, minRole: 'manager' } },
  { path: '/systemconf', name: "systemConf", component: SystemConf, meta: { requiresAuth: true, requiredRole: 'ADMIN' } },
  { path: '/taskshistory', name: "tasksHistory", component: TasksHistory, meta: { requiresAuth: true } },
  { path: '/addtask', name: "addTask", component: AddTask, meta: { requiresAuth: true, minRole: 'manager' } },
  { path: '/settings', name: "userSettings", component: UserSettings, meta: { requiresAuth: true } },
  { path: '/allusers', name: "allUsers", component: AllUsers, meta: { requiresAuth: true, minRole: 'manager' } },
  { path: '/edittask', name: "editTask", component: EditTask, meta: { requiresAuth: true, minRole: 'manager' } },
  { path: '/taskdetails/:id', name: "taskDetails", component: TaskDetails, meta: { requiresAuth: true } },
  { 
    path: '/teammembers/:id',
    name: "teamMembers", 
    component: TeamMembersManage, 
    meta: { requiresAuth: true, minRole: 'manager' },
    props: route => ({ id: parseInt(route.params.id) || null })
  },
  { 
    path: '/users/:id/edit', 
    name: "editUser", 
    component: UserSettings, 
    meta: { requiresAuth: true, minRole: 'manager' },
    props: route => ({ userId: parseInt(route.params.id) || null })
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