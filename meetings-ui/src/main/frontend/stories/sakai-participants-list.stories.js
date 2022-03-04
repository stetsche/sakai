import SakaiParticipantsList from "../src/components/sakai-participants-list.vue";

export default {
  component: SakaiParticipantsList,
  title: "components/SakaiParticipantsList",
};

const Template = (args, { argTypes }) => ({
  components: { SakaiParticipantsList },
  props: Object.keys(argTypes),
  template: `<SakaiParticipantsList v-bind="$props"/>`,
});

export const Primary = Template.bind({});
Primary.args = {};
