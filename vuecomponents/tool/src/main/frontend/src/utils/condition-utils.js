export const ConditionOperator = {
    SMALLER_THAN: "SMALLER_THAN",
    SMALLER_THAN_OR_EQUAL_TO: "SMALLER_THAN_OR_EQUAL_TO",
    EQUAL_TO: "EQUAL_TO",
    GREATER_THAN_OR_EQUAL_TO: "GREATER_THAN_OR_EQUAL_TO",
    GREATER_THAN: "GREATER_THAN",
    AND: "AND",
    OR: "OR",
};

export const CONDITION_OPERATORS = [
    ConditionOperator.SMALLER_THAN,
    ConditionOperator.SMALLER_THAN_OR_EQUAL_TO,
    ConditionOperator.EQUAL_TO,
    ConditionOperator.GREATER_THAN_OR_EQUAL_TO,
    ConditionOperator.GREATER_THAN,
];

export const PARENT_CONDITION_OPERATORS = [
    ConditionOperator.AND,
    ConditionOperator.OR,
];

export const ConditionType = {
    COMPLETED: "COMPLETED",
    PARENT: "PARENT",
    POINTS: "POINTS",
    ROOT: "ROOT",
};

export const CONDITION_TYPES = [
    ConditionType.COMPLETED,
    ConditionType.POINTS,
];

export const CONDITION_BUNDLE_NAME = "condition";

export function formatCondition(i18n, toolId, type, condition) {
    return "Require a score " + condition.operator + " " + condition.argument + " Points";
}

export function formatOperator(i18n, operator) {
    return i18n[operator.toLowerCase()];
}

export function makeParentCondition(siteId, operator = ConditionOperator.OR) {
    return {
        type: ConditionType.PARENT,
        siteId,
        toolId: null,
        itemId: null,
        operator,
        argument: null,
        subConditions:[],
    }
}

export function makeRootCondition(siteId, toolId, itemId) {
    return {
        type: "ROOT",
        siteId,
        toolId,
        itemId,
        operator: "AND",
        argument: null,
        subConditions:[],
    }
}

export function nonRootConditionFilter(condition) {
    return condition.type != ConditionType.ROOT;
}

export function nonParentConditionFilter(condition) {
    return condition.type != ConditionType.PARENT;
}

export function andParentConditionFilter(condition) {
    return condition.type == ConditionType.PARENT
            && condition.operator == ConditionOperator.AND;
}

export function orParentConditionFilter(condition) {
    return condition.type == ConditionType.PARENT
            && condition.operator == ConditionOperator.OR;
}

export default {
    formatCondition,
};