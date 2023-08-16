<template>
  <div class="condition-picker">
    <div class="condition" v-if="conditionsSelectable">
      <b>{{ i18n["pick_condition_as_prereq"] }}</b>
      <BFormGroup label="Lessons item" class="mb-2">
        <BFormSelect plain v-model="selectedItemOption" :options="itemOptions"></BFormSelect>
      </BFormGroup>
      <BFormGroup :disabled="!selectedItemOption" label="Condition" class="mb-2">
        <BFormSelect plain v-model="selectedConditionOption" :options="conditionOptions"></BFormSelect>
      </BFormGroup>
      <BButton @click="onAddCondition" :disabled="!selectionValid" variant="primary">
        <BSpinner v-if="saving" small aria-label="Saving condition as requirement" />
        <BIcon v-else icon="plus-circle" aria-hidden="true" />
        {{ i18n["add_condition_as_prereq"] }}
      </BButton>
    </div>
    <div class="condition" v-else>
      <BAlert show variant="info" class="mb-2">
        {{ i18n["no_condition_to_pick"] }}
      </BAlert>
    </div>
    <div class="mt-2" v-if="savedEntries?.length > 0">
      <b class="mb-2">{{ i18n["existing_prereq_conditions"] }}</b>
      <BListGroup>
        <BListGroupItem class="d-flex align-items-center" v-for="savedEntry in savedEntries" :key="savedEntry.condition.id">
          <ConditionText :item="savedEntry.toolItem?.name" :condition="savedEntry.condition" />
          <div class="d-flex ms-auto align-items-center">
            <BButton @click="onRemoveSubCondition(savedEntry.condition)" variant="link" title="Remove Condition">
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
  @import "../bootstrap-styles/alert.scss";
  @import "../bootstrap-styles/buttons.scss";
  @import "../bootstrap-styles/card.scss";
</style>

<script>
  // Vue and vue plugins
  import Vue from 'vue';
  import { BootstrapVueIcons } from 'bootstrap-vue';

  // Components
  import {
    BAlert,
    BButton,
    BCard,
    BFormGroup,
    BFormSelect,
    BIcon,
    BListGroup,
    BListGroupItem,
    BSpinner
  } from 'bootstrap-vue';

  import ConditionText from "./condition-text.vue";

  // Mixins
  import i18nMixin from "../mixins/i18n-mixin.js";

  // Utils
  import {
    formatCondition,
    makeRootCondition,
  } from "../utils/condition-utils.js";

  // Api
  import {
    createCondition,
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
      BAlert,
      BButton,
      BCard,
      BFormGroup,
      BFormSelect,
      BIcon,
      BListGroup,
      BListGroupItem,
      BSpinner,
      ConditionText,
    },
    mixins: [ i18nMixin ],
    props: {
      siteId: { type: String },
      toolId: { type: String },
      itemId: { type: String },
      lessonId: { type: String },
    },
    data: function() {
      return {
        i18nBundleName: "condition",
        toolItems: defaultToolItems,
        rootCondition: defaultRootCondition,
        selectedItemOption: defaultSelectedItem,
        selectedConditionOption: defaultSelectedCondition,
        savedConditionId: null,
        saving: false,
        loading: true,
      }
    },
    computed: {
      availableToolItems() {
        return this.toolItems
            .filter((item) => this.itemId ? item.id != this.itemId : true);
      },
      disabledToolItems() {
        return this.availableToolItems.filter((item) => item.conditions.filter((c) => this.isConditionSelectable(c)).length == 0);
      },
      itemOptions() {
        return this.availableToolItems.map(item => {
          return {
            value: item.id,
            text: item.name,
            disabled: !!this.disabledToolItems.find((i) => i.id == item.id),
          }
        });
      },
      conditionsSelectable() {
        return this.itemOptions.filter((sco) => !sco.disabled).length > 0;
      },
      selectedToolItem() {
        return this.selectedItemOption
            ? this.availableToolItems.find((item) => item.id == this.selectedItemOption)
            : null;
      },
      selectableConditions() {
        return this.toolItems.filter((item) => this.itemId ? item.id != this.itemId : true);
      },
      conditionOptions() {
        return this.selectedToolItem
            ? this.selectedToolItem.conditions.map(condition => {
                return {
                  value: condition.id,
                  text: formatCondition(null, null, null, condition),
                  disabled: !!this.savedConditions.find((c) => c.id == condition.id),
                };
              })
            : null;
      },
      selectedCondition() {
        return this.selectedConditionOption
            ? this.selectedToolItem.conditions.find((c) => c.id == this.selectedConditionOption)
            : null;
      },
      // conditionOptions() {
      //   if (this.selectedItemOption) {
      //     return this.toolItems.find(item => item.id === this.selectedItemOption).conditions.map(condition => {
      //       return {
      //         value: condition.id,
      //         text: formatCondition(null, null, null, condition),
      //       };
      //     });
      //   } else {
      //     return null;
      //   }
      // },
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
        return this.selectedItemOption && this.selectedConditionOption;
      },
      savedConditions() {
        return this.rootCondition?.subConditions ?? [];
      }
    },
    methods: {
      isConditionSelectable(condition) {
        return !this.savedConditions.find((c) => c.id == condition.id);
      },
      async getOrCreateRootCondition() {
        if (this.rootCondition == defaultRootCondition) {
          this.rootCondition = await createCondition(makeRootCondition(this.siteId, this.toolId, this.itemId));
        }

        return this.rootCondition;
      },
      onAddCondition() {
        this.addCondition({...this.selectedCondition}).then(
            (rootCondition) => this.onSubConditionAdded(rootCondition),
            (error) => this.onError(error)
        );
      },
      async addCondition(condition) {
        const modifiedRootCondition = { ...await this.getOrCreateRootCondition() };
        modifiedRootCondition.subConditions.push(condition)

        const updatedRootCondition = await updateCondition(modifiedRootCondition);

        if (updatedRootCondition != null) {
          return updatedRootCondition;
        } else {
          throw new Error("Condition could not be added");
        }
      },
      onSubConditionAdded(rootCondition) {
        this.selectedItemOption = null;
        this.selectedConditionOption = null;
        this.rootCondition = rootCondition;
      },
      saveCondition() {
        if (this.selectionValid) {
          this.savedConditionId = this.selectedConditionOption;
        }
      },
      changeCondition() {
        this.savedConditionId = defaultSelectedItem;
      },
      onRemoveSubCondition(condition) {
        // Set saving true for condition
        const index = this.rootCondition.subConditions.findIndex((c) => c.id === condition.id);
        this.rootCondition.subConditions.splice(index, 1, { ...condition, saving: true });

        this.removeSubCondition(condition).then(
            (rootCondition) => {
              this.onSubConditionRemoved(rootCondition)
            },
            (error) => {
              this.onError(error);
              this.rootCondition.subConditions.splice(index, 1, { ...condition, saving: false });
            }
        );
      },
      async removeSubCondition(condition) {
        const modifiedRootCondition = {
          ...this.rootCondition,
          subConditions: this.rootCondition.subConditions.filter((c) => c.id != condition.id),
        };

        const updatedRootCondition = await updateCondition(modifiedRootCondition);

        if (updatedRootCondition != null) {
          return updatedRootCondition;
        } else {
          throw new Error("Condition could not be removed");
        }
      },
      onSubConditionRemoved(rootCondition) {
        this.rootCondition = rootCondition;
      },
      onError(error) {
        console.error(error)
      },
      async loadData() {
        const conditionsPromise = getToolItemsWithConditionsForLesson(this.siteId, this.lessonId);
        const rootConditionPromise = getRootCondition(this.siteId, this.toolId, this.itemId);

        const [ conditions, rootCondition ] = await Promise.all([ conditionsPromise, rootConditionPromise ]);

        this.toolItems = conditions ?? defaultToolItems;
        this.rootCondition = rootCondition ?? defaultRootCondition;
      }
    },
    mounted() {
      this.loadData();

      // Setup watcher to load fresh data when siteId, toolId, or itemId changes, debounced by 100ms
      this.$watch((vm) => [vm.siteId, vm.toolId, vm.itemId], () => this.loadData());
    }
  };
</script>
