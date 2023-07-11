import { allParamsNonNull, queryParams, fetchJson, fetchText } from "../utils/core-utils.js";
import { isValidCondition } from "../utils/condition-utils.js";

// Get conditions that are provided by the specified item
export async function getConditionsForItem(siteId, toolId, itemId) {

    if (!allParamsNonNull(siteId, toolId, itemId)) return null;

    const response = await fetch(`/api/sites/${siteId}/conditions${queryParams({ toolId, itemId })}`)

    console.log("query", `/api/sites/${siteId}/conditions${queryParams({ toolId, itemId })}`)
    if (response.ok) {
        return await response.json();
    } else {
        console.error("Conditions could not be fetched:", response.statusText);
        return null;
    }
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

    if (typeof condition != "object" || !isValidCondition(condition)) return null;

    return await fetchJson(`/api/sites/${condition.siteId}/conditions`, {
        method: "POST",
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


const ConditionsApi = {
    getConditionsForItem,
    getConditionsForSite,
};

export default ConditionsApi;