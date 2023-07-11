<template>
  <div class="condition-picker mb-5">
    <div class="saved-condition" v-if="savedCondition">
      <b>Selected condition:</b>
      <BCard class="mb-2">
        Require the item <b>{{ savedItem.text }}</b> to have a score {{ savedCondition.type }} {{ savedCondition.argument }} Points
      </BCard>
      <BButton @click="changeCondition" variant="primary">
        <BSpinner v-if="saving" small aria-label="Removing condition as requirement" />
        <BIcon v-else icon="trash" aria-hidden="true" />
        Remove condition as requirement
      </BButton>

    </div>
    <div class="condition" v-else>
      <b>Select a condition, that is required for this item to be visible: {{ savedConditionId }}</b>
      <BFormGroup label="Lessons item" class="mb-2">
        <BFormSelect plain v-model="selectedItem" :options="itemOptions"></BFormSelect>
      </BFormGroup>
      <BFormGroup :disabled="!selectedItem" label="Condition" class="mb-2">
        <BFormSelect plain v-model="selectedCondition" :options="conditionOptions"></BFormSelect>
      </BFormGroup>
      <BButton @click="saveCondition" :disabled="!selectionValid" variant="primary">
        <BSpinner v-if="saving" small aria-label="Saving condition as requirement" />
        <BIcon v-else icon="save" aria-hidden="true" />
        Save condition as Requirement
      </BButton>
    </div>
  </div>
</template>

<style lang="scss">
  @import "bootstrap/dist/css/bootstrap.css";
  @import "bootstrap-vue/dist/bootstrap-vue-icons.min.css";
  @import "../bootstrap-styles/buttons.scss";
  @import "../bootstrap-styles/card.scss";
</style>

<script>
  // Vue and vue plugins
  import Vue from 'vue';
  import { BootstrapVueIcons } from 'bootstrap-vue';

  // Components
  import {
    BButton,
    BCard,
    BFormGroup,
    BFormSelect,
    BIcon,
    BListGroup,
    BListGroupItem,
    BSpinner
  } from 'bootstrap-vue';

  // Mixins
  import i18nMixin from "../mixins/i18n-mixin.js";

  // Utils
  import { formatCondition } from "../utils/condition-utils.js";

  // Use plugins
  Vue.use(BootstrapVueIcons)

  const defaultSelectedCondition = null;
  const defaultSelectedItem = null;

  export default {
    name: "condition-picker",
    components: {
      BListGroup,
      BListGroupItem,
      BFormSelect,
      BFormGroup,
      BButton,
      BCard,
      BIcon,
      BSpinner,
    },
    mixins: [i18nMixin],
    props: {
      toolId: { type: String },
      collectionId: { type: String }    // const conditions = await getConditions(this.toolId, this.itemId);
    },
    data: function() {
      return {
        items: [],
        selectedItem: defaultSelectedItem,
        selectedCondition: defaultSelectedCondition,
        savedConditionId: null,
        saving: false,
        loading: true,
      }
    },
    computed: {
      itemOptions() {
        return this.items.map(item => {
          return {
            value: item.id,
            text: item.text,
          }
        });
      },
      conditionOptions() {
        if (this.selectedItem) {
          return this.items.find(item => item.id === this.selectedItem).conditions.map(condition => {
            return {
              value: condition.id,
              text: formatCondition(null, null, null, condition),
            };
          });
        } else {
          return null;
        }
      },
      savedItem() {
        if (!this.savedConditionId) return null;

        return this.items.find(item => item.conditions.find(condition => condition.id == this.savedConditionId));
      },
      savedCondition() {
        if (!this.savedItem) return null;

        return this.savedItem.conditions.find(condition => condition.id == this.savedConditionId);
      },
      selectionValid() {
        return this.selectedItem && this.selectedCondition;
      }
    },
    methods: {
      saveCondition() {
        if (this.selectionValid) {
          this.savedConditionId = this.selectedCondition;
        }
      },
      changeCondition() {
        this.savedConditionId = defaultSelectedItem;
      },
    },
    mounted() {
      this.items = [
        {
          id: "1",
          text: "Quiz 1 [Open on: Apr 19, 2023, 11:15:00 AM] [Due on: Apr 26, 2023, 11:15:00 AM]",
          conditions: [
            {
              id: "a",
              type: "SMALLER_THEN",
              argument: "5",
            },
            {
              id: "b",
              type: "GREATER_THEN",
              argument: "10",
            },
          ]
        },
        {
          id: "2",
          text: "Quiz 2 [Open on: Apr 19, 2023, 11:15:00 AM] [Due on: Apr 26, 2023, 11:15:00 AM]",
          conditions: [
            {
              id: "c",
              type: "SMALLER_THEN",
              argument: "4",
            },
            {
              id: "d",
              type: "GREATER_THEN",
              argument: "12",
            },
          ]
        },
        {
          id: "3",
          text: "Quiz 3 [Open on: Apr 19, 2023, 11:15:00 AM] [Due on: Apr 26, 2023, 11:15:00 AM]",
          conditions: [
            {
              id: "e",
              type: "GREATER_THEN",
              argument: "8",
            },
          ]
        },
      ];
      // const conditions = await getConditions(this.toolId, this.itemId);
      // this.conditions = conditions ?? [];
    }
  };
</script>
