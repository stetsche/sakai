<template>
  <div ref="component" class="condition-editor">
    <b>
    <label class="mb-2" for="create-condition">{{ labels.createCondition }}</label>
    </b>
    <form id="create-condition" class="d-flex align-items-center gap-2">
      <span>Require this item to have a score</span>
      <div>
        <BFormSelect class="form-select" v-model="form.type" :options="types" :disabled="saving"></BFormSelect>
      </div>
      <BFormGroup>
        <BFormInput type="text" class="attribute" v-model="form.attribute" :disabled="saving" />
      </BFormGroup>
      <span>Points</span>
      <BButton class="ms-auto" variant="primary" @click="addCondition" :disabled="!inputValid || saving">
        <BIcon v-if="!saving" icon="plus-circle" aria-hidden="true" />
        <BSpinner v-if="saving" small aria-label="Saving condition" />
        Add condition
      </BButton>
    </form>
    <div class="mt-2" v-if="conditions.length > 0">
      <b class="mb-2">{{ labels.existingConditions }}</b>
      <BListGroup>
        <BListGroupItem class="d-flex align-items-center" v-for="condition in conditions" :key="condition.id">
          <span class="me-1">Require this item to have a score</span>
          <b>{{ typeLabel(condition.type) }} {{ condition.attribute }}</b>
          <span class="ms-1">Points</span>
          <div class="d-flex ms-auto align-items-center">
            <BBadge v-if="condition.new" variant="success">NEW</BBadge>
            <BButton @click="removeCondition(condition)" variant="link" title="Remove Condition">
              <BIcon v-if="!condition.saving" icon="trash" aria-hidden="true" font-scale="1.2" />
              <BSpinner v-if="condition.saving" small aria-label="Removing condition" />
            </BButton>
          </div>
        </BListGroupItem>
      </BListGroup>
    </div>
  </div>
</template>

<style lang="scss">
  @import "bootstrap/dist/css/bootstrap.css";
  @import "bootstrap-vue/dist/bootstrap-vue-icons.min.css";
  @import "../bootstrap-styles/buttons.scss";
  @import "../bootstrap-styles/form.scss";

  .condition {
    display: flex;
    justify-content: space-between;
  }

  .attribute {
    width: 5em;
  }

  .badge-success {
    background-color: var(--successBanner-bordercolor);
  }
</style>

<script>
// Vue and plugins
import Vue from 'vue';
import { BootstrapVueIcons } from 'bootstrap-vue';

// Components
import {
  BButton,
  BFormGroup,
  BFormInput,
  BFormSelect,
  BIcon,
  BListGroup,
  BListGroupItem,
  BSpinner,
  BBadge,
} from 'bootstrap-vue';

// Mixins
import i18nMixin from "../mixins/i18n-mixin.js";

// API
import { getConditions } from "../api/conditions-api.js";

Vue.use(BootstrapVueIcons)

const defaultType = "greater_then";
const defaultAttribute = "";

export default {
  name: "condition-editor",
  components: {
    BButton,
    BFormGroup,
    BFormInput,
    BFormSelect,
    BIcon,
    BListGroup,
    BListGroupItem,
    BSpinner,
    BBadge,
  },
  mixins: [i18nMixin],
  props: {
    toolId: { type: String },
    itemId: { type: String },
    labelCreateCondition: { type: String, default: null },
    labelExistingConditions : { type: String, default: null },
  },
  data: function() {
    return {
      saving: false,
      conditions: [],
      form: {
        type: defaultType,
        attribute: defaultAttribute,
      },
      types: [
        {
          value: 'smaller_then',
          text: 'smaller then'
        },
        {
          value: 'equal_to',
          text: 'equal to'
        },
        {
          value: 'greater_then',
          text: 'greater then'
        }
      ]
    }
  },
  methods: {
    addCondition() {
      if (this.inputValid) {
        this.saving = true;
        setTimeout(() => {
          this.onConditionSaved({
            id: "new-condition-" + this.conditions.length,
            type: this.form.type,
            attribute: this.form.attribute,
            new: true
          });
        }, 1000);
      }
    },
    onConditionSaved(condition) {
        this.conditions.push(condition)
        this.saving = false;
        this.form.type = defaultType;
        this.form.attribute = defaultAttribute;
    },
    removeCondition(condition) {
      const conditionId = condition.id;

      // Set saving true for condition
      this.conditions.splice(
        this.conditions.findIndex((c) => c.id === conditionId),
        1,
        { ...condition, saving: true }
      );

      setTimeout(() => {
        this.onConditionRemoved(conditionId);
      }, 1000);
    },
    onConditionRemoved(conditionId) {
      // Remove condition form conditions
      this.conditions.splice(this.conditions.findIndex((c) => c.id === conditionId), 1);
    },
    typeLabel(type) {
      return this.types.find(t => t.value === type)?.text ?? "";
    },
  },
  computed: {
    inputValid() {
      const value = this.form.attribute.trim();
      return value != ""
          ? Number(value) != NaN && Number(value) >= 0
          : null;
    },
    labels() {
      return {
        createCondition: this.labelCreateCondition ?? "Create new condition based on this item:",
        existingConditions: this.labelExistingConditions ?? "Existing conditions based on this item:",
      };
    }
  },
  async mounted() {

    const conditions = await getConditions(this.toolId, this.itemId);
    this.conditions = conditions ?? [];

    }
};
</script>
