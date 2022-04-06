/**
* Copyright (c) 2022 Apereo Foundation
* 
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*             http://opensource.org/licenses/ecl2
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.meetings.controller;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.meetings.api.MeetingService;
import org.sakaiproject.meetings.api.model.AttendeeType;
import org.sakaiproject.meetings.api.model.Meeting;
import org.sakaiproject.meetings.controller.data.GroupData;
import org.sakaiproject.meetings.controller.data.MeetingData;
import org.sakaiproject.meetings.controller.data.NotificationType;
import org.sakaiproject.meetings.exceptions.MeetingsException;
import org.sakaiproject.meetings.teams.MicrosoftTeamsService;
import org.sakaiproject.meetings.teams.data.TeamsMeetingData;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.oauth2.sdk.util.StringUtils;


/**
 * MainController
 * 
 * This is the controller used by Spring MVC to handle requests
 * 
 */
@SuppressWarnings("deprecation")
@Slf4j
@RestController
public class MeetingsController {

    @Autowired
    private UserDirectoryService userDirectoryService;
    
    @Autowired
	private SessionManager sessionManager;
	
    @Autowired
	private ToolManager toolManager;
    
    @Autowired
    private SiteService siteService;
    
    @Autowired
    private SecurityService securityService;
	
    @Autowired
    private MeetingService meetingService;
    
    @Autowired
    private CalendarService calendarService;
    
    private MicrosoftTeamsService teamsService = new MicrosoftTeamsService();
    
    private static final String ONLINE_MEETING_ID = "onlineMeetingId";
    private static final String ORGANIZER_USER = "organizerUser";
    private static final String CALENDAR_EVENT_ID = "calendarEventId";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String MEETING_EVENT_TYPE = "Meeting";

    /**
     * Method to obtain the current user
     * @return
     */
    private User getCurrentUser() {
        String userId = sessionManager.getCurrentSessionUserId();
        try {
            return userDirectoryService.getUser(userId);
        } catch (UserNotDefinedException e) {
            log.error("User {} not found.", userId);
        }
        return null;
    }

    /**
     * Method to obtain the current site id
     * @return
     */
    private String getCurrentSiteId() {
        return toolManager.getCurrentPlacement().getContext();
    }
    
    /**
     * Retrieves the groups list from a site
     * @param siteId
     * @return
     * @throws MeetingsException
     */
    @GetMapping(value = "/meetings/site/{siteId}/groups", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<GroupData> getSiteGroups(@PathVariable String siteId) throws MeetingsException {
        List<GroupData> siteGroups = new ArrayList<>();
        try {
            Site site = siteService.getSite(siteId);
            Collection<Group> groups = site.getGroups();
            groups.stream().forEach(group -> {
               GroupData data = new GroupData();
               data.setGroupId(group.getId());
               data.setGroupName(group.getTitle());
               siteGroups.add(data);
            });
        } catch (IdUnusedException e) {
            log.error("Error retrieving groups", e);
            throw new MeetingsException(e.getLocalizedMessage());
        }
        return siteGroups;
    }
    
    /**
     * Retrieves current user permission to edit meetings
     * @return
     */
    @GetMapping(value = "/meetings/user/editperms/site/{siteId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean canUpdateSite(@PathVariable String siteId) {
        boolean result = false;
        String userId = sessionManager.getCurrentSessionUserId();
        try {
            Site site = siteService.getSite(siteId);
            result = (securityService.unlock(userId, SiteService.SECURE_UPDATE_SITE, site.getReference()) || securityService.isSuperUser(userId));
        } catch (IdUnusedException e) {
            log.error("Error retrieving user permission", e);
            result = false;
        }
        return result;
    }

    /**
     * Retrieves all current user meetings
     * @return
     */
    @GetMapping(value = "/meeting/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<Meeting> getAllMeetings() {
        String userId = getCurrentUser().getId();
        List<Site> sites = siteService.getUserSites();
        List<String> siteIds = sites.stream().map(e->e.getId()).collect(Collectors.toList());
        List<Group> groups = new ArrayList<>();
        siteIds.stream().forEach(s -> {
            try {
                Site site = siteService.getSite(s);
                groups.addAll(site.getGroupsWithMember(userId));
                site.getGroupsWithMember(userId);
            } catch (IdUnusedException e) {
                log.error("Error while retrieving group list on Meetings", e);
            };
        });
        List<String> groupIds = groups.stream().map(e->e.getId()).collect(Collectors.toList());
        return meetingService.getUserMeetings(userId, siteIds, groupIds);
    }
    
    /**
     * Method to save a new meeting
     * @param data
     * @return
     * @throws MeetingsException
     */
    @PostMapping(value = "/meeting", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Meeting createMeeting(@RequestBody MeetingData data) throws MeetingsException {
        Meeting meeting = null;
        User user = getCurrentUser();
        try {
            meeting = new Meeting();
            BeanUtils.copyProperties(data, meeting);
            if (StringUtils.isNotBlank(data.getStartDate())) {
                Instant startDate = LocalDateTime.parse(data.getStartDate(), DateTimeFormatter.ofPattern(DATE_FORMAT)).atZone(ZoneId.systemDefault()).toInstant();
                meeting.setStartDate(startDate);
            }
            if (StringUtils.isNotBlank(data.getEndDate())) {
                Instant endDate = LocalDateTime.parse(data.getEndDate(), DateTimeFormatter.ofPattern(DATE_FORMAT)).atZone(ZoneId.systemDefault()).toInstant();
                meeting.setEndDate(endDate);
            }
            TeamsMeetingData meetingTeams = teamsService.onlineMeeting(user.getEmail(), meeting.getTitle(), meeting.getStartDate(), meeting.getEndDate());
            meeting.setUrl(meetingTeams.getJoinUrl());
            meeting = meetingService.createMeeting(meeting);
            meetingService.addAttendeeToMeeting(meeting, user.getId(), AttendeeType.USER);
            meetingService.setMeetingProperty(meeting, ONLINE_MEETING_ID, meetingTeams.getId());
            meetingService.setMeetingProperty(meeting, ORGANIZER_USER, user.getEmail());
            if (/*data.isSaveToCalendar() &&*/StringUtils.isNotBlank(data.getStartDate()) && StringUtils.isNotBlank(data.getEndDate())) {
                this.saveToCalendar(meeting);
            }
            //this.sendNotification(meeting, data.getNotificationType());
        } catch (ParseException e) {
            log.error("Error creating meeting", e);
            throw new MeetingsException(e.getLocalizedMessage());
        }
        return meeting;
    }
    
    /**
     * Method to update an existing meeting
     * @param data
     * @param meetingId
     * @return
     * @throws MeetingsException 
     */
    @PutMapping(value = "/meeting/{meetingId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Meeting updateMeeting(@RequestBody MeetingData data, @PathVariable String meetingId) throws MeetingsException {
        Optional<Meeting> optMeeting = meetingService.getMeetingById(meetingId);
        Meeting meeting = null;
        if (optMeeting.isPresent()) {
            meeting = optMeeting.get();
            meeting.setTitle(data.getTitle());
            meeting.setDescription(data.getDescription());
            Instant startDate = LocalDateTime.parse(data.getStartDate(), DateTimeFormatter.ofPattern(DATE_FORMAT)).atZone(ZoneId.systemDefault()).toInstant();
            Instant endDate = LocalDateTime.parse(data.getEndDate(), DateTimeFormatter.ofPattern(DATE_FORMAT)).atZone(ZoneId.systemDefault()).toInstant();
            meeting.setStartDate(startDate);
            meeting.setEndDate(endDate);
            meetingService.updateMeeting(meeting);
            if (/*data.isSaveToCalendar() &&*/StringUtils.isNotBlank(data.getStartDate()) && StringUtils.isNotBlank(data.getEndDate())) {
                this.saveToCalendar(meeting);
            }
            //this.sendNotification(meeting, data.getNotificationType());
        }
        return meeting;
    }
    
    /**
     * Method to remove an existing meeting
     * @param meetingId
     * @throws MeetingsException
     */
    @DeleteMapping(value = "/meeting/{meetingId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteMeeting(@PathVariable String meetingId) throws MeetingsException {
        try {
            this.removeFromCalendar(meetingId);
            meetingService.deleteMeetingById(meetingId);
        } catch (Exception e) {
            log.error("Error deleting meeting", e);
            throw new MeetingsException(e.getLocalizedMessage());
        }
    }
    
    /**
     * Method to send notifications to users about meetings, by level of priority
     * @param meeting
     * @param type
     */
    private void sendNotification(Meeting meeting, NotificationType type) {
        switch (type) {
        case LOW:
            break;
        case HIGH:
            break;
        case NONE:
            break;
        default:
            break;
        }
    }
    
    /**
     * Method to save a meeting as an event of the Sakai calendar
     * @param meeting
     * @throws MeetingsException
     */
    private void saveToCalendar(Meeting meeting) throws MeetingsException {
        List<String> references = calendarService.getCalendarReferences(meeting.getSiteId());
        try {
            Calendar calendar = calendarService.getCalendar(references.get(0));
            String calendarEventId = meetingService.getMeetingProperty(meeting, CALENDAR_EVENT_ID);
            CalendarEventEdit eventEdit = null;
            if (StringUtils.isNotBlank(calendarEventId)) {
                eventEdit = calendar.getEditEvent(calendarEventId, CalendarService.EVENT_MODIFY_CALENDAR);
            } else {
                eventEdit = calendar.addEvent();
            }
            long duration = meeting.getEndDate().toEpochMilli() - meeting.getStartDate().toEpochMilli();
            TimeRange range = TimeService.newTimeRange(meeting.getStartDate().toEpochMilli(), duration);
            eventEdit.setRange(range);
            eventEdit.setType(MEETING_EVENT_TYPE);
            eventEdit.setDisplayName(meeting.getTitle());
            eventEdit.setDescription(meeting.getDescription());
            calendar.commitEvent(eventEdit);
            if (StringUtils.isBlank(calendarEventId)) {
                meetingService.setMeetingProperty(meeting, CALENDAR_EVENT_ID, eventEdit.getId());
            }
        } catch (IdUnusedException | PermissionException | InUseException e) {
            log.error("Error saving meeting to the calendar", e);
            throw new MeetingsException(e.getLocalizedMessage());
        }
    }
    
    /**
     * Method to remove a calendar event based on a meeting
     * @param meetingId
     * @throws MeetingsException
     */
    private void removeFromCalendar(String meetingId) throws MeetingsException {
        try {
            Optional<Meeting> optMeeting = meetingService.getMeetingById(meetingId);
            if (optMeeting.isPresent()) {
                Meeting meeting = optMeeting.get();
                List<String> references = calendarService.getCalendarReferences(meeting.getSiteId());
                Calendar calendar = calendarService.getCalendar(references.get(0));
                String calendarEventId = meetingService.getMeetingProperty(meeting, CALENDAR_EVENT_ID);
                CalendarEventEdit eventRemove = calendar.getEditEvent(calendarEventId, CalendarService.EVENT_REMOVE_CALENDAR_EVENT);
                calendar.removeEvent(eventRemove);
            }
        } catch (IdUnusedException | PermissionException | InUseException e) {
            log.error("Error removing meeting from the calendar", e);
            throw new MeetingsException(e.getLocalizedMessage());
        }
    }

}
