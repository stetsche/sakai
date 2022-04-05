<template>
  <div
    :class="[
      { 'sakai-search': type == 'search' },
    ]"
  >
    <slot name="prepend" />
    <input
      v-model="value"
      @input="handleInput($event.target.value)"
      :id="id"
      :name="name"
      :type="type"
      :role="type == 'search' ? 'search' : undefined"
      :disabled="disabled"
      :placeholder="placeholder"
      :aria-label="arialabel"
      :class="inputClasses"
      :required="required"
    />
    <div v-if="validating && hasValidation" class="invalid-feedback">
      {{validationStatus.message}}
    </div>
    <slot name="append"></slot>
  </div>
</template>

<script>
export default {
  data() {
    return {
      validating: false,
    }
  },
  props: {
    value: {
      type: [Number, String, Array],
      default: undefined,
    },
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
    placeholder: {
      type: String,
      default: null,
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
    validate: {
      type: String,
      default: undefined,
    },
    required: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    inputClasses() {
      let classes = [];
      if(this.validating && this.hasValidation && !this.validationStatus.isValid) {
        classes.push("is-invalid");
      }
      if(this.type == "checkbox") {
        classes.push("form-check-input");
      } else {
        classes.push("form-control");
      }
      return classes.join(" ");
    },
    validations() {
      let validations = [];
      if(this.required) {
        validations.push({ type: "required" });
      }
      if(this.maxlength) {
        validations.push({
          type: "maxlength",
          value: this.maxlength
        });
      }
      if(this.minlength) {
        validations.push({
          type: "minlength",
          value: this.minlength
        })
      }
      return validations;
    },
    hasValidation() {
      return this.validations.length > 0;
    },
    validationStatus() {
      //Setup Status boject
      var status = {
        isValid: false,
        message: ''
      }
      //Return if we are not validating yet
      if(!this.validating) { return { } }
      //Go through validation options
      this.validations.forEach(validationInput => {
        let validationType = validationInput instanceof String || typeof validationInput === 'string'
          ? validationInput : validationInput.type;
        switch(validationType) {
          case 'required':
            if(this.type == "checkbox" && this.value == false) {
              status.message = validationInput.message ? validationInput.message : "This checkbox must be checked";
            } else if (this.value == undefined || this.value == "") {
              status.message = validationInput.message ? validationInput.message : "This field must be filled out";
            } else {
              status.isValid = true;
            }
            break;
          default:
            console.error("Unknown validation string", validationInput);
            break;
        }
      });
      return status;
    }
  },
  watch: {
    validationStatus(newStatus) {
      this.$emit('validation', { ...newStatus }.isValid );
    }
  },
  methods: {
    handleInput(value) {
      //Emit update:value to work with v-model:value
      this.$emit('update:value', value);
      //If we are not validating yet, we will want to after the first input
      if(!this.validating && this.hasValidation) {
        this.validating = true;
      }
    },
  }
};
</script>

<style>
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
</style>
