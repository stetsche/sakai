package org.sakaiproject.meetings.impl;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.meetings.api.model.Meeting;
import org.sakaiproject.meetings.api.model.MeetingProperty;
import org.sakaiproject.meetings.api.persistence.MeetingPropertyRepository;
import org.sakaiproject.meetings.api.persistence.MeetingRepository;

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

	public void createMeeting(Meeting meetingData) {
		meetingRepository.save(meetingData);
	}
	
	public void deleteMeetingById(String id) {
		meetingPropertyRepository.deletePropertiesByMeetingId(id);
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
	
}
