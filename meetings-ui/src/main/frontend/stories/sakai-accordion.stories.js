import SakaiAccordion from "../src/components/sakai-accordion.vue";
import SakaiAccordionItem from "../src/components/sakai-accordion-item.vue";
import data from "./data/accordionData.json";

export default {
  components: [SakaiAccordion, SakaiAccordionItem],
  title: "Components/sakai-accordion",
};

const template = `
  <sakai-accordion v-bind="$props">
    <sakai-accordion-item
      v-for="item in items"
      :title="item.title"
      :key="item.id"
    >
      <p v-for="i in 5" :key="i">
        {{item.content}}
      </p>
    </sakai-accordion-item>
  </sakai-accordion>
`;

export const Grouped = (args, { argTypes }) => ({
  components: { SakaiAccordion, SakaiAccordionItem },
  props: Object.keys(argTypes),
  data() {
    return {
      items: data.groupedAccordion,
    };
  },
  template: template,
});

const IndependentTemplate = (args, { argTypes }) => ({
  components: { SakaiAccordion, SakaiAccordionItem },
  props: Object.keys(argTypes),
  data() {
    return {
      items: data.independentAccordion,
    };
  },
  template: template,
});
export const Independent = IndependentTemplate.bind({});
Independent.args = {
  independent: true,
};

const FlushTemplate = (args, { argTypes }) => ({
  components: { SakaiAccordion, SakaiAccordionItem },
  props: Object.keys(argTypes),
  data() {
    return {
      items: data.flushAccordion,
    };
  },
  template: template,
  args: {
    flush: true,
  },
});
export const Flush = FlushTemplate.bind({});
Flush.args = {
  flush: true,
};
