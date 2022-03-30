package org.sakaiproject.meetings.impl.persistence;

import org.sakaiproject.meetings.api.persistence.MeetingRepository;
import org.sakaiproject.serialization.BasicSerializableRepository;

import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.meetings.api.model.Meeting;

public class MeetingRepositoryImpl extends BasicSerializableRepository<Meeting, String> implements MeetingRepository {

    public Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }
    
    public Optional<Meeting> findById(String id) {
        Meeting meeting = (Meeting) startCriteriaQuery().add(Restrictions.eq("id", id)).uniqueResult();
        return Optional.ofNullable(meeting);
    }

    @Override
    public void deleteById(String id) {
        CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
        CriteriaDelete<Meeting> criteriaDelete = criteriaBuilder.createCriteriaDelete(Meeting.class);
        Root<Meeting> root = criteriaDelete.from(Meeting.class);
        criteriaDelete.where(criteriaBuilder.equal(root.get("id"), id));
        getCurrentSession().createQuery(criteriaDelete);
    }


}
