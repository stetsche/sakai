import SakaiModal from "../src/components/sakai-modal.vue";
import SakaiButton from "../src/components/sakai-button.vue";

export default {
  component: SakaiModal,
  title: "components/SakaiModal",
};

const Template = (args, { argTypes }) => ({
  components: { SakaiModal, SakaiButton },
  props: Object.keys(argTypes),
  template: `<SakaiModal body="Text example.">
                <template #activator>
                    <SakaiButton text="Try Modal"/>
                </template>
                <template #footer>
                    <SakaiButton text="Close"/>
                    <SakaiButton text="Save Changes" :primary="true"/>
                </template>
            </SakaiModal>`,
});

export const Primary = Template.bind({});
Primary.args = {};
