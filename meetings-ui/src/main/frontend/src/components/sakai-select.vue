<template>
  <select
    class="form-select"
    :name="name"
    :id="id"
    :value="value"
    @change="$emit('change', $event.target.value)"
  >
    <option v-for="item in items" :key="item.value" :value="item.value">
      {{ item.string }}
    </option>
  </select>
</template>

<script>
import "/node_modules/bootstrap/js/dist/dropdown.js";
export default {
  components: {},
  props: {
    id: {
      type: String,
      default: null,
    },
    items: {
      type: Array,
      default: () => [
        {
          id: 0,
          icon: "fa-lock",
          string: "Permissions",
          url: "https://translate.google.es/?hl=es&sl=es&tl=en&op=translate",
        },
        {
          id: 1,
          icon: "fa-book",
          string: "Templates",
          url: "https://getbootstrap.com/docs/5.0/components/card/#list-groups",
        },
      ],
    },

    name: {
      type: String,
      default: "select",
    },
    value: {
      type: String,
      default: "string",
    },
    "item-key": {
      type: String,
      default: "id",
    },
  },
};
</script>

<style scoped>
@import "../assets/sakai-colors.css";
.form-select {
  background-color: var(--sakai-background-color-1);
  background-image: url("data:image/svg+xml;charset=utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16'%3E%3Cpath fill='grey' d='M2 5l6 6 6-6'/%3E%3C/svg%3E");
  color: var(--sakai-text-color-1);
  border: 1px solid var(--sakai-border-color);
  border-radius: 5px;
  width: 100%;
  height: 2.25rem;
}
.form-select:focus {
  outline: 3px solid var(--focus-outline-color);
}
</style>
