import SakaiInputLabelled from '../src/components/sakai-input-labelled.vue';

export default {
    component: SakaiInputLabelled,
    title: 'components/SakaiInputLabelled'
}

const Template = (args, { argTypes }) => ({
    components: { SakaiInputLabelled},
    props: Object.keys(argTypes),
    template: `<SakaiInputLabelled v-bind="$props"/>`
});

export const Primary = Template.bind({});
Primary.args = {
};