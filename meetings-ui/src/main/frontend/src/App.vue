<template>
  <div id="app" class="p-3">
    <div class="d-flex 1 mb-2 gap-1 div-heigth">
      <sakai-icon iconkey="videocamera" />
      <router-link to="/meetings-ui" title="Tool Home" class="tool-heading"
        >MEETINGS</router-link
      >
      <SakaiButton
        id="themeswitch"
        class="ms-auto"
        @click="switchtheme()"
        text="Switch theme"
        :textHidden="true"
      >
        <template #prepend>
          <i class="bi bi-moon-fill"></i>
        </template>
      </SakaiButton>
      <SakaiButton text="Help">
        <template #prepend>
          <sakai-icon class="me-1" iconkey="question" />
        </template>
      </SakaiButton>
      <SakaiButton text="Maximize" :textHidden="true">
        <template #prepend>
          <sakai-icon iconkey="maximize" />
        </template>
      </SakaiButton>
    </div>
    <router-view></router-view>
  </div>
</template>
<script>
import "vue-router";
import SakaiButton from "./components/sakai-button.vue";
import SakaiIcon from "./components/sakai-icon.vue";
import toggletheme from "./assets/toggletheme.js";

// eslint-disable-next-line
import { Dropdown } from "bootstrap";
export default {
  name: "app",
  components: {
    SakaiButton,
    SakaiIcon,
  },
  methods: {
    switchtheme: function () {
      toggletheme();
    },
  },
  props: {
    siteid: { Type: String, default: "test" },
    toolid: { Type: String, default: "test" },
  }
};
</script>

<style>
@import "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.6.3/css/font-awesome.min.css";
@import "https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.0/font/bootstrap-icons.css";
@import "../node_modules/bootstrap/dist/css/bootstrap.min.css";
@import "./assets/page.css";
.icon-wrap {
  margin: 4px 4px 0px 0px;
}
.div-heigth {
  line-height: 1.4rem;
}
.overflow {
  overflow: hidden;
  position: fixed;
}
</style>
