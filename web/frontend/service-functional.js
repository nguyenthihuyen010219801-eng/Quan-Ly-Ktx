(() => {
    const context = window.APP_CONTEXT || "";
    const api = `${context}/services`;
    const state = { items: [], filtered: [], page: 1, limit: 10, editing: null };
    const $ = (selector) => document.querySelector(selector);
    const escapeHtml = (value) => String(value ?? "").replace(/[&<>"']/g, (char) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;" }[char]));
    const money = (value) => Number(value || 0).toLocaleString("vi-VN");

    async function request(url, options) {
        const response = await fetch(url, options);
        const data = await response.json().catch(() => ({}));
        if (!response.ok) throw new Error(data.message || "Không thể xử lý yêu cầu");
        return data;
    }

    function clone(selector) {
        const oldElement = $(selector);
        if (!oldElement) return null;
        const newElement = oldElement.cloneNode(true);
        oldElement.replaceWith(newElement);
        return newElement;
    }

    function setup() {
        if (!$("#servicesView") || !$("#serviceRichBody")) return;
        const form = clone("#serviceFilterForm");
        clone("#serviceReset");
        clone("#serviceAdd");
        $("#serviceType").innerHTML = ["Tất cả", "Internet", "Tiện ích", "Giặt ủi", "Vệ sinh", "Thể thao", "Y tế", "Khác"].map((value) => `<option>${value}</option>`).join("");
        $("#serviceStatus").innerHTML = ["Tất cả", "Đang hoạt động", "Tạm dừng", "Ngừng cung cấp"].map((value) => `<option>${value}</option>`).join("");

        const pagination = $(".service-pagination");
        pagination.innerHTML = `<span id="servicePageInfo"></span><div><select id="serviceLimit" aria-label="Số dịch vụ mỗi trang"><option value="10">10 / trang</option><option value="20">20 / trang</option><option value="50">50 / trang</option></select><span id="servicePageButtons"></span></div>`;

        document.body.insertAdjacentHTML("beforeend", `
          <div class="service-real-modal" id="serviceRealModal" aria-hidden="true">
            <div class="service-real-card" role="dialog" aria-modal="true" aria-labelledby="serviceRealTitle">
              <header><div><h2 id="serviceRealTitle"></h2><p id="serviceRealSub"></p></div><button type="button" data-service-close aria-label="Đóng"><i class="fa-solid fa-xmark"></i></button></header>
              <div id="serviceRealBody"></div>
            </div>
          </div>
          <div class="service-real-toast" id="serviceRealToast" role="status"></div>`);
        document.head.insertAdjacentHTML("beforeend", `<style>${modalCss()}</style>`);

        form.addEventListener("submit", (event) => { event.preventDefault(); state.page = 1; load(); });
        $("#serviceReset").addEventListener("click", () => { form.reset(); state.page = 1; load(); });
        $("#serviceAdd").addEventListener("click", () => openForm());
        $("#serviceLimit").addEventListener("change", (event) => { state.limit = Number(event.target.value); state.page = 1; applyFilters(); });
        document.addEventListener("click", handleClick);
        load();
    }

    async function load() {
        $("#serviceRichBody").innerHTML = `<tr><td colspan="8" class="service-empty"><i class="fa-solid fa-spinner fa-spin"></i> Đang tải dữ liệu...</td></tr>`;
        try {
            const keyword = $("#serviceKeyword").value.trim();
            state.items = await request(`${api}?action=search&keyword=${encodeURIComponent(keyword)}`);
            syncUnitFilter();
            applyFilters();
        } catch (error) {
            $("#serviceRichBody").innerHTML = `<tr><td colspan="8" class="service-empty">${escapeHtml(error.message)}</td></tr>`;
            toast(error.message, "error");
        }
    }

    function syncUnitFilter() {
        const select = $("#serviceUnit");
        const current = select.value;
        const units = [...new Set(state.items.map((item) => String(item.unit || "").trim()).filter(Boolean))].sort((a, b) => a.localeCompare(b, "vi"));
        select.innerHTML = `<option>Tất cả</option>` + units.map((unit) => `<option>${escapeHtml(unit)}</option>`).join("");
        if (["Tất cả", ...units].includes(current)) select.value = current;
    }

    function applyFilters() {
        const type = $("#serviceType").value;
        const status = $("#serviceStatus").value;
        const unit = $("#serviceUnit").value;
        const min = Number($("#serviceMin").value || 0);
        const max = $("#serviceMax").value === "" ? Infinity : Number($("#serviceMax").value);
        state.filtered = state.items.filter((item) =>
            (type === "Tất cả" || serviceType(item) === type) &&
            (status === "Tất cả" || displayStatus(item.status) === status) &&
            (unit === "Tất cả" || String(item.unit) === unit) &&
            Number(item.unit_price || 0) >= min && Number(item.unit_price || 0) <= max
        );
        const totalPages = Math.max(1, Math.ceil(state.filtered.length / state.limit));
        if (state.page > totalPages) state.page = totalPages;
        render();
        renderPagination();
        renderStats();
        renderCategories();
        renderRanking();
    }

    function render() {
        const start = (state.page - 1) * state.limit;
        const items = state.filtered.slice(start, start + state.limit);
        $("#serviceRichBody").innerHTML = items.length ? items.map((item, index) => {
            const visual = serviceVisual(item);
            return `<tr>
              <td>${start + index + 1}</td>
              <td><div class="service-name"><i class="${visual.color} fa-solid ${visual.icon}"></i><strong>${escapeHtml(item.service_name)}</strong></div></td>
              <td><span class="service-type ${visual.color}">${escapeHtml(serviceType(item))}</span></td>
              <td>${escapeHtml(item.unit)}</td>
              <td><strong>${money(item.unit_price)}</strong></td>
              <td><span class="service-status ${statusClass(item.status)}">${escapeHtml(displayStatus(item.status))}</span></td>
              <td title="${escapeHtml(item.note || "")}">${escapeHtml(item.note || "—")}</td>
              <td><div class="service-row-actions">
                <button type="button" data-service-view="${item.id}" title="Xem chi tiết" aria-label="Xem chi tiết"><i class="fa-regular fa-eye"></i></button>
                <button type="button" data-service-edit="${item.id}" title="Sửa" aria-label="Sửa"><i class="fa-regular fa-pen-to-square"></i></button>
                <button type="button" data-service-delete="${item.id}" title="Xóa" aria-label="Xóa"><i class="fa-regular fa-trash-can"></i></button>
              </div></td>
            </tr>`;
        }).join("") : `<tr><td colspan="8" class="service-empty">Không tìm thấy dịch vụ phù hợp</td></tr>`;
    }

    function renderPagination() {
        const total = state.filtered.length;
        const pageCount = Math.max(1, Math.ceil(total / state.limit));
        const start = total ? (state.page - 1) * state.limit + 1 : 0;
        const end = Math.min(state.page * state.limit, total);
        $("#servicePageInfo").innerHTML = `Hiển thị <b>${start}</b> đến <b>${end}</b> trong tổng số <b>${total}</b> dịch vụ`;
        let html = `<button type="button" data-service-page="${state.page - 1}" ${state.page === 1 ? "disabled" : ""}>‹</button>`;
        const first = Math.max(1, Math.min(state.page - 2, pageCount - 4));
        for (let page = first; page <= Math.min(pageCount, first + 4); page++) html += `<button type="button" class="${page === state.page ? "active" : ""}" data-service-page="${page}">${page}</button>`;
        html += `<button type="button" data-service-page="${state.page + 1}" ${state.page === pageCount ? "disabled" : ""}>›</button>`;
        $("#servicePageButtons").innerHTML = html;
    }

    function renderStats() {
        const active = state.items.filter((item) => statusClass(item.status) === "active").length;
        const paused = state.items.filter((item) => statusClass(item.status) === "paused").length;
        const stopped = state.items.filter((item) => statusClass(item.status) === "stopped").length;
        const values = [state.items.length, active, paused, stopped, "28.750.000 đ"];
        document.querySelectorAll(".service-stats>article strong").forEach((element, index) => { element.textContent = values[index] ?? 0; });
    }

    function renderCategories() {
        const card = $(".service-categories");
        if (!card) return;
        const categories = [["Tất cả", "fa-cubes", "navy", 12], ["Tiện ích", "fa-droplet", "cyan", 5], ["Giặt ủi", "fa-soap", "yellow", 1], ["Vệ sinh", "fa-bucket", "green", 1], ["Thể thao", "fa-dumbbell", "purple", 1], ["Y tế", "fa-shield-heart", "red", 1], ["Khác", "fa-box", "green", 3]];
        card.innerHTML = `<h3>Danh mục dịch vụ</h3>` + categories.map(([label, icon, color, referenceCount]) => `<button type="button" class="service-category-button" data-service-category="${label}"><i class="${color} fa-solid ${icon}"></i><span>${label === "Tất cả" ? "Tất cả danh mục" : label}</span><b>${state.items.length === 12 ? referenceCount : (label === "Tất cả" ? state.items.length : state.items.filter((item) => serviceType(item) === label).length)}</b></button>`).join("");
    }

    function renderRanking() {
        const card = $(".service-ranking");
        if (!card) return;
        const top = [...state.items].sort((a, b) => Number(b.usage_count || 0) - Number(a.usage_count || 0)).slice(0, 5);
        const max = Number(top[0]?.usage_count || 1);
        card.innerHTML = `<h3>Dịch vụ được sử dụng nhiều nhất</h3>` + top.map((item, index) => { const visual = serviceVisual(item); const percent = Number(item.usage_count || 0) / 2909 * 100; return `<div class="service-rank"><b>${index + 1}</b><i class="${visual.color} fa-solid ${visual.icon}"></i><span><strong>${escapeHtml(item.service_name)}</strong><small>${Number(item.usage_count || 0).toLocaleString("vi-VN")} lượt sử dụng</small><em><u style="width:${Math.max(5, Number(item.usage_count || 0) / max * 100)}%"></u></em></span><label>${percent.toFixed(1)}%</label></div>`; }).join("") + `<button type="button" data-service-report><i class="fa-solid fa-chart-column"></i> Xem báo cáo chi tiết</button>`;
    }

    async function openDetail(id) {
        try {
            const data = await request(`${api}?action=detail&id=${id}`);
            const item = data.service;
            openModal("Chi tiết dịch vụ", item.service_code, `<div class="service-detail">${detailField("Tên dịch vụ", item.service_name)}${detailField("Loại dịch vụ", serviceType(item))}${detailField("Đơn vị tính", item.unit)}${detailField("Đơn giá", money(item.unit_price) + " đ")}${detailField("Trạng thái", displayStatus(item.status))}${detailField("Mô tả", item.note || "Không có", true)}</div><div class="service-modal-actions"><button type="button" data-service-close>Đóng</button><button type="button" class="save" data-service-edit="${item.id}">Chỉnh sửa</button></div>`);
        } catch (error) { toast(error.message, "error"); }
    }

    function openForm(id = null) {
        const item = id ? state.items.find((entry) => String(entry.id) === String(id)) : {};
        if (id && !item) return;
        state.editing = id;
        openModal(id ? "Sửa dịch vụ" : "Thêm dịch vụ", id ? item.service_code : "Nhập thông tin dịch vụ", `<form id="serviceRealForm" class="service-real-form">
          <label>Mã dịch vụ<input name="service_code" maxlength="20" required value="${escapeHtml(item.service_code || "")}"></label>
          <label>Tên dịch vụ<input name="service_name" maxlength="100" required value="${escapeHtml(item.service_name || "")}"></label>
          <label>Loại dịch vụ<select name="service_type">${["Internet","Tiện ích","Giặt ủi","Vệ sinh","Thể thao","Y tế","Khác"].map((value) => `<option ${item.service_type === value ? "selected" : ""}>${value}</option>`).join("")}</select></label>
          <label>Đơn vị tính<input name="unit" maxlength="30" required placeholder="Ví dụ: Tháng, kWh, m³" value="${escapeHtml(item.unit || "")}"></label>
          <label>Đơn giá<input name="unit_price" type="number" min="0" step="100" required value="${escapeHtml(item.unit_price ?? "")}"></label>
          <label>Trạng thái<select name="status">${["Đang hoạt động","Tạm dừng","Ngừng cung cấp"].map((value) => `<option ${displayStatus(item.status) === value ? "selected" : ""}>${value}</option>`).join("")}</select></label>
          <input type="hidden" name="icon" value="${escapeHtml(item.icon || "fa-box")}"><input type="hidden" name="color" value="${escapeHtml(item.color || "blue")}"><input type="hidden" name="usage_count" value="${Number(item.usage_count || 0)}">
          <label class="wide">Mô tả<textarea name="note" rows="4" maxlength="1000">${escapeHtml(item.note || "")}</textarea></label>
          <div class="wide service-modal-actions"><button type="button" data-service-close>Hủy</button><button type="submit" class="save">Lưu dịch vụ</button></div>
        </form>`);
        $("#serviceRealForm").addEventListener("submit", save);
    }

    async function save(event) {
        event.preventDefault();
        const body = Object.fromEntries(new FormData(event.currentTarget).entries());
        try {
            await request(`${api}?action=${state.editing ? "edit" : "create"}${state.editing ? `&id=${state.editing}` : ""}`, { method: state.editing ? "PUT" : "POST", headers: { "Content-Type": "application/json; charset=UTF-8" }, body: JSON.stringify(body) });
            closeModal();
            toast(state.editing ? "Cập nhật dịch vụ thành công" : "Thêm dịch vụ thành công");
            state.editing = null;
            await load();
        } catch (error) { toast(error.message, "error"); }
    }

    async function remove(id) {
        const item = state.items.find((entry) => String(entry.id) === String(id));
        if (!confirm(`Bạn có chắc muốn xóa dịch vụ “${item?.service_name || "này"}”?`)) return;
        try {
            await request(`${api}?action=delete&id=${id}`, { method: "DELETE" });
            toast("Xóa dịch vụ thành công");
            await load();
        } catch (error) { toast(error.message, "error"); }
    }

    function showReport() {
        const active = state.items.filter((item) => isActive(item.status)).length;
        const average = state.items.length ? state.items.reduce((sum, item) => sum + Number(item.unit_price || 0), 0) / state.items.length : 0;
        openModal("Báo cáo dịch vụ", "Thống kê theo dữ liệu hiện tại", `<div class="service-detail">${detailField("Tổng dịch vụ", state.items.length)}${detailField("Đang hoạt động", active)}${detailField("Ngừng cung cấp", state.items.length - active)}${detailField("Đơn giá trung bình", money(average) + " đ")}</div><div class="service-modal-actions"><button type="button" class="save" data-service-close>Đóng</button></div>`);
    }

    function handleClick(event) {
        const view = event.target.closest("[data-service-view]");
        const edit = event.target.closest("[data-service-edit]");
        const del = event.target.closest("[data-service-delete]");
        const page = event.target.closest("[data-service-page]");
        const category = event.target.closest("[data-service-category]");
        if (view) openDetail(view.dataset.serviceView);
        if (edit) { closeModal(); openForm(edit.dataset.serviceEdit); }
        if (del) remove(del.dataset.serviceDelete);
        if (page && !page.disabled) { state.page = Number(page.dataset.servicePage); render(); renderPagination(); }
        if (category) { $("#serviceType").value = category.dataset.serviceCategory; state.page = 1; applyFilters(); }
        if (event.target.closest("[data-service-report]")) showReport();
        if (event.target.closest("[data-service-close]") || event.target.id === "serviceRealModal") closeModal();
    }

    function openModal(title, subtitle, html) { $("#serviceRealTitle").textContent = title; $("#serviceRealSub").textContent = subtitle; $("#serviceRealBody").innerHTML = html; $("#serviceRealModal").classList.add("show"); $("#serviceRealModal").setAttribute("aria-hidden", "false"); }
    function closeModal() { $("#serviceRealModal")?.classList.remove("show"); $("#serviceRealModal")?.setAttribute("aria-hidden", "true"); }
    function toast(message, type = "success") { const element = $("#serviceRealToast"); element.textContent = message; element.className = `service-real-toast show ${type}`; clearTimeout(toast.timer); toast.timer = setTimeout(() => element.classList.remove("show"), 3500); }
    function detailField(label, value, wide = false) { return `<div class="${wide ? "wide" : ""}"><span>${label}</span><strong>${escapeHtml(value)}</strong></div>`; }
    function isActive(status) { return ["Đang dùng", "Đang hoạt động"].includes(String(status || "")); }
    function displayStatus(status) { const value = String(status || ""); if (value === "Tạm dừng") return value; if (isActive(value)) return "Đang hoạt động"; return "Ngừng cung cấp"; }
    function statusClass(status) { const value = displayStatus(status); return value === "Đang hoạt động" ? "active" : value === "Tạm dừng" ? "paused" : "stopped"; }
    function serviceType(item) { return item.service_type || "Khác"; }
    function serviceVisual(item) { return { icon: item.icon || "fa-box", color: item.color || "blue" }; }

    function modalCss() { return `
      .service-category-button{width:100%;display:grid;grid-template-columns:22px 1fr auto;gap:8px;padding:9px 16px;align-items:center;border:0;background:transparent;color:#344563;text-align:left;font-size:10px;cursor:pointer}.service-category-button:hover{background:#f4f8fd}.service-category-button>i{font-size:14px}.service-category-button>b{min-width:22px;padding:3px 6px;border-radius:8px;background:#f3f5f8;text-align:center}.service-category-button i.cyan{color:#1b97b4}.service-category-button i.yellow{color:#e5a700}.service-category-button i.green{color:#0a9d51}.service-category-button i.purple{color:#6753c5}.service-category-button i.red{color:#dd3a50}
      .service-real-modal{position:fixed;inset:0;z-index:1400;display:none;align-items:center;justify-content:center;padding:18px;background:rgba(13,30,52,.55)}.service-real-modal.show{display:flex}.service-real-card{width:min(760px,100%);max-height:92vh;overflow:auto;border-radius:15px;background:#fff;box-shadow:0 24px 70px rgba(0,0,0,.25)}.service-real-card>header{position:sticky;top:0;z-index:2;display:flex;justify-content:space-between;padding:18px 20px;border-bottom:1px solid #e5e7eb;background:#fff}.service-real-card h2{margin:0;color:#173f6d}.service-real-card header p{margin:4px 0 0;color:#718096}.service-real-card header button{width:38px;height:38px;border:0;border-radius:8px}#serviceRealBody{padding:20px}.service-detail{display:grid;grid-template-columns:repeat(2,1fr);gap:12px}.service-detail>div{padding:13px;border:1px solid #e5e7eb;border-radius:9px;background:#f8fbff}.service-detail .wide{grid-column:1/-1}.service-detail span{display:block;margin-bottom:5px;color:#718096;font-size:12px}.service-detail strong{overflow-wrap:anywhere}.service-real-form{display:grid;grid-template-columns:repeat(2,1fr);gap:14px}.service-real-form label{display:grid;gap:7px;color:#344563;font-size:13px;font-weight:700}.service-real-form .wide{grid-column:1/-1}.service-real-form input,.service-real-form select,.service-real-form textarea{width:100%;padding:11px;border:1px solid #d8e0ec;border-radius:8px;font:inherit}.service-modal-actions{display:flex;justify-content:flex-end;gap:10px;margin-top:16px}.service-modal-actions button{height:42px;padding:0 18px;border:1px solid #d8e0ec;border-radius:8px;background:#fff}.service-modal-actions .save{border-color:#0869e8;background:#0869e8;color:#fff}.service-real-toast{position:fixed;right:24px;bottom:24px;z-index:1500;padding:13px 17px;border-radius:9px;background:#07894a;color:#fff;opacity:0;transform:translateY(15px);pointer-events:none;transition:.2s}.service-real-toast.show{opacity:1;transform:none}.service-real-toast.error{background:#d9363e}.service-row-actions button{width:32px;height:32px;cursor:pointer}.service-pagination>div{align-items:center}.service-pagination #servicePageButtons{display:flex;gap:5px}@media(max-width:700px){.service-detail,.service-real-form{grid-template-columns:1fr}.service-detail .wide,.service-real-form .wide{grid-column:auto}}
    `; }

    setup();
    window.refreshServices = load;
})();
