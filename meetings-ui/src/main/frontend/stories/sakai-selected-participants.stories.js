import SakaiSelectedParticipants from "../src/components/sakai-selected-participants.vue";
import data from "./data/users.json";

export default {
  components: SakaiSelectedParticipants,
  title: "Components/sakai-selected-participants",
};

const Template = (args, { argTypes }) => ({
  components: { SakaiSelectedParticipants },
  props: Object.keys(argTypes),
  template: `
      <sakai-selected-participants v-bind="$props"/>
  `,
});

export const Primary = Template.bind({});
Primary.args = {
  maxUsers: 6,
  title: "Room 1",
  users: data.users,
};
