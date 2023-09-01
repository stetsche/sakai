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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.facade.ExtendedTimeFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.webapi.beans.SiteEntityRestBean;
import org.sakaiproject.webapi.beans.TimeExceptionRestBean;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class SiteEntityController extends AbstractSakaiApiController {


    @Autowired
    private PersistenceService persistenceService;

    @Setter
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

        log.info("publishedAssessments {}", publishedAssessments);
        log.info("assessmentExtendedTimesMap {}", assessmentExtendedTimesMap);

        Set<SiteEntityRestBean> assessmentSiteEntities = publishedAssessments.stream()
                .map(assessment -> SiteEntityRestBean.of(assessment,
                        assessmentExtendedTimesMap.get(assessment.getPublishedAssessmentId()).stream()
                                .map(extendedTime -> TimeExceptionRestBean.of(siteId, extendedTime))
                                .collect(Collectors.toSet())))
                .collect(Collectors.toSet());

        return ResponseEntity.ok(assessmentSiteEntities);
    }
}
