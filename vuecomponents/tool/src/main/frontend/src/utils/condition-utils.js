const conditionOperators = [
    "SMALLER_THEN",
    "EQUAL_AS",
    "GREATER_THEN",
    "AND",
    "OR",
];

export function formatCondition(i18n, toolId, type, condition) {

    return "Require a score " + condition.operator + " " + condition.argument + " Points";
}

export function isValidCondition(condition) {

    if (typeof condition != "object") return false;

    const { operator, argument, siteId, toolId, itemId } = condition;

    if (!conditionOperators.includes(operator)) {
        console.error("Invalid condition operator:", operator);
        return false;
    }

    return !!siteId && !!toolId && !!itemId;
}

export default {
    formatCondition,
    isValidCondition,
};