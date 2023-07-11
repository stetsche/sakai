export async function getConditions(toolId, itemId) {

    const response = await fetch(`/api/sites/mercury/conditions?toolId=${toolId}&itemId=${itemId}`)

    if (response.ok) {
        return await response.json();
    } else {
        console.error("Conditions could not be fetched:", response.statusText);
        return null;
    }
}

export async function getItemsforCollection(toolId, collectionId) {

    const response = await fetch(`/api/sites/mercury/conditions/items?toolId=${toolId}&collectionId=${collectionId}`)

    if (response.ok) {
        return await response.json();
    } else {
        console.error("Conditions could not be fetched:", response.statusText);
        return null;
    }
}

// export async function fetchConditions(toolId, itemId) {
//     return fetch(`/api/sites/OMITTED/conditions`, {
//         method: "GET",
//         body: { toolId, itemId }
//     });
// }

export async function fetchConfig(siteId) {
    if (typeof siteId !== "string") {
        console.error(`Passed value siteId [${siteId}] is invalid.`);
        return false;
    }

    const response = await fetch(`/api/sites/${siteId}/card-game/config`);

    if (response.ok) {
        return await response.json();
    } else {
        console.error("Config could not be fetched:", response.statusText);
        return null;
    }
}

const ConditionsApi = {
    getConditions
};

export default ConditionsApi;