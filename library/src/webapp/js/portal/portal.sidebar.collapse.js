class SidebarCollapseButton {

    get collapsed() {
        return this._element.getAttribute("data-portal-sidebar-collapsed") === "true" ? true : false;
    }

    set collapsed(newValue) {
        this._element.setAttribute("data-portal-sidebar-collapsed", newValue);
    }

    get title() {
        return this._element.getAttribute("title");
    }

    set title(newValue) {
        this._element.setAttribute("title", newValue);
        this._tooltip.setContent({'.tooltip-inner': newValue});
    }

    constructor(element, config) {
        this._i18n = config?.i18n;
        this._toggleClass = config?.toggleClass;
        this._portalConstainer = config?.portalConstainer;
        this._element = element;
        this._element.addEventListener("click", this.toggle.bind(this));
        this._tooltip = bootstrap.Tooltip.getOrCreateInstance(this._element);
    }

    toggle() {
        this.collapsed = !this.collapsed;
        this._portalConstainer.classList.toggle(this._toggleClass);
        this.title = this.collapsed ? this._i18n.titleCollapsed : this._i18n.titleExpanded;
    }
}
