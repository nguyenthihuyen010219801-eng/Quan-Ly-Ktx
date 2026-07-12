(function () {
    "use strict";

    const modalSelectors = [
        ".modal.show",
        ".account-modal.show",
        ".invoice-real-modal.show",
        ".finance-real-modal.show",
        ".service-real-modal.show",
        ".request-modal.show"
    ];
    const closeSelectors = [
        ".close-modal",
        "[data-close-management]",
        "[data-account-close]",
        "[data-invoice-close]",
        "[data-finance-close]",
        "[data-service-close]",
        "[data-request-close]"
    ].join(",");
    const returnFocus = new WeakMap();
    const knownOpen = new WeakSet();
    let lastInvalidForm = null;

    const validationToast = document.createElement("div");
    validationToast.className = "form-validation-toast";
    validationToast.setAttribute("role", "alert");
    document.body.appendChild(validationToast);
    const validationStyle = document.createElement("style");
    validationStyle.textContent = `.form-validation-toast{position:fixed;right:24px;bottom:24px;z-index:2000;max-width:390px;padding:13px 17px;border-radius:9px;background:#d9363e;color:#fff;font-weight:700;opacity:0;transform:translateY(12px);pointer-events:none;transition:.18s}.form-validation-toast.show{opacity:1;transform:none}.field-invalid{border-color:#d9363e!important;box-shadow:0 0 0 3px rgba(217,54,62,.12)!important}`;
    document.head.appendChild(validationStyle);

    function fieldName(field) {
        const label = field.closest("label");
        if (label) return label.childNodes[0]?.textContent?.replace("*", "").trim().toLowerCase() || "trường bắt buộc";
        return field.getAttribute("aria-label") || field.name || "trường bắt buộc";
    }

    function showValidation(message) {
        validationToast.textContent = message;
        validationToast.classList.add("show");
        clearTimeout(showValidation.timer);
        showValidation.timer = setTimeout(() => validationToast.classList.remove("show"), 3200);
    }

    document.addEventListener("input", (event) => {
        const field = event.target;
        if (!(field instanceof HTMLInputElement || field instanceof HTMLSelectElement || field instanceof HTMLTextAreaElement)) return;
        field.setCustomValidity("");
        field.classList.remove("field-invalid");
    }, true);

    document.addEventListener("invalid", (event) => {
        const field = event.target;
        field.classList.add("field-invalid");
        const form = field.form;
        if (form && form !== lastInvalidForm) {
            lastInvalidForm = form;
            showValidation(`Vui lòng kiểm tra ${fieldName(field)}.`);
            setTimeout(() => { lastInvalidForm = null; }, 0);
        }
    }, true);

    document.addEventListener("submit", (event) => {
        const form = event.target;
        if (!(form instanceof HTMLFormElement)) return;
        const requiredFields = [...form.querySelectorAll("input[required], select[required], textarea[required]")];
        const empty = requiredFields.find((field) => typeof field.value === "string" && !field.value.trim());
        if (!empty) return;
        empty.setCustomValidity(`Vui lòng nhập ${fieldName(empty)}.`);
        empty.classList.add("field-invalid");
        event.preventDefault();
        event.stopImmediatePropagation();
        form.reportValidity();
        empty.focus();
    }, true);

    function visible(element) {
        if (!element) return false;
        const style = getComputedStyle(element);
        return style.display !== "none" && style.visibility !== "hidden";
    }

    function openModals() {
        return [...document.querySelectorAll(modalSelectors.join(","))]
            .filter(visible)
            .sort((a, b) => Number(getComputedStyle(a).zIndex) - Number(getComputedStyle(b).zIndex));
    }

    function removeDuplicateHeadings() {
        document.querySelectorAll(".report-heading.section-title").forEach((heading) => {
            const toolbar = heading.querySelector(".report-filter");
            if (toolbar) heading.before(toolbar);
            heading.remove();
        });
        document.querySelectorAll("#settingsView > .section-title, .invoice-breadcrumb, .finance-breadcrumb, .service-breadcrumb, .request-breadcrumb").forEach((element) => element.remove());
    }

    function cleanupClosedModal(modal) {
        modal.querySelectorAll(".field-invalid, .is-invalid, .input-error").forEach((field) => {
            field.classList.remove("field-invalid", "is-invalid", "input-error");
            field.setCustomValidity?.("");
        });
        modal.querySelectorAll(".validation-message").forEach((message) => message.remove());
        validationToast.classList.remove("show");
        if (!openModals().length) document.body.style.removeProperty("overflow");
    }

    function syncFocus() {
        document.querySelectorAll(modalSelectors.join(",")).forEach((modal) => {
            if (visible(modal) && !knownOpen.has(modal)) {
                knownOpen.add(modal);
                returnFocus.set(modal, document.activeElement);
                modal.setAttribute("role", "dialog");
                modal.setAttribute("aria-modal", "true");
                requestAnimationFrame(() => {
                    modal.querySelector("input:not([type='hidden']):not([disabled]), select:not([disabled]), textarea:not([disabled]), button:not([disabled])")?.focus();
                });
            } else if (!visible(modal) && knownOpen.has(modal)) {
                knownOpen.delete(modal);
                const opener = returnFocus.get(modal);
                if (opener && document.contains(opener)) opener.focus();
            }
        });
    }

    new MutationObserver(() => {
        removeDuplicateHeadings();
        syncFocus();
    }).observe(document.body, {
        subtree: true,
        childList: true,
        attributes: true,
        attributeFilter: ["class", "aria-hidden"]
    });

    document.addEventListener("keydown", (event) => {
        if (event.key !== "Escape") return;
        const modal = openModals().at(-1);
        if (!modal) return;
        const closeButton = modal.querySelector(closeSelectors);
        if (!closeButton) return;
        event.preventDefault();
        event.stopPropagation();
        closeButton.click();
    });

    document.addEventListener("click", (event) => {
        const closeButton = event.target.closest(closeSelectors);
        if (!closeButton) return;
        const modal = closeButton.closest(modalSelectors.join(","));
        if (modal) requestAnimationFrame(() => cleanupClosedModal(modal));
    }, true);

    removeDuplicateHeadings();
    syncFocus();
})();
