class SitesSidebar {

    constructor(element, config) {
        this._i18n = config?.i18n;
        this._element = element;
        const pinButtonElements = element.querySelectorAll(".site-opt-pin");
        pinButtonElements.forEach((buttonEl) => new PinButton(buttonEl, { i18n: this._i18n?.pinButtons}));
        document.addEventListener("site-pin-change", this.handlePinChange)
    }

    async setView(mobile) {
        const mobileClasses = ["portal-nav-sidebar-mobile", "offcanvas", "offcanvas-start"];
        const desktopClasses = ["portal-nav-sidebar-desktop"];

        this._element.style.visibility = "hidden";
        if (mobile) {
            //Set mobile view
            mobileClasses.forEach((cssClass) => {
                this._element.classList.add(cssClass);
            });
            desktopClasses.forEach((cssClass) => {
                this._element.classList.remove(cssClass);
            });
        } else {
            //Set desktop view
            
            //Check if we can find an offcanvas instance and dispose it
            let offcanvas = bootstrap.Offcanvas.getInstance(this._element);
            if (offcanvas) {
                offcanvas.dispose();
            }

            mobileClasses.forEach((cssClass) => {
                this._element.classList.remove(cssClass);
            });
            desktopClasses.forEach((cssClass) => {
                this._element.classList.add(cssClass);
            });
            this._element.style.visibility = "visible";
        }
        this._element.classList.remove("d-none");
    }

    async handlePinChange(event) {
        console.log("fetchiing new pinned value",event.detail);
    }
}

class PinButton {

    get title() {
        return this._element.getAttribute("title");
    }

    set title(newValue) {
        this._element.setAttribute("title", newValue);
        this._element.setAttribute("data-bs-original-title", newValue);
        this._tooltip.show();
    }

    get pinned() {
        return this._element.getAttribute("data-pinned") == "true" ? true : false;
    }

    set pinned(newPinned) {
        this._element.setAttribute("data-pinned", newPinned);
    }

    constructor(element, config) {
        this._element = element;
        this._i18n = config?.i18n;
        this._site = element.getAttribute("data-pin-site");
        this._tooltip = bootstrap.Tooltip.getOrCreateInstance(this._element);
        element.addEventListener("click", this.toggle.bind(this));
    }

    toggle() {
        this.pinned = !this.pinned
        this.title = this.pinned ? this._i18n.titleUnpin : this._i18n.titlePin;
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

    //Dispatches event which will cause a fetch to cange pinned value
    emitPinChange() {
        const eventName = "site-pin-change";
        const eventPayload = {
            pinned: this.pinned,
            siteId: this._site
        };
        this._element.dispatchEvent(new CustomEvent(eventName, { "bubbles": true, "detail": eventPayload }));
    }
}
