import SakaiMeetingCard from "../src/components/sakai-meeting-card.vue";
import { meetingList as meetings } from "../data/db.json";
import dayjs from "dayjs";

export default {
  component: SakaiMeetingCard,
  title: "Components/sakai-meeting-card",
};

const Template = (args, { argTypes }) => ({
  components: { SakaiMeetingCard },
  props: Object.keys(argTypes),
  template: '<sakai-meeting-card v-bind="$props"/>',
});

const common = Template.bind({});
common.args = {
  contextTitle: "Frontend Development - Webcomponents",
  title: "Developing Webcomponents utilizing Storybook",
};

const meetingLive = {
  ...meetings[5],
  startDate: dayjs().subtract(25, "minute"),
  endDate: dayjs().add(35, "minute"),
};

export const Live = Template.bind({});
Live.args = {
  ...common.args,
  live: true,
  participants: meetingLive.participants,
  startDate: meetingLive.startDate,
  endDate: meetingLive.endDate,
  actions: meetingLive.actions,
};

const meetingFuture = {
  ...meetings[1],
  startDate: dayjs().add(24, "days").hour(13).minute(30),
  endDate: dayjs().add(24, "days").hour(14).minute(30),
};

export const Future = Template.bind({});
Future.args = {
  ...common.args,
  live: false,
  participants: meetingFuture.participants,
  startDate: meetingFuture.startDate,
  endDate: meetingFuture.endDate,
  actions: meetingFuture.actions,
};

const meetingPast = {
  ...meetings[3],
  startDate: dayjs().subtract(4, "days").hour(16).minute(20),
  endDate: dayjs().subtract(4, "days").hour(17).minute(20),
};

export const Past = Template.bind({});
Past.args = {
  ...common.args,
  live: false,
  participants: meetingPast.participants,
  startDate: meetingPast.startDate,
  endDate: meetingPast.endDate,
  actions: meetingPast.actions,
};
