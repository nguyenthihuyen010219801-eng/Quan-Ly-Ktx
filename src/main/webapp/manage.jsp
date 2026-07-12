<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<%
    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);
    String contextPath = request.getContextPath();
    String sessionRole = String.valueOf(session.getAttribute("accountRole"));
    String sessionName = String.valueOf(session.getAttribute("accountFullName"));
    String sessionUsername = String.valueOf(session.getAttribute("accountUsername"));
    String initialView = request.getParameter("initialView");
    if (initialView == null || !java.util.Set.of(
            "dashboard", "students", "buildings", "rooms", "invoices",
            "services", "requests", "finance", "reports", "accounts", "settings").contains(initialView)) {
        initialView = "";
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý ký túc xá</title>
    <link rel="stylesheet" href="<%= contextPath %>/frontend/sidebar.css?v=5">
    <script src="<%= contextPath %>/frontend/sidebar.js?v=5" defer></script>
    <link rel="stylesheet" href="<%= contextPath %>/frontend/styleManage.css?v=5">
    <link rel="stylesheet" href="<%= contextPath %>/frontend/report.css">
    <link rel="stylesheet" href="<%= contextPath %>/frontend/account.css">
    <link rel="stylesheet" href="<%= contextPath %>/frontend/settings.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
    <div id="sidebarContainer"></div>

    <main class="main">
        <header class="topbar">
            <div class="topbar-left">
                <h1 class="page-title" id="pageTitle">Tổng quan</h1>
            </div>
            <div class="topbar-right">
                <button class="notification-button top-icon" type="button" aria-label="Thông báo" title="Thông báo">
                    <i class="fa-regular fa-bell"></i>
                </button>
                <div class="today">
                    <i class="fa-regular fa-calendar"></i>
                    <span id="todayText"></span>
                </div>
            </div>
        </header>

        <section id="dashboardView" class="view active">
            <div class="stats-grid" id="statsGrid"></div>

            <div class="dashboard-charts">
                <article class="panel chart-panel">
                    <div class="panel-head">
                        <h2>Số lượng sinh viên theo tòa nhà</h2>
                    </div>
                    <div id="buildingChart" class="donut-layout"></div>
                </article>

                <article class="panel chart-panel" id="revenuePanel">
                    <div class="panel-head">
                        <h2 id="revenueTitle">Doanh thu theo tuần</h2>
                        <div class="chart-tabs" id="revenueTabs">
                            <button type="button" class="active" data-range="week">Tuần</button>
                            <button type="button" data-range="month">Tháng</button>
                            <button type="button" data-range="year">Năm</button>
                        </div>
                    </div>
                    <div id="revenueChart" class="line-chart"></div>
                </article>

                <article class="panel chart-panel">
                    <div class="panel-head">
                        <h2>Tình trạng phòng</h2>
                    </div>
                    <div id="roomChart" class="donut-layout"></div>
                </article>
            </div>

            <div class="dashboard-lists">
                <article class="panel">
                    <div class="panel-head">
                        <h2>Sinh viên mới đăng ký</h2>
                        <button class="link-btn" type="button" data-go-students>Xem tất cả</button>
                    </div>
                    <div id="recentStudents" class="compact-list"></div>
                </article>

                <article class="panel">
                    <div class="panel-head">
                        <h2>Hóa đơn gần đây</h2>
                        <button class="link-btn" type="button">Xem tất cả</button>
                    </div>
                    <div id="recentInvoices" class="compact-list"></div>
                </article>

                <article class="panel">
                    <div class="panel-head">
                        <h2>Yêu cầu sửa chữa mới</h2>
                        <button class="link-btn" type="button">Xem tất cả</button>
                    </div>
                    <div id="recentRepairs" class="compact-list"></div>
                </article>

                <article class="panel">
                    <div class="panel-head">
                        <h2>Phản hồi mới nhất</h2>
                        <button class="link-btn" type="button">Xem tất cả</button>
                    </div>
                    <div id="recentFeedback" class="compact-list"></div>
                </article>
            </div>
        </section>

        <section id="studentsView" class="view">
            <form id="filterForm" class="filter-card">
                <label>
                    Tìm kiếm
                    <span>
                        <input id="searchInput" type="search" placeholder="Nhập mã SV, họ tên, SĐT, email...">
                        <i class="fa-solid fa-magnifying-glass"></i>
                    </span>
                </label>
                <label>
                    Tòa nhà
                    <select id="buildingFilter">
                        <option value="">Tất cả</option>
                    </select>
                </label>
                <label>
                    Phòng
                    <select id="roomFilter">
                        <option value="">Tất cả phòng</option>
                    </select>
                </label>
                <label>
                    Giới tính
                    <select id="genderFilter">
                        <option value="">Tất cả</option>
                        <option value="Nam">Nam</option>
                        <option value="Nữ">Nữ</option>
                    </select>
                </label>
                <label>
                    Trạng thái
                    <select id="statusFilter">
                        <option value="">Tất cả</option>
                        <option value="Đang ở">Đang ở</option>
                        <option value="Chờ duyệt">Chờ duyệt</option>
                        <option value="Đã trả phòng">Đã trả phòng</option>
                    </select>
                </label>
                <button class="primary" type="submit">
                    <i class="fa-solid fa-magnifying-glass"></i>
                    Tìm kiếm
                </button>
                <button class="secondary" type="button" id="resetFilters">
                    <i class="fa-solid fa-rotate-right"></i>
                    Đặt lại
                </button>
            </form>

            <div class="table-toolbar">
                <button class="success" type="button" data-add-student>
                    <i class="fa-solid fa-plus"></i>
                    Thêm sinh viên
                </button>
            </div>

            <div class="table-card">
                <table>
                    <thead>
                        <tr>
                            <th>STT</th>
                            <th>Mã SV</th>
                            <th>Họ tên</th>
                            <th>Giới tính</th>
                            <th>Ngày sinh</th>
                            <th>SĐT</th>
                            <th>Email</th>
                            <th>Khoa</th>
                            <th>Lớp</th>
                            <th>Phòng</th>
                            <th>Tòa</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody id="studentsBody"></tbody>
                </table>

                <div class="pagination-row">
                    <div class="pagination" id="pagination"></div>
                </div>
            </div>
        </section>
    </main>

    <div class="modal" id="studentModal" aria-hidden="true">
        <div class="modal-card">
            <div class="modal-head">
                <div>
                    <h2 id="modalTitle">Thêm sinh viên</h2>
                    <p id="modalSub">Nhập đầy đủ thông tin sinh viên</p>
                </div>
                <button class="icon-btn close-modal" type="button" aria-label="Đóng">
                    <i class="fa-solid fa-xmark"></i>
                </button>
            </div>

            <form id="studentForm" class="student-form">
                <input type="hidden" id="studentId">
                <label>Mã sinh viên<input id="studentCode" required></label>
                <label>Họ tên<input id="fullName" required></label>
                <label>Giới tính
                    <select id="gender" required>
                        <option value="Nam">Nam</option>
                        <option value="Nữ">Nữ</option>
                    </select>
                </label>
                <label>Ngày sinh<input id="birthday" type="date"></label>
                <label>SĐT<input id="phone"></label>
                <label>Email<input id="email" type="email"></label>
                <label>CCCD<input id="citizenId"></label>
                <label>Khoa<input id="faculty"></label>
                <label>Ngành<input id="major"></label>
                <label>Lớp<input id="className"></label>
                <label>Niên khóa<input id="schoolYear"></label>
                <label>Phòng
                    <select id="roomId">
                        <option value="">Chưa chọn phòng</option>
                    </select>
                </label>
                <label>Ngày nhận phòng<input id="checkinDate" type="date"></label>
                <label>Ngày trả phòng<input id="checkoutDate" type="date"></label>
                <label>Trạng thái
                    <select id="status">
                        <option value="Đang ở">Đang ở</option>
                        <option value="Chờ duyệt">Chờ duyệt</option>
                        <option value="Đã trả phòng">Đã trả phòng</option>
                    </select>
                </label>
                <label>Phụ huynh<input id="parentName"></label>
                <label>SĐT phụ huynh<input id="parentPhone"></label>
                <label class="span-3">Địa chỉ<textarea id="address" rows="3"></textarea></label>

                <div class="form-actions span-3">
                    <button class="secondary" type="button" id="resetStudentForm">Làm lại</button>
                    <button class="primary" type="submit">Lưu sinh viên</button>
                </div>
            </form>

            <div id="studentDetail" class="student-detail" hidden></div>
        </div>
    </div>

    <script>
        const API = "<%= contextPath %>";
        const state = {
            view: "dashboard",
            page: 1,
            limit: 5,
            total: 0,
            students: [],
            rooms: [],
            buildings: [],
            mode: "add",
            editingStudent: null,
            saving: false
        };

        const $ = (selector) => document.querySelector(selector);
        const $$ = (selector) => document.querySelectorAll(selector);

        function escapeHtml(value) {
            return String(value ?? "").replace(/[&<>"']/g, (char) => ({
                "&": "&amp;",
                "<": "&lt;",
                ">": "&gt;",
                '"': "&quot;",
                "'": "&#039;"
            }[char]));
        }

        async function loadSidebar(user) {
            await window.loadSharedSidebar({
                setView,
                user,
                sidebarUrl: "<%= contextPath %>/frontend/sidebar.jsp"
            });
            window.enableReportSidebar?.();
            window.enableAccountSidebar?.();
            window.enableSettingsSidebar?.();
            window.setSidebarActive?.(state.view);
        }

        function formatDate(value) {
            if (!value) return "";
            const date = new Date(value);
            if (Number.isNaN(date.getTime())) return value;
            return date.toLocaleDateString("vi-VN");
        }

        function toInputDate(value) {
            if (!value) return "";
            const date = new Date(value);
            if (Number.isNaN(date.getTime())) return "";
            return date.toISOString().slice(0, 10);
        }

        function money(value) {
            return Number(value || 0).toLocaleString("vi-VN") + "đ";
        }

        function setView(view) {
            const managementViews = ["buildings", "rooms", "services", "invoices", "requests", "finance", "reports", "accounts", "settings"];
            const titles = {
                dashboard: "Tổng quan",
                students: "Quản lý sinh viên",
                buildings: "Quản lý tòa nhà",
                rooms: "Quản lý phòng",
                services: "Quản lý dịch vụ",
                invoices: "Quản lý hóa đơn",
                requests: "Yêu cầu - Phản hồi",
                finance: "Quản lý thu chi",
                reports: "Báo cáo - Thống kê",
                accounts: "Quản lý tài khoản",
                settings: "Cài đặt hệ thống"
            };
            if (!titles[view]) view = "dashboard";
            state.view = view;
            window.setSidebarActive?.(view);
            $("#dashboardView").classList.toggle("active", view === "dashboard");
            $("#studentsView").classList.toggle("active", view === "students");
            document.querySelector("#managementDetailView")?.classList.remove("active");
            managementViews.forEach((name) => document.querySelector(`#${name}View`)?.classList.toggle("active", view === name));
            $("#pageTitle").textContent = titles[view];
            if (view === "dashboard") loadDashboard();
            if (view === "students") loadStudents();
            if (["buildings", "rooms"].includes(view)) window.renderManagementView?.(view);
            if (view === "services") window.renderServiceView?.();
            if (view === "invoices") window.renderInvoiceView?.();
            if (view === "requests") window.renderRequestView?.();
            if (view === "finance") window.renderFinanceView?.();
            if (view === "reports") window.renderReportView?.();
            if (view === "accounts") window.renderAccountView?.();
            if (view === "settings") window.renderSettingsView?.();
        }

        async function requestJson(url, options) {
            const res = await fetch(url, {
                cache: "no-store",
                ...(options || {})
            });
            const data = await res.json().catch(() => ({}));
            if (!res.ok) throw new Error(data.message || "Không tải được dữ liệu");
            return data;
        }

        async function loadMeta() {
            const data = await requestJson(`${API}/api/manage/meta`);
            state.rooms = data.rooms || [];
            state.buildings = data.buildings || [];

            $("#buildingFilter").innerHTML = `<option value="">Tất cả</option>` + state.buildings.map((item) =>
                `<option value="${item.id}">${escapeHtml(item.building_name)}</option>`
            ).join("");

            const roomOptions = state.rooms.map((item) =>
                `<option value="${item.id}">${escapeHtml(item.room_code)} - ${escapeHtml(item.building_name || "Chưa có tòa")}</option>`
            ).join("");

            $("#roomFilter").innerHTML = `<option value="">Tất cả phòng</option>` + roomOptions;
            $("#roomId").innerHTML = `<option value="">Chưa chọn phòng</option>` + roomOptions;
        }

        async function loadDashboard() {
            try {
                const data = await requestJson(`${API}/api/manage/dashboard`);
                state.dashboard = data;
                renderStats(data.stats || {});
                renderDonut("#buildingChart", data.byBuilding || [], "building_name", "total");
                renderRevenueChart("week");
                renderDonut("#roomChart", data.roomStatus || [], "status", "total");
                renderRecent(data.recentStudents || []);
                renderInvoices(data.recentInvoices || []);
                renderRepairs(data.recentRepairs || []);
                renderFeedback(data.recentFeedback || []);
            } catch (err) {
                $("#statsGrid").innerHTML = `<p class="empty">${escapeHtml(err.message)}</p>`;
            }
        }

        function renderStats(stats) {
            const cards = [
                ["Tổng sinh viên", stats.totalStudents || 0, "fa-users", "blue"],
                ["Phòng đang sử dụng", stats.usedRooms || 0, "fa-building", "green"],
                ["Phòng còn trống", stats.emptyRooms || 0, "fa-door-open", "orange"],
                ["Sinh viên chờ duyệt", stats.pendingStudents || 0, "fa-user-clock", "purple"],
                ["Hóa đơn chưa thanh toán", stats.unpaidInvoices || 0, "fa-file-invoice-dollar", "red"],
                ["Doanh thu tháng này", money(stats.revenueThisMonth || 0), "fa-circle-dollar-to-slot", "blue"],
                ["Yêu cầu / Phản hồi mới", stats.newFeedback || 0, "fa-comment-dots", "teal"],
                ["Hồ sơ chưa hoàn tất", stats.incompleteProfiles || 0, "fa-file", "blue"],
                ["Phòng đang bảo trì", stats.maintenanceRooms || 0, "fa-screwdriver-wrench", "green"]
            ];

            if (window.CURRENT_USER?.role !== "quanly") {
                const sensitive = cards.findIndex((card) => card[0] === "Doanh thu tháng này");
                if (sensitive >= 0) cards.splice(sensitive, 1);
            }

            $("#statsGrid").innerHTML = cards.map(([label, value, icon, color]) => `
                <article class="stat-card">
                    <div class="stat-icon ${color}"><i class="fa-solid ${icon}"></i></div>
                    <div>
                        <span>${label}</span>
                        <strong>${value}</strong>
                    </div>
                </article>
            `).join("");
        }

        function renderDonut(selector, items, labelKey, valueKey) {
            const colors = ["#2f73e8", "#29c789", "#f8b51d", "#8b5cf6", "#f05c5c"];
            const total = items.reduce((sum, item) => sum + Number(item[valueKey] || 0), 0);

            if (!total) {
                $(selector).innerHTML = `<p class="empty">Chưa có dữ liệu</p>`;
                return;
            }

            let current = 0;
            const segments = items.map((item, index) => {
                const value = Number(item[valueKey] || 0);
                const start = current;
                const end = current + (value / total) * 100;
                current = end;
                return `${colors[index % colors.length]} ${start}% ${end}%`;
            }).join(", ");

            $(selector).innerHTML = `
                <div class="donut" style="background:conic-gradient(${segments})">
                    <span></span>
                </div>
                <div class="donut-legend">
                    ${items.map((item, index) => {
                        const value = Number(item[valueKey] || 0);
                        const percent = Math.round((value / total) * 1000) / 10;
                        return `
                            <div>
                                <i style="background:${colors[index % colors.length]}"></i>
                                <span>${escapeHtml(item[labelKey] || "Chưa rõ")} (${value})</span>
                                <strong>${percent}%</strong>
                            </div>
                        `;
                    }).join("")}
                </div>
            `;
        }

        function vndLabel(value) {
            return `${Math.round(Number(value || 0)).toLocaleString("vi-VN")} VND`;
        }

        function revenueSeries(range) {
            const data = state.dashboard || {};
            const stats = data.stats || {};
            const revenue = Number(stats.revenueThisMonth || 0);
            const safeBase = Math.max(revenue || 6600000, 1000000);

            if (range === "month") {
                const byMonth = Array.from({ length: 12 }, (_, index) => ({ label: `T${index + 1}`, value: 0 }));
                (data.monthlyRevenue || []).forEach((item) => {
                    const index = Number(item.month) - 1;
                    if (index >= 0 && index < byMonth.length) byMonth[index].value = Number(item.revenue || 0);
                });
                if (byMonth.every((item) => item.value === 0)) {
                    [0.55, 0.65, 0.72, 0.68, 0.8, 0.74, 0.88, 0.83, 0.92, 0.86, 0.95, 1].forEach((factor, index) => {
                        byMonth[index].value = safeBase * factor;
                    });
                }
                return byMonth;
            }

            if (range === "year") {
                const year = new Date().getFullYear();
                return [4, 3, 2, 1, 0].map((offset, index) => ({
                    label: String(year - offset),
                    value: safeBase * ([0.42, 0.58, 0.76, 0.88, 1][index])
                }));
            }

            return ["T2", "T3", "T4", "T5", "T6", "T7", "CN"].map((label, index) => ({
                label,
                value: (safeBase / 7) * ([0.72, 0.86, 0.78, 1, 0.9, 0.82, 0.94][index])
            }));
        }

        function renderRevenueChart(range) {
            const titles = {
                week: "Doanh thu theo tuần",
                month: "Doanh thu theo tháng",
                year: "Doanh thu theo năm"
            };
            const items = revenueSeries(range);
            const width = 760;
            const height = 190;
            const left = 124;
            const right = 22;
            const top = 4;
            const bottom = 44;
            const chartWidth = width - left - right;
            const chartHeight = height - top - bottom;
            const maxValue = Math.max(...items.map((item) => item.value), 1);
            const yMax = maxValue;
            const points = items.map((item, index) => {
                const x = left + (chartWidth / Math.max(items.length - 1, 1)) * index;
                const y = top + chartHeight - (item.value / yMax) * chartHeight;
                return { ...item, x, y };
            });
            const grid = Array.from({ length: 5 }, (_, index) => {
                const y = top + (chartHeight / 4) * index;
                const value = yMax - (yMax / 4) * index;
                return { y, value };
            });

            $("#revenueTitle").textContent = titles[range];
            $("#revenueChart").innerHTML = `
                <svg viewBox="0 0 ${width} ${height}" aria-label="${titles[range]}">
                    <g class="grid-lines">
                        ${grid.map((line) => `
                            <line x1="${left}" y1="${line.y}" x2="${width - right}" y2="${line.y}"></line>
                            <text x="${left - 16}" y="${line.y + 4}">${vndLabel(line.value)}</text>
                        `).join("")}
                    </g>
                    <polyline points="${points.map((point) => `${point.x},${point.y}`).join(" ")}"></polyline>
                    ${points.map((point) => `<circle cx="${point.x}" cy="${point.y}" r="6"></circle>`).join("")}
                    <g class="x-labels">
                        ${points.map((point) => `<text x="${point.x}" y="${height - 12}">${point.label}</text>`).join("")}
                    </g>
                </svg>
            `;
        }

        function renderRecent(items) {
            $("#recentStudents").innerHTML = items.map((student) => `
                <button type="button" data-view-student="${student.id}">
                    <div class="avatar small">${escapeHtml(initials(student.full_name))}</div>
                    <span>
                        <strong>${escapeHtml(student.full_name)}</strong>
                        <small>${escapeHtml(student.student_code)} - ${escapeHtml(student.room_code || "Chưa xếp phòng")}</small>
                    </span>
                    <time>${formatDate(student.created_at)}</time>
                </button>
            `).join("") || `<p class="empty">Chưa có sinh viên</p>`;
        }

        function renderInvoices(items) {
            $("#recentInvoices").innerHTML = items.map((item) => `
                <button type="button" data-view-student="${item.id}">
                    <i class="list-icon fa-regular fa-file-lines"></i>
                    <span>
                        <strong>HD${String(item.id).padStart(6, "0")}</strong>
                        <small>${escapeHtml(item.room_code || "Chưa phòng")} - ${escapeHtml(item.full_name)}</small>
                    </span>
                    <em>${money(item.amount)}</em>
                </button>
            `).join("") || `<p class="empty">Chưa có hóa đơn</p>`;
        }

        function renderRepairs(items) {
            $("#recentRepairs").innerHTML = items.map((item) => `
                <button type="button">
                    <i class="list-icon fa-solid fa-screwdriver-wrench"></i>
                    <span>
                        <strong>${escapeHtml(item.room_code || "Phòng chưa rõ")} - ${escapeHtml(item.building_name || "")}</strong>
                        <small>${escapeHtml(item.note)}</small>
                    </span>
                    <time>${escapeHtml(item.status || "Mới")}</time>
                </button>
            `).join("") || `<p class="empty">Chưa có yêu cầu sửa chữa</p>`;
        }

        function renderFeedback(items) {
            $("#recentFeedback").innerHTML = items.map((item) => `
                <button type="button" data-view-student="${item.id}">
                    <i class="list-icon fa-regular fa-comment-dots"></i>
                    <span>
                        <strong>${escapeHtml(item.full_name)} - ${escapeHtml(item.room_code || "Chưa phòng")}</strong>
                        <small>${escapeHtml(item.message)}</small>
                    </span>
                    <time>${formatDate(item.created_at)}</time>
                </button>
            `).join("") || `<p class="empty">Chưa có phản hồi</p>`;
        }

        function collectFilters() {
            return new URLSearchParams({
                page: state.page,
                limit: state.limit,
                search: $("#searchInput").value.trim(),
                building_id: $("#buildingFilter").value,
                room_id: $("#roomFilter").value,
                gender: $("#genderFilter").value,
                status: $("#statusFilter").value
            });
        }

        async function loadStudents() {
            try {
                const params = collectFilters();
                params.set("_", Date.now());
                const data = await requestJson(`${API}/api/manage/students?${params}`);
                state.students = data.students || [];
                state.total = data.total || 0;
                renderStudents();
                renderPagination();
            } catch (err) {
                $("#studentsBody").innerHTML = `<tr><td colspan="13" class="empty">${escapeHtml(err.message)}</td></tr>`;
            }
        }

        function renderStudents() {
            const start = (state.page - 1) * state.limit;
            $("#studentsBody").innerHTML = state.students.map((student, index) => `
                <tr data-row-student="${student.id}">
                    <td>${start + index + 1}</td>
                    <td><a>${escapeHtml(student.student_code)}</a></td>
                    <td><strong>${escapeHtml(student.full_name)}</strong></td>
                    <td>${escapeHtml(student.gender || "")}</td>
                    <td>${formatDate(student.birthday)}</td>
                    <td>${escapeHtml(student.phone || "")}</td>
                    <td>${escapeHtml(student.email || "")}</td>
                    <td>${escapeHtml(student.faculty || "")}</td>
                    <td>${escapeHtml(student.class_name || "")}</td>
                    <td>${escapeHtml(student.room_code || "")}</td>
                    <td>${escapeHtml(student.building_name || "")}</td>
                    <td><span class="badge ${statusClass(student.status)}">${escapeHtml(student.status || "")}</span></td>
                    <td>
                        <div class="row-actions">
                            <button type="button" title="Xem" data-view-student="${student.id}"><i class="fa-regular fa-eye"></i></button>
                            <button type="button" title="Sửa" data-edit-student="${student.id}"><i class="fa-regular fa-pen-to-square"></i></button>
                            ${window.CURRENT_USER?.role === "quanly" ? `<button type="button" title="Xóa" data-delete-student="${student.id}"><i class="fa-regular fa-trash-can"></i></button>` : ""}
                        </div>
                    </td>
                </tr>
            `).join("") || `<tr><td colspan="13" class="empty">Không tìm thấy sinh viên</td></tr>`;

        }

        function statusClass(status) {
            if (status === "Đang ở") return "active";
            if (status === "Chờ duyệt") return "pending";
            return "danger";
        }

        function renderPagination() {
            const pages = Math.max(Math.ceil(state.total / state.limit), 1);
            const buttons = [];
            buttons.push(`<button type="button" ${state.page === 1 ? "disabled" : ""} data-page-action="prev">&lt;</button>`);
            for (let page = 1; page <= pages; page++) {
                buttons.push(`<button type="button" class="${page === state.page ? "active" : ""}" data-page="${page}">${page}</button>`);
            }
            buttons.push(`<button type="button" ${state.page === pages ? "disabled" : ""} data-page-action="next">&gt;</button>`);
            $("#pagination").innerHTML = buttons.join("");
        }

        async function getStudent(id) {
            return requestJson(`${API}/api/manage/students/${id}?_=${Date.now()}`);
        }

        async function openDetail(id) {
            const student = await getStudent(id);
            state.mode = "view";
            $("#modalTitle").textContent = "Thông tin sinh viên";
            $("#modalSub").textContent = `${student.student_code} - ${student.full_name}`;
            $("#studentForm").hidden = true;
            $("#studentDetail").hidden = false;
            $("#studentDetail").innerHTML = `
                <div class="profile-head">
                    <div class="avatar">${escapeHtml(initials(student.full_name))}</div>
                    <div>
                        <h3>${escapeHtml(student.full_name)}</h3>
                        <p>${escapeHtml(student.student_code)} - ${escapeHtml(student.status || "")}</p>
                    </div>
                </div>
                <div class="detail-grid">
                    ${detailItem("Giới tính", student.gender)}
                    ${detailItem("Ngày sinh", formatDate(student.birthday))}
                    ${detailItem("SĐT", student.phone)}
                    ${detailItem("Email", student.email)}
                    ${detailItem("CCCD", student.citizen_id)}
                    ${detailItem("Khoa", student.faculty)}
                    ${detailItem("Ngành", student.major)}
                    ${detailItem("Lớp", student.class_name)}
                    ${detailItem("Niên khóa", student.school_year)}
                    ${detailItem("Phòng", student.room_code)}
                    ${detailItem("Tòa nhà", student.building_name)}
                    ${detailItem("Ngày nhận phòng", formatDate(student.checkin_date))}
                    ${detailItem("Ngày trả phòng", formatDate(student.checkout_date))}
                    ${detailItem("Phụ huynh", student.parent_name)}
                    ${detailItem("SĐT phụ huynh", student.parent_phone)}
                    ${detailItem("Địa chỉ", student.address, true)}
                </div>
            `;
            openModal();
        }

        function detailItem(label, value, wide = false) {
            return `<div class="${wide ? "wide" : ""}"><span>${label}</span><strong>${escapeHtml(value || "Chưa cập nhật")}</strong></div>`;
        }

        function initials(name) {
            return (name || "SV").split(" ").slice(-2).map((word) => word[0]).join("").toUpperCase();
        }

        async function openForm(mode, id = null) {
            state.mode = mode;
            state.editingStudent = null;
            $("#studentCode").readOnly = false;
            $("#studentForm").hidden = false;
            $("#studentDetail").hidden = true;
            $("#modalTitle").textContent = mode === "add" ? "Thêm sinh viên" : "Sửa sinh viên";
            $("#modalSub").textContent = mode === "add" ? "Nhập thông tin và bấm Lưu sinh viên" : "Cập nhật thông tin sinh viên";
            $("#studentForm").reset();
            $("#studentId").value = "";
            $("#status").value = "Chờ duyệt";

            if (mode === "edit" && id) {
                const student = await getStudent(id);
                state.editingStudent = student;
                fillForm(student);
                $("#studentCode").readOnly = true;
            }
            openModal();
        }

        function fillForm(student) {
            $("#studentId").value = student.id || "";
            $("#studentCode").value = student.student_code || "";
            $("#fullName").value = student.full_name || "";
            $("#gender").value = student.gender || "Nam";
            $("#birthday").value = toInputDate(student.birthday);
            $("#phone").value = student.phone || "";
            $("#email").value = student.email || "";
            $("#citizenId").value = student.citizen_id || "";
            $("#faculty").value = student.faculty || "";
            $("#major").value = student.major || "";
            $("#className").value = student.class_name || "";
            $("#schoolYear").value = student.school_year || "";
            $("#roomId").value = student.room_id || "";
            $("#checkinDate").value = toInputDate(student.checkin_date);
            $("#checkoutDate").value = toInputDate(student.checkout_date);
            $("#status").value = student.status || "Chờ duyệt";
            $("#parentName").value = student.parent_name || "";
            $("#parentPhone").value = student.parent_phone || "";
            $("#address").value = student.address || "";
        }

        function collectStudentForm() {
            const studentCode = state.mode === "edit" && state.editingStudent
                ? state.editingStudent.student_code
                : $("#studentCode").value.trim();

            return {
                student_code: studentCode,
                full_name: $("#fullName").value.trim(),
                gender: $("#gender").value,
                birthday: $("#birthday").value || null,
                phone: $("#phone").value.trim(),
                email: $("#email").value.trim(),
                citizen_id: $("#citizenId").value.trim(),
                address: $("#address").value.trim(),
                parent_name: $("#parentName").value.trim(),
                parent_phone: $("#parentPhone").value.trim(),
                faculty: $("#faculty").value.trim(),
                major: $("#major").value.trim(),
                class_name: $("#className").value.trim(),
                school_year: $("#schoolYear").value.trim(),
                room_id: $("#roomId").value || null,
                checkin_date: $("#checkinDate").value || null,
                checkout_date: $("#checkoutDate").value || null,
                status: $("#status").value
            };
        }

        function openModal() {
            $("#studentModal").classList.add("show");
            $("#studentModal").setAttribute("aria-hidden", "false");
        }

        function closeModal() {
            $("#studentModal").classList.remove("show");
            $("#studentModal").setAttribute("aria-hidden", "true");
            state.saving = false;
        }

        async function saveStudent(event) {
            event.preventDefault();
            if (state.saving) return;

            const id = $("#studentId").value;
            if (state.mode === "edit" && !id) {
                alert("Không tìm thấy sinh viên đang sửa. Em đóng form rồi bấm Sửa lại giúp anh nhé.");
                return;
            }

            const method = id ? "PUT" : "POST";
            const url = id ? `${API}/api/manage/students/${id}` : `${API}/api/manage/students`;
            const submitButton = event.submitter || $("#studentForm button[type='submit']");

            try {
                state.saving = true;
                if (submitButton) {
                    submitButton.disabled = true;
                    submitButton.textContent = "Đang lưu...";
                }

                const data = await requestJson(url, {
                    method,
                    headers: {"Content-Type": "application/json; charset=UTF-8"},
                    body: JSON.stringify(collectStudentForm())
                });

                if (!data.success) {
                    alert(data.message || "Không lưu được sinh viên");
                    return;
                }

                if (id) {
                    const savedStudent = await getStudent(id);
                    const index = state.students.findIndex((student) => String(student.id) === String(id));
                    if (index >= 0) {
                        state.students[index] = savedStudent;
                        renderStudents();
                    }
                    state.editingStudent = savedStudent;
                }

                closeModal();
                await loadMeta();
                await loadStudents();
                await loadDashboard();
                alert(data.message || "Lưu sinh viên thành công");
            } catch (err) {
                alert(err.message || "Không lưu được sinh viên");
            } finally {
                state.saving = false;
                if (submitButton) {
                    submitButton.disabled = false;
                    submitButton.textContent = "Lưu sinh viên";
                }
            }
        }

        async function deleteStudent(id) {
            if (!confirm("Em chắc chắn muốn xóa sinh viên này?")) return;
            const data = await requestJson(`${API}/api/manage/students/${id}`, { method: "DELETE" });
            if (!data.success) {
                alert(data.message || "Không xóa được sinh viên");
                return;
            }
            await loadStudents();
            await loadDashboard();
        }

        document.addEventListener("click", (event) => {
            if (event.target.closest("[data-go-students]")) setView("students");
            const rangeBtn = event.target.closest("#revenueTabs [data-range]");
            if (rangeBtn) {
                $$("#revenueTabs [data-range]").forEach((button) => button.classList.toggle("active", button === rangeBtn));
                renderRevenueChart(rangeBtn.dataset.range);
            }
            if (event.target.closest("[data-add-student]")) openForm("add");
            if (event.target.closest(".close-modal") || event.target.id === "studentModal") closeModal();

            const viewBtn = event.target.closest("[data-view-student]");
            if (viewBtn) {
                event.stopPropagation();
                openDetail(viewBtn.dataset.viewStudent);
            }

            const editBtn = event.target.closest("[data-edit-student]");
            if (editBtn) {
                event.stopPropagation();
                openForm("edit", editBtn.dataset.editStudent);
            }

            const deleteBtn = event.target.closest("[data-delete-student]");
            if (deleteBtn) {
                event.stopPropagation();
                deleteStudent(deleteBtn.dataset.deleteStudent);
            }

            const row = event.target.closest("[data-row-student]");
            if (row && !event.target.closest("button")) openDetail(row.dataset.rowStudent);

            const pageBtn = event.target.closest("[data-page]");
            if (pageBtn) {
                state.page = Number(pageBtn.dataset.page);
                loadStudents();
            }

            const pageAction = event.target.closest("[data-page-action]");
            if (pageAction) {
                const pages = Math.max(Math.ceil(state.total / state.limit), 1);
                if (pageAction.dataset.pageAction === "prev") state.page = Math.max(1, state.page - 1);
                if (pageAction.dataset.pageAction === "next") state.page = Math.min(pages, state.page + 1);
                loadStudents();
            }
        });

        function updateClock() {
            const now = new Date();
            $("#todayText").textContent = now.toLocaleString("vi-VN", {
                timeZone: "Asia/Ho_Chi_Minh",
                weekday: "long",
                day: "2-digit",
                month: "2-digit",
                year: "numeric",
                hour: "2-digit",
                minute: "2-digit",
                second: "2-digit"
            });
        }

        $("#filterForm").addEventListener("submit", (event) => {
            event.preventDefault();
            state.page = 1;
            loadStudents();
        });

        $("#resetFilters").addEventListener("click", () => {
            $("#filterForm").reset();
            state.page = 1;
            loadStudents();
        });

        $("#studentForm").addEventListener("submit", saveStudent);
        $("#resetStudentForm").addEventListener("click", () => {
            if (state.mode === "edit" && state.editingStudent) {
                fillForm(state.editingStudent);
                return;
            }

            $("#studentForm").reset();
            $("#studentId").value = "";
            $("#studentCode").readOnly = false;
            $("#status").value = "Chờ duyệt";
        });
        document.addEventListener("DOMContentLoaded", async () => {
            const user = window.CURRENT_USER;
            await loadSidebar(user);
            if (user?.role !== "quanly") document.getElementById("revenuePanel")?.remove();
            updateClock();
            setInterval(updateClock, 1000);
            await loadMeta();
            await loadDashboard();
            setView(window.INITIAL_MANAGEMENT_VIEW || "dashboard");
        });
    </script>
    <script>
        window.APP_CONTEXT = "<%= contextPath %>";
        window.INITIAL_MANAGEMENT_VIEW = "<%= initialView %>";
        window.CURRENT_USER = {username: "<%= sessionUsername.replace("\\", "\\\\").replace("\"", "\\\"") %>", full_name: "<%= sessionName.replace("\\", "\\\\").replace("\"", "\\\"") %>", role: "<%= sessionRole %>"};
    </script>
    <script src="<%= contextPath %>/frontend/management-pages.js"></script>
    <script src="<%= contextPath %>/frontend/invoice-page.js"></script>
    <script src="<%= contextPath %>/frontend/invoice-functional.js"></script>
    <script src="<%= contextPath %>/frontend/request-page.js"></script>
    <script src="<%= contextPath %>/frontend/finance-page.js"></script>
    <script src="<%= contextPath %>/frontend/finance-functional.js"></script>
    <script src="<%= contextPath %>/frontend/service-page.js"></script>
    <script src="<%= contextPath %>/frontend/service-functional.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.7/dist/chart.umd.min.js"></script>
    <script src="<%= contextPath %>/frontend/report-page.js"></script>
    <script src="<%= contextPath %>/frontend/account-page.js"></script>
    <script src="<%= contextPath %>/frontend/settings-page.js"></script>
    <script src="<%= contextPath %>/frontend/keyboard-shortcuts.js?v=5"></script>
</body>
</html>
