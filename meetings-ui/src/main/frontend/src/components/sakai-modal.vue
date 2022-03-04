<template>
  <div>
    <div
      data-bs-toggle="modal"
      data-bs-target="#modal"
      style="width: fit-content"
    >
      <slot name="activator"> </slot>
    </div>

    <div
      class="modal fade"
      id="modal"
      tabindex="-1"
      aria-labelledby="modalLabel"
      aria-hidden="true"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="modalLabel">{{ title }}</h5>
            <sakai-button
              data-bs-dismiss="modal"
              text="Close"
              :textHidden="true"
              :clear="true"
              :circle="true"
            >
              <template #append>
                <sakai-icon iconkey="close" />
              </template>
            </sakai-button>
          </div>
          <div v-if="$slots.body" class="modal-body">
            <slot name="body"></slot>
          </div>
          <div v-if="$slots.footer" class="modal-footer">
            <slot name="footer"> </slot>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// eslint-disable-next-line
import { Modal } from "bootstrap";
import SakaiButton from "./sakai-button.vue";
import SakaiIcon from "./sakai-icon.vue";
export default {
  components: { SakaiButton, SakaiIcon },
  props: {
    title: {
      type: String,
      default: "Modal title",
    },
  },
};
</script>

<style scoped>
.modal-content {
  background-color: var(--sakai-background-color-1);
}
.modal-header {
  border-bottom: none !important;
}
.modal-footer {
  border-top: none !important;
}
</style>
