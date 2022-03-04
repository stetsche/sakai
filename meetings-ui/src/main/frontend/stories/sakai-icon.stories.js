import SakaiIcon from "../src/components/sakai-icon.vue";
import icons from "../src/assets/icons.js";

export default {
  component: SakaiIcon,
  title: "Components/sakai-icon",
  argTypes: {
    iconkey: {
      control: "select",
      options: Object.keys(icons),
    },
  },
};

const Template = (args, { argTypes }) => ({
  components: { SakaiIcon },
  props: Object.keys(argTypes),
  template: '<sakai-icon style="font-size:5rem" v-bind="$props"/>',
});

export const AllIcons = Template.bind({});
AllIcons.args = {
  iconkey: "attachment",
};
