package org.sakaiproject.meetings.impl;

import org.sakaiproject.meetings.api.HelloWorldService;
import org.sakaiproject.meetings.api.model.Person;
import org.sakaiproject.meetings.api.persistence.HelloWorldRepository;
import org.springframework.transaction.annotation.Transactional;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class HelloWorldServiceImpl implements HelloWorldService {

    @Setter
    private HelloWorldRepository helloWorldRepository;

    public void init() {
        log.info("Initializing Hello World Service");
    }

    public Iterable<Person> findAll() {
        log.info("Returning all users");
        return helloWorldRepository.findAll();
    }

    public Person findPerson(String id) {
        log.info("Finding person by Id {}", id);
        return helloWorldRepository.findOne(id);
    }

    public Iterable<Person> findPersonsBySite(String siteId) {
        log.info("Finding persons by site {}", siteId);
        return helloWorldRepository.findPersonsBySite(siteId);
    }

    public void newPerson(Person person) {
        log.info("Saving person {}", person);
        helloWorldRepository.persist(person);
    }

    public void savePerson(Person person) {
        log.info("Saving person {}", person);
        helloWorldRepository.merge(person);
    }

    public void deletePersonById(String personId) {
        log.info("Deleting person {}", personId);
        helloWorldRepository.delete(personId);
    }

    public void deletePerson(Person person) {
        log.info("Deleting person {}", person);
        helloWorldRepository.delete(person);
    }

    public long countPersons() {
        return helloWorldRepository.count();
    }

}
