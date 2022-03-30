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

import org.sakaiproject.meetings.api.MeetingService;
import org.sakaiproject.meetings.api.model.Meeting;
import org.sakaiproject.meetings.controller.data.MeetingData;
import org.sakaiproject.meetings.teams.MicrosoftTeamsService;
import org.sakaiproject.meetings.teams.data.TeamsMeetingData;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * MainController
 * 
 * This is the controller used by Spring MVC to handle requests
 * 
 */
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
    private MeetingService meetingService;
    
    private MicrosoftTeamsService teamsService = new MicrosoftTeamsService();

    private final String INDEX_TEMPLATE = "index";
    
    public static final String ONLINE_MEETING_ID = "onlineMeetingId";
    public static final String ORGANIZER_USER = "organizerUser";

    
    public User getCurrentUser() {
        String userId = sessionManager.getCurrentSessionUserId();
        log.info("Current user is {}", userId);
        try {
            return userDirectoryService.getUser(userId);
        } catch (UserNotDefinedException e) {
            log.error("User {} not found.", userId);
        }
        return null;
    }

    public String getCurrentSiteId() {
        return toolManager.getCurrentPlacement().getContext();
    }
    

    @GetMapping(value = "/meeting/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<Meeting> getAllMeetings() {    
        return meetingService.getAllMeetings();
    }

    
    @PostMapping(value = "/meeting", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Meeting createMeeting(@RequestBody MeetingData data) {
        Meeting meeting = null;
        try {
            meeting = new Meeting();
            BeanUtils.copyProperties(data, meeting);
            TeamsMeetingData meetingTeams = teamsService.onlineMeeting("ropemar@ropemargmail.onmicrosoft.com", meeting.getTitle(), meeting.getStartDate(), meeting.getEndDate());
            meeting.setUrl(meetingTeams.getJoinUrl());
            meetingService.createMeeting(meeting);
            meetingService.setMeetingProperty(meeting, ONLINE_MEETING_ID, meetingTeams.getId());
            meetingService.setMeetingProperty(meeting, ORGANIZER_USER, "ropemar@ropemargmail.onmicrosoft.com");
        } catch (ParseException e) {
            log.error("Error creating meeting", e);
        }
        return meeting;
    }
    
    @PutMapping(value = "/meeting/{meetingId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Meeting updateMeeting(@RequestBody MeetingData data, @PathVariable String meetingId) {
        return null;
    }
    
    @DeleteMapping(value = "/meeting/{meetingId}")
    public ResponseEntity<?> deleteMeeting(@PathVariable String meetingId) {
        try {
            meetingService.deleteMeetingById(meetingId);
        } catch (Exception e) {
            log.error("Error deleting meeting", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    
    @GetMapping("/groups")
    public String groups(Model model) {
        teamsService.getGroups();
        return "index";
    }
    
    @GetMapping("/createteam")
    public String createTeam(Model model) {
        try {
            teamsService.createTeam();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "index";
    }
    
    @GetMapping("/users")
    public String users(Model model) {
        teamsService.getAzureUserList();
        return "index";
    }

}
