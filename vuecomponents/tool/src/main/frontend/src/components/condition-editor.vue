<template>
  <div ref="component" class="condition-editor">
    <b>
    <label class="mb-2" for="create-condition">{{ labels.createCondition }} (item:{{ this.itemId }})</label>
    </b>
    <form id="create-condition" class="d-flex align-items-center gap-2">
      <span>Require this item to have a score</span>
      <div>
        <BFormSelect class="form-select" v-model="form.type" :options="types" :disabled="saving"></BFormSelect>
      </div>
      <BFormGroup>
        <BFormInput type="text" class="argument" v-model="form.argument" :disabled="saving" />
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
          <b>{{ typeLabel(condition.type) }} {{ condition.argument }}</b>
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
  @import "../bootstrap-styles/badges.scss";
  @import "../bootstrap-styles/buttons.scss";

  .condition {
    display: flex;
    justify-content: space-between;
  }

  .argument {
    width: 5em;
  }
</style>

<script>
// Vue and vue plugins
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
import {
  getConditionsForItem,
  createCondition,
  deleteCondition,
} from "../api/conditions-api.js";

import {
  isValidCondition,
} from "../utils/condition-utils.js";
// Libs
import { debounce } from "lodash";

Vue.use(BootstrapVueIcons)

const defaultType = "GREATER_THEN";
const defaultArgument = "";

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
  //mixins: [i18nMixin],
  props: {
    siteId: { type: String },
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
        argument: defaultArgument,
      },
      types: [
        {
          value: 'SMALLER_THEN',
          text: 'smaller then'
        },
        {
          value: 'EQUAL_AS',
          text: 'equal to'
        },
        {
          value: 'GREATER_THEN',
          text: 'greater then'
        }
      ]
    }
  },
  methods: {
    onConditionSaved(condition) {
        this.conditions.push(condition)
        this.saving = false;
        this.form.type = defaultType;
        this.form.argument = defaultArgument;
    },
    onConditionRemoved(conditionId) {
      // Remove condition form conditions
      this.conditions.splice(this.conditions.findIndex((c) => c.id === conditionId), 1);
    },
    async addCondition() {
      if (this.inputValid) {
        this.saving = true;

        const condition = {
          type: "POINTS",
          siteId: this.siteId,
          toolId: this.toolId,
          itemId: this.itemId,
          operator: this.form.type,
          argument: this.form.argument,
        };

        const createdCondition = await createCondition(condition);

        if (createdCondition) {
          this.onConditionSaved({...createdCondition, new: true });
        } else {
          console.error("Condition not created");
          this.saving = false;
        }
      }
    },
    async removeCondition(condition) {
      // Set saving true for condition
      this.conditions.splice(
        this.conditions.findIndex((c) => c.id === condition.id),
        1,
        { ...condition, saving: true }
      );

      const removedConditionId = await deleteCondition(condition);
      if (removedConditionId != null) {
        this.onConditionRemoved(condition.id);
      } else {
        console.log("Condition not deleted")
        this.conditions.splice(
          this.conditions.findIndex((c) => c.id === condition.id),
          1,
          { ...condition, saving: false }
        );
      }

    },
    typeLabel(type) {
      return this.types.find(t => t.value === type)?.text ?? "";
    },
    async loadData() {
      this.conditions = await getConditionsForItem(this.siteId, this.toolId, this.itemId) ?? [];
    },
  },
  computed: {
    inputValid() {
      const value = this.form.argument.trim();
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
  mounted() {
    this.loadData();

    // Setup watcher to load fresh data when siteId, toolId, or itemId changes, debounced by 100ms
    this.$watch((vm) => [vm.siteId, vm.toolId, vm.itemId], () => this.loadData());
  },
}
</script>
