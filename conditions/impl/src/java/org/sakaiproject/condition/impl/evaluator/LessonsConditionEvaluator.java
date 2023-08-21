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
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
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


    private static final String ASSESSMENT_TOOL_ID = "sakai.samigo";
    private static final String ASSIGNMENT_TOOL_ID = AssignmentServiceConstants.SAKAI_ASSIGNMENT;

    @Autowired
    private AssignmentConditionEvaluator assignmentConditionEvaluator;

    @Autowired
    private AssessmentConditionEvaluator assessmentConditionEvaluator;

    @Autowired
    @Qualifier("org.sakaiproject.lessonbuildertool.model.SimplePageToolDaoTarget")
    private SimplePageToolDao lessonService;


    @Override
    public boolean evaluateCondition(Condition condition, String userId) {
        if (!NumberUtils.isParsable(condition.getItemId())) {
            log.error("Lesson item id [{}] associated with condition with id [{}] item is not parsable",
                    condition.getItemId(), condition.getId());
            return false;
        }

        SimplePageItem lessonItem = lessonService.findItem(Long.parseLong(condition.getItemId()));

        if (lessonItem == null) {
            log.error("Lesson item with id [{}] associated with condition with id [{}] not found",
                    condition.getItemId(), condition.getId());
            return false;
        }

        String toolId, itemId;
        ConditionEvaluator conditionEvaluator;
        switch (lessonItem.getType()) {
            case SimplePageItem.ASSIGNMENT:
                toolId = ASSIGNMENT_TOOL_ID;
                itemId = parseItemIdFromRef(lessonItem.getSakaiId());
                conditionEvaluator = assignmentConditionEvaluator;
                break;
            case SimplePageItem.ASSESSMENT:
                toolId = ASSESSMENT_TOOL_ID;
                itemId = parseItemIdFromRef(lessonItem.getSakaiId());
                conditionEvaluator = assessmentConditionEvaluator;
                break;
            default:
                log.error("Can not evaluate condition with id [{}] associated with lesson item of unhandled type [{}]",
                        condition.getId(), lessonItem.getType());
                return false;
        }

        Condition evaluationCondition = Condition.builderOf(condition)
                .toolId(toolId)
                .itemId(itemId)
                .build();

        return conditionEvaluator.evaluateCondition(evaluationCondition, userId);
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
