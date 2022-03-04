package org.sakaiproject.meetings.impl.persistence;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.meetings.api.model.Person;
import org.sakaiproject.meetings.api.persistence.HelloWorldRepository;
import org.sakaiproject.serialization.BasicSerializableRepository;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class HelloWorldRepositoryImpl extends BasicSerializableRepository<Person, String> implements HelloWorldRepository {

    public Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<Person> findPersonsBySite(String siteId) {
        return startCriteriaQuery().add(Restrictions.eq("context", siteId)).list();
    }

}
