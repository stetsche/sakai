<template>
  <div
    :class="[
      'sakai-wrapper',
      { 'sakai-search': type == 'search' },
      { 'sakai-disabled': disabled },
      { 'sakai-invalid': invalid },
      null,
    ]"
  >
    <slot name="prepend" />
    <input
      v-model="internalValue"
      :id="id"
      :name="name"
      :type="type"
      :role="type == 'search' ? 'search' : null"
      :disabled="disabled"
      :placeholder="placeholder"
      :aria-label="arialabel"
    />

    <slot name="append">
      <!-- <sakai-icon
          v-if="type == 'date'"
          class="icon-append"
          iconkey="calendar"
        ></sakai-icon> -->
    </slot>
  </div>
</template>

<script>
// import sakaiIcon from "./sakai-icon.vue";
export default {
  // components: { sakaiIcon },
  data() {
    return {
      internalValue: null,
    };
  },
  watch: {
    internalValue(newValue) {
      this.$emit("input", newValue);
    },
  },
  props: {
    id: {
      type: String,
      default: null,
    },
    type: {
      type: String,
      default: "text",
    },
    name: {
      type: String,
      default: "input",
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    invalid: {
      type: Boolean,
      default: false,
    },
    placeholder: {
      type: String,
      default: null,
    },
    value: {
      type: [String, Boolean, Number],
      default: "",
    },
    min: {
      //for input number
      type: [String, Number],
      default: undefined,
    },
    arialabel: {
      type: String,
      default: undefined,
    },
  },
  mounted() {
    this.internalValue = this.value;
  },
  model: {
    prop: "value",
    event: "input",
  },
};
</script>

<style>
@import "../assets/sakai-colors.css";
.sakai-wrapper {
  position: relative;
  display: flex;
}
.sakai-search {
  min-width: 100px;
  background: var(--sakai-background-color-1);
  border: 1px solid var(--sakai-border-color);
  border-radius: 5px;
  height: 2.25rem;
  width: 100%;
}
.sakai-search:focus-within,
input:focus {
  outline: 3px solid var(--focus-outline-color);
}
input {
  background: var(--sakai-background-color-1);
  color: var(--sakai-text-color-1);
  border: 1px solid var(--sakai-border-color);
  border-radius: 5px;
  box-shadow: none;
  padding: 0.375rem;
  width: 100%;
}
::placeholder {
  color: var(--sakai-text-color-dimmed);
}
input[type="search"] {
  color: var(--sakai-text-color-1);
  background: transparent;
  border: none;
  outline: none;
  padding: 0.375rem;
  width: 100%;
}
input[type="checkbox"] {
  appearance: none;
  height: 15px;
  width: 15px;
  border-radius: 3px;
  background-color: var(--sakai-background-color-1);
  border: 1px solid var(--sakai-border-color);
}
input[type="checkbox"]:checked {
  background-image: url("data:image/svg+xml;charset=utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='2 2 12 12'%3E%3Cpath fill='white' d='M10.97 4.97a.75.75 0 0 1 1.07 1.05l-3.99 4.99a.75.75 0 0 1-1.08.02L4.324 8.384a.75.75 0 1 1 1.06-1.06l2.094 2.093 3.473-4.425a.267.267 0 0 1 .02-.022z'/%3E%3C/svg%3E");
  background-color: var(--sakai-color-blue--darker-3);
}
input[type="date"]::-webkit-calendar-picker-indicator,
input[type="datetime-local"]::-webkit-calendar-picker-indicator {
  background-image: url("data:image/svg+xml;charset=utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='-2 -2 20 20'%3E%3Cpath fill='grey' d='M3.5 0a.5.5 0 0 1 .5.5V1h8V.5a.5.5 0 0 1 1 0V1h1a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V3a2 2 0 0 1 2-2h1V.5a.5.5 0 0 1 .5-.5zM1 4v10a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V4H1z'/%3E%3C/svg%3E");
  cursor: pointer;
}
input[type="date"]::-webkit-calendar-picker-indicator:focus-visible,
input[type="datetime-local"]::-webkit-calendar-picker-indicator:focus-visible {
  outline: 3px solid var(--focus-outline-color);
}
.search-icon {
  padding: 0 0 0 8px;
  align-self: center;
}
.icon-append {
  padding: 0 8px 0 0;
  align-self: center;
}
.sakai-invalid {
  outline: 1px solid rgb(205 137 137);
  color: rgb(205 137 137);
}
.sakai-disabled {
  outline: 1px solid rgb(255, 255, 255);
  color: rgb(217 217 217);
}
</style>
