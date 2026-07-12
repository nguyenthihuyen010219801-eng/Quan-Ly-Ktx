(function () {
    function initSidebar(options) {
        const root = document.getElementById("sidebarContainer");
        if (!root) return;

        const setView = options.setView;
        const user = options.user;

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
            const menu = event.target.closest(".menu-item");
            if (menu && menu.dataset.view && typeof setView === "function") {
                setView(menu.dataset.view);
            }

            if (event.target.closest("#logoutBtn")) {
                fetch((window.APP_CONTEXT || "") + "/api/logout", {method: "POST"})
                    .finally(() => { window.location.href = (window.APP_CONTEXT || "") + "/login.jsp"; });
            }
        });
    }

    async function loadSharedSidebar(options) {
        const root = document.getElementById("sidebarContainer");
        if (!root) return;

        const response = await fetch(options.sidebarUrl || "/frontend/sidebar.html");
        if (!response.ok) throw new Error("Không thể tải thanh điều hướng");

        const template = document.createElement("template");
        template.innerHTML = await response.text();
        root.replaceChildren(template.content.cloneNode(true));
        initSidebar(options);
    }

    window.initSidebar = initSidebar;
    window.loadSharedSidebar = loadSharedSidebar;
})();
