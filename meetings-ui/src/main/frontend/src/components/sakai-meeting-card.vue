<template>
  <div class="card" aria-describedby="title">
    <div class="card-header h-100">
      <div class="mt-1 mb-2 contextTitle">{{ contextTitle }}</div>
      <h2 id="title" class="card-title" :title="title">{{ title }}</h2>
    <SakaiDropdownButton
      :items="menuitems"
      class="card-menu"
      :circle="true"
      :clear="true"
      :textHidden="true"
      text="Options"
    >
      <template #append>
        <sakai-icon iconkey="menu_kebab" />
      </template>
    </SakaiDropdownButton>
      <div class="d-flex flex-row flex-wrap mb-2">
        <div>
          {{ schedule }}
        </div>
        <div v-if="currentStatus != status.over" class="mx-1">
          <span>|</span>
        </div>
        <div
          v-if="currentStatus != status.over"
          class="d-flex flex-row flex-nowrap"
        >
          <div>
            <span class="sr-only">{{ `${i18n.status} ` }}</span>
            <span>{{ statusText }}</span>
          </div>
          <div class="mx-1">
            <sakai-icon
              :iconkey="statusIcon"
              :color="
                currentStatus == status.live ? liveIconColor : otherIconColor
              "
            />
          </div>
        </div>
      </div>
      <sakai-modal title="Description">
        <template #activator>
          <sakai-button
            v-if="description"
            text="View Description"
            :link="true"
            tabindex="0"
          ></sakai-button>
        </template>
        <template #body>{{ description }}</template>
      </sakai-modal>
      <div v-if="false" class="d-flex gap-1 my-2">
        <sakai-avatar
          v-for="participant in shownParticipants"
          :key="participant.userid"
          :userId="participant.userid"
          :text="participant.text"
          :userName="participant.name"
          :size="avatarHeight"
        />
      </div>
      <sakai-avatar-list
        :userlist="shownParticipants"
        :avatarsize="avatarHeight"
        :length="maxAvatars"
      ></sakai-avatar-list>
    </div>
    <div class="card-body p-0 d-flex">
      <div class="action-list d-flex">
        <div v-for="action in actions" :key="action.icon">
          <sakai-button
            :circle="true"
            :clear="true"
            :text="action.label"
            :textHidden="true"
          >
            <template #prepend>
              <sakai-icon :iconkey="action.icon" />
            </template>
          </sakai-button>
        </div>
        <slot name="actions"> </slot>
      </div>
      <div class="ms-auto p-1">
        <slot name="right"> </slot>
        <sakai-button
          v-if="currentStatus != status.over"
          :disabled="!live"
          :primary="true"
          text="Join Meeting"
        >
        </sakai-button>
      </div>
    </div>
  </div>
</template>

<style>
.action-list > div {
  border-right: 1px solid var(--sakai-border-color);
  padding: 0.125rem;
}
</style>

<style scoped>
.card {
  border: 1px solid var(--sakai-border-color);
  border-radius: 6px;
}
.card-header {
  background-color: var(--sakai-background-color-2);
  border-radius: 6px 6px 0 0;
}

.card-body {
  background-color: var(--sakai-background-color-1);
  border-radius: 0 0 0px 6px;
}
</style>

<style scoped>
.card-menu {
  position: absolute;
  right: 0.5rem;
  top: 0.5rem;
}

h2 {
  font-weight: 600;
  font-size: 22px;
}

.contextTitle {
  text-transform: uppercase;
}
</style>

<script>
import SakaiAvatar from "./sakai-avatar.vue";
import SakaiAvatarList from "./sakai-avatar-list.vue";
import SakaiIcon from "./sakai-icon.vue";
import SakaiButton from "./sakai-button.vue";
import SakaiDropdownButton from "./sakai-dropdown-button.vue";

import dayjs from "dayjs";
import localizedFormat from "dayjs/plugin/localizedFormat";
import relativeTime from "dayjs/plugin/relativeTime";
import SakaiModal from "./sakai-modal.vue";
//import locale_es from 'dayjs/locale/es';

dayjs.extend(relativeTime);
dayjs.extend(localizedFormat);
//dayjs.locale(locale_es);
export default {
  components: {
    SakaiAvatar,
    SakaiAvatarList,
    SakaiIcon,
    SakaiButton,
    SakaiDropdownButton,
    SakaiModal,
  },
  data() {
    return {
      status: { live: 0, waiting: 1, timeUntil: 2, over: 3 },
      avatarHeight: 40,
      liveIconColor: "var(--sakai-record-color)",
      otherIconColor: "var(--sakai-secondary-color-1)",
      i18n: {
        status: "Status",
      },
    };
  },
  props: {
    title: { type: String, default: undefined },
    contextTitle: { type: String, default: undefined },
    description: { type: String, default: undefined },
    live: { type: Boolean, default: false },
    maxAvatars: {
      default: 5,
      validator: function (value) {
        return value >= 1;
      },
    },
    startDate: {
      validator: function (value) {
        return dayjs(value).isValid();
      },
    },
    endDate: {
      validator: function (value) {
        return dayjs(value).isValid();
      },
    },
    menuitems: { type: Array, default: new Array() },
    participants: { type: Array, default: new Array() },
    actions: { type: Array, default: new Array() },
  },
  computed: {
    schedule: function () {
      let start = dayjs(this.startDate);
      let end = dayjs(this.endDate);
      let startTextFormat;
      let endTextFormat;
      if (dayjs().isSame(start, "year")) {
        //starts this year
        if (dayjs().isSame(start, "week")) {
          //starts this week
          if (dayjs().isSame(start, "day")) {
            //starts today
            startTextFormat = "LT";
          } else {
            //starts this week, not today
            startTextFormat = "ddd LT";
          }
        } else {
          //starts this year, not this week
          startTextFormat = "lll";
        }
      } else {
        //starts other year
        startTextFormat = "lll";
      }

      if (start.isSame(end, "day")) {
        //meeting covers just one local day
        endTextFormat = "LT";
      } else {
        //meeting covers more then one local day
        endTextFormat = dayjs().isSame(end, "week") ? "ddd LT" : "lll";
      }
      let startText = start.format(startTextFormat);
      let endText = end.format(endTextFormat);
      return startText + " - " + endText;
    },
    currentStatus: function () {
      if (this.live) {
        return this.status.live;
      } else {
        if (dayjs().isBefore(dayjs(this.startDate))) {
          return this.status.timeUntil;
        } else if (dayjs().isAfter(dayjs(this.endDate))) {
          return this.status.over;
        } else {
          return this.status.waiting;
        }
      }
    },
    statusText: function () {
      switch (this.currentStatus) {
        case this.status.live:
          return "live";
        case this.status.waiting:
          return "waiting for start";
        case this.status.timeUntil:
          return "starts " + dayjs().to(dayjs(this.startDate));
        default:
          return "something went wrong";
      }
    },
    statusIcon: function () {
      switch (this.currentStatus) {
        case this.status.live:
          return "live";
        case this.status.waiting:
          return "hourglass_emty";
        case this.status.timeUntil:
          return "bell";
        default:
          return "error";
      }
    },
    shownParticipants: function () {
      let maxAvatars = Math.round(this.maxAvatars);
      if (maxAvatars && this.participants.length > maxAvatars) {
        let hidden = this.participants.length - (maxAvatars - 1);
        let shown = this.participants.slice(0, maxAvatars - 1);
        let plus = { text: "+" + hidden };
        shown.push(plus);
        return shown;
      } else {
        return this.participants;
      }
    },
  },
};
</script>
