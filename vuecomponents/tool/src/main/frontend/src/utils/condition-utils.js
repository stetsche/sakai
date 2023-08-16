const conditionOperators = [
    "SMALLER_THAN",
    "EQUAL_TO",
    "GREATER_THAN",
    "AND",
    "OR",
];

export function formatCondition(i18n, toolId, type, condition) {

    return "Require a score " + condition.operator + " " + condition.argument + " Points";
}

export function formatOperator(i18n, operator) {
    return i18n[operator.toLowerCase()];
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

export function makeRootCondition(siteId, toolId, itemId) {
    return {
        type: "ROOT",
        siteId,
        toolId,
        itemId,
        operator: "AND",
        argument: null,
    }
}

export function nonRootConditionFilter(condition) {
    return condition.type != "ROOT";
}

export function nonParentConditionFilter(condition) {
    return condition.type != "PARENT";
}


export default {
    formatCondition,
    isValidCondition,
};