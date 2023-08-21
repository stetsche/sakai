<template>
  <div class="condition-text d-flex gap-1" v-html="formattedText"></div>
</template>

<script>
  // Mixins
  import i18nMixin from "../mixins/i18n-mixin.js";

  // Utils
  import {
    CONDITION_BUNDLE_NAME,
    ConditionType,
    formatOperator
  } from "../utils/condition-utils.js";

  export default {
    name: "condition-text",
    mixins: [ i18nMixin ],
    props: {
      item: { type: String },
      condition: { type: Object },
    },
    data: function() {
      return {
        i18nBundleName: CONDITION_BUNDLE_NAME,
      }
    },
    methods: {
      isConditionType(type) {
        return condition && condition.type == type;
      },
      formatText(text, ...inserts) {
        let formattedText = text;

        inserts?.forEach((insert, index) => {
          const boldInsert = `<b>${insert}</b>`;

          formattedText = formattedText?.replace(`{${index}}`, boldInsert);
        });

        return formattedText
      },
    },
    computed: {
      formattedText() {
        switch(this.condition.type) {
          case ConditionType.POINTS:
            const commonParams = [ this.formattedOperator, this.condition.argument ];

            if (this.item) {
              return this.formatText(this.i18n["display_the_item_points"], this.item, ...commonParams);
            } else {
              return this.formatText(this.i18n["display_this_item_points"], ...commonParams);
            }
          default:
            console.error(`Formatting of condition with type '${this.condition.type}' is not implemented`);
            return this.i18n["unknown_condition"];
        }
      },
      formattedOperator() {
        return this.condition.operator ? formatOperator(this.i18n, this.condition.operator) : "";
      }
    },
  }
</script>
