package org.sakaiproject.meetings.service;

import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HelloWorldToolService {

    @Autowired
    private UserDirectoryService userDirectoryService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ToolManager toolManager;

    public User getCurrentUser() {
        String userId = sessionManager.getCurrentSessionUserId();
        log.info("Current user is {}", userId);
        try {
            return userDirectoryService.getUser(userId);
        } catch (UserNotDefinedException e) {
            log.error("User {} not found.", userId);
        }
        return null;
    }

    public String getCurrentSiteId() {
        return toolManager.getCurrentPlacement().getContext();
    }

}
