import Vue from "vue";
import VueRouter from "vue-router";
import Main from "./pages/Main.vue";
import Settings from "./pages/CreateMeetting.vue";
import Permissions from "./pages/Permissions.vue";
import App from "./App.vue";

const routes = [
  { path: "/portal/site/:siteid/tool/:toolid", name:"Main", component: Main, props: true},
  { path: "/portal/site/:siteid/tool/:toolid/settings", name: "Settings", component: Settings, props: true },
  { path: "/portal/site/:siteid/tool/:toolid/permissions", name: "Permissions", component: Permissions, props: true },
];
const router = new VueRouter({ mode: "history", routes: routes });

Vue.use(VueRouter);
Vue.config.productionTip = false;
new Vue({
  router,
  render: (h) => h(App),
}).$mount("#app");
