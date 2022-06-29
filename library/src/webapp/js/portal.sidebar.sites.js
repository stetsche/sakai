class SitesSidebar {

    constructor(sidebarNavElement, config) {
        this._i18n = config?.i18n;
        const pinButtonElements = sidebarNavElement.querySelectorAll(".site-opt-pin");
        pinButtonElements.forEach((element) => new PinButton(element, { i18n: this._i18n?.pinButtons}));
        document.addEventListener("site-pin-change", this.handlePinChange)
    }

    async handlePinChange(event) {
        //console.log("fetchiing new pinned value",event.detail);
    }
}

class PinButton {

    set title(newTitle) {
        this._element.title = newTitle;
    }

    set pinned(newPinned) {
        this._element.setAttribute("data-pinned", newPinned);
    }

    constructor(element, config) {
        this._element = element;
        this._i18n = config?.i18n;
        this._site = element.getAttribute("data-pin-site");
        this._pinned = element.getAttribute("data-pinned") == "true" ? true : false;

        element.addEventListener("click", this.toggle.bind(this));
    }

    toggle() {
        this._pinned = !this._pinned
        this.title = this._pinned ? this._i18n.titleUnpin : this._i18n.titlePin;
        this.toggleIcon()
        this.emitPinChange();
    }

    toggleIcon() {
        const buttonClasses =  this._element.classList;
        const pinnedIcon = "bi-pin";
        const unPinnedIcon = "bi-pin-fill";
        buttonClasses.toggle(pinnedIcon);
        buttonClasses.toggle(unPinnedIcon);
    }

    emitPinChange() {
        //Dispatches event which will cause a fetch to cange pinned value
        const eventName = "site-pin-change";
        const eventPayload = {
            pinned: this._pinned,
            siteId: this._site
        };
        this._element.dispatchEvent(new CustomEvent(eventName, { "bubbles": true, "detail": eventPayload }));
    }
}