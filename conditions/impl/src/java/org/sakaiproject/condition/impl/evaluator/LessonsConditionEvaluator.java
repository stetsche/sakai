/**
 * Copyright (c) 2023 The Apereo Foundation
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
package org.sakaiproject.condition.impl.evaluator;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.sakaiproject.condition.api.ConditionEvaluator;
import org.sakaiproject.condition.api.exception.UnsupportedToolIdException;
import org.sakaiproject.condition.api.model.Condition;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class LessonsConditionEvaluator extends BaseConditionEvaluator {


    private static final String ASSIGNMENT_TOOL_ID = "sakai.assignment";

    @Autowired
    private AssignmentConditionEvaluator assignmentConditionEvaluator;

    @Autowired
    @Qualifier("org.sakaiproject.lessonbuildertool.model.SimplePageToolDaoTarget")
    private SimplePageToolDao lessonService;

    @Override
    public boolean evaluateCondition(Condition condition, String userId) {
        Condition adjustedCondition = getAdjustedCondition(condition);
        return getItemEvaluator(adjustedCondition.getToolId())
                .evaluateCondition(adjustedCondition, userId);
    }

    private Condition getAdjustedCondition(Condition condition) {
        return getLessonItem(condition.getItemId()).map(lessonItem -> {
                String toolId, itemId;

                switch (lessonItem.getType()) {
                    case SimplePageItem.ASSIGNMENT:
                        toolId = ASSIGNMENT_TOOL_ID;
                        itemId = parseItemIdFromRef(lessonItem.getSakaiId());
                        break;
                    default:
                        toolId = null;
                        itemId = null;
                        break;
                }

                return Condition.builderOf(condition)
                    .toolId(toolId)
                    .itemId(itemId)
                    .build();
        }).orElse(null);
    }

    private Optional<SimplePageItem> getLessonItem(String itemId) {
        if (NumberUtils.isParsable(itemId)) {
            return Optional.ofNullable(lessonService.findItem(Long.parseLong(itemId)));
        } else {
            return Optional.empty();
        }
    }

    private String parseItemIdFromRef(String ref) {
        String [] refParts = StringUtils.split(ref, '/');

        if (ref != null && refParts.length >= 2) {
            switch (refParts[0]) {
                case "assignment":
                case "sam_pub":
                    return refParts[1];
                default:
                    log.error("Unhandled reference [{}]", ref);
                    break;
            }
        } else {
            log.error("Invalid reference [{}]", ref);
        }

        return null;
    }

    private ConditionEvaluator getItemEvaluator(String toolId) {
        switch (toolId) {
            case ASSIGNMENT_TOOL_ID:
                return assignmentConditionEvaluator;
            default:
                throw new UnsupportedToolIdException(toolId);
        }
    }
}
