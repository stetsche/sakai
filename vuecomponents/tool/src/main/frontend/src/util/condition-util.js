export function formatCondition(i18n, toolId, type, condition) {

 return "Require a score " + condition.type + " " + condition.attribute + " Points";
}

const ConditionUtil = {
    formatCondition
}

export default ConditionUtil;