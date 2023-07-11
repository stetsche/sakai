// Check if all parameters passed to methods are defined and not null
export function allParamsNonNull(...parameters) {
    if (!parameters) return false;

    return parameters.findIndex(param => param == null || param == undefined) == -1;
}

// Check if all parameters passed to methods are defined and not null
export function queryParams(paramsObject) {
    if (!paramsObject || Object.keys(paramsObject).length == 0) return "";

    return "?" + new URLSearchParams(paramsObject);
}

export async function fetchJson(...params) {
    return await fetchData(async (response) => await response.json(), ...params);
}

export async function fetchText(...params) {
    return await fetchData(async (response) => response.text, ...params);
}

// Abstracted fetch logic
export async function fetchData(responseHandler, ...params) {
    const response = await fetch(...params);

    if (response.ok) {
        return await responseHandler(response);
    } else {
        console.error("Data could not be fetched:", {
            url: params[0],
            status: response.statusText
        });
        return null;
    }
}

const CoreUtils = {
    allParamsNonNull,
};

export default CoreUtils;