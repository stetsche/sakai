import SakaiAccordionItem from "../src/components/sakai-accordion-item.vue";
import data from "./data/accordionData.json";

export default {
  components: SakaiAccordionItem,
  title: "Components/sakai-accordion-item",
};

const template = `
  <div>
    <sakai-accordion-item
      v-for="item in items"
      :title="item.title"
      :key="item.id"
      :open="item.open"
    >
      <p v-for="i in 5" :key="i">
        {{item.content}}
      </p>
    </sakai-accordion-item>
  </div>
`;

export const Primary = (args, { argTypes }) => ({
  components: { SakaiAccordionItem },
  props: Object.keys(argTypes),
  data() {
    return {
      items: data.accordionItems,
    };
  },
  template: template,
});
