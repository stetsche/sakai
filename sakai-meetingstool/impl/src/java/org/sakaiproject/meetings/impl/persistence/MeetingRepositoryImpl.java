package org.sakaiproject.meetings.impl.persistence;

import org.sakaiproject.meetings.api.persistence.MeetingRepository;
import org.sakaiproject.serialization.BasicSerializableRepository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.sakaiproject.meetings.api.model.AttendeeType;
import org.sakaiproject.meetings.api.model.Meeting;
import org.sakaiproject.meetings.api.model.MeetingAttendee;

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
        getCurrentSession().createQuery("delete from Meeting where id = :id").setParameter("id", id).executeUpdate();
    }
    
    @Override
    public List<Meeting> getMeetings(String userId, List<String> siteIds, List<String> groupIds) {
        CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<Meeting> query = criteriaBuilder.createQuery(Meeting.class);
        Root<Meeting> root = query.from(Meeting.class);
        Join<Meeting, MeetingAttendee> joinAttendees = root.join("attendees");
        Predicate orClause = criteriaBuilder.disjunction();
        
        if (userId != null) {
            Predicate userRestriction = criteriaBuilder.and(
                    criteriaBuilder.equal(joinAttendees.get("type"), AttendeeType.USER),
                    criteriaBuilder.equal(joinAttendees.get("objectId"), userId));
            orClause.getExpressions().add(userRestriction);
        }
        if (!CollectionUtils.isEmpty(siteIds)) {
            Predicate siteRestriction = criteriaBuilder.and(
                    criteriaBuilder.equal(joinAttendees.get("type"), AttendeeType.SITE),
                    joinAttendees.get("objectId").in(siteIds));
            orClause.getExpressions().add(siteRestriction);
        }
        if (!CollectionUtils.isEmpty(groupIds)) {
            Predicate groupRestriction = criteriaBuilder.and(
                    criteriaBuilder.equal(joinAttendees.get("type"), AttendeeType.GROUP),
                    joinAttendees.get("objectId").in(groupIds));
            orClause.getExpressions().add(groupRestriction);
        }
        
        query.select(root).where(orClause).distinct(true);
        
        return getCurrentSession().createQuery(query).getResultList();
    }
    
}
