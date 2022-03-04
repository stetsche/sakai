package org.sakaiproject.meetings.api.persistence;

import org.sakaiproject.meetings.api.model.Person;
import org.sakaiproject.serialization.SerializableRepository;

public interface HelloWorldRepository extends SerializableRepository<Person, String> {

    Iterable<Person> findPersonsBySite(String siteId);

}
