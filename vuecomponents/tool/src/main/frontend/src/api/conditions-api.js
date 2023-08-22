import {
    allParamsNonNull,
    queryParams,
    fetchJson,
    fetchText,
    registerGlobalModule
} from "../utils/core-utils.js";

import {
    nonRootConditionFilter,
    nonParentConditionFilter,
    lessonItemName
} from "../utils/condition-utils.js";

// Self-import
import * as ConditionsApi from "./conditions-api";

// Get conditions that are provided by the specified item
export async function getConditionsForItem(siteId, toolId, itemId) {
    if (!allParamsNonNull(siteId, toolId, itemId)) return null;

    return await fetchJson(`/api/sites/${siteId}/conditions${queryParams({ toolId, itemId })}`);
}

export async function getRootCondition(siteId, toolId, itemId) {
    const conditions = await getConditionsForItem(siteId, toolId, itemId);

    return conditions?.find((c) => c.type == "ROOT");
}

export async function getToolItemsWithConditionsForLesson(siteId, lessonId) {
    if (!allParamsNonNull(siteId, lessonId)) return null;

    const lessonPromise = fetchJson(`/direct/lessons/lesson/${lessonId}.json`);
    const conditionsPromise = getConditionsForSite(siteId);

    const [ lesson, conditions ] = await Promise.all([ lessonPromise, conditionsPromise ]);

    if (!lesson?.contentsList || lesson.contentsList.length == 0 || conditions == null) {
        console.error("Lesson or conditions not found");
        return null;
    }

    const lessonConditions = conditions.filter((c) => c.toolId == "sakai.lessonbuildertool");

    return lesson.contentsList
            .map((lessonItem) => {
                const itemConditions = conditions
                        .filter(nonRootConditionFilter)
                        .filter(nonParentConditionFilter)
                        .filter((C) => C.itemId == lessonItem.id);
                return {
                    id: lessonItem.id,
                    name: lessonItemName(lessonItem),
                    conditions: itemConditions,
                };
            })
            .filter((lessonItem) => lessonItem.conditions.length > 0);
    // return conditions
    //         .filter((condition) => condition.toolId == "sakai.lessonbuildertool")
    //         .map((condition) => {
    //             const lessonItem = lesson.contentsList
    //                     .find((lessonItem) => lessonItem.id == condition.itemId);
    //             return lessonItem
    //                 ? { id: lessonItem.id, name: lessonItem.name, condition }
    //                 : null;
    //         })
    //         .filter(allParamsNonNull);
}

// Get conditions that are available on the specified site
export async function getConditionsForSite(siteId) {
    if (!allParamsNonNull(siteId)) return null;

    const response = await fetch(`/api/sites/${siteId}/conditions`)

    if (response.ok) {
        return await response.json();
    } else {
        console.error("Conditions could not be fetched:", response.statusText);
        return null;
    }
}


// Create condition
export async function createCondition(condition) {
    if (typeof condition != "object") return null;

    return await fetchJson(`/api/sites/${condition.siteId}/conditions`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(condition)
    })
}

// Create condition
export async function updateCondition(condition) {
    if (typeof condition != "object") return null;

    return await fetchJson(`/api/sites/${condition.siteId}/conditions/${condition.id}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(condition)
    })
}

// Delete condition
export async function deleteCondition({ id: conditionId, siteId}) {
    if (!allParamsNonNull(siteId, conditionId)) return null;

    return await fetchText(`/api/sites/${siteId}/conditions/${conditionId}`, {
        method: "DELETE",
    })
}

registerGlobalModule("conditionApi", ConditionsApi)

export default ConditionsApi;