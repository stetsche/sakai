<template>
  <div>
    <sakai-accordion>
      <sakai-accordion-item title="1. Meeting Information" :open="true">
        <div class="pb-4">
          <div class="col-xs-12 col-md-4 col-xl-3">
            <SakaiInputLabelled title="Meetings Title" />
          </div>
          <div class="col-xs-12 col-md-8 col-xl-5 mt-3">
            <SakaiInputLabelled title="Description" textarea="true" />
          </div>
          <div class="col-xs-12 col-md-4 col-xl-3">
            <!-- <div class="row mt-3 align-items-md-end">
              <div class="col">
                <SakaiInputLabelled title="Preupload presentation" />
              </div>
              <div class="col-sm-12 col-md-auto mt-3">
                <SakaiButton text="Add" class="w-100" />
              </div>
            </div> -->
            <div class="row mt-3">
              <div class="col">
                <SakaiInputLabelled
                  title="Video conferencing service"
                  select="true"
                  :items="confServ"
                  value="microsoft_teams"
                />
              </div>
            </div>
            <div class="row mt-3">
              <div class="col">
                <div class="d-flex">
                  <SakaiInput type="checkbox" />
                  <label class="ms-2" for="input">Record Meeting</label>
                </div>
                <div class="d-flex">
                  <SakaiInput type="checkbox" />
                  <label class="ms-2" for="input">Disable Chat</label>
                </div>
                <div class="d-flex">
                  <SakaiInput type="checkbox" />
                  <label class="ms-2" for="input">Wait For Moderator</label>
                </div>
              </div>
            </div>
          </div>
        </div>
      </sakai-accordion-item>
      <!-- <sakai-accordion-item title="2. Participants">
        <div class="pb-4">
          <div class="row">
            <div class="col-xs-12 col-md-4 col-xl-3">
              <div class="row align-items-md-end">
                <div class="col">
                  <SakaiInputLabelled
                    title="Participant Type"
                    select="true"
                    :items="partType"
                  />
                </div>
                <div class="col-sm-12 col-md-auto mt-3">
                  <SakaiButton text="Apply" class="w-100" />
                </div>
              </div>
            </div>
          </div>
          <div class="row">
            <div class="col-sm-12 col-xl-7">
              <div class="row align-items-md-end">
                <div class="col">
                  <SakaiParticipantsList
                    :participants="participants"
                    @select="createRoom"
                    class="mt-4"
                  />
                </div>
              </div>
            </div>
            <div class="col-sm-12 col-xl-5">
              <sakai-selected-participants
                v-if="selectedParticipants.length > 0"
                :maxUsers="8"
                title="Room 1"
                :users="selectedParticipants"
              />
            </div>
          </div>
        </div>
      </sakai-accordion-item> -->
      <sakai-accordion-item title="3. Availability">
        <div class="col-xs-12 col-md-4 col-xl-3 pb-4">
          <div class="row align-items-md-end mb-3">
            <SakaiInputLabelled title="Open Date" type="datetime-local" />
          </div>
          <div class="row align-items-md-end mb-3">
            <SakaiInputLabelled title="Closed Date" type="datetime-local" />
          </div>
          <div class="row align-items-md-end mb-3">
            <SakaiInputLabelled
              title="Save to Calendar"
              select="true"
              value="calendar_google"
              :items="calendars"
            />
          </div>
        </div>
      </sakai-accordion-item>
      <sakai-accordion-item title="4. Notifications">
        <div class="col-sm-12 col-xl-7 pb-4">
          <div class="sak-banner-info" v-if="notifications.length === 0">
            Currently no notifications specified
          </div>
          <div
            class="d-flex flex-column gap-3 mb-3 align-items-md-end flex-md-row"
            v-for="notification in notifications"
            :key="notification.id"
          >
            <sakai-input-labelled
              :select="true"
              :title="notification.notificationTypes.label"
              :items="notification.notificationTypes.options"
              v-model="notification.notificationTypes.selected"
              class="w-100"
            />
            <div class="d-flex flex-row gap-3 align-items-end w-100">
              <sakai-input
                arialabel="Number of days, minutes or hours to notify"
                v-model.number="notification.frequency.times"
                type="number"
                style="max-width: 3rem"
              />
              <sakai-input-labelled
                :select="true"
                :title="notification.frequency.label"
                :items="notification.frequency.options"
                v-model="notification.frequency.selected"
                class="w-100"
              />
            </div>
            <div>
              <sakai-button
                class="d-none d-md-block ml-n3"
                text="Remove Notification"
                :textHidden="true"
                :clear="true"
                :circle="true"
                @click="removeNotification(notification.id)"
              >
                <template #append>
                  <sakai-icon iconkey="remove" />
                </template>
              </sakai-button>
            </div>
            <sakai-button
              class="d-block d-md-none"
              text="Remove Notification"
              @click="removeNotification(notification.id)"
            >
            </sakai-button>
          </div>
          <div class="row">
            <div class="col-sm-12 col-md-auto">
              <sakai-button
                text="New Notification"
                :primary="true"
                @click="addNotification"
                class="w-100"
              />
            </div>
          </div>
        </div>
      </sakai-accordion-item>
      <!-- <sakai-accordion-item title="5. Meeting Add-ons">
        <div class="pb-4">
          <div class="d-flex">
            <SakaiInput type="checkbox" />
            <label class="ms-2" for="input">Include Whiteboard</label>
          </div>
          <SakaiButton text="Add Poll" :primary="true" class="mt-3" />
        </div>
      </sakai-accordion-item> -->
    </sakai-accordion>
    <div class="d-flex mt-5">
      <SakaiButton
        text="Save"
        @click="handleSave"
        :primary="true"
        class="me-2"
      />
      <SakaiButton
        text="Save as Template"
        @click="handleSaveTemplate"
        class="me-2"
      />
      <SakaiButton text="Cancel" @click="handleCancel" />
    </div>
  </div>
</template>

<script>
import SakaiAccordionItem from "../components/sakai-accordion-item.vue";
import SakaiAccordion from "../components/sakai-accordion.vue";
import SakaiInputLabelled from "../components/sakai-input-labelled.vue";
import SakaiButton from "../components/sakai-button.vue";
import SakaiInput from "../components/sakai-input.vue";
// import SakaiParticipantsList from "../components/sakai-participants-list.vue";
// import SakaiSelectedParticipants from "../components/sakai-selected-participants.vue";
import SakaiIcon from "../components/sakai-icon.vue";
export default {
  components: {
    SakaiAccordionItem,
    SakaiAccordion,
    SakaiInputLabelled,
    SakaiButton,
    SakaiInput,
    // SakaiParticipantsList,
    // SakaiSelectedParticipants,
    SakaiIcon,
  },
  data() {
    return {
      notifications: [],
      notificationTemplate: {
        id: 0,
        notificationTypes: {
          label: "Notification Type",
          selected: "email",
          options: [
            {
              string: "Email",
              value: "email",
            },
            {
              string: "Browser",
              value: "browser",
            },
          ],
        },
        frequency: {
          label: "Frequency",
          selected: "days",
          times: 1,
          options: [
            {
              string: "Days before",
              value: "days",
            },
            {
              string: "Hours before",
              value: "hours",
            },
            {
              string: "Minutes before",
              value: "minutes",
            },
          ],
        },
      },
      participants: [
        {
          form: "square",
          userId: "9072hbs3-sb23-sfef-f93r-9q678g7g3qrh",
          userName: "Bailey Rutheee",
        },
        {
          userId: "454db719-443a-400f-b4d4-4dfada8091c0",
          userName: "Victor van Dijkdd",
        },
        {
          userId: "67aefef6-32df-8fe7-87fe-90721ar79def",
          userName: "Aufderhar Jamison",
        },
      ],
      selectedParticipants: [],
      calendars: [
        {
          string: "Google Calendar",
          value: "calendar_google",
        },
        {
          string: "Microsoft Outlook",
          value: "calendar_outlook",
        },
      ],
    };
  },
  props: {
    confServ: {
      type: Array,
      default: () => [
        {
          string: "Big Bule Button",
          value: "big_blue_button",
        },
        {
          string: "Microsoft Teams",
          value: "microsoft_teams",
        },
        {
          string: "Zoom",
          value: "zoom",
        },
      ],
    },
    partType: {
      type: Array,
      default: () => [
        {
          string: "All Site Members",
          value: "all_site_members",
        },
        {
          string: "Role",
          value: "role",
        },
        {
          string: "Selections/Groups",
          value: "sections_or_groups",
        },
        {
          string: "Users",
          value: "users",
        },
      ],
    },
  },
  methods: {
    handleSave: function () {
      this.$router.push({ name: "Main" });
    },
    handleSaveTemplate: function () {
      this.$router.push({ name: "Main" });
    },
    handleCancel: function () {
      this.$router.push({ name: "Main" });
    },
    createRoom(participants) {
      this.$set(this, "selectedParticipants", participants);
    },
    addNotification() {
      let newNotification = { ...this.notificationTemplate };
      if (this.notifications.length > 0) {
        newNotification.id =
          this.notifications[this.notifications.length - 1].id + 1;
      }
      let updatedNotifications = [...this.notifications];
      updatedNotifications.push(newNotification);
      this.$set(this, "notifications", updatedNotifications);
    },
    removeNotification(id) {
      let index = this.notifications.findIndex(
        (notification) => notification.id == id
      );
      if (index > -1) {
        this.notifications.splice(index, 1);
      }
    },
  },
};
</script>
<style></style>
