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

import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SiteEntityRestBean {


    private Long id;
    private SiteEntityType type;
    private String title;
    private Instant openDate;
    private Instant dueDate;
    private Instant closeDate;
    private Set<String> groupRefs;
    private Set<TimeExceptionRestBean> timeExceptions;


    @SuppressWarnings("unchecked")
    public static SiteEntityRestBean of(PublishedAssessmentFacade assessment, Set<TimeExceptionRestBean> timeExceptions) {
        Set<String> assessmentGroupRefs = Optional.ofNullable(assessment.getReleaseToGroups())
                .map(Map::keySet)
                .orElse(Collections.emptySet());

        return SiteEntityRestBean.builder()
                .id(assessment.getPublishedAssessmentId())
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
