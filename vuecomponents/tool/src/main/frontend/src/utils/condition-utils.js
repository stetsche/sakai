export const CONDITION_OPERATORS = [
    "SMALLER_THAN",
    "SMALLER_THAN_OR_EQUAL_TO",
    "EQUAL_TO",
    "GREATER_THAN_OR_EQUAL_TO",
    "GREATER_THAN"
];

export const PARENT_CONDITION_OPERATORS = [
    "AND",
    "OR",
];

export function formatCondition(i18n, toolId, type, condition) {
    return "Require a score " + condition.operator + " " + condition.argument + " Points";
}

export function formatOperator(i18n, operator) {
    return i18n[operator.toLowerCase()];
}

export function makeParentCondition(siteId, operator = "AND") {
    return {
        type: "PARENT",
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
    return condition.type != "ROOT";
}

export function nonParentConditionFilter(condition) {
    return condition.type != "PARENT";
}

export function andParentConditionFilter(condition) {
    return condition.type == "PARENT" && condition.operator == "AND";
}

export function orParentConditionFilter(condition) {
    return condition.type == "PARENT" && condition.operator == "OR";
}

export default {
    formatCondition,
};