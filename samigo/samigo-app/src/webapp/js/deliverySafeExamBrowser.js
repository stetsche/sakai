// CONSTANTS

const siteId = "IS_NOT_NEEDED";
const formId = "takeAssessmentForm";
// Replacing http: to seb: and https: to sebs:
const sebProtocol = window.location.protocol.replace('http','seb');
const startButtonId = formId + ":restViewHidden";
const launchSebLinkId = "sebLaunchSeb";
const downloadSebLink = seb.downloadLink;
const downloadSebLinkId = "sebDownloadSeb";
const downloadConfigLinkId = "sebDownloadConfiguration";
const relativeConfigLink = seb.relativeConfigLink;
const assessmentId = seb.assessmentId;
const loadingMessage = window.please_wait;

// GETTERS

function getSebApi() {
    return window.SafeExamBrowser = window.SafeExamBrowser || null;
}

function getDownloadConfigLink() {
    return window.location.origin + relativeConfigLink;
}

function getLaunchSebLink() {
    const protocol = window.location.protocol;
    return getDownloadConfigLink().replace(protocol, sebProtocol) + "?launch=true";
}

function isStartView() {
    return document.getElementById(startButtonId) ? true : false;
}

// HELPER FUNCTIONS

function clickStartButton() {
    const startButton = document.getElementById(startButtonId);
    if (startButton) {
        startButton.click();
    } else {
        console.error("Could not find hidden begin button");
    }
}

async function configureLink(linkId, href) {
    const link = document.getElementById(linkId);
    if (link && href && href !== "" && href !== "#") {
        link.setAttribute("href", href);
    } else {
        link.remove();
        console.debug(`Link with Id ${linkId} removed, due to invalid href ${href}.`);
    }
}

async function hideStartView() {
    const form = document.getElementById(formId);
    if (form) {
        form.style.display = "none";
    }
}

async function showLoadingMessage(message) {
    if (window.$ && $.blockUI) {
        const spinnerPath = "/library/image/sakai/spinner.gif";

        $.blockUI({
            message: `
                <h3>
                    ${message}
                    <img aria-hidden="true" src="${spinnerPath}" />
                </h3>
            `,
            overlayCSS: {
                backgroundColor: '#ccc',
                opacity: 0.25
            }
        });
    } else {
        console.error("JQuery ($) and/or $.blockUI is not defined, not showing loading message.");
    }
}

async function fetchValidationData({ configKey, browserExamKey }) {
    const url = window.location.href;
    const apiPath = `/api/sites/${siteId}/assessments/published/${assessmentId}/sebValidation`;
    const data = { configKey, examKey: browserExamKey, url };
    const response = await fetch(apiPath, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    });
    return response.ok;
}

// LOGIC

const sebApi = getSebApi();

async function sebKeysUpdated() {
    const delivered = await fetchValidationData(sebApi.security);

    if (!delivered) {
        console.error("Could not deliver validation data");
    }

    if (isStartView()) {
        clickStartButton();
    }
}

// Check if sebApi is available, this will indicate if SEB is used right now
if (sebApi) {
    // Let SEB update the keys and register sebKeysUpdated as callback
    sebApi.security.updateKeys(sebKeysUpdated);

    // Check if this is the sebSetup view, hide it and display loading bar
    document.addEventListener("DOMContentLoaded", () => {
        if (isStartView()) {
            hideStartView();
            showLoadingMessage(loadingMessage);
        }
    }, { once : true });
} else {
    // Configure links
    document.addEventListener("DOMContentLoaded", () => {
        configureLink(launchSebLinkId, getLaunchSebLink());
        configureLink(downloadSebLinkId, downloadSebLink);
        configureLink(downloadConfigLinkId, getDownloadConfigLink());
    }, { once : true });
}
