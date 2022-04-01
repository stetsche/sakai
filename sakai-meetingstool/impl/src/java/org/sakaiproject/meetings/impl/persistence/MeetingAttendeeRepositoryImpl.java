package org.sakaiproject.meetings.impl.persistence;

import org.hibernate.Session;
import org.sakaiproject.meetings.api.model.MeetingAttendee;
import org.sakaiproject.meetings.api.persistence.MeetingAttendeeRepository;
import org.sakaiproject.serialization.BasicSerializableRepository;

public class MeetingAttendeeRepositoryImpl extends BasicSerializableRepository<MeetingAttendee, Long> implements MeetingAttendeeRepository{

    public Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }
    
    @Override
    public void removeAttendeesByMeetingId(String meetingId) {
        getCurrentSession().createQuery("delete from MeetingAttendee where meeting.id = :id").setParameter("id", meetingId).executeUpdate();
    }
    
}
