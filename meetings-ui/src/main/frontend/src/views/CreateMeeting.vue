<template>
  <div>
    <SakaiAccordion>
      <SakaiAccordionItem title="1. Meeting Information" :open="true">
        <div class="pb-4">
          <div class="col-md-4 col-xl-3">
            <SakaiInputLabelled
              title="Meetings Title"
              v-model:value="formdata.title"
              :required="true"
              @validation="setValidation('title', $event)"
            />
          </div>
          <div class="col-md-8 col-xl-5 mt-3">
            <SakaiInputLabelled
              title="Description"
              textarea="true"
              v-model:value="formdata.description"
            />
          </div>
          <div class="col-md-4 col-xl-3">
            <!--
            <div class="row mt-3 align-items-md-end">
              <div class="col">
                <SakaiInputLabelled title="Preupload presentation" />
              </div>
              <div class="col-sm-12 col-md-auto mt-3">
                <SakaiButton text="Add" class="w-100" />
              </div>
            </div>
            -->
            <div class="row mt-3">
              <div class="col">
                <SakaiInputLabelled
                  title="Video conferencing service"
                  select="true"
                  :items="confServ"
                  :disabled="true"
                  v-model:value="formdata.conf_service"
                />
              </div>
            </div>
            <!--
            <div class="row mt-3">
              <div class="col">
                <div class="d-flex">
                  <SakaiInputLabelled text="Record Meeting" type="checkbox"/>
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
            -->
          </div>
        </div>
      </SakaiAccordionItem>
      <!-- <SakaiAccordionItem title="2. Participants">
        <div class="pb-4">
          <div class="row">
            <div class="col-md-4 col-xl-3">
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
      </SakaiAccordionItem> -->
      <SakaiAccordionItem title="2. Availability">
        <div class="col-md-4 col-xl-3 pb-4">
          <div class="row align-items-md-end mb-3">
            <SakaiInputLabelled
              title="Open Date"
              type="datetime-local"
              v-model:value="formdata.date_open"
            />
          </div>
          <div class="row align-items-md-end mb-3">
            <SakaiInputLabelled
              title="Closed Date"
              type="datetime-local"
              v-model:value="formdata.date_close"
            />
          </div>
          <!--
          <div class="row align-items-md-end mb-3">
            <SakaiInputLabelled
              title="Add meeting to calendar"
              v-model="formdata.addToCalendar"
              type="checkbox"
            />
          </div>
-->
        </div>
      </SakaiAccordionItem>
      <!--
      <SakaiAccordionItem title="4. Notifications">
        <div class="col-sm-12 col-xl-7 pb-4">
          <div class="sak-banner-info" v-if="notifications.length === 0">
            Currently no notifications specified
          </div>
          <div
            class="d-flex flex-column gap-3 mb-3 align-items-md-end flex-md-row"
            v-for="notification in notifications"
            :key="notification.id"
          >
            <SakaiInputLabelled
              :select="true"
              :title="notification.notificationTypes.label"
              :items="notification.notificationTypes.options"
              v-model="notification.notificationTypes.selected"
              class="w-100"
            />
            <div class="d-flex flex-row gap-3 align-items-end w-100">
              <SakaiInput
                arialabel="Number of days, minutes or hours to notify"
                v-model.number="notification.frequency.times"
                type="number"
                style="max-width: 3rem"
              />
              <SakaiInputLabelled
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
      </SakaiAccordionItem>
      -->
      <!--
      <SakaiAccordionItem title="5. Meeting Add-ons">
        <div class="pb-4">
          <div class="d-flex">
            <SakaiInput type="checkbox" />
            <label class="ms-2" for="input">Include Whiteboard</label>
          </div>
          <SakaiButton text="Add Poll" :primary="true" class="mt-3" />
        </div>
      </SakaiAccordionItem>
-->
    </SakaiAccordion>
    <div class="d-flex mt-5">
      <SakaiButton
        text="Save"
        @click="handleSave"
        :primary="true"
        class="me-2"
        :disabled="!allValid"
      />
      <SakaiButton text="Cancel" @click="handleCancel" />
    </div>
  </div>
</template>

<script>
import dayjs from "dayjs";
import SakaiAccordionItem from "../components/sakai-accordion-item.vue";
import SakaiAccordion from "../components/sakai-accordion.vue";
import SakaiInputLabelled from "../components/sakai-input-labelled.vue";
import SakaiButton from "../components/sakai-button.vue";
import SakaiInput from "../components/sakai-input.vue";
// import SakaiParticipantsList from "../components/sakai-participants-list.vue";
// import SakaiSelectedParticipants from "../components/sakai-selected-participants.vue";
import SakaiIcon from "../components/sakai-icon.vue";
import constants from "../resources/constants.js";
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
      formdata: {
        title: "",
        description: "",
        conf_service: "",
        save_to_calendar: "",
        date_open: dayjs().format("YYYY-MM-DDTHH:mm:ss"),
        date_close: dayjs().add(1, "hour").format("YYYY-MM-DDTHH:mm:ss"),
      },
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
      confServ: [
        {
          string: "Microsoft Teams",
          value: "microsoft_teams",
        },
      ],
      partType: [
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
      validations: { title: false },
    };
  },
  props: {
    id: { type: String, default: undefined },
    title: { type: String, default: "" },
    description: { type: String, default: "" },
    conf_service: { type: String, default: "microsoft_teams" },
    save_to_calendar: { type: String, default: true },
    date_open: {
      validator: function (value) {
        return dayjs(value).isValid();
      },
    },
    date_close: {
      validator: function (value) {
        return dayjs(value).isValid();
      },
    },
  },
  computed: {
    allValid() {
      return !Object.values(this.validations).includes(false);
    },
  },
  methods: {
    setValidation(field, valid) {
      this.validations[field] = valid;
    },
    handleSave: function () {
      let saveData = {
        id: this.id,
        title: this.formdata.title,
        siteId: this.$route.params.siteid,
        description: this.formdata.description,
        startDate: dayjs(this.formdata.date_open)
          .subtract(portal.user.offsetFromServerMillis, "millis")
          .format("YYYY-MM-DDTHH:mm:ss"),
        endDate: dayjs(this.formdata.date_close)
          .subtract(portal.user.offsetFromServerMillis, "millis")
          .format("YYYY-MM-DDTHH:mm:ss"),
      };
      let methodToCall = constants.toolPlacement;
      let restMethod = "POST";
      if (this.id) {
        methodToCall = methodToCall + "/meeting/" + this.id;
        restMethod = "PUT";
      } else {
        methodToCall = methodToCall + "/meeting";
      }
      // Invoke REST Controller - Save (POST or PUT)
      fetch(methodToCall, {
        credentials: "include",
        method: restMethod,
        cache: "no-cache",
        headers: { "Content-Type": "application/json; charset=utf-8" },
        body: JSON.stringify(saveData),
      })
        .then((res) => res.json())
        .then((data) => {
          this.$router.push({ name: "Main" });
        })
        .catch((error) => console.error("Error:", error))
        .then((response) => console.log("Success:", response));
    },
    handleCancel() {
      this.$router.push({ name: "Main" });
    },
    createRoom(participants) {
      this.selectedParticipants = participants;
    },
    addNotification() {
      let newNotification = { ...this.notificationTemplate };
      if (this.notifications.length > 0) {
        newNotification.id =
          this.notifications[this.notifications.length - 1].id + 1;
      }
      let updatedNotifications = [...this.notifications];
      updatedNotifications.push(newNotification);
      this.notifications = updatedNotifications;
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
  created() {
    if (this.title) {
      this.validations.title = true;
      this.formdata.title = this.title;
    }
    if (this.description) {
      this.formdata.description = this.description;
    }
    if (this.conf_service) {
      this.formdata.conf_service = this.conf_service;
    }
    if (this.saveToCalendar) {
      this.formdata.saveToCalendar = this.save_to_calendar;
    }
    if (this.date_open) {
      this.formdata.date_open = dayjs(this.date_open)
        .add(portal.user.offsetFromServerMillis, "millis")
        .format("YYYY-MM-DDTHH:mm:ss");
    }
    if (this.date_close) {
      this.formdata.date_close = dayjs(this.date_close)
        .add(portal.user.offsetFromServerMillis, "millis")
        .format("YYYY-MM-DDTHH:mm:ss");
    }
  },
};
</script>
<style></style>
