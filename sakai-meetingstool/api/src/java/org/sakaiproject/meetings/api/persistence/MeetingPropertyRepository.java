package org.sakaiproject.meetings.api.persistence;

import org.sakaiproject.meetings.api.model.MeetingProperty;
import org.sakaiproject.serialization.SerializableRepository;

public interface MeetingPropertyRepository  extends SerializableRepository<MeetingProperty, Long> {

	public MeetingProperty findFirstByName(String name);

	public void deletePropertiesByMeetingId(String meetingId);
	
}
