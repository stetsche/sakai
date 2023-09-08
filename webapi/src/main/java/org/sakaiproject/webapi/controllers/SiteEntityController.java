/******************************************************************************
 * Copyright 2023 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import java.util.AbstractMap;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.facade.AuthzQueriesFacadeAPI;
import org.sakaiproject.tool.assessment.facade.ExtendedTimeFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.webapi.beans.SiteEntityRestBean;
import org.sakaiproject.webapi.beans.TimeExceptionRestBean;
import org.sakaiproject.webapi.beans.SiteEntityRestBean.SiteEntityType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
public class SiteEntityController extends AbstractSakaiApiController {


    private static final String GROUP_TAKE_ASSESSMENT_PERM = "TAKE_PUBLISHED_ASSESSMENT";
    private static final String SITE_SEGMENT = "/site/";

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private PersistenceService persistenceService;

    private PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();


    @GetMapping(value = "/sites/{siteId}/entities/assessments", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<SiteEntityRestBean>> getSiteAssessments(@PathVariable String siteId) {
        String userId = checkSakaiSession().getUserId();
        checkSite(siteId);

        ExtendedTimeFacade extendedTimeFacade = persistenceService.getExtendedTimeFacade();

        List<PublishedAssessmentFacade> publishedAssessments = publishedAssessmentService.getBasicInfoOfAllPublishedAssessments(
                userId, "publishedAssessmentId", true, siteId);

        Map<Long, List<ExtendedTime>> assessmentExtendedTimesMap = publishedAssessments.stream()
                .map(assessment -> {
                    PublishedAssessmentData assessmentData = new PublishedAssessmentData();
                    BeanUtils.copyProperties(assessment, assessmentData);

                    return new AbstractMap.SimpleEntry<Long, List<ExtendedTime>>(assessment.getPublishedAssessmentId(),
                        extendedTimeFacade.getEntriesForPub(assessmentData));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Set<SiteEntityRestBean> assessmentSiteEntities = publishedAssessments.stream()
                .map(assessment -> SiteEntityRestBean.of(assessment,
                        assessmentExtendedTimesMap.get(assessment.getPublishedAssessmentId()).stream()
                                .map(extendedTime -> TimeExceptionRestBean.of(siteId, extendedTime))
                                .collect(Collectors.toSet())))
                .collect(Collectors.toSet());

        return ResponseEntity.ok(assessmentSiteEntities);
    }

    @PatchMapping(path = "/sites/{siteId}/entities", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<SiteEntityRestBean>> updateSiteEntities(@PathVariable String siteId,
            @RequestBody Set<SiteEntityRestBean> patchEntities) {
        Site site = checkSite(siteId);
        String userId = checkSakaiSession().getUserId();

        ExtendedTimeFacade extendedTimeFacade = persistenceService.getExtendedTimeFacade();

        Map<String, Object> toolEntities = new HashMap<>();

        // First iteration - Check if request is valid and keep the retrieved entities
        for (SiteEntityRestBean patchEntity : patchEntities) {
            SiteEntityType entityType = patchEntity.getType();

            if (StringUtils.isBlank(patchEntity.getId()) || entityType == null) {
                return ResponseEntity.badRequest().build();
            }

            switch(entityType) {
                case ASSESSMENT:
                    PublishedAssessmentFacade assessment = publishedAssessmentService.getPublishedAssessment(patchEntity.getId(), true);
                    boolean timeExceptionsValid = patchEntity.getTimeExceptions().stream()
                        .map(TimeExceptionRestBean::isValid)
                        .filter(timeExceptionValid -> !timeExceptionValid)
                        .findAny()
                        .orElse(true);

                    if (assessment == null || !timeExceptionsValid) {
                        return ResponseEntity.badRequest().build();
                    }

                    if (!canUpdateAssessment(userId, assessment)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }

                    toolEntities.put(entityKey(patchEntity), assessment);
                    break;
                case ASSIGNMENT:
                    Assignment assignment;
                    try {
                        assignment = assignmentService.getAssignment(patchEntity.getId());

                    } catch (IdUnusedException e) {
                        return ResponseEntity.badRequest().build();
                    } catch (PermissionException e) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }

                    if (!canUpdateAssignment(userId, assignment)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }

                    toolEntities.put(entityKey(patchEntity), assignment);
                    break;
                case RESOURCE:
                    if (patchEntity.getCloseDate() != null) {
                        return ResponseEntity.badRequest().build();
                    }
                    break;
                case FORUM:
                    if (patchEntity.getCloseDate() != null) {
                        return ResponseEntity.badRequest().build();
                    }
                    break;
                default:
                    return ResponseEntity.badRequest().build();
            }
        }

        Set<SiteEntityRestBean> updatedEntities = new HashSet<>();

        // Second iteration - Persist updated entities and map to bean
        for (SiteEntityRestBean patchEntity : patchEntities) {
            switch(patchEntity.getType()) {
                case ASSESSMENT:
                    PublishedAssessmentFacade assessment = (PublishedAssessmentFacade) toolEntities.get(entityKey(patchEntity));
                    PublishedAssessmentData assessmentData = (PublishedAssessmentData) assessment.getData();
                    String assessmentId = assessment.getPublishedAssessmentId().toString();

                    AssessmentAccessControlIfc accessControl = publishedAssessmentService.loadPublishedAccessControl(
                            assessment.getPublishedAssessmentId());

                    Optional.ofNullable(patchEntity.getOpenDate())
                            .ifPresent(openDate -> accessControl.setStartDate(Date.from(openDate)));

                    Optional.ofNullable(patchEntity.getDueDate())
                            .ifPresent(dueDate -> accessControl.setDueDate(Date.from(dueDate)));

                    Optional.ofNullable(patchEntity.getCloseDate())
                            .ifPresent(closeDate -> accessControl.setDueDate(Date.from(closeDate)));

                    Optional.ofNullable(patchEntity.getGroupRefs())
                            .ifPresent(groupRefs -> {
                                AuthzQueriesFacadeAPI authzQueries = persistenceService.getAuthzQueriesFacade();
                                // Remove previous authorizations
                                authzQueries.removeAuthorizationByQualifierAndFunction(assessmentId, GROUP_TAKE_ASSESSMENT_PERM);

                                // Add authorization for each group
                                groupRefs.stream()
                                        .map(groupRef -> groupIdFromRef(groupRef))
                                        .forEach(groupId -> authzQueries.createAuthorization(groupId, GROUP_TAKE_ASSESSMENT_PERM, assessmentId));

                                // Set "release to" to site TITLE (?) or constant for groups
                                accessControl.setReleaseTo(groupRefs.isEmpty() ? site.getTitle() : AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS);
                            });

                    Optional.ofNullable(patchEntity.getTimeExceptions())
                            .ifPresent(timeExceptions -> {
                                List<ExtendedTime> extendedTimes = timeExceptions.stream()
                                        .map(TimeExceptionRestBean::toExtendedTime)
                                        .peek(timeException -> timeException.setPubAssessment(assessmentData))
                                        .collect(Collectors.toList());

                                extendedTimeFacade.saveEntriesPub(assessmentData, extendedTimes);;
                            });

                    publishedAssessmentService.saveOrUpdatePublishedAccessControl(accessControl);

                    PublishedAssessmentFacade updatedAssessment = publishedAssessmentService.getPublishedAssessment(patchEntity.getId(), true);
                    updatedEntities.add(SiteEntityRestBean.of(updatedAssessment, timeExceptionSet(updatedAssessment)));
                    break;
                case ASSIGNMENT:
                    Assignment assignment = (Assignment) toolEntities.get(entityKey(patchEntity));

                    Optional.ofNullable(patchEntity.getOpenDate())
                            .ifPresent(openDate -> assignment.setOpenDate(openDate));

                    Optional.ofNullable(patchEntity.getDueDate())
                            .ifPresent(dueDate -> assignment.setDueDate(dueDate));

                    Optional.ofNullable(patchEntity.getCloseDate())
                            .ifPresent(closeDate -> assignment.setCloseDate(closeDate));

                    Optional.ofNullable(patchEntity.getGroupRefs())
                            .ifPresent(groupRefs -> {
                                Set<String> assignmentGroups = assignment.getGroups();

                                // Remove old groups
                                assignmentGroups.clear();

                                if (groupRefs.isEmpty()) {
                                    // Assign assignment to entire site
                                    assignment.setTypeOfAccess(Assignment.Access.SITE);
                                } else {
                                    // Assign assignment to groups
                                    assignment.setTypeOfAccess(Assignment.Access.GROUP);
                                    // Add new groups
                                    assignmentGroups.addAll(groupRefs);
                                }
                            });

                    Assignment updatedAssignment;
                    try {
                        assignmentService.updateAssignment(assignment);
                        updatedAssignment = assignmentService.getAssignment(assignment.getId());
                    } catch (PermissionException | IdUnusedException e) {
                        // Permission already evaluated before and assignmentId should be safe
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                    updatedEntities.add(SiteEntityRestBean.of(updatedAssignment));
                case RESOURCE:
                case FORUM:
                default:
                    break;
            }
        }

        return ResponseEntity.ok(updatedEntities.stream()
                .sorted(SiteEntityRestBean.comparator())
                .collect(Collectors.toSet()));
    }

    private boolean canUpdateAssessment(String userId, PublishedAssessmentFacade assessment) {
        String siteRef = SITE_SEGMENT + assessment.getOwnerSiteId();

        return securityService.unlock(userId, SamigoConstants.AUTHZ_EDIT_ASSESSMENT_ANY, siteRef);
    }

    private boolean canUpdateAssignment(String userId, Assignment assignment) {
        String siteRef = SITE_SEGMENT + AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getContext();

        return securityService.unlock(userId, AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT, siteRef)
                && securityService.unlock(userId, AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, siteRef);
    }

    private String groupIdFromRef(String groupRef) {
        return StringUtils.substringAfterLast(groupRef, "/");
    }

    private String entityKey(SiteEntityRestBean siteEntity) {
        return siteEntity.getType().toString() + ":" + siteEntity.getId();
    }

    private Set<TimeExceptionRestBean> timeExceptionSet(PublishedAssessmentFacade assessment) {
        ExtendedTimeFacade extendedTimeFacade = persistenceService.getExtendedTimeFacade();
        String siteId = assessment.getOwnerSiteId();

        PublishedAssessmentData assessmentData = new PublishedAssessmentData();
        BeanUtils.copyProperties(assessment, assessmentData);

        return extendedTimeFacade.getEntriesForPub(assessmentData).stream()
                .map(extendedTime -> TimeExceptionRestBean.of(siteId, extendedTime))
                .collect(Collectors.toSet());
    }
}
