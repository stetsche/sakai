package org.sakaiproject.meetings.impl.persistence;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.meetings.api.model.MeetingProperty;
import org.sakaiproject.meetings.api.persistence.MeetingPropertyRepository;
import org.sakaiproject.serialization.BasicSerializableRepository;

public class MeetingPropertyRepositoryImpl extends BasicSerializableRepository<MeetingProperty, Long> implements MeetingPropertyRepository{

    public Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }
    
    @Override
    public MeetingProperty findFirstByName(String name) {
        MeetingProperty meetingProperty = (MeetingProperty) startCriteriaQuery().add(Restrictions.eq("name", name)).uniqueResult();
        return meetingProperty;
    }

    @Override
    public void deletePropertiesByMeetingId(String meetingId) {
        getCurrentSession().createQuery("delete from MeetingProperty where meeting.id = :id").setParameter("id", meetingId).executeUpdate();
    }

}
