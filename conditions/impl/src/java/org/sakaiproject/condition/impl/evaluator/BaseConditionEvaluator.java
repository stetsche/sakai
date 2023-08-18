package org.sakaiproject.condition.impl.evaluator;

import java.math.BigDecimal;

import org.apache.commons.lang3.compare.ComparableUtils;
import org.sakaiproject.condition.api.ConditionEvaluator;
import org.sakaiproject.condition.api.model.ConditionOperator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseConditionEvaluator implements ConditionEvaluator {


    protected boolean evaluateScore(BigDecimal userScore,ConditionOperator conditionOperator, BigDecimal conditionScore) {
        boolean result;

        switch (conditionOperator) {
            case SMALLER_THAN:
                result = ComparableUtils.is(userScore).lessThan(conditionScore);
                break;
            case GREATER_THAN:
                result = ComparableUtils.is(userScore).greaterThan(conditionScore);
                break;
            case EQUAL_TO:
                result = ComparableUtils.is(userScore).equalTo(conditionScore);
                break;
            default:
                log.error("Invalid operator [{}] to evaluate score", conditionOperator);
                result = false;
                break;
        }

        log.debug("Evaluated score: {} {} {} => {}", userScore, conditionOperator, conditionScore, result);
        return result;
    }
}
