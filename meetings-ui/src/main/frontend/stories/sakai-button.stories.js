import SakaiButton from '../src/components/sakai-button.vue';

export default {
  component: SakaiButton,
  title: 'Components/SakaiButton',
};

const Template = (args, { argTypes }) => ({
  components: { SakaiButton },
  props: Object.keys(argTypes),
  template: 
    `<SakaiButton v-bind="$props">
        <template #prepend>
          <i class="fa fa-question me-1"></i>
        </template>
     </SakaiButton>`,
});

export const Primary = Template.bind({});
Primary.args = {
};