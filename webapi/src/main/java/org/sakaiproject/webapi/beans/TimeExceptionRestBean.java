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
import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(of = "forEntityRef")
public class TimeExceptionRestBean {


    private String forEntityRef;
    private Instant openDate;
    private Instant dueDate;
    private Instant closeDate;


    public static TimeExceptionRestBean of(String siteId, ExtendedTime extendedTime) {
        String entityRef = Optional.ofNullable(StringUtils.trimToNull(extendedTime.getUser()))
                .map(userId -> "/user/" + userId)
                .or(() -> Optional.ofNullable(StringUtils.trimToNull(extendedTime.getGroup()))
                        .map(groupId -> "/site/" + siteId + "/group/" + groupId))
                .orElse(null);
        return TimeExceptionRestBean.builder()
                .forEntityRef(entityRef)
                .openDate(Optional.ofNullable(extendedTime.getStartDate()).map(Date::toInstant).orElse(null))
                .dueDate(Optional.ofNullable(extendedTime.getDueDate()).map(Date::toInstant).orElse(null))
                .closeDate(Optional.ofNullable(extendedTime.getRetractDate()).map(Date::toInstant).orElse(null))
                .build();
    }
}
