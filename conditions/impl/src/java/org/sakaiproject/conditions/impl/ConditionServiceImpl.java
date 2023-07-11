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
package org.sakaiproject.conditions.impl;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.conditions.api.model.Condition;

import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import org.sakaiproject.conditions.api.ConditionEvaluator;
import org.sakaiproject.conditions.api.ConditionService;
import org.sakaiproject.conditions.api.exception.ConditionEvaluatorUnavailableException;
import org.sakaiproject.conditions.api.persistence.ConditionRepository;

@Slf4j
public class ConditionServiceImpl implements ConditionService {


    @Setter
    ConditionRepository conditionRepository;

    @Setter
    Map<String, ConditionEvaluator> conditionEvaluators;

    public void init() {
        log.info("Initializing Condition Service");

        if (conditionEvaluators != null) {
            for (Entry<String, ConditionEvaluator> conditionEvaluatorEntry : conditionEvaluators.entrySet()) {
                log.info("--> Registered condition evaluator [{}] for tool with id [{}]",
                        conditionEvaluatorEntry.getValue().getClass().getName(), conditionEvaluatorEntry.getKey());
            }
        } else {
            log.error("Condition resolvers not set");
        }
    }

    public boolean evaluateCondition(Condition condition) {
        ConditionEvaluator conditionEvaluator = getConditionEvaluator(condition);

        return conditionEvaluator.evaluateCondition(condition);
    }

    private ConditionEvaluator getConditionEvaluator(Condition condition) {
        String toolId = condition.getToolId();
        Optional<ConditionEvaluator> conditionEvaluator = Optional.ofNullable(conditionEvaluators.get(toolId));

        return conditionEvaluator.orElseThrow(() -> new ConditionEvaluatorUnavailableException(toolId));
    }
}
