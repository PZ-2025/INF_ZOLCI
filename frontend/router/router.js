import { createRouter, createWebHistory } from 'vue-router';
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

const routes = [
  { path: "/", component: Teams }, // Default route
  { path: "/teams", component: Teams },
  { path: '/allemployees', component: AllEmployees },
  { path: '/login', component: LoginForm },
  { path: '/raportgenerate', component: RaportGenerate },
  { path: '/systemconf', component: SystemConf },
  { path: '/taskshistory', component: TasksHistory },
  { path: "/teamdetails", component: TeamDetails },
  { path: "/teamtasks", component: TeamsTasks },
  { path: '/settings', component: UserSettings },
  { path: '/allusers', component: AllUsers },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
