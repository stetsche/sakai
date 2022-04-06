<template>
  <div>
    <span ref="feedback" class="sr-only" aria-live="polite">emty</span>
    <div class="header-menu d-flex flex-column flex-md-row gap-2 mb-4 div-heigth">
      <div class="order-1 me-md-auto">
        <SakaiButton
          text="Create New Meeting"
          @click="handleCreateNewMeeting"
          class="w-100"
        >
          <template #prepend>
            <sakai-icon class="me-1" iconkey="plus" />
          </template>
        </SakaiButton>
      </div>
<!-- 
      <SakaiInput
        type="search"
        placeholder="Search for meetings"
        class="order-0 order-md-2 w-auto"
        style="min-width: 20%"
      >
        <template #prepend>
          <sakai-icon class="search-icon" iconkey="search" />
        </template>
      </SakaiInput>
      <SakaiDropdownButton class="order-3" :items="items" text="Options">
      </SakaiDropdownButton>
-->
    </div>
    <div v-if="happeningToday.length > 0">
      <div class="section-heading">
        <h1 id="flush-headingOne" class="h4">Today</h1>
        <hr aria-hidden="true" class="mb-0 mt-2" />
      </div>
      <div>
        <div class="accordion-body p-0 pb-4">
          <ul
            class="
              list-unstyled
              row row-cols-1 row-cols-md-2 row-cols-xl-3 row-cols-xxl-4
              align-content-stretch
            "
          >
            <li
              class="col pt-4"
              v-for="meeting in happeningToday"
              :key="meeting.id"
            >
              <sakai-meeting-card
                class="h-100"
                :id="meeting.id"
                :title="meeting.title"
                :description="meeting.description"
                :contextTitle="meeting.contextTitle"
                :participants="meeting.participants"
                :actions="meeting.actions"
                :live="meeting.live"
                :startDate="meeting.startDate"
                :endDate="meeting.endDate"
				:url="meeting.url"
				@onDeleted="handleMeetingDelete"
              >
              </sakai-meeting-card>
            </li>
          </ul>
        </div>
      </div>
    </div>
    <div v-if="inFuture.length > 0">
      <div class="section-heading">
        <h1 class="accordion-header h4" id="flush-headingTwo">Future</h1>
        <hr aria-hidden="true" class="mb-0 mt-2" />
      </div>
      <div>
        <ul class="list-unstyled accordion-body p-0 pb-4">
          <li class="row row-cols-1 row-cols-md-2 row-cols-xl-3 row-cols-xxl-4">
            <div class="col pt-4" v-for="meeting in inFuture" :key="meeting.id">
              <sakai-meeting-card
                class="h-100"
                :id="meeting.id"
                :title="meeting.title"
                :description="meeting.description"
                :contextTitle="meeting.contextTitle"
                :participants="meeting.participants"
                :actions="meeting.actions"
                :live="meeting.live"
                :startDate="meeting.startDate"
                :endDate="meeting.endDate"
				:url="meeting.url"
				@onDeleted="handleMeetingDelete"
              >
              </sakai-meeting-card>
            </div>
          </li>
        </ul>
      </div>
    </div>
    <div v-if="inPast.length > 0">
      <div
        class="section-heading d-flex align-items-end"
      >
        <h1 class="mb-0 h4" id="flush-headingThree">Past</h1>
<!--         
        <div class="ms-auto">
          <div @click="btnPress2 = !btnPress2" class="ms-auto">
            <SakaiDropdownButton :items="showAll" text="Show All" :clear="true"></SakaiDropdownButton>
          </div>
        </div>
-->
      </div>
      <hr aria-hidden="true" class="mb-0 mt-2" />
      <div>
        <div class="accordion-body p-0 pb-4">
          <ul
            class="
              list-unstyled
              row row-cols-1 row-cols-md-2 row-cols-xl-3 row-cols-xxl-4
            "
            v-if="inPast.length > 0"
          >
            <li class="col pt-4" v-for="meeting in inPast" :key="meeting.id">
              <sakai-meeting-card
                class="h-100"
                :id="meeting.id"
                :title="meeting.title"
                :description="meeting.description"
                :contextTitle="meeting.contextTitle"
                :participants="meeting.participants"
                :actions="meeting.actions"
                :live="meeting.live"
                :startDate="meeting.startDate"
                :endDate="meeting.endDate"
                @onDeleted="handleMeetingDelete"
              >
              </sakai-meeting-card>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import dayjs from "dayjs";
import SakaiMeetingCard from "../components/sakai-meeting-card.vue";
import SakaiInput from "../components/sakai-input.vue";
import SakaiButton from "../components/sakai-button.vue";
import SakaiDropdownButton from "../components/sakai-dropdown-button.vue";
import SakaiIcon from "../components/sakai-icon.vue";
import dbData from "../../data/db.json";
import constants from "../resources/constants.js";

// eslint-disable-next-line

export default {
  components: {
    SakaiMeetingCard,
    SakaiInput,
    SakaiButton,
    SakaiDropdownButton,
    SakaiIcon,
  },
  data() {
    return {
      meetingsList: [],
      alert:"abc",
      btnPress1: false,
      btnPress2: false,
      items: [
        {
          id: 0,
          icon: "permissions",
          string: "Permissions",
          route: "/permissions",
        },
        {
          id: 1,
          icon: "template",
          string: "Templates",
          action: this.handleTemplates,
        },
      ],
      showAll: [
        {
          id: 0,
          icon: "all",
          string: "All",
          action: this.handleShowAll,
        },
        {
          id: 1,
          icon: "play",
          string: "Recordings",
          action: this.handleShowRecordings,
        },
      ]
    };
  },
  methods: {
    handleCreateNewMeeting: function () {
      this.$router.push({ name: "EditMeeting" });
    },
    handleTemplates: function () {
      this.$refs.feedback.innerHTML = "Here will be a menu to work with meeting templates";
    },
    handleMeetingDelete: function (id) {
      if (id) {
        this.meetingsList = this.meetingsList.filter( (meeting) => meeting.id !== id );
      }
    },
    handleMeetingEdit: function () {
      this.$refs.feedback.innerHTML = "Meetings are not editable yet, but the action would be triggered now";
    },
    handleShowAll: function () {
      this.$refs.feedback.innerHTML = "Displaying all past meetings";
    },
    handleShowRecordings: function () {
      this.$refs.feedback.innerHTML = "Displaying past meetings with recordings";
    },
    loadMeetingsList: function () {
      fetch(constants.toolPlacement + "/meeting/all")
      .then(r => {
        if (r.ok) {
          return r.json();
        }
        throw new Error(`Failed to get meetings from ${url}`);
      })
      .then(data => {
        data.forEach(meeting => {
            meeting.live = false;
            if (dayjs().isAfter(dayjs(meeting.startDate)) && dayjs().isBefore(dayjs(meeting.endDate))) {
                meeting.live = true;
            }
        });
        this.meetingsList = [...data];
      })
      .catch (error => console.error(error));
    },
    switchtheme: function () {
    },
  },
  computed: {
    happeningToday: function () {
      return this.meetingsList.filter(
        (meeting) =>
          dayjs().isSame(dayjs(meeting.startDate), "day") || meeting.live
      );
    },
    inPast: function () {
      return this.meetingsList.filter(
        (meeting) =>
          dayjs().isAfter(dayjs(meeting.startDate), "day") && !meeting.live
      );
    },
    inFuture: function () {
      return this.meetingsList.filter(
        (meeting) =>
          dayjs().isBefore(dayjs(meeting.startDate), "day") && !meeting.live
      );
    },
  },
  mounted: function () {
    this.loadMeetingsList();
  },
};
</script>
