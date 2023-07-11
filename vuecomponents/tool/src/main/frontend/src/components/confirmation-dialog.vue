<template>
  <div>
    <button @click="showModal">show</button>
    <BModal ref="modal" hide-backdrop></BModal>
  </div>
</template>

<style>
  @import "../../node_modules/bootstrap/dist/css/bootstrap.css";
</style>

<script>
import i18nMixin from "../mixins/i18n-mixin.js";
import { BModal } from 'bootstrap-vue';

export default {
  name: "confirmation-dialog",
  mixins: [i18nMixin],
  components: {
    BModal
  },
  methods: {
    showModal: function() {
        this.$refs.modal.show();
    },
    hideModal: function() {
        this.$refs.modal.hide();
    },
  }
};
</script>
