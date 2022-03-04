import SakaiSelect from "../src/components/sakai-select.vue";

export default {
    component: SakaiSelect,
    title: 'components/SakaiSelect'
}

const Template = (args, { argTypes }) => ({
    components: { SakaiSelect},
    props: Object.keys(argTypes),
    template: `<SakaiSelect v-bind="$props"/>`
});

export const Primary = Template.bind({});
Primary.args = {
};