package org.sakaiproject.meetings.impl;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.sakaiproject.meetings.api.model.AttendeeType;
import org.sakaiproject.meetings.api.model.Meeting;
import org.sakaiproject.meetings.api.model.MeetingAttendee;
import org.sakaiproject.meetings.api.model.MeetingProperty;
import org.sakaiproject.meetings.api.persistence.MeetingAttendeeRepository;
import org.sakaiproject.meetings.api.persistence.MeetingPropertyRepository;
import org.sakaiproject.meetings.api.persistence.MeetingRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.sakaiproject.meetings.api.MeetingService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class MeetingServiceImpl implements MeetingService {

    @Setter
	MeetingRepository meetingRepository;
	
    @Setter
	MeetingPropertyRepository meetingPropertyRepository;
    
    @Setter
    MeetingAttendeeRepository meetingAttendeeRepository;

    public void init() {
        log.info("Initializing Meeting Service");
    }
    
	public Iterable<Meeting> getAllMeetings() {
		return meetingRepository.findAll();
	}
	
	public List<Meeting> getUserMeetings(String userId, List<String> siteIds, List <String> groupIds) {
        return meetingRepository.getMeetings(userId, siteIds, groupIds);
    }
	
	public Optional<Meeting> getMeetingById(String id) {
		return meetingRepository.findById(id);
	}

	public Meeting createMeeting(Meeting meetingData) {
		return meetingRepository.save(meetingData);
	}
	
	public void updateMeeting(Meeting meetingData) {
	    meetingRepository.update(meetingData);
	}
	
	public void deleteMeetingById(String id) {
		meetingPropertyRepository.deletePropertiesByMeetingId(id);
		meetingAttendeeRepository.removeAttendeesByMeetingId(id);
		meetingRepository.deleteById(id);
	}
	
	public void setMeetingProperty(Meeting meeting, String property, String value) {
		MeetingProperty meetingProp = new MeetingProperty();
		meetingProp.setMeeting(meeting);
		meetingProp.setName(property);
		meetingProp.setValue(value);
		meetingPropertyRepository.save(meetingProp);
	}

	public String getMeetingProperty(Meeting meeting, String property) {
		String result = null;
		MeetingProperty prop = meetingPropertyRepository.findFirstByName(property);
		if (prop != null) {
			result = prop.getValue();
		}
		return result;
	}
	
	public void addAttendeeToMeeting(Meeting meeting, String objectId, AttendeeType type) {
	    MeetingAttendee attendee = new MeetingAttendee();
	    attendee.setObjectId(objectId);
	    attendee.setType(type);
	    if (CollectionUtils.isEmpty(meeting.getAttendees())) {
	        meeting.setAttendees(Arrays.asList(attendee));
	    } else {
	        meeting.getAttendees().add(attendee);
	    }
	    meetingRepository.update(meeting);
	    attendee.setMeeting(meeting);
	    attendee = meetingAttendeeRepository.save(attendee);
	}
	
}
