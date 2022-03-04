package org.sakaiproject.meetings.impl.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.meetings.api.HelloWorldService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloWorldJob implements Job {

    @Setter
    private HelloWorldService helloWorldService;

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Hello world job, we have {} persons.", helloWorldService.countPersons());
    }

}
