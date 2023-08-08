<template>
  <div class="condition-picker mb-5">
    <div class="saved-condition" v-if="savedCondition">
      <b>Selected condition:</b>
      <BCard class="mb-2">
        Require the item <b>{{ savedItem.name }}</b> to have a score {{ savedCondition.operator }} {{ savedCondition.argument }} Points
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
    <div class="mt-2" v-if="savedEntries?.length > 0">
      <b class="mb-2">Saved conditions:</b>
      <BListGroup>
        <BListGroupItem class="d-flex align-items-center" v-for="savedEntry in savedEntries" :key="savedEntry.condition.id">
          <span class="me-1">Require the item</span>
          <b>{{ savedEntry.toolItem.name }}</b>
          <span class="mx-1">to have a score</span>
          <b>{{ savedEntry.condition.operator }} {{ savedEntry.condition.argument }}</b>
          <span class="ms-1">Points</span>
          <div class="d-flex ms-auto align-items-center">
            <BButton @click="removeSubCondition(savedEntry.condition)" variant="link" title="Remove Condition">
              <BIcon v-if="!savedEntry.saving" icon="trash" aria-hidden="true" font-scale="1.2" />
              <BSpinner v-if="savedEntry.condition.saving" small aria-label="Removing condition" />
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
  import {
    getRootCondition,
    getToolItemsWithConditionsForLesson,
    updateCondition,
  } from '../api/conditions-api';

  // Use plugins
  Vue.use(BootstrapVueIcons)

  const defaultToolItems = [];
  const defaultRootCondition = null;
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
      siteId: { operator: String },
      toolId: { operator: String },
      itemId: { operator: String },
      lessonId: { operator: String },
    },
    data: function() {
      return {
        toolItems: defaultToolItems,
        rootCondition: defaultRootCondition,
        selectedItem: defaultSelectedItem,
        selectedCondition: defaultSelectedCondition,
        savedConditionId: null,
        saving: false,
        loading: true,
      }
    },
    computed: {
      itemOptions() {
        console.log(JSON.parse(JSON.stringify(this.toolItems)));
        return this.toolItems
            .filter((item) => this.itemId ? item.id != this.itemId : true)
            .map(item => {
              return {
                value: item.id,
                text: item.name,
              }
            });
      },
      conditionOptions() {
        if (this.selectedItem) {
          return this.toolItems.find(item => item.id === this.selectedItem).conditions.map(condition => {
            return {
              value: condition.id,
              text: formatCondition(null, null, null, condition),
            };
          });
        } else {
          return null;
        }
      },
      savedEntries() {
        return this.rootCondition?.subConditions.map((condition) => {
          return {
            condition,
            toolItem: this.toolItems.find((item) => item.id == condition.itemId) ?? null
          }
        })
      },
      savedItem() {
        if (!this.savedConditionId) return null;

        return this.toolItems.find(item => item.conditions.find(condition => condition.id == this.savedConditionId));
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
      onSubConditionRemoved(conditionId) {
        // Remove condition form conditions
        this.rootCondition.subConditions.splice(this.rootCondition.subConditions.findIndex((c) => c.id === conditionId), 1);
      },
      async removeSubCondition(condition) {
        // Set saving true for condition
        const index = this.rootCondition.subConditions.findIndex((c) => c.id === condition.id);
        this.rootCondition.subConditions.splice(index, 1, { ...condition, saving: true });

        const alteredRootCondition = {
          ...this.rootCondition,
          subConditions: this.rootCondition.subConditions.filter((c) => c.id != condition.id),
        };

        const updatedRootCondition = await updateCondition(alteredRootCondition);

        if (updatedRootCondition != null) {
          this.rootCondition = updatedRootCondition;
        } else {
          console.log("Condition not removed")
          this.rootCondition.subConditions.splice(index, 1, { ...condition, saving: false });
        }
      },
      async loadData() {
        const conditionsPromise = getToolItemsWithConditionsForLesson(this.siteId, this.lessonId);
        const rootConditionPromise = getRootCondition(this.siteId, this.toolId, this.itemId);

        const [ conditions, rootCondition ] = await Promise.all([ conditionsPromise, rootConditionPromise ]);

        this.toolItems = conditions ?? defaultToolItems;
        this.rootCondition = rootCondition ?? defaultRootCondition;
        console.log("rootCondition", rootCondition)
      }
    },
    mounted() {
      this.loadData();

      // Setup watcher to load fresh data when siteId, toolId, or itemId changes, debounced by 100ms
      this.$watch((vm) => [vm.siteId, vm.toolId, vm.itemId], () => this.loadData());
    }
  };
</script>
