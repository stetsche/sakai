<template>
  <div class="condition-editor mb-5">
    CONDITION PICKER:
    <div class="condition">
      <BFormGroup label="Lessons item" class="mb-2">
        <BFormSelect plain v-model="selectedItem" :options="itemOptions"></BFormSelect>
      </BFormGroup>
      <BFormGroup :disabled="!selectedItem" label="Condition" class="mb-2">
        <BFormSelect plain v-model="selectedCondition" :options="conditionOptions"></BFormSelect>
      </BFormGroup>
      <BButton variant="primary" class="ms-auto">Set condition as Requirement</BButton>
    </div>
  </div>
</template>

<style lang="scss">
  @import "../../node_modules/bootstrap/dist/css/bootstrap.css";
  @import "../bootstrap-styles/buttons.scss";

</style>

<script>
import { BListGroup, BListGroupItem, BFormSelect, BFormGroup, BButton } from 'bootstrap-vue';
import i18nMixin from "../mixins/i18n-mixin.js";
import { formatCondition } from "../util/condition-util.js";

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
  },
  methods: {
  },
  async mounted() {
    this.items = [
      {
        id: "1",
        text: "answer this question",
        conditions: [
          {
            id: "a",
            type: "smaller_then",
            attribute: "5",
          },
          {
            id: "b",
            type: "greater_then",
            attribute: "10",
          },
        ]
      },
      {
        id: "2",
        text: "asdf",
        conditions: [
          {
            id: "c",
            type: "smaller_then",
            attribute: "4",
          },
          {
            id: "d",
            type: "greater_then",
            attribute: "12",
          },
        ]
      },
      {
        id: "3",
        text: "test [Open on: Apr 19, 2023, 11:15:00 AM] [Due on: Apr 26, 2023, 11:15:00 AM]",
        conditions: [
          {
            id: "e",
            type: "greater_then",
            attribute: "8",
          },
        ]
      },
    ];
    // const conditions = await getConditions(this.toolId, this.itemId);
    // this.conditions = conditions ?? [];
  }
};
</script>
