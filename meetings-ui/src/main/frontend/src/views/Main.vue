<template>
  <div>
    <div class="header-menu d-flex flex-column flex-md-row gap-2 div-heigth" :class="editPermission ? 'mb-4' : ''">
      <div class="order-1 me-md-auto">
        <SakaiButton
          v-if="editPermission"
          text="Create New Meeting"
          @click="handleCreateNewMeeting"
          class="w-100"
        >
          <template #prepend>
            <SakaiIcon class="me-1" iconkey="plus" />
          </template>
        </SakaiButton>
      </div>
<!-- 
      <SakaiInput
        type="search"
        placeholder="Search for meetings"
        class="order-0 order-md-2 w-auto"
        style="min-width: 20%"
        v-model:value="searchString"
      >
      </SakaiInput>
      <SakaiDropdownButton class="order-3" :items="items" text="Options">
      </SakaiDropdownButton>
-->
    </div>
    <div v-if="searching">
      <div class="section-heading">
        <h1 id="flush-headingOne" class="h4">Search results</h1>
        <hr aria-hidden="true" class="mb-0 mt-2" />
      </div>
      <div>
        <div class="accordion-body p-0 pb-4">
          <div v-if="searchResult.length === 0" class="sak-banner-info">
            No results for this search
          </div>
          <ul
            v-else
            class="
              list-unstyled
              row row-cols-1 row-cols-md-2 row-cols-xl-3 row-cols-xxl-4
              align-content-stretch
            "
          >
            <li
              class="col pt-4"
              v-for="meeting in searchResult"
              :key="meeting.id"
            >
              <SakaiMeetingCard
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
                :editable="editPermission"
                :url="meeting.url"
                @onDeleted="handleMeetingDelete"
              >
              </SakaiMeetingCard>
            </li>
          </ul>
        </div>
      </div>
    </div>
    <div v-if="happeningToday.length > 0 && !searching">
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
              <SakaiMeetingCard
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
                :editable="editPermission"
                :url="meeting.url"
                @onDeleted="handleMeetingDelete"
              >
              </SakaiMeetingCard>
            </li>
          </ul>
        </div>
      </div>
    </div>
    <div v-if="inFuture.length > 0 && !searching">
      <div class="section-heading">
        <h1 class="accordion-header h4" id="flush-headingTwo">Future</h1>
        <hr aria-hidden="true" class="mb-0 mt-2" />
      </div>
      <div>
        <ul class="list-unstyled accordion-body p-0 pb-4">
          <li class="row row-cols-1 row-cols-md-2 row-cols-xl-3 row-cols-xxl-4">
            <div class="col pt-4" v-for="meeting in inFuture" :key="meeting.id">
              <SakaiMeetingCard
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
                :editable="editPermission"
                :url="meeting.url"
                @onDeleted="handleMeetingDelete"
              >
              </SakaiMeetingCard>
            </div>
          </li>
        </ul>
      </div>
    </div>
    <div v-if="inPast.length > 0 && !searching">
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
              <SakaiMeetingCard
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
                :editable="editPermission"
                @onDeleted="handleMeetingDelete"
              >
              </SakaiMeetingCard>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import SakaiMeetingCard from "../components/sakai-meeting-card.vue";
import SakaiInput from "../components/sakai-input.vue";
import SakaiButton from "../components/sakai-button.vue";
import SakaiDropdownButton from "../components/sakai-dropdown-button.vue";
import SakaiIcon from "../components/sakai-icon.vue";
import dbData from "../../data/db.json";
import constants from "../resources/constants.js";

import dayjs from "dayjs";
import isTodayPlugin from "dayjs/plugin/isToday";
dayjs.extend(isTodayPlugin)
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
      searchString: '',
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
    },
    handleMeetingDelete: function (id) {
      if (id) {
        this.meetingsList = this.meetingsList.filter( (meeting) => meeting.id !== id );
      }
    },
    handleMeetingEdit: function () {
    },
    handleShowAll: function () {
    },
    handleShowRecordings: function () {
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
        console.log(this.meetingsList)
      })
      .catch (error => console.error(error));
    },
    meetingsComperator(a,b) {
      return dayjs(a.startDate).isBefore(b.startDate) ? -1 : 1;
    },
    loadEditPermission() {
      //TODO fetch permissions from editperms endpoint 
      this.editPermission =  true;
    },
    switchtheme: function () {
    },
  },
  computed: {
    happeningToday: function () {
      //Filter meetingsList for meetings that happen today, and are not over
      return this.meetingsList.filter(
        (meeting) =>
          dayjs(meeting.startDate).isToday() && dayjs(meeting.endDate).isAfter(dayjs()) || meeting.live
      ).sort(this.meetingsComperator);
    },
    inPast: function () {
      return this.meetingsList.filter(
        (meeting) =>
          dayjs(meeting.startDate).isBefore(dayjs()) && !meeting.live
      ).sort(this.meetingsComperator);
    },
    inFuture: function () {
      return this.meetingsList.filter(
        (meeting) =>
          dayjs().isBefore(dayjs(meeting.startDate), "day") && !meeting.live
      ).sort(this.meetingsComperator);
    },
    searching: function () {
      return this.searchString !== '';
    },
    searchResult: function () {
      let searchString = this.searchString;
      if (!this.searching) { return [] }
      return this.meetingsList.filter(
        (meeting) =>
          meeting.title.search(searchString) > -1 || meeting.description.search(searchString) > -1
      );
    },
  },
  mounted: function () {
    this.loadMeetingsList();
    this.loadEditPermission();
  },
};
</script>
