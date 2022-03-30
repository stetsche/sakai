import { createRouter, createWebHistory } from 'vue-router';
import Main from '../views/Main.vue';

const routeprefix = '/portal/site/:siteid/tool/:toolid';
const routes = [
  {
    path: routeprefix + '/',
    name: 'Main',
    component: Main,
    props: true
  },
  {
    path: routeprefix + '/meeting',
    name: 'EditMeeting',
    props: true,
    component: function () {
      return import('../views/CreateMeeting.vue')
    }
  },
  {
    path: routeprefix + '/permissions',
    name: 'Permissions',
    props: true,
    component: function () {
      return import('../views/Permissions.vue')
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
