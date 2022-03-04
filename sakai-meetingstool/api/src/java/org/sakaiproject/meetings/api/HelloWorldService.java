package org.sakaiproject.meetings.api;

import org.sakaiproject.meetings.api.model.Person;

/**
 * HelloWorldService is the service that says hi.
 */

public interface HelloWorldService {

    public Person findPerson(String id);

    public Iterable<Person> findAll();

    public Iterable<Person> findPersonsBySite(String siteId);

    public void newPerson(Person person);

    public void savePerson(Person person);

    public void deletePersonById(String personId);

    public void deletePerson(Person person);

    public long countPersons();

}
