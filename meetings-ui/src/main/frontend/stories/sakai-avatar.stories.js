import SakaiAvatar from "../src/components/sakai-avatar.vue";

export default {
  component: SakaiAvatar,
  title: "Components/sakai-avatar",
  argTypes: {
    userId: {
      control: "select",
      options: [
        "",
        "454db719-443a-400f-b4d4-4dfada8091c0",
        "67aefef6-32df-8fe7-87fe-90721ar79def",
        "34058dsf-fd23-w9df-9ed6-234dsf381edn",
        "9072hbs3-sb23-sfef-f93r-9q678g7g3qrh",
        "klsde436-45kl-ds80-214f-f399nfm3lw93",
      ],
    },
  },
};

const Template = (args, { argTypes }) => ({
  components: { SakaiAvatar },
  props: Object.keys(argTypes),
  template: '<sakai-avatar v-bind="$props"/>',
});

export const Primary = Template.bind({});
Primary.args = {
  size: 300,
  userId: "67aefef6-32df-8fe7-87fe-90721ar79def",
  userName: "Aufderhar Jamison",
};

export const NoImage = Template.bind({});
NoImage.args = {
  size: 300,
  userId: "454db719-443a-400f-b4d4-4dfada8091c0",
  userName: "Victor van Dijk",
};

export const Square = Template.bind({});
Square.args = {
  form: "square",
  size: 300,
  userId: "9072hbs3-sb23-sfef-f93r-9q678g7g3qrh",
  userName: "Bailey Ruthe",
};

export const Broken = Template.bind({});
Broken.args = {
  form: "square",
  size: 300,
  userId: "this-is-not-a-user-id",
  userName: "no one",
};

export const Text = Template.bind({});
Text.args = {
  size: 300,
  text: "+6",
};
color: "green";
