const conditionOperators = [
    "SMALLER_THEN",
    "EQUAL_AS",
    "GREATER_THEN",
];

export function formatCondition(i18n, toolId, type, condition) {

 return "Require a score " + condition.type + " " + condition.attribute + " Points";
}

export function isValidCondition(condition) {

    if (typeof condition != "object") return false;

    const { operator, argument, siteId, toolId, itemId } = condition;

    return conditionOperators.includes(operator)
            && !!argument && !!siteId && !!toolId && !!itemId;
}

export default {
    formatCondition,
    isValidCondition,
};