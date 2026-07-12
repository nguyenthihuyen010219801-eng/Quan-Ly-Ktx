(function () {
    function initSidebar(options) {
        const root = document.getElementById("sidebarContainer");
        if (!root) return;

        const setView = options.setView;
        const user = options.user;
        const sidebar = root.querySelector(".sidebar");
        const toggle = root.querySelector(".sidebar-toggle");

        function setCollapsed(collapsed) {
            sidebar.classList.toggle("collapsed", collapsed);
            toggle.setAttribute("aria-expanded", String(!collapsed));
            toggle.setAttribute("aria-label", collapsed ? "Mở rộng thanh bên" : "Thu gọn thanh bên");
            toggle.title = collapsed ? "Mở rộng thanh bên" : "Thu gọn thanh bên";
            root.querySelectorAll(".menu-item").forEach((item) => {
                const label = item.querySelector("span")?.textContent.trim();
                if (label) item.title = collapsed ? label : "";
            });
            localStorage.setItem("sidebarCollapsed", String(collapsed));
        }

        setCollapsed(localStorage.getItem("sidebarCollapsed") === "true");

        root.querySelectorAll("[data-manager-only]").forEach((item) => {
            item.hidden = !user || user.role !== "quanly";
        });

        window.setSidebarActive = function setSidebarActive(view) {
            root.querySelectorAll(".menu-item").forEach((item) => {
                item.classList.toggle("active", item.dataset.view === view);
            });
        };

        if (user && user.full_name) {
            const accountName = root.querySelector("#accountName");
            if (accountName) accountName.textContent = user.full_name;
        }

        root.addEventListener("click", (event) => {
            if (event.target.closest(".sidebar-toggle")) {
                setCollapsed(!sidebar.classList.contains("collapsed"));
                return;
            }
            const menu = event.target.closest(".menu-item");
            if (menu && menu.dataset.view && typeof setView === "function") {
                setView(menu.dataset.view);
            }

            if (event.target.closest("#logoutBtn")) {
                const contextPath = window.APP_CONTEXT || "";
                fetch(contextPath + "/api/logout", {method: "POST", cache: "no-store"})
                    .then((response) => response.json().catch(() => ({})))
                    .then((data) => window.location.replace(data.redirectUrl || contextPath + "/login.jsp"))
                    .catch(() => window.location.replace(contextPath + "/login.jsp"));
            }
        });
    }

    async function loadSharedSidebar(options) {
        const root = document.getElementById("sidebarContainer");
        if (!root) return;

        const response = await fetch(options.sidebarUrl || "/frontend/sidebar.html", {cache: "no-store"});
        if (!response.ok) throw new Error("Không thể tải thanh điều hướng");

        const template = document.createElement("template");
        template.innerHTML = await response.text();
        root.replaceChildren(template.content.cloneNode(true));
        initSidebar(options);
    }

    window.initSidebar = initSidebar;
    window.loadSharedSidebar = loadSharedSidebar;
})();
