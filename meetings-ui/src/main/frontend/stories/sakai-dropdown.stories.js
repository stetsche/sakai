import SakaiDropdown from "../src/components/sakai-dropdown.vue";
import SakaiButton from "../src/components/sakai-button.vue";

export default {
  component: SakaiDropdown,
  title: "Components/SakaiDropdown",
};

const Template = (args, { argTypes }) => ({
  components: { SakaiDropdown, SakaiButton },
  props: Object.keys(argTypes),
  template: `<div @click="showMenu = !showMenu">
        <SakaiDropdown v-bind="$props">
          <template #activation>
            <SakaiButton text="Options" role="menu">
              <template #append>
                <i class="fa" :class="showMenu ? 'fa-chevron-up' : 'fa-chevron-down'"></i>
              </template>
            </SakaiButton>
          </template>
        </SakaiDropdown>
      </div>`,
});

export const Primary = Template.bind({});
Primary.args = {
  showMenu: false,
};
