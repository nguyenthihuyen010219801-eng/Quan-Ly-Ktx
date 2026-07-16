(() => {
    const contextPath = window.APP_CONTEXT || "";
    const endpoints = {
        buildings: `${contextPath}/buildings`,
        rooms: `${contextPath}/rooms`,
        services: `${contextPath}/services`
    };
    const managedViews = ["buildings", "rooms", "services"];
    const labels = { buildings: "Quản lý tòa nhà", rooms: "Quản lý phòng", services: "Quản lý dịch vụ" };
    const store = { buildings: [], rooms: [], services: [] };
    const $ = (selector) => document.querySelector(selector);
    const escapeHtml = (value) => String(value ?? "").replace(/[&<>"']/g, (char) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;" }[char]));
    const money = (value) => Number(value || 0).toLocaleString("vi-VN") + "đ";
    const request = async (url, options) => {
        const res = await fetch(url, options);
        const data = await res.json().catch(() => ({}));
        if (!res.ok) throw new Error(data.message || "Không thể xử lý yêu cầu");
        return data;
    };

    function injectUi() {
        const main = document.querySelector("main.main");
        main.insertAdjacentHTML("beforeend", `
            <section id="buildingsView" class="view management-view">${toolbar("building", "Tìm theo mã, tên tòa nhà...", "Thêm tòa nhà")}<div class="table-card management-table"><table><thead><tr><th>Mã tòa nhà</th><th>Tên tòa nhà</th><th>Số tầng</th><th>Ghi chú</th><th>Số phòng</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody id="buildingsBody"></tbody></table></div></section>
            <section id="roomsView" class="view management-view"><div class="management-toolbar"><label class="search-field"><i class="fa-solid fa-magnifying-glass"></i><input id="searchRoom" type="search" placeholder="Tìm theo mã, tên phòng..."></label><select id="roomBuildingFilter"><option value="">Tất cả tòa nhà</option></select><button class="primary" type="button" data-search-action="room">Tìm kiếm</button><button class="success" type="button" data-add="room"><i class="fa-solid fa-plus"></i> Thêm phòng</button></div><div class="table-card management-table"><table><thead><tr><th>Mã phòng</th><th>Tên phòng</th><th>Tòa nhà</th><th>Loại phòng</th><th>Tầng</th><th>Sức chứa</th><th>Hiện tại</th><th>Giá phòng</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody id="roomsBody"></tbody></table></div></section>
            <section id="servicesView" class="view management-view">${toolbar("service", "Tìm theo mã, tên dịch vụ...", "Thêm dịch vụ")}<div class="table-card management-table"><table><thead><tr><th>Mã dịch vụ</th><th>Tên dịch vụ</th><th>Đơn vị tính</th><th>Đơn giá</th><th>Ghi chú</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody id="servicesBody"></tbody></table></div></section>
            <section id="managementDetailView" class="view management-view"><div id="managementDetailContent"></div></section>`);
        main.insertAdjacentHTML("beforeend", `            <div class="modal" id="managementModal" aria-hidden="true"><div class="modal-card management-modal"><div class="modal-head"><div><h2 id="managementTitle"></h2><p id="managementSub">Nhập thông tin và lưu thay đổi</p></div><button class="icon-btn" type="button" data-close-management aria-label="Đóng"><i class="fa-solid fa-xmark"></i></button></div><form id="managementForm" class="student-form"></form></div></div>`);
        document.head.insertAdjacentHTML("beforeend", `<style>
          .management-view.active{display:grid;grid-template-rows:auto auto minmax(0,1fr);gap:10px}.management-toolbar{padding:12px;background:#fff;border:1px solid var(--line);border-radius:8px;display:flex;gap:10px;align-items:center}.management-toolbar select{height:40px;border:1px solid #cfd9e8;border-radius:7px;padding:0 10px;background:#fff}.search-field{height:40px;min-width:280px;display:flex;align-items:center;gap:9px;border:1px solid #cfd9e8;border-radius:7px;padding:0 12px;background:#fff}.search-field input{border:0;outline:0;width:100%}.management-toolbar .success{margin-left:auto}.management-table{overflow:auto}.management-table table{min-width:900px}.action-row{display:flex;gap:6px}.action-row button{border:0;border-radius:6px;padding:7px 9px;color:#fff}.detail-action{background:#667085;display:inline-flex;gap:5px;align-items:center}.edit-action{background:#0b72e7}.delete-action{background:#ef4444}.status-pill{display:inline-block;padding:4px 8px;border-radius:999px;background:#e8f7ef;color:#11824b;font-size:12px;font-weight:700}.status-pill.off{background:#f4f5f7;color:#667085}.empty-row{text-align:center;color:var(--muted);padding:24px}.management-modal{max-width:780px}.management-form-note{color:var(--muted);font-size:13px}.management-view .section-title{margin:0}.detail-head{display:flex;align-items:center;gap:16px;margin-bottom:14px}.detail-head h2,.table-card h3{margin:0}.detail-head p{margin:4px 0 0;color:var(--muted)}.detail-summary{display:grid;grid-template-columns:repeat(4,1fr);gap:10px;margin-bottom:12px}.detail-summary-wide{grid-template-columns:repeat(3,1fr)}.detail-value{background:#fff;border:1px solid var(--line);border-radius:8px;padding:12px}.detail-value span{display:block;color:var(--muted);font-size:13px}.detail-value strong{font-size:18px}.tenant-list{margin:0;padding-left:18px;min-width:180px}.tenant-list span{display:block;color:var(--muted);font-size:12px}.detail-empty{color:var(--muted);padding:12px 0}.tenant-card{padding:14px}@media(max-width:900px){.management-toolbar{flex-wrap:wrap}.search-field{min-width:0;flex:1 1 240px}.management-toolbar .success{margin-left:0}}
        </style>`);
        document.head.insertAdjacentHTML("beforeend", `<style>
          .management-view.active{grid-template-rows:auto minmax(0,1fr)}
          #roomsView{gap:16px;padding-bottom:8px}
          #roomsView .management-toolbar{padding:16px 18px;border:1px solid #e3eaf3;border-radius:14px;box-shadow:0 8px 24px rgba(23,43,77,.06);background:linear-gradient(135deg,#fff 0%,#f8fbff 100%)}
          #roomsView .search-field{height:44px;min-width:320px;border-color:#d9e2ef;border-radius:10px;background:#fff;transition:border-color .2s,box-shadow .2s}
          #roomsView .search-field:focus-within{border-color:#2784ff;box-shadow:0 0 0 3px rgba(39,132,255,.13)}
          #roomsView .search-field i{color:#7a8ca5}
          #roomsView .search-field input{font-size:14px;color:#172b4d;background:transparent}
          #roomsView .management-toolbar select{height:44px;min-width:170px;border-color:#d9e2ef;border-radius:10px;color:#344563;outline:none;cursor:pointer}
          #roomsView .management-toolbar select:focus{border-color:#2784ff;box-shadow:0 0 0 3px rgba(39,132,255,.13)}
          #roomsView .management-toolbar button{height:44px;border:0;border-radius:10px;padding:0 17px;font-weight:800;transition:transform .18s,box-shadow .18s,filter .18s}
          #roomsView .management-toolbar button:hover{transform:translateY(-1px);filter:brightness(1.03)}
          #roomsView .management-toolbar .primary{box-shadow:0 7px 16px rgba(11,114,231,.2)}
          #roomsView .management-toolbar .success{box-shadow:0 7px 16px rgba(22,163,74,.2)}
          #roomsView .management-table{max-height:calc(100vh - 285px);overflow:auto;border:1px solid #e5e7eb;border-radius:14px;box-shadow:0 10px 30px rgba(23,43,77,.08);background:#fff;scrollbar-width:thin;scrollbar-color:#9eb7d4 #edf3f9}
          #roomsView .management-table::-webkit-scrollbar{width:7px;height:7px}
          #roomsView .management-table::-webkit-scrollbar-track{background:#edf3f9;border-radius:999px}
          #roomsView .management-table::-webkit-scrollbar-thumb{background:#9eb7d4;border:2px solid #edf3f9;border-radius:999px}
          #roomsView .management-table::-webkit-scrollbar-thumb:hover{background:#6f94bc}
          #roomsView .management-table table{width:100%;min-width:1160px;border-collapse:separate;border-spacing:0;table-layout:auto}
          #roomsView .management-table thead th{position:sticky;top:0;z-index:3;height:52px;padding:0 18px;background:#f5f9ff;color:#24466f;font-size:11px;font-weight:800;letter-spacing:.6px;text-transform:uppercase;border-bottom:1px solid #dce5f0;vertical-align:middle;white-space:nowrap;box-shadow:0 1px 0 rgba(36,70,111,.04)}
          #roomsView .management-table tbody td{height:66px;padding:0 18px;color:#344563;border-bottom:1px solid #e5e7eb;vertical-align:middle;white-space:nowrap}
          #roomsView .management-table tbody tr{transition:background-color .18s ease}
          #roomsView .management-table tbody tr:hover{background:#f8fbff}
          #roomsView .management-table tbody tr:last-child td{border-bottom:0}
          #roomsView .management-table th:nth-child(-n+4),#roomsView .management-table td:nth-child(-n+4){text-align:left}
          #roomsView .management-table th:nth-child(5),#roomsView .management-table th:nth-child(6),#roomsView .management-table th:nth-child(7),#roomsView .management-table th:nth-child(9),#roomsView .management-table th:nth-child(10),#roomsView .management-table td:nth-child(5),#roomsView .management-table td:nth-child(6),#roomsView .management-table td:nth-child(7),#roomsView .management-table td:nth-child(9),#roomsView .management-table td:nth-child(10){text-align:center}
          #roomsView .management-table th:nth-child(8),#roomsView .management-table td:nth-child(8){text-align:right}
          #roomsView .management-table tbody td:first-child{color:#0969da;font-weight:800}
          #roomsView .management-table tbody td:nth-child(2){font-weight:700;color:#172b4d}
          #roomsView .management-table tbody td:nth-child(6),#roomsView .management-table tbody td:nth-child(7){font-weight:700}
          #roomsView .management-table tbody td:nth-child(8){font-weight:600;color:#174a7e;font-variant-numeric:tabular-nums}
          #roomsView .status-pill{display:inline-flex;min-width:88px;align-items:center;justify-content:center;padding:7px 12px;border:1px solid transparent;border-radius:999px;font-size:12px;font-weight:800;line-height:1;box-shadow:none}
          #roomsView .status-pill.available{border-color:#b7e4c7;background:#e9f9ef;color:#147a45}
          #roomsView .status-pill.full{border-color:#ffd3a8;background:#fff4e8;color:#ad5700}
          #roomsView .status-pill.maintenance{border-color:#ffc7ca;background:#ffedef;color:#c8323c}
          #roomsView .action-row{gap:8px;justify-content:center;align-items:center}
          #roomsView .action-row button{width:40px;min-width:40px;height:40px;padding:0;border-radius:8px;display:inline-flex;align-items:center;justify-content:center;box-shadow:0 3px 8px rgba(23,43,77,.14);transition:transform .18s,box-shadow .18s,filter .18s}
          #roomsView .action-row .detail-action{width:auto;min-width:40px;padding:0 12px}
          #roomsView .action-row button:hover{transform:translateY(-2px);box-shadow:0 7px 14px rgba(23,43,77,.18);filter:brightness(1.04)}
          #roomsView .detail-action{background:#5f6f86}
          #roomsView .edit-action{background:#0b72e7}
          #roomsView .delete-action{background:#e5484d}
          @media(max-width:1100px){#roomsView .management-toolbar{flex-wrap:wrap}#roomsView .search-field{flex:1 1 310px}#roomsView .management-toolbar .success{margin-left:0}}
          @media(max-width:900px){#roomsView .management-table{max-height:calc(100vh - 330px)}#roomsView .management-table table{min-width:1080px}#roomsView .management-table thead th,#roomsView .management-table tbody td{padding-left:14px;padding-right:14px}}
          @media(max-width:640px){#roomsView{gap:12px}#roomsView .section-title h2{font-size:21px}#roomsView .management-toolbar{padding:12px;border-radius:11px}#roomsView .search-field,#roomsView .management-toolbar select,#roomsView .management-toolbar button{width:100%;min-width:0}#roomsView .management-toolbar .success{margin-left:0}#roomsView .management-table{max-height:calc(100vh - 390px);border-radius:12px}#roomsView .management-table table{min-width:1040px}#roomsView .management-table tbody td{height:62px}#roomsView .action-row{justify-content:center}}
        </style>`);
    }
    function toolbar(type, placeholder, addLabel) { const inputId = type === "building" ? "searchBuilding" : "searchService"; const add=window.CURRENT_USER?.role==="quanly"?`<button class="success" type="button" data-add="${type}"><i class="fa-solid fa-plus"></i> ${addLabel}</button>`:"";return `<div class="management-toolbar"><label class="search-field"><i class="fa-solid fa-magnifying-glass"></i><input id="${inputId}" type="search" placeholder="${placeholder}"></label><button class="primary" type="button" data-search-action="${type}">Tìm kiếm</button>${add}</div>`; }

    function statusPill(value) {
        const status = String(value || "");
        const statusClass = status === "Còn trống" ? "available" : status === "Đã đầy" ? "full" : status === "Bảo trì" ? "maintenance" : status === "Ngưng dùng" ? "off" : "";
        return `<span class="status-pill ${statusClass}">${escapeHtml(value)}</span>`;
    }
    async function loadBuildings() {
        const search = $("#searchBuilding").value.trim();
        store.buildings = await request(`${endpoints.buildings}?action=search&keyword=${encodeURIComponent(search)}`);
        $("#buildingsBody").innerHTML = store.buildings.length ? store.buildings.map((item) => `<tr><td>${escapeHtml(item.building_code)}</td><td>${escapeHtml(item.building_name)}</td><td>${item.floors}</td><td>${escapeHtml(item.note || "")}</td><td>${item.room_count}</td><td>${statusPill(item.status)}</td>${actions("building", item.id)}</tr>`).join("") : empty(7);
        updateBuildingOptions();
    }
    async function loadRooms() {
        const search = $("#searchRoom").value.trim(), buildingId = $("#roomBuildingFilter").value;
        store.rooms = await request(`${endpoints.rooms}?action=search&keyword=${encodeURIComponent(search)}&building_id=${encodeURIComponent(buildingId)}`);
        $("#roomsBody").innerHTML = store.rooms.length ? store.rooms.map((item) => `<tr><td>${escapeHtml(item.room_code)}</td><td>${escapeHtml(item.room_name || "")}</td><td>${escapeHtml(item.building_name || "")}</td><td>${escapeHtml(item.room_type || "Tiêu chuẩn")}</td><td>${item.floor || 1}</td><td>${item.capacity}</td><td>${item.current_quantity}</td><td>${money(item.price)}</td><td>${statusPill(item.status)}</td>${actions("room", item.id)}</tr>`).join("") : empty(10);
    }
    async function loadServices() {
        const search = $("#searchService").value.trim();
        store.services = await request(`${endpoints.services}?action=search&keyword=${encodeURIComponent(search)}`);
        $("#servicesBody").innerHTML = store.services.length ? store.services.map((item) => `<tr><td>${escapeHtml(item.service_code)}</td><td>${escapeHtml(item.service_name)}</td><td>${escapeHtml(item.unit)}</td><td>${money(item.unit_price)}</td><td>${escapeHtml(item.note || "")}</td><td>${statusPill(item.status)}</td>${actions("service", item.id)}</tr>`).join("") : empty(7);
    }
    function searchBuildings(event) { event?.preventDefault(); return loadBuildings(); }
    function searchRooms(event) { event?.preventDefault(); return loadRooms(); }
    function searchServices(event) { event?.preventDefault(); return loadServices(); }
    function empty(columns) { return `<tr><td colspan="${columns}" class="empty-row">Chưa có dữ liệu phù hợp</td></tr>`; }
    function actions(type, id) { const detail = `<button class="detail-action" type="button" data-detail="${type}" data-id="${id}" title="Xem chi tiết"><i class="fa-solid fa-eye"></i><span>Xem chi tiết</span></button>`;const manage=window.CURRENT_USER?.role==="quanly"?`<button class="edit-action" type="button" data-edit="${type}" data-id="${id}" title="Sửa"><i class="fa-solid fa-pen"></i></button><button class="delete-action" type="button" data-delete="${type}" data-id="${id}" title="Xóa"><i class="fa-solid fa-trash"></i></button>`:"";return `<td><div class="action-row">${detail}${manage}</div></td>`; }
    function tenantList(tenants) {
        if (!tenants.length) return `<p class="detail-empty">Chưa có dữ liệu người thuê trong phòng này</p>`;
        return `<ul class="tenant-list">${tenants.map((tenant) => `<li><strong>${escapeHtml(tenant.full_name)}</strong><span>${escapeHtml(tenant.student_code || "")} ${tenant.phone ? `- ${escapeHtml(tenant.phone)}` : ""}</span></li>`).join("")}</ul>`;
    }
    function detailValue(label, value) { return `<div class="detail-value"><span>${label}</span><strong>${value}</strong></div>`; }
    async function openServiceDetail(id) {
        try {
            const data = await request(`${endpoints.services}?action=detail&id=${id}`);
            const s = data.service;
            ["buildings", "rooms", "services"].forEach((name) => $(`#${name}View`)?.classList.remove("active"));
            $("#managementDetailView").classList.add("active");
            window.setSidebarActive?.("services");
            $("#pageTitle").textContent = "Chi tiết dịch vụ";
            $("#managementDetailContent").innerHTML = `<div class="detail-head"><button class="secondary" type="button" data-back-detail="services"><i class="fa-solid fa-arrow-left"></i> Quay lại danh sách</button><div><h2>${escapeHtml(s.service_name)}</h2><p>${escapeHtml(s.service_code)}</p></div></div><div class="detail-summary detail-summary-wide">${detailValue("Mã dịch vụ", escapeHtml(s.service_code))}${detailValue("Đơn vị tính", escapeHtml(s.unit))}${detailValue("Đơn giá", money(s.unit_price))}${detailValue("Trạng thái", statusPill(s.status))}${detailValue("Ghi chú", escapeHtml(s.note || "Không có"))}</div><div class="table-card tenant-card"><h3>Phòng/người thuê đang sử dụng</h3><p class="detail-empty">Chưa có dữ liệu sử dụng dịch vụ này</p></div>`;
        } catch (err) { alert(err.message); }
    }
    async function openDetail(type, id) {
        if (type === "service") return openServiceDetail(id);
        try {
            const data = await request(`${endpoints[type + "s"]}?action=detail&id=${id}`);
            const ownerView = type === "building" ? "buildings" : "rooms";
            ["buildings", "rooms", "services"].forEach((name) => $(`#${name}View`)?.classList.remove("active"));
            const detailView = $("#managementDetailView");
            detailView.classList.add("active");
            window.setSidebarActive?.(ownerView);
            $("#pageTitle").textContent = type === "building" ? "Chi tiết tòa nhà" : "Chi tiết phòng";
            if (type === "building") {
                const b = data.building, stats = data.stats;
                $("#managementDetailContent").innerHTML = `<div class="detail-head"><button class="secondary" type="button" data-back-detail="${ownerView}"><i class="fa-solid fa-arrow-left"></i> Quay lại danh sách</button><div><h2>${escapeHtml(b.building_name)}</h2><p>Mã tòa nhà: ${escapeHtml(b.building_code)} · ${b.floors} tầng</p></div></div><div class="detail-summary">${detailValue("Tổng số phòng", stats.total_rooms)}${detailValue("Còn trống", stats.available_rooms)}${detailValue("Đã đầy", stats.full_rooms)}${detailValue("Bảo trì", stats.maintenance_rooms)}</div><div class="table-card management-table"><h3>Danh sách phòng thuộc tòa nhà</h3><table><thead><tr><th>Mã phòng</th><th>Tầng</th><th>Sức chứa</th><th>Trạng thái</th><th>Người đang ở</th></tr></thead><tbody>${data.rooms.length ? data.rooms.map((room) => `<tr><td>${escapeHtml(room.room_code)}</td><td>${room.floor || 1}</td><td>${room.current_quantity}/${room.capacity} người</td><td>${statusPill(room.status)}</td><td>${tenantList(room.tenants)}</td></tr>`).join("") : `<tr><td colspan="5" class="empty-row">Chưa có phòng thuộc tòa nhà này</td></tr>`}</tbody></table></div>`;
            } else {
                const r = data.room;
                $("#managementDetailContent").innerHTML = `<div class="detail-head"><button class="secondary" type="button" data-back-detail="${ownerView}"><i class="fa-solid fa-arrow-left"></i> Quay lại danh sách</button><div><h2>${escapeHtml(r.room_name || r.room_code)}</h2><p>${escapeHtml(r.building_name || "Chưa có tòa nhà")} · ${escapeHtml(r.room_code)}</p></div></div><div class="detail-summary detail-summary-wide">${detailValue("Tòa nhà", escapeHtml(r.building_name || ""))}${detailValue("Tầng", r.floor || 1)}${detailValue("Sức chứa", `${r.capacity} người`)}${detailValue("Hiện tại", `${r.current_quantity} người`)}${detailValue("Giá phòng", money(r.price))}${detailValue("Trạng thái", statusPill(r.status))}</div><div class="table-card tenant-card"><h3>Người đang thuê</h3>${data.tenants.length ? `<table><thead><tr><th>Họ tên</th><th>MSSV</th><th>SĐT</th><th>Email</th></tr></thead><tbody>${data.tenants.map((tenant) => `<tr><td>${escapeHtml(tenant.full_name)}</td><td>${escapeHtml(tenant.student_code || "")}</td><td>${escapeHtml(tenant.phone || "")}</td><td>${escapeHtml(tenant.email || "")}</td></tr>`).join("")}</tbody></table>` : `<p class="detail-empty">Chưa có dữ liệu người thuê trong phòng này</p>`}</div>`;
            }
        } catch (err) { alert(err.message); }
    }
    function closeDetail(view) { $("#managementDetailView")?.classList.remove("active"); window.setView(view); }    function updateBuildingOptions() {
        const filter = $("#roomBuildingFilter"), current = filter.value;
        filter.innerHTML = `<option value="">Tất cả tòa nhà</option>` + store.buildings.map((b) => `<option value="${b.id}">${escapeHtml(b.building_name)}</option>`).join("");
        filter.value = current;
    }
    function field(label, input, extra="") { return `<label ${extra}>${label}${input}</label>`; }
    function openForm(type, item = {}) {
        const form = $("#managementForm"), isEdit = Boolean(item.id);
        $("#managementTitle").textContent = `${isEdit ? "Sửa" : "Thêm"} ${labels[type + "s"].toLowerCase()}`;
        let content = `<input type="hidden" name="recordId" value="${item.id || ""}"><input type="hidden" name="recordType" value="${type}">`;
        if (type === "building") content += field("Mã tòa nhà", `<input name="building_code" required value="${escapeHtml(item.building_code || "")}">`) + field("Tên tòa nhà", `<input name="building_name" required value="${escapeHtml(item.building_name || "")}">`) + field("Số tầng", `<input name="floors" type="number" min="1" required value="${item.floors || 1}">`) + field("Trạng thái", `<select name="status">${["Đang hoạt động", "Bảo trì", "Ngừng hoạt động"].map((s) => `<option ${item.status === s ? "selected" : ""}>${s}</option>`).join("")}</select>`) + field("Ghi chú", `<textarea name="note" rows="3">${escapeHtml(item.note || "")}</textarea>`, 'class="span-3"');
        if (type === "room") content += field("Mã phòng", `<input name="room_code" required value="${escapeHtml(item.room_code || "")}">`) + field("Tên phòng", `<input name="room_name" required value="${escapeHtml(item.room_name || "")}">`) + field("Tòa nhà", `<select name="building_id" required><option value="">Chọn tòa nhà</option>${store.buildings.map((b) => `<option value="${b.id}" ${String(item.building_id) === String(b.id) ? "selected" : ""}>${escapeHtml(b.building_name)}</option>`).join("")}</select>`) + field("Loại phòng", `<input name="room_type" required value="${escapeHtml(item.room_type || "Tiêu chuẩn")}">`) + field("Tầng", `<input name="floor" type="number" min="1" required value="${item.floor || 1}">`) + field("Sức chứa", `<input name="capacity" type="number" min="1" required value="${item.capacity || 1}">`) + field("Số người hiện tại", `<input name="current_quantity" type="number" min="0" required value="${item.current_quantity || 0}">`) + field("Giá phòng", `<input name="price" type="number" min="0" required value="${item.price ?? ""}">`) + field("Trạng thái", `<select name="status">${["Còn trống", "Đã đầy", "Bảo trì"].map((s) => `<option ${item.status === s ? "selected" : ""}>${s}</option>`).join("")}</select>`);
        if (type === "service") content += field("Mã dịch vụ", `<input name="service_code" required value="${escapeHtml(item.service_code || "")}">`) + field("Tên dịch vụ", `<input name="service_name" required value="${escapeHtml(item.service_name || "")}">`) + field("Đơn vị tính", `<input name="unit" required placeholder="Ví dụ: kWh, m³, tháng" value="${escapeHtml(item.unit || "")}">`) + field("Đơn giá", `<input name="unit_price" type="number" min="0" required value="${item.unit_price || 0}">`) + field("Trạng thái", `<select name="status"><option ${item.status === "Đang dùng" ? "selected" : ""}>Đang dùng</option><option ${item.status === "Ngưng dùng" ? "selected" : ""}>Ngưng dùng</option></select>`) + field("Ghi chú", `<textarea name="note" rows="3">${escapeHtml(item.note || "")}</textarea>`, 'class="span-3"');
        form.innerHTML = content + `<div class="form-actions span-3"><button class="secondary" type="button" data-close-management>Hủy</button><button class="primary" type="submit">Lưu thay đổi</button></div>`;
        $("#managementModal").classList.add("show");
        $("#managementModal").setAttribute("aria-hidden", "false");
    }
    function closeForm() { $("#managementModal").classList.remove("show"); $("#managementModal").setAttribute("aria-hidden", "true"); }
    async function submitForm(event) {
        event.preventDefault();
        const form = new FormData(event.currentTarget), type = form.get("recordType"), id = form.get("recordId");
        const payload = Object.fromEntries(form.entries()); delete payload.recordType; delete payload.recordId;
        try { await request(`${endpoints[type + "s"]}?action=${id ? "edit" : "create"}${id ? `&id=${id}` : ""}`, { method: id ? "PUT" : "POST", headers: { "Content-Type": "application/json; charset=UTF-8" }, body: JSON.stringify(payload) }); closeForm(); await refresh(type); alert("Đã lưu thành công"); }
        catch (err) { alert(err.message); }
    }
    async function refresh(type) { if (type === "building") { await loadBuildings(); await loadRooms(); } if (type === "room") await loadRooms(); if (type === "service") await loadServices(); }
    async function remove(type, id) { if (!confirm("Bạn có chắc muốn xóa mục này?")) return; try { await request(`${endpoints[type + "s"]}?action=delete&id=${id}`, { method: "DELETE" }); await refresh(type); alert("Đã xóa thành công"); } catch (err) { alert(err.message); } }
    function activate(view) { $("#managementDetailView")?.classList.remove("active"); managedViews.forEach((name) => $(`#${name}View`).classList.toggle("active", name === view)); $("#pageTitle").textContent = labels[view]; if (view === "buildings") loadBuildings().catch(showError); if (view === "rooms") Promise.all([loadBuildings(), loadRooms()]).catch(showError); if (view === "services") loadServices().catch(showError); }
    function showError(err) { alert(err.message || "Không thể tải dữ liệu"); }
    function setupEvents() {
        document.addEventListener("click", (event) => { const add = event.target.closest("[data-add]"), edit = event.target.closest("[data-edit]"), del = event.target.closest("[data-delete]"), detail = event.target.closest("[data-detail]"), searchAction = event.target.closest("[data-search-action]"); if (add) openForm(add.dataset.add); if (detail) openDetail(detail.dataset.detail, detail.dataset.id); if (edit) { const list = store[edit.dataset.edit + "s"] || [], item = list.find((x) => String(x.id) === edit.dataset.id); if (item) openForm(edit.dataset.edit, item); } if (del) remove(del.dataset.delete, del.dataset.id); if (searchAction) { const actions = { building: searchBuildings, room: searchRooms, service: searchServices }; actions[searchAction.dataset.searchAction]?.(event); } if (event.target.closest("[data-close-management]")) closeForm(); if (event.target.closest("[data-back-detail]")) closeDetail(event.target.closest("[data-back-detail]").dataset.backDetail); });
        $("#managementForm").addEventListener("submit", submitForm);
        $("#searchBuilding").addEventListener("keydown", (event) => { if (event.key === "Enter") searchBuildings(event); });
        $("#searchRoom").addEventListener("keydown", (event) => { if (event.key === "Enter") searchRooms(event); });
        $("#searchService").addEventListener("keydown", (event) => { if (event.key === "Enter") searchServices(event); });
        $("#roomBuildingFilter").addEventListener("change", loadRooms);
    }
    const boot = () => { injectUi();if(window.CURRENT_USER?.role!=="quanly")document.querySelector('[data-add="room"]')?.remove();setupEvents(); window.renderManagementView = activate; };
    boot();
})();
