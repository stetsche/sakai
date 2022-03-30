package org.sakaiproject.meetings.api;

import java.util.Optional;

import org.sakaiproject.meetings.api.model.Meeting;

public interface MeetingService {

	public Iterable<Meeting> getAllMeetings() ;
	public void createMeeting(Meeting meetingData);
	public void deleteMeetingById(String id);
	public Optional<Meeting> getMeetingById(String id);
	public void setMeetingProperty(Meeting meeting, String property, String value);
	public String getMeetingProperty(Meeting meeting, String property);
	
	
}
