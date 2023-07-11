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
package org.sakaiproject.condition.impl;

import org.sakaiproject.condition.api.ConditionEvaluator;
import org.sakaiproject.condition.api.exception.UnsupportedToolIdException;
import org.sakaiproject.condition.api.model.Condition;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class LessonsConditionEvaluator implements ConditionEvaluator {


    private static final String ASSIGNMENT_TOOL_ID = "sakai.assignment";

    @Autowired
    private AssignmentConditionEvaluator assignmentConditionEvaluator;


    @Override
    public boolean evaluateCondition(Condition condition, String userId) {
        Condition adjustedCondition = getAdjustedCondition(condition);
        return getItemEvaluator(adjustedCondition.getToolId())
                .evaluateCondition(adjustedCondition, userId);
    }

    @Override
    public boolean isConditionUsed(Condition condition) {
        log.info("condition used???");
        return false;
    }

    private Condition getAdjustedCondition(Condition condition) {
        String itemId = "2";
        String toolId = ASSIGNMENT_TOOL_ID;

        return Condition.builderOf(condition)
                .toolId(toolId)
                .itemId(itemId)
                .build();
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
