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
package org.sakaiproject.webapi.beans;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteEntityRestBean {

    private static final String SITE_SEGMENT = "/site/";
    private static final String GROUP_SEGMENT = "/group/";

    private String id;
    private SiteEntityType type;
    private String title;
    private Instant openDate;
    private Instant dueDate;
    private Instant closeDate;
    private Set<String> groupRefs;
    private Set<TimeExceptionRestBean> timeExceptions;


    @SuppressWarnings("unchecked")
    public static SiteEntityRestBean of(PublishedAssessmentFacade assessment, Set<TimeExceptionRestBean> timeExceptions) {
        String siteId = assessment.getOwnerSiteId();
        Set<String> assessmentGroupRefs = Optional.ofNullable(assessment.getReleaseToGroups())
                .map(Map::keySet)
                .map(groupIds -> (Set<String>) groupIds)
                .map(groupIds -> groupIds.stream()
                        .map(groupId -> SITE_SEGMENT + siteId + GROUP_SEGMENT + groupId)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
        String assessmentId = Optional.ofNullable(assessment.getPublishedAssessmentId())
                .map(id -> id.toString())
                .orElse(null);

        return SiteEntityRestBean.builder()
                .id(assessmentId)
                .type(SiteEntityType.ASSESSMENT)
                .title(assessment.getTitle())
                .openDate(Optional.ofNullable(assessment.getStartDate()).map(Date::toInstant).orElse(null))
                .dueDate(Optional.ofNullable(assessment.getDueDate()).map(Date::toInstant).orElse(null))
                .closeDate(Optional.ofNullable(assessment.getRetractDate()).map(Date::toInstant).orElse(null))
                .groupRefs(assessmentGroupRefs)
                .timeExceptions(timeExceptions)
                .build();
    }

    public enum SiteEntityType {
        ASSESSMENT,
        ASSIGNMENT,
        FORUM,
        RESOURCE,
    }

}
