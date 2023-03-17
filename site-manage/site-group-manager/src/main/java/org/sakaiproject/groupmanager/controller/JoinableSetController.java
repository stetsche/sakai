/**
 * Copyright (c) 2003-2021 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.groupmanager.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.groupmanager.constants.GroupManagerConstants;
import org.sakaiproject.groupmanager.form.JoinableSetForm;
import org.sakaiproject.groupmanager.service.SakaiService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

@Slf4j
@Controller
public class JoinableSetController {    

    @Inject
    private MessageSource messageSource;

    @Autowired
    private SakaiService sakaiService;

    @RequestMapping(value = "/joinableset")
    public String showJoinableSet(Model model, @RequestParam(value="groupId", required = false) String groupId) {
        log.debug("showJoinableSet()");

        Optional<Site> siteOptional = sakaiService.getCurrentSite();
        if (!siteOptional.isPresent()) {
            return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
        }

        Site site = siteOptional.get();

        // Variable definition
        JoinableSetForm joinableSetForm = new JoinableSetForm();
        List<Group> joinableSetGroups = new ArrayList<Group>();
        List<User> notJoinedUserList = new ArrayList<User>();

        if (StringUtils.isNotBlank(groupId)) {
            // Get the joinable set Id
            Group joinableGroup = site.getGroup(groupId);
            // Should always have the joinable set property, based on the index.html condition.
            String joinableSetId = joinableGroup.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET);
            // Set the joinable set Id
            joinableSetForm.setJoinableSetId(joinableSetId);
            // Set the joinable set title
            joinableSetForm.setGroupTitle(joinableSetId);
            // Get the groups associated to the existing joinable set
            joinableSetGroups = site.getGroups().stream().filter(g -> joinableSetId.equalsIgnoreCase(g.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET))).collect(Collectors.toList());
            boolean allowUnjoin = !joinableSetGroups.isEmpty() ? joinableSetGroups.get(0).getProperties().getProperty(Group.GROUP_PROP_JOINABLE_UNJOINABLE) != null ? Boolean.valueOf(joinableSetGroups.get(0).getProperties().getProperty(Group.GROUP_PROP_JOINABLE_UNJOINABLE)) : false : false;
            joinableSetForm.setAllowUnjoin(allowUnjoin);
            // Get all the members of the groups that belongs to the joinable set. 
            List<Member> joinedUserList = new ArrayList<Member>();
            joinableSetGroups.forEach(g -> {
                joinedUserList.addAll(g.getMembers());
            });

            // Put all the members of the site that does not belong to any group of the set in a list.
            site.getUsers().forEach( userId -> {
                if (!joinedUserList.stream().anyMatch(member -> userId.equals(member.getUserId()))) {
                    Optional<User> optionalUser = sakaiService.getUser(userId);
                    if (optionalUser.isPresent()) {
                        notJoinedUserList.add(optionalUser.get());
                    }
                }

            });

            ZoneId userTimeZone = sakaiService.getUserTimeZone().toZoneId();
            String joinableOpenDate = joinableGroup.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_OPEN_DATE);
            String joinableCloseDate = joinableGroup.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_CLOSE_DATE);
            // Convert stored datetimes: from UTC to user's time zone.
            if (joinableOpenDate != null) {
                LocalDateTime openDate = LocalDateTime.parse(joinableOpenDate);
                ZonedDateTime utcOpenDate = ZonedDateTime.of(openDate, ZoneOffset.UTC);
                LocalDateTime zonedOpenDate = LocalDateTime.ofInstant(utcOpenDate.toInstant(), userTimeZone);
                joinableOpenDate = zonedOpenDate.toString();
            }
            if (joinableCloseDate != null) {
                LocalDateTime closeDate = LocalDateTime.parse(joinableCloseDate);
                ZonedDateTime utcCloseDate = ZonedDateTime.of(closeDate, ZoneOffset.UTC);
                LocalDateTime zonedCloseDate = LocalDateTime.ofInstant(utcCloseDate.toInstant(), userTimeZone);
                joinableCloseDate = zonedCloseDate.toString();
            }
            joinableSetForm.setJoinableOpenDate(joinableOpenDate);
            joinableSetForm.setJoinableCloseDate(joinableCloseDate);
            joinableSetForm.setGroupNumber(0);
            joinableSetForm.setGroupMaxMembers(1);

            // In order to display the right view after joinableset.error.wrongdatesorder
            model.addAttribute("currentGroupId", groupId);
        }

        // Fill the model with the variables.
        model.addAttribute("joinableSetForm", joinableSetForm);
        model.addAttribute("joinableSetGroups", joinableSetGroups);
        model.addAttribute("notJoinedUserList", notJoinedUserList);

        return GroupManagerConstants.JOINABLE_SET_TEMPLATE;
    }

    @PostMapping(value = "/saveJoinableSet")
    public String saveJoinableSet(@ModelAttribute JoinableSetForm joinableSetForm, Model model, @RequestParam(value="groupId", required = false) String groupId) {
        log.debug("saveJoinableSet called, saving a group with title {}", joinableSetForm.getGroupTitle());

        Optional<Site> siteOptional = sakaiService.getCurrentSite();
        if (!siteOptional.isPresent()) {
            return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
        }

        Site site = siteOptional.get();

        // Variable definition
        Locale userLocale = sakaiService.getCurrentUserLocale();
        String joinableSetId = joinableSetForm.getJoinableSetId();
        String joinableSetTitle = joinableSetForm.getGroupTitle();
        boolean editingJoinableSet = StringUtils.isNotBlank(joinableSetId);
        int joinableSetGroupNumber = joinableSetForm.getGroupNumber();
        int joinableSetMaxMembers = joinableSetForm.getGroupMaxMembers();
        String joinableOpenDate = joinableSetForm.getJoinableOpenDate();
        String joinableCloseDate = joinableSetForm.getJoinableCloseDate();
        boolean sendMail = joinableSetForm.isSendMail();
        boolean sendReminder = joinableSetForm.isSendReminder();
        String allowPreviewMembership = Boolean.toString(joinableSetForm.isAllowPreviewMembership());
        String allowUnjoin = Boolean.toString(joinableSetForm.isAllowUnjoin());
        String allowViewMembership = Boolean.toString(joinableSetForm.isAllowViewMembership());
        List<String> groupTitles = new ArrayList<String>();
        List<Group> siteGroups = new ArrayList<Group>(site.getGroups());
        siteGroups.forEach(g -> groupTitles.add(g.getTitle()));

        //Ensure the group title is provided
        if (StringUtils.isBlank(joinableSetTitle)) {
            model.addAttribute("errorMessage", messageSource.getMessage("joinableset.error.providetitle", null, userLocale));
            return showJoinableSet(model, null);
        }

        //Ensure the set max members is inside the limits
        if (joinableSetMaxMembers <= 0 || joinableSetMaxMembers > 999) {
            model.addAttribute("errorMessage", messageSource.getMessage("joinableset.error.maxmembers", null, userLocale));
            return showJoinableSet(model, null);
        }

        ZoneId userTimeZone = sakaiService.getUserTimeZone().toZoneId();
        LocalDateTime utcOpenDate = null;
        LocalDateTime utcCloseDate = null;
        // Convert input datetimes: from user's time zone to UTC.
        if (StringUtils.isNotBlank(joinableOpenDate)) {
            LocalDateTime openDate = LocalDateTime.parse(joinableOpenDate);
            ZonedDateTime zonedOpenDate = ZonedDateTime.of(openDate, userTimeZone);
            utcOpenDate = LocalDateTime.ofInstant(zonedOpenDate.toInstant(), ZoneOffset.UTC);
        }
        if (StringUtils.isNotBlank(joinableCloseDate)) {
            LocalDateTime closeDate = LocalDateTime.parse(joinableCloseDate);
            ZonedDateTime zonedCloseDate = ZonedDateTime.of(closeDate, userTimeZone);
            utcCloseDate = LocalDateTime.ofInstant(zonedCloseDate.toInstant(), ZoneOffset.UTC);
        }
        if (utcOpenDate != null && utcCloseDate != null) {
            // If both dates are provided, make sure openDate comes before closeDate.
            if (utcOpenDate.isAfter(utcCloseDate)) {
                model.addAttribute("errorMessage", messageSource.getMessage("joinableset.error.wrongdatesorder", null, userLocale));
                return showJoinableSet(model, groupId);
            }
        }
        if (utcOpenDate != null) {
            joinableOpenDate = utcOpenDate.toString();
        }
        if (utcCloseDate != null) {
            joinableCloseDate = utcCloseDate.toString();
        }

        if (sendMail) {
            String siteName = site.getTitle();
            Optional<Group> joinableGroup = siteGroups.stream()
            .filter((Group g) -> joinableSetTitle.equalsIgnoreCase(g.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET))).findAny();

            if (joinableGroup.isPresent()) {
                for (String userId : site.getUsers()) {
                    Optional<User> siteUser = sakaiService.getUser(userId);
                    if (siteUser.isPresent()) {
                        try {
                            sakaiService.notifyAboutJoinableSet(siteName, siteUser.get(), joinableGroup.get(), !editingJoinableSet);
                        } catch (Exception e) {
                            String notiErrorMsg = messageSource.getMessage("joinableset.error.notificationfail", null, userLocale);
                            log.warn(notiErrorMsg);
                            model.addAttribute("errorMessage", notiErrorMsg);
                            return showJoinableSet(model, groupId);
                        }
                    }
                }
            }
        }
        if (sendReminder) {
            // Execute method accepts a single parameter...
            String dataPair = site.getTitle() + "," + joinableSetTitle;
            System.out.println("- SCHEDULE 1");
            sakaiService.scheduleReminder(utcCloseDate.minusDays(1).toInstant(ZoneOffset.UTC), dataPair);
        }

        if (!editingJoinableSet) {
            //Ensure the set number is inside the limits
            if (joinableSetGroupNumber <= 0 || joinableSetGroupNumber > 999) {
                model.addAttribute("errorMessage", messageSource.getMessage("joinableset.error.numbergroups", null, userLocale));
                return showJoinableSet(model, null);
            }

            //Ensure the joinable set title is not duplicated
            if (editingJoinableSet && siteGroups.stream().anyMatch(g -> joinableSetTitle.equalsIgnoreCase(g.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET))) ) {
                model.addAttribute("errorMessage", messageSource.getMessage("joinableset.error.duplicatedtitle", null, userLocale));
                return showJoinableSet(model, null);
            }
        } else {
            //Ensure the set number is inside the limits
            if (joinableSetGroupNumber < 0 || joinableSetGroupNumber > 999) {
                model.addAttribute("errorMessage", messageSource.getMessage("joinableset.error.numbergroups", null, userLocale));
                return showJoinableSet(model, null);
            }

            // Get the groups that belong to the joinable set
            List<Group> joinableSetGroups = siteGroups.stream().filter(g -> joinableSetId.equalsIgnoreCase(g.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET))).collect(Collectors.toList());
            // For each group, update the joinableSet title and properties.
            for (Group group : joinableSetGroups) {
                group.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_SET, joinableSetTitle);
                group.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_UNJOINABLE, allowUnjoin);
                if (joinableOpenDate.isBlank()) {
                    group.getProperties().removeProperty(Group.GROUP_PROP_JOINABLE_OPEN_DATE);
                } else {
                    group.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_OPEN_DATE, joinableOpenDate);
                }
                if (joinableCloseDate.isBlank()) {
                    group.getProperties().removeProperty(Group.GROUP_PROP_JOINABLE_CLOSE_DATE);
                } else {
                    group.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_CLOSE_DATE, joinableCloseDate);
                }
            }
        }

        for (int i = 1; i <= joinableSetGroupNumber; i++) {
            String groupTitle = String.format("%s %d", joinableSetTitle, i);

            // Always look for a new Id if the name is already in use by a group
            while (groupTitles.contains(groupTitle)) {
                groupTitle+=i;
            }

            Group newGroup = site.addGroup();
            newGroup.getProperties().addProperty(Group.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
            newGroup.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_SET, joinableSetTitle);
            newGroup.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_SET_MAX, String.valueOf(joinableSetMaxMembers));
            newGroup.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_SET_PREVIEW, allowPreviewMembership);
            newGroup.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_UNJOINABLE, allowUnjoin);
            newGroup.getProperties().addProperty(Group.GROUP_PROP_VIEW_MEMBERS, allowViewMembership);
            if (StringUtils.isNotBlank(joinableOpenDate)) {
                newGroup.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_OPEN_DATE, joinableOpenDate);
            }
            if (StringUtils.isNotBlank(joinableCloseDate)) {
                newGroup.getProperties().addProperty(Group.GROUP_PROP_JOINABLE_CLOSE_DATE, joinableCloseDate);
            }
            newGroup.setTitle(groupTitle);
        }

        sakaiService.saveSite(site);

        return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
    }

    @RequestMapping(value = "/deleteJoinableSet")
    public String deleteJoinableSet(@RequestParam(name="joinableSetId", required=true) String joinableSetId, Model model) {
        log.debug("deleteJoinableSet() called with joinable set id {}.", joinableSetId);

        Optional<Site> siteOptional = sakaiService.getCurrentSite();
        if (!siteOptional.isPresent()) {
            return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
        }

        Site site = siteOptional.get();

        if (StringUtils.isBlank(joinableSetId)) {
            log.error("Trying to delete a joinable set with empty Id.");
            return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
        }

        boolean anyGroupUpdated = false;

        // Delete the joinable set from all the site groups which is present.
        for (Group group : site.getGroups()) {
            String groupJoinableSet = group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET);
            if (StringUtils.isNotBlank(groupJoinableSet) && joinableSetId.equalsIgnoreCase(groupJoinableSet)) {
                group.getProperties().removeProperty(Group.GROUP_PROP_JOINABLE_SET);
                group.getProperties().removeProperty(Group.GROUP_PROP_JOINABLE_SET_MAX);
                group.getProperties().removeProperty(Group.GROUP_PROP_JOINABLE_SET_PREVIEW);
                group.getProperties().removeProperty(Group.GROUP_PROP_JOINABLE_UNJOINABLE);
                anyGroupUpdated = true;
            }
        }

        if (anyGroupUpdated) {
            sakaiService.saveSite(site);
        }

        return GroupManagerConstants.REDIRECT_MAIN_TEMPLATE;
    }

}
