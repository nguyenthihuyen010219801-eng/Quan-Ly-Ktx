package com.dormitory.filter;

import com.google.gson.Gson;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@WebFilter("/*")
public class AuthorizationFilter implements Filter {
    private static final Gson GSON = new Gson();

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI().substring(req.getContextPath().length());

        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("accountUserId") == null) {
            if (isApi(path)) json(resp, 401, "Vui lòng đăng nhập để tiếp tục.");
            else resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String role = String.valueOf(session.getAttribute("accountRole"));
        if (!"quanly".equals(role) && !"nhanvien".equals(role)) {
            session.invalidate();
            if (isApi(path)) json(resp, 401, "Phiên đăng nhập không hợp lệ.");
            else resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        if (!isApi(path)) {
            resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            resp.setHeader("Pragma", "no-cache");
            resp.setDateHeader("Expires", 0);
        }

        if ("nhanvien".equals(role) && !staffAllowed(req, path)) {
            if (isApi(path)) json(resp, 403, "Bạn không có quyền thực hiện chức năng này.");
            else {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/access-denied.jsp").forward(req, resp);
            }
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean staffAllowed(HttpServletRequest req, String path) {
        String method = req.getMethod();
        if (path.equals("/manage.jsp")) {
            String view = req.getParameter("initialView");
            return view == null || !SetHolder.MANAGER_VIEWS.contains(view);
        }
        if (path.equals("/reports") || path.startsWith("/api/reports") ||
            path.startsWith("/api/finance-transactions") || path.startsWith("/api/accounts")) return false;
        if (path.startsWith("/api/settings")) return "POST".equals(method) && path.equals("/api/settings/password");
        if (path.startsWith("/api/manage/students")) return !"DELETE".equals(method);
        if (path.equals("/rooms") || path.equals("/buildings") || path.equals("/services")) return "GET".equals(method);
        if (path.startsWith("/api/invoices")) {
            if ("GET".equals(method)) return true;
            if ("POST".equals(method)) return path.equals("/api/invoices") || path.matches("/api/invoices/\\d+/pay");
            return false;
        }
        if (path.startsWith("/api/requests")) {
            if ("GET".equals(method) || "PUT".equals(method)) return true;
            return "POST".equals(method) && path.matches("/api/requests/\\d+/responses");
        }
        return true;
    }

    private boolean isPublic(String path) {
        return path.equals("/") || path.equals("/index.jsp") || path.equals("/login.jsp") ||
               path.equals("/access-denied.jsp") || path.equals("/api/login") ||
               path.equals("/api/session") || path.equals("/api/logout") ||
               path.startsWith("/frontend/") || path.startsWith("/images/") || path.startsWith("/logo/") ||
               path.equals("/favicon.ico");
    }

    private boolean isApi(String path) { return path.startsWith("/api/"); }

    private void json(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(GSON.toJson(Map.of("success", false, "message", message)));
    }

    private static final class SetHolder {
        private static final java.util.Set<String> MANAGER_VIEWS = java.util.Set.of("finance", "reports", "accounts", "settings");
    }
}
