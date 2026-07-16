<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String contextPath = request.getContextPath();
%>
<aside class="sidebar">
    <div class="brand">
        <img src="<%= contextPath %>/images/Logo.jpg" alt="Ký túc xá">
        <div class="brand-copy">
            <strong>KÝ TÚC XÁ</strong>
            <span>Hệ thống quản lý</span>
        </div>
        <button class="sidebar-toggle" type="button" aria-label="Thu gọn thanh bên" aria-expanded="true" title="Thu gọn thanh bên">
            <i class="fa-solid fa-bars"></i>
        </button>
    </div>

    <nav class="menu">
        <button class="menu-item active" data-view="dashboard">
            <i class="fa-solid fa-house"></i>
            <span>Tổng quan</span>
        </button>
        <button class="menu-item" data-view="students">
            <i class="fa-regular fa-user"></i>
            <span>Quản lý sinh viên</span>
        </button>
        <a class="menu-item" href="${pageContext.request.contextPath}/rooms">
            <i class="fa-regular fa-building"></i>
            <span>Quản lý phòng</span>
        </a>
        <a class="menu-item" href="${pageContext.request.contextPath}/buildings">
            <i class="fa-regular fa-building"></i>
            <span>Quản lý tòa nhà</span>
        </a>
        <button class="menu-item" type="button" data-view="invoices">
            <i class="fa-solid fa-file-invoice-dollar"></i>
            <span>Quản lý hóa đơn</span>
        </button>
        <a class="menu-item" href="${pageContext.request.contextPath}/services">
            <i class="fa-solid fa-users-gear"></i>
            <span>Quản lý dịch vụ</span>
        </a>
        <button class="menu-item" type="button" data-view="requests">
            <i class="fa-regular fa-comments"></i>
            <span>Yêu cầu - Phản hồi</span>
        </button>
        <button class="menu-item" type="button" data-view="finance" data-manager-only>
            <i class="fa-solid fa-money-bill-transfer"></i>
            <span>Quản lý thu chi</span>
        </button>
        <button class="menu-item" type="button" data-view="reports" data-manager-only>
            <i class="fa-solid fa-chart-simple"></i>
            <span>Báo cáo - Thống kê</span>
        </button>
        <button class="menu-item" type="button" data-view="accounts" data-manager-only>
            <i class="fa-regular fa-user-circle"></i>
            <span>Quản lý tài khoản</span>
        </button>
        <button class="menu-item" type="button" data-view="settings" data-manager-only>
            <i class="fa-solid fa-gear"></i>
            <span>Cài đặt hệ thống</span>
        </button>
    </nav>

    <div class="sidebar-footer">
        <div class="account">
            <div class="avatar small">QL</div>
            <div>
                <strong id="accountName">Nguyễn Thị Lan</strong>
                <span>Quản trị viên</span>
            </div>
        </div>
        <button class="logout-btn" type="button" id="logoutBtn">
            <i class="fa-solid fa-right-from-bracket"></i>
            <span>Đăng xuất</span>
        </button>
    </div>
</aside>
