(() => {
    const context = window.APP_CONTEXT || "";
    const api = `${context}/api/reports`;
    const state = { range: "month", charts: [], loaded: false };
    const $ = (selector) => document.querySelector(selector);
    const escapeHtml = (value) => String(value ?? "").replace(/[&<>"']/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"}[c]));
    const money = (value) => Number(value || 0).toLocaleString("vi-VN") + "đ";
    const date = (value) => value ? new Date(value).toLocaleDateString("vi-VN") : "";

    function createView() {
        if ($("#reportsView")) return;
        const view = document.createElement("section");
        view.id = "reportsView";
        view.className = "view";
        view.innerHTML = `
          <div class="report-heading section-title"><div><h2>Báo cáo - Thống kê</h2><p>Trang chủ / Báo cáo / Thống kê tổng hợp</p></div>
            <div class="report-filter" id="reportFilter">
              <button data-report-range="today">Hôm nay</button><button data-report-range="7days">7 ngày</button>
              <button class="active" data-report-range="month">Tháng</button><button data-report-range="quarter">Quý</button><button data-report-range="year">Năm</button>
              <div class="report-custom"><input type="date" id="reportFrom" aria-label="Từ ngày"><span>đến</span><input type="date" id="reportTo" aria-label="Đến ngày"><button class="primary" id="reportApply">Áp dụng</button></div>
            </div></div>
          <div class="stats-grid report-stats" id="reportStats"><div class="report-loading">Đang tải số liệu...</div></div>
          <div class="report-charts">
            ${chartPanel("Doanh thu theo tháng","reportRevenueChart")}${chartPanel("Sinh viên theo tòa nhà","reportBuildingChart")}
            ${chartPanel("Tỷ lệ phòng","reportRoomChart")}${chartPanel("Tình trạng hóa đơn","reportInvoiceChart")}
          </div>
          <div class="report-tables" id="reportTables"></div>`;
        $(".main").appendChild(view);
        bind();
    }

    function chartPanel(title, id) { return `<article class="panel report-chart"><div class="panel-head"><h2>${title}</h2></div><canvas id="${id}"></canvas></article>`; }

    function bind() {
        $("#reportFilter").addEventListener("click", event => {
            const button = event.target.closest("[data-report-range]");
            if (!button) return;
            state.range = button.dataset.reportRange;
            document.querySelectorAll("[data-report-range]").forEach(item => item.classList.toggle("active", item === button));
            load();
        });
        $("#reportApply").addEventListener("click", () => {
            if (!$("#reportFrom").value || !$("#reportTo").value) return;
            state.range = "custom";
            document.querySelectorAll("[data-report-range]").forEach(item => item.classList.remove("active"));
            load();
        });
    }

    async function load() {
        $("#reportStats").innerHTML = `<div class="report-loading">Đang tải số liệu...</div>`;
        const params = new URLSearchParams({range: state.range});
        if (state.range === "custom") { params.set("from", $("#reportFrom").value); params.set("to", $("#reportTo").value); }
        try {
            const response = await fetch(`${api}?${params}`, {cache:"no-store"});
            const payload = await response.json();
            if (!response.ok) throw new Error(payload.message || "Không thể tải báo cáo");
            render(payload.data);
            state.loaded = true;
        } catch (error) {
            $("#reportStats").innerHTML = `<div class="report-loading report-error">${escapeHtml(error.message)}</div>`;
        }
    }

    function render(data) {
        const summary = data.summary || {};
        const cards = [
          ["Tổng sinh viên",summary.total_students,"fa-users","blue"],["Tổng phòng",summary.total_rooms,"fa-door-open","teal"],
          ["Phòng trống",summary.empty_rooms,"fa-house-circle-check","green"],["Phòng đã sử dụng",summary.used_rooms,"fa-bed","purple"],
          ["Tổng hóa đơn",summary.total_invoices,"fa-file-invoice","blue"],["Chưa thanh toán",summary.unpaid_invoices,"fa-clock","orange"],
          ["Doanh thu",money(summary.revenue),"fa-money-bill-trend-up","green"],["Tổng yêu cầu",summary.total_requests,"fa-comments","red"]
        ];
        $("#reportStats").innerHTML = cards.map(([label,value,icon,color]) => `<article class="stat-card"><i class="stat-icon ${color} fa-solid ${icon}"></i><div><span>${label}</span><strong>${escapeHtml(value ?? 0)}</strong></div></article>`).join("");
        renderCharts(data); renderTables(data);
    }

    function renderCharts(data) {
        state.charts.forEach(chart => chart.destroy()); state.charts = [];
        if (!window.Chart) throw new Error("Không tải được thư viện biểu đồ");
        state.charts.push(chart("reportRevenueChart","line",data.monthlyRevenue,"#0b72e7"));
        state.charts.push(chart("reportBuildingChart","bar",data.studentsByBuilding,"#17b26a"));
        state.charts.push(chart("reportRoomChart","doughnut",data.roomRatio,["#17b26a","#0b72e7","#f59e0b","#ef4444"]));
        state.charts.push(chart("reportInvoiceChart","doughnut",data.invoiceStatus,["#17b26a","#f59e0b","#ef4444","#7c3aed","#0b72e7"]));
    }

    function chart(id,type,items,color) {
        items = items || [];
        return new Chart($("#"+id),{type,data:{labels:items.map(i=>i.label),datasets:[{data:items.map(i=>Number(i.value||0)),backgroundColor:color,borderColor:color,borderWidth:type==="line"?3:1,tension:.35,fill:type==="line"}]},options:{responsive:true,maintainAspectRatio:false,plugins:{legend:{display:type==="doughnut",position:"bottom"}},scales:type==="doughnut"?{}:{y:{beginAtZero:true,grid:{color:"#edf1f7"}},x:{grid:{display:false}}}}});
    }

    function renderTables(data) {
        $("#reportTables").innerHTML = [
          table("Sinh viên mới",["Mã SV","Họ tên","Phòng","Ngày tạo"],data.newStudents,i=>[i.student_code,i.full_name,i.room_code,date(i.created_at)]),
          table("Phòng còn trống",["Phòng","Tòa nhà","Sức chứa","Còn chỗ"],data.availableRooms,i=>[i.room_code,i.building_name,i.capacity,i.available_slots]),
          table("Hóa đơn chưa thanh toán",["Mã HĐ","Sinh viên","Số tiền","Trạng thái"],data.unpaidInvoices,i=>[i.invoice_code,i.full_name,money(i.total_amount),i.status]),
          table("Yêu cầu chưa xử lý",["Mã YC","Sinh viên","Loại","Trạng thái"],data.pendingRequests,i=>[i.request_code,i.full_name,i.request_type,i.status])
        ].join("");
    }

    function table(title, headers, items, values) {
        const body=(items||[]).map(item=>`<tr>${values(item).map(value=>`<td>${escapeHtml(value)}</td>`).join("")}</tr>`).join("")||`<tr><td colspan="${headers.length}" class="empty">Chưa có dữ liệu</td></tr>`;
        return `<article class="table-card report-table"><div class="panel-head" style="padding:12px"><h2>${title}</h2></div><table><thead><tr>${headers.map(h=>`<th>${h}</th>`).join("")}</tr></thead><tbody>${body}</tbody></table></article>`;
    }

    window.enableReportSidebar = () => {
        const item = [...document.querySelectorAll("#sidebarContainer .menu-item")].find(el => el.textContent.includes("Báo cáo"));
        if (item) { item.dataset.view = "reports"; item.classList.remove("muted"); }
    };
    window.renderReportView = () => { createView(); load(); };
    document.addEventListener("DOMContentLoaded", createView);
})();
