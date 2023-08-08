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

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.compare.ComparableUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.condition.api.ConditionEvaluator;
import org.sakaiproject.condition.api.model.Condition;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssignmentConditionEvaluator implements ConditionEvaluator {


    @Autowired
    private AssignmentService assignmentService;


    @Override
    public boolean evaluateCondition(Condition condition, String userId) {
        log.info("evaluating...");
        Boolean result = null;
        Assignment assignment = getAssignment(condition.getItemId());

        if (assignment != null && NumberUtils.isParsable(condition.getArgument())) {
            String submitterId = assignmentService.getSubmitterIdForAssignment(assignment, userId);
            log.info("submitterId {}", submitterId);
            List<Double> submissionGrades = assignmentService.getSubmissions(assignment).stream()
                    // Filter by user
                    .filter(submission -> submission.getSubmitters().stream()
                            .filter(submissionSubmitter -> StringUtils.equals(submissionSubmitter.getSubmitter(), submitterId))
                            .findAny()
                            .isPresent())
                    // Map to grade
                    .map(submission -> assignmentService.getGradeForSubmitter(submission, submitterId))
                    .filter(NumberUtils::isParsable)
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());

            BigDecimal highestGrade = BigDecimal.valueOf(submissionGrades.stream().sorted(Comparator.reverseOrder()).findFirst().orElse(null));
            BigDecimal conditionGrade = new BigDecimal(condition.getArgument());

            log.debug("{} {} {}", highestGrade, condition.getOperator(), conditionGrade);

            if (highestGrade != null) {
                switch (condition.getOperator()) {
                    case SMALLER_THEN:
                        result = ComparableUtils.is(highestGrade).lessThan(conditionGrade);
                        break;
                    case GREATER_THEN:
                        result = ComparableUtils.is(highestGrade).greaterThan(conditionGrade);
                        break;
                    case EQUAL_AS:
                        result = ComparableUtils.is(highestGrade).equalTo(conditionGrade);
                        break;
                    default:
                        break;
                }
            }
        }

        log.info("result: [{}]", result);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public boolean isConditionUsed(Condition condition) {
        log.info("condition used???");
        return false;
    }

    private Assignment getAssignment(String assignmentId) {
        Assignment assignment;

        try {
            assignment = assignmentService.getAssignment(assignmentId);
        } catch (IdUnusedException | PermissionException e) {
            assignment = null;
        }

        return assignment;
    }
}
