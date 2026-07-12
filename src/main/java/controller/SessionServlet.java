package com.dormitory.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = {"/api/session", "/api/logout"})
public class SessionServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("accountUserId") == null) {
            writeJson(resp, 401, jsonMap("success", false, "message", "Chưa đăng nhập")); return;
        }
        writeJson(resp, jsonMap("success", true, "user", jsonMap(
                "id", session.getAttribute("accountUserId"),
                "username", session.getAttribute("accountUsername"),
                "full_name", session.getAttribute("accountFullName"),
                "role", session.getAttribute("accountRole"))));
    }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!"/api/logout".equals(req.getServletPath())) { writeJson(resp, 405, jsonMap("message", "Method not allowed")); return; }
        HttpSession session = req.getSession(false); if (session != null) session.invalidate();
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        writeJson(resp, jsonMap("success", true, "redirectUrl", req.getContextPath() + "/login.jsp"));
    }
}
